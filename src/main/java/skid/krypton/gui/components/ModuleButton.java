package skid.krypton.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import skid.krypton.Krypton;
import skid.krypton.font.Fonts;
import skid.krypton.gui.CategoryWindow;
import skid.krypton.gui.Component;
import skid.krypton.module.Module;
import skid.krypton.module.setting.*;
import skid.krypton.utils.*;
import skid.krypton.utils.TextRenderer;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class ModuleButton {
    public List<Component> settings;
    public CategoryWindow parent;
    public Module module;
    public int offset;
    public boolean extended;
    public int settingOffset;
    public Color currentColor;
    public Color currentAlpha;
    public Animation animation;
    
    // Uranium Green Color Scheme
    private final Color URANIUM_ACCENT = new Color(80, 200, 80, 255);
    private final Color URANIUM_ACCENT_DARK = new Color(50, 150, 50, 255);
    private final Color URANIUM_HOVER = new Color(80, 200, 80, 20);
    private final Color URANIUM_ENABLED = new Color(80, 200, 80, 255);
    private final Color URANIUM_DISABLED = new Color(150, 180, 150, 255);
    private final Color URANIUM_BG = new Color(22, 28, 22, 230);
    private final Color URANIUM_DESCRIPTION_BG = new Color(20, 25, 20, 245);
    private final Color URANIUM_BORDER = new Color(50, 70, 50, 100);
    private final Color URANIUM_GLOW = new Color(80, 200, 80, 60);
    
    private float hoverAnimation;
    private float enabledAnimation;
    private static final float CORNER_RADIUS = 6.0f;

    public ModuleButton(final CategoryWindow parent, final Module module, final int offset) {
        this.settings = new ArrayList<>();
        this.animation = new Animation(0.0);
        this.hoverAnimation = 0.0f;
        this.enabledAnimation = 0.0f;
        this.parent = parent;
        this.module = module;
        this.offset = offset;
        this.extended = false;
        this.settingOffset = parent.getHeight();
        
        for (final Object next : module.getSettings()) {
            if (next instanceof BooleanSetting) {
                this.settings.add(new Checkbox(this, (Setting) next, this.settingOffset));
            } else if (next instanceof NumberSetting) {
                this.settings.add(new NumberBox(this, (Setting) next, this.settingOffset));
            } else if (next instanceof ModeSetting) {
                this.settings.add(new ModeBox(this, (Setting) next, this.settingOffset));
            } else if (next instanceof BindSetting) {
                this.settings.add(new Keybind(this, (Setting) next, this.settingOffset));
            } else if (next instanceof StringSetting) {
                this.settings.add(new TextBox(this, (Setting) next, this.settingOffset));
            } else if (next instanceof MinMaxSetting) {
                this.settings.add(new Slider(this, (Setting) next, this.settingOffset));
            } else if (next instanceof ItemSetting) {
                this.settings.add(new ItemBox(this, (Setting) next, this.settingOffset));
            }
            this.settingOffset += parent.getHeight();
        }
    }

    public void render(final DrawContext drawContext, final int mouseX, final int mouseY, final float delta) {
        if (this.parent.getY() + this.offset > MinecraftClient.getInstance().getWindow().getHeight()) {
            return;
        }
        
        // Update settings components
        for (Component component : this.settings) {
            component.onUpdate();
        }
        
        this.updateAnimations(mouseX, mouseY, delta);
        
        final int x = this.parent.getX();
        final int y = this.parent.getY() + this.offset;
        final int width = this.parent.getWidth();
        final int height = this.parent.getHeight();
        
        this.renderButtonBackground(drawContext, x, y, width, height);
        this.renderIndicator(drawContext, x, y, height);
        this.renderModuleInfo(drawContext, x, y, width, height);
        
        if (this.extended) {
            this.renderSettings(drawContext, mouseX, mouseY, delta);
        }
        
        // Tooltip on hover
        if (this.isHovered(mouseX, mouseY) && !this.parent.dragging) {
            CharSequence description = this.module.getDescription();
            if (description != null && description.length() > 0) {
                Krypton.INSTANCE.GUI.setTooltip(description, mouseX + 10, mouseY + 10);
            }
        }
    }

    private void updateAnimations(final int mouseX, final int mouseY, final float delta) {
        final float deltaTime = delta * 0.05f;
        
        // Hover animation
        float hoverTarget = (this.isHovered(mouseX, mouseY) && !this.parent.dragging) ? 1.0f : 0.0f;
        this.hoverAnimation = (float) MathUtil.exponentialInterpolate(this.hoverAnimation, hoverTarget, 0.05f, deltaTime);
        
        // Enabled animation
        float enabledTarget = this.module.isEnabled() ? 1.0f : 0.0f;
        this.enabledAnimation = (float) MathUtil.exponentialInterpolate(this.enabledAnimation, enabledTarget, 0.005f, deltaTime);
        this.enabledAnimation = (float) MathUtil.clampValue(this.enabledAnimation, 0.0, 1.0);
    }

    private void renderButtonBackground(final DrawContext drawContext, final int x, final int y, final int width, final int height) {
        final Color bgColor = ColorUtil.a(URANIUM_BG, URANIUM_HOVER, this.hoverAnimation);
        final boolean isLast = this.parent.moduleButtons.get(this.parent.moduleButtons.size() - 1) == this;
        
        if (isLast && !this.extended) {
            RenderUtils.renderRoundedQuad(drawContext.getMatrices(), bgColor, 
                x, y, x + width, y + height, 0.0, 0.0, CORNER_RADIUS, CORNER_RADIUS, 50.0);
        } else if (isLast && this.extended) {
            RenderUtils.renderRoundedQuad(drawContext.getMatrices(), bgColor, 
                x, y, x + width, y + height, 0.0, 0.0, 0.0, 0.0, 50.0);
        } else {
            drawContext.fill(x, y, x + width, y + height, bgColor.getRGB());
        }
        
        // Separator line
        if (this.parent.moduleButtons.indexOf(this) > 0) {
            drawContext.fill(x + 8, y, x + width - 8, y + 1, URANIUM_BORDER.getRGB());
        }
    }

    private void renderIndicator(final DrawContext drawContext, final int x, final int y, final int height) {
        Color color = this.module.isEnabled() ? URANIUM_ENABLED : URANIUM_ACCENT;
        
        final float indicatorWidth = 5.0f * this.enabledAnimation;
        if (indicatorWidth > 0.1f) {
            RenderUtils.renderRoundedQuad(drawContext.getMatrices(), 
                ColorUtil.a(URANIUM_DISABLED, color, this.enabledAnimation), 
                x, y + 2, x + indicatorWidth, y + height - 2, 
                1.5f, 1.5f, 1.5f, 1.5f, 60.0);
        }
    }

    private void renderModuleInfo(final DrawContext drawContext, final int x, final int y, final int width, final int height) {
        // Module name with Uranium styling using new font
        Color nameColor = ColorUtil.a(URANIUM_DISABLED, URANIUM_ENABLED, this.enabledAnimation);
        Fonts.FONT.drawString(drawContext.getMatrices(), this.module.getName(), 
            x + 12, y + height / 2 - 6, nameColor.getRGB());
        
        // Modern toggle button
        final int toggleX = x + width - 32;
        final int toggleY = y + height / 2 - 8;
        
        // Background
        Color toggleBg = this.module.isEnabled() ? URANIUM_ENABLED : new Color(50, 70, 50, 200);
        RenderUtils.renderRoundedQuad(drawContext.getMatrices(), toggleBg,
            toggleX, toggleY, toggleX + 20, toggleY + 16, 8, 8, 8, 8, 30);
        
        // Handle
        int handleX = this.module.isEnabled() ? toggleX + 9 : toggleX + 3;
        RenderUtils.renderRoundedQuad(drawContext.getMatrices(), Color.WHITE,
            handleX, toggleY + 2, handleX + 8, toggleY + 14, 6, 6, 6, 6, 30);
        
        // Glow effect for enabled modules
        if (this.module.isEnabled()) {
            RenderUtils.renderRoundedQuad(drawContext.getMatrices(), URANIUM_GLOW,
                toggleX - 2, toggleY - 2, toggleX + 22, toggleY + 18, 10, 10, 10, 10, 30);
        }
    }

    private void renderSettings(final DrawContext drawContext, final int mouseX, final int mouseY, final float delta) {
        final int settingsY = this.parent.getY() + this.offset + this.parent.getHeight();
        final double animHeight = this.animation.getAnimation();
        
        RenderSystem.enableScissor(
            this.parent.getX(), 
            Krypton.mc.getWindow().getHeight() - (settingsY + (int) animHeight), 
            this.parent.getWidth(), 
            (int) animHeight
        );
        
        for (Component component : this.settings) {
            component.render(drawContext, mouseX, mouseY - settingsY, delta);
        }
        
        this.renderSliderControls(drawContext);
        RenderSystem.disableScissor();
    }

    private void renderSliderControls(final DrawContext drawContext) {
        for (final Component component : this.settings) {
            if (component instanceof NumberBox numberBox) {
                this.renderModernSliderKnob(drawContext, 
                    component.parentX() + Math.max(numberBox.lerpedOffsetX, 2.5), 
                    component.parentY() + numberBox.offset + component.parentOffset() + 27.5, 
                    numberBox.currentColor1);
            } else if (component instanceof Slider slider) {
                this.renderModernSliderKnob(drawContext, 
                    component.parentX() + Math.max(slider.lerpedOffsetMinX, 2.5), 
                    component.parentY() + component.offset + component.parentOffset() + 27.5, 
                    slider.accentColor1);
                this.renderModernSliderKnob(drawContext, 
                    component.parentX() + Math.max(slider.lerpedOffsetMaxX, 2.5), 
                    component.parentY() + component.offset + component.parentOffset() + 27.5, 
                    slider.accentColor1);
            }
        }
    }

    private void renderModernSliderKnob(final DrawContext drawContext, final double x, final double y, final Color color) {
        RenderUtils.renderCircle(drawContext.getMatrices(), new Color(0, 0, 0, 100), x, y, 7.0, 18);
        RenderUtils.renderCircle(drawContext.getMatrices(), color, x, y, 5.5, 16);
        RenderUtils.renderCircle(drawContext.getMatrices(), new Color(255, 255, 255, 70), x, y - 1.0, 3.0, 12);
    }

    public void onExtend() {
        for (ModuleButton button : this.parent.moduleButtons) {
            button.extended = false;
        }
    }

    public void keyPressed(final int keyCode, final int scanCode, final int modifiers) {
        for (Component component : this.settings) {
            component.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    public void mouseDragged(final double mouseX, final double mouseY, final int button, final double deltaX, final double deltaY) {
        if (this.extended) {
            for (Component component : this.settings) {
                component.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
            }
        }
    }

    public void mouseClicked(final double mouseX, final double mouseY, final int button) {
        if (this.isHovered(mouseX, mouseY)) {
            if (button == 0) {
                final int toggleX = this.parent.getX() + this.parent.getWidth() - 32;
                final int toggleY = this.parent.getY() + this.offset + this.parent.getHeight() / 2 - 8;
                
                if (mouseX >= toggleX && mouseX <= toggleX + 20 && mouseY >= toggleY && mouseY <= toggleY + 16) {
                    this.module.toggle();
                } else if (!this.module.getSettings().isEmpty() && mouseX > this.parent.getX() + this.parent.getWidth() - 25) {
                    if (!this.extended) {
                        this.onExtend();
                    }
                    this.extended = !this.extended;
                } else {
                    this.module.toggle();
                }
            } else if (button == 1) {
                if (this.module.getSettings().isEmpty()) return;
                if (!this.extended) this.onExtend();
                this.extended = !this.extended;
            }
        }
        
        if (this.extended) {
            for (Component setting : this.settings) {
                setting.mouseClicked(mouseX, mouseY, button);
            }
        }
    }

    public void onGuiClose() {
        this.currentAlpha = null;
        this.currentColor = null;
        this.hoverAnimation = 0.0f;
        this.enabledAnimation = this.module.isEnabled() ? 1.0f : 0.0f;
        
        for (Component component : this.settings) {
            component.onGuiClose();
        }
    }

    public void mouseReleased(final double mouseX, final double mouseY, final int button) {
        for (Component component : this.settings) {
            component.mouseReleased(mouseX, mouseY, button);
        }
    }

    public boolean isHovered(final double mouseX, final double mouseY) {
        return mouseX > this.parent.getX() && 
               mouseX < this.parent.getX() + this.parent.getWidth() && 
               mouseY > this.parent.getY() + this.offset && 
               mouseY < this.parent.getY() + this.offset + this.parent.getHeight();
    }
}
