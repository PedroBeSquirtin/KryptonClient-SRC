package skid.krypton.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import skid.krypton.Krypton;
import skid.krypton.gui.CategoryWindow;
import skid.krypton.gui.Component;
import skid.krypton.module.Module;
import skid.krypton.module.setting.*;
import skid.krypton.utils.*;
import skid.krypton.utils.TextRenderer;

import java.awt.*;
import java.util.ArrayList;
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
    
    private final Color MODULE_BG = new Color(22, 30, 22, 235);
    private final Color HOVER_BG = new Color(120, 255, 120, 20);
    private final Color BORDER_COLOR = new Color(65, 95, 65, 120);
    private final Color GLOW_GREEN = new Color(70, 180, 70, 60);
    private final Color BUTTON_ON = new Color(50, 140, 50, 255);
    private final Color BUTTON_OFF = new Color(55, 55, 65, 200);
    private final Color TEXT_WHITE = new Color(255, 255, 255, 255);
    
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
        this.settingOffset = parent != null ? parent.getHeight() : 30;
        
        if (module != null && module.getSettings() != null) {
            for (final Object next : module.getSettings()) {
                if (next == null) continue;
                try {
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
                } catch (Exception e) {
                    e.printStackTrace();
                }
                this.settingOffset += parent != null ? parent.getHeight() : 30;
            }
        }
    }
    
    private int getTextWidth(String text) {
        try {
            return TextRenderer.getWidth(text);
        } catch (Exception e) {
            return MinecraftClient.getInstance().textRenderer.getWidth(text);
        }
    }
    
    private void drawText(DrawContext context, String text, int x, int y, int color) {
        try {
            TextRenderer.drawString(text, context, x, y, color);
        } catch (Exception e) {
            context.drawText(MinecraftClient.getInstance().textRenderer, text, x, y, color, false);
        }
    }

    public void render(final DrawContext drawContext, final int mouseX, final int mouseY, final float delta) {
        try {
            if (this.parent == null || this.module == null) return;
            
            final int x = this.parent.getX();
            final int y = this.parent.getY() + this.offset;
            final int width = this.parent.getWidth();
            final int height = this.parent.getHeight();
            
            if (y + height < 0 || y > MinecraftClient.getInstance().getWindow().getHeight()) {
                return;
            }
            
            if (this.settings != null) {
                for (Component component : this.settings) {
                    if (component != null) {
                        component.onUpdate();
                    }
                }
            }
            
            this.updateAnimations(mouseX, mouseY, delta);
            
            this.renderButtonBackground(drawContext, x, y, width, height);
            this.renderModuleInfo(drawContext, x, y, width, height);
            
            if (this.extended && this.settings != null) {
                this.renderSettings(drawContext, mouseX, mouseY, delta);
            }
            
            if (this.isHovered(mouseX, mouseY)) {
                CharSequence description = this.module.getDescription();
                if (description != null && description.length() > 0 && Krypton.INSTANCE.GUI != null) {
                    Krypton.INSTANCE.GUI.setTooltip(description, mouseX + 15, mouseY + 5);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateAnimations(final int mouseX, final int mouseY, final float delta) {
        final float deltaTime = delta * 0.05f;
        
        float hoverTarget = this.isHovered(mouseX, mouseY) ? 1.0f : 0.0f;
        this.hoverAnimation = (float) MathUtil.exponentialInterpolate(this.hoverAnimation, hoverTarget, 0.05f, deltaTime);
        
        float enabledTarget = (this.module != null && this.module.isEnabled()) ? 1.0f : 0.0f;
        this.enabledAnimation = (float) MathUtil.exponentialInterpolate(this.enabledAnimation, enabledTarget, 0.005f, deltaTime);
        this.enabledAnimation = (float) MathUtil.clampValue(this.enabledAnimation, 0.0, 1.0);
    }

    private void renderButtonBackground(final DrawContext drawContext, final int x, final int y, final int width, final int height) {
        final Color bgColor = ColorUtil.a(MODULE_BG, HOVER_BG, this.hoverAnimation);
        
        boolean isLast = false;
        if (this.parent != null && this.parent.moduleButtons != null && this.parent.moduleButtons.size() > 0) {
            isLast = this.parent.moduleButtons.get(this.parent.moduleButtons.size() - 1) == this;
        }
        
        if (isLast && !this.extended) {
            RenderUtils.renderRoundedQuad(drawContext.getMatrices(), bgColor, 
                x, y, x + width, y + height, 0.0, 0.0, CORNER_RADIUS, CORNER_RADIUS, 50.0);
        } else if (isLast && this.extended) {
            RenderUtils.renderRoundedQuad(drawContext.getMatrices(), bgColor, 
                x, y, x + width, y + height, 0.0, 0.0, 0.0, 0.0, 50.0);
        } else {
            drawContext.fill(x, y, x + width, y + height, bgColor.getRGB());
        }
        
        if (this.parent != null && this.parent.moduleButtons != null && this.parent.moduleButtons.indexOf(this) > 0) {
            drawContext.fill(x + 8, y, x + width - 8, y + 1, BORDER_COLOR.getRGB());
        }
    }

    private void renderModuleInfo(final DrawContext drawContext, final int x, final int y, final int width, final int height) {
        if (this.module == null) return;
        
        // Module name - perfectly centered vertically
        String moduleName = this.module.getName() != null ? this.module.getName().toString() : "";
        int textY = y + (height - 8) / 2;
        drawText(drawContext, moduleName, x + 12, textY, TEXT_WHITE.getRGB());
        
        // ON/OFF Button
        final int buttonWidth = 42;
        final int buttonHeight = 22;
        final int buttonX = x + width - buttonWidth - 12;
        final int buttonY = y + (height - buttonHeight) / 2;
        
        boolean isEnabled = this.module != null && this.module.isEnabled();
        
        // Button background
        Color buttonBg = isEnabled ? BUTTON_ON : BUTTON_OFF;
        RenderUtils.renderRoundedQuad(drawContext.getMatrices(), buttonBg,
            buttonX, buttonY, buttonX + buttonWidth, buttonY + buttonHeight, 11, 11, 11, 11, 30);
        
        // Button text - PERFECTLY CENTERED (dead center)
        String buttonText = isEnabled ? "ON" : "OFF";
        int textWidth = getTextWidth(buttonText);
        int textXPos = buttonX + (buttonWidth - textWidth) / 2;
        int textYPos = buttonY + (buttonHeight - 8) / 2;
        
        int textColor = isEnabled ? 0xFFFFFF : 0xAAAAAA;
        drawText(drawContext, buttonText, textXPos, textYPos, textColor);
        
        if (isEnabled) {
            RenderUtils.renderRoundedQuad(drawContext.getMatrices(), GLOW_GREEN,
                buttonX - 1, buttonY - 1, buttonX + buttonWidth + 1, buttonY + buttonHeight + 1, 12, 12, 12, 12, 30);
            RenderUtils.renderRoundedQuad(drawContext.getMatrices(), BUTTON_ON,
                x + 4, y + 4, x + 6, y + height - 4, 2, 2, 2, 2, 30);
        }
    }

    private void renderSettings(final DrawContext drawContext, final int mouseX, final int mouseY, final float delta) {
        if (this.parent == null) return;
        
        final int settingsY = this.parent.getY() + this.offset + this.parent.getHeight();
        final double animHeight = this.animation.getAnimation();
        
        if (animHeight <= 0) return;
        
        RenderSystem.enableScissor(
            this.parent.getX(), 
            Krypton.mc.getWindow().getHeight() - (settingsY + (int) animHeight), 
            this.parent.getWidth(), 
            (int) animHeight
        );
        
        if (this.settings != null) {
            for (Component component : this.settings) {
                if (component != null) {
                    component.render(drawContext, mouseX, mouseY - settingsY, delta);
                }
            }
        }
        
        this.renderSliderControls(drawContext);
        RenderSystem.disableScissor();
    }

    private void renderSliderControls(final DrawContext drawContext) {
        if (this.settings == null) return;
        for (final Component component : this.settings) {
            if (component == null) continue;
            try {
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
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void renderModernSliderKnob(final DrawContext drawContext, final double x, final double y, final Color color) {
        RenderUtils.renderCircle(drawContext.getMatrices(), new Color(0, 0, 0, 100), x, y, 7.0, 18);
        RenderUtils.renderCircle(drawContext.getMatrices(), color, x, y, 5.5, 16);
        RenderUtils.renderCircle(drawContext.getMatrices(), new Color(255, 255, 255, 70), x, y - 1.0, 3.0, 12);
    }

    public void onExtend() {
        if (this.parent == null || this.parent.moduleButtons == null) return;
        for (ModuleButton button : this.parent.moduleButtons) {
            if (button != null) {
                button.extended = false;
            }
        }
    }

    public void keyPressed(final int keyCode, final int scanCode, final int modifiers) {
        if (keyCode <= 0) return;
        if (this.settings != null) {
            for (Component component : this.settings) {
                if (component != null) {
                    component.keyPressed(keyCode, scanCode, modifiers);
                }
            }
        }
    }

    public void mouseDragged(final double mouseX, final double mouseY, final int button, final double deltaX, final double deltaY) {
        if (this.extended && this.settings != null) {
            for (Component component : this.settings) {
                if (component != null) {
                    component.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
                }
            }
        }
    }

    public void mouseClicked(final double mouseX, final double mouseY, final int button) {
        if (this.parent == null || this.module == null) return;
        
        if (this.isHovered(mouseX, mouseY)) {
            final int buttonWidth = 42;
            final int buttonHeight = 22;
            final int buttonX = this.parent.getX() + this.parent.getWidth() - buttonWidth - 12;
            final int buttonY = this.parent.getY() + this.offset + (this.parent.getHeight() - buttonHeight) / 2;
            
            if (button == 0) {
                if (mouseX >= buttonX && mouseX <= buttonX + buttonWidth && mouseY >= buttonY && mouseY <= buttonY + buttonHeight) {
                    this.module.toggle();
                } else if (this.module.getSettings() != null && !this.module.getSettings().isEmpty() && mouseX > this.parent.getX() + this.parent.getWidth() - 25) {
                    if (!this.extended) {
                        this.onExtend();
                    }
                    this.extended = !this.extended;
                } else {
                    this.module.toggle();
                }
            } else if (button == 1) {
                if (this.module.getSettings() == null || this.module.getSettings().isEmpty()) return;
                if (!this.extended) this.onExtend();
                this.extended = !this.extended;
            }
        }
        
        if (this.extended && this.settings != null) {
            for (Component setting : this.settings) {
                if (setting != null) {
                    setting.mouseClicked(mouseX, mouseY, button);
                }
            }
        }
    }

    public void onGuiClose() {
        this.currentAlpha = null;
        this.currentColor = null;
        this.hoverAnimation = 0.0f;
        this.enabledAnimation = (this.module != null && this.module.isEnabled()) ? 1.0f : 0.0f;
        
        if (this.settings != null) {
            for (Component component : this.settings) {
                if (component != null) {
                    component.onGuiClose();
                }
            }
        }
    }

    public void mouseReleased(final double mouseX, final double mouseY, final int button) {
        if (this.settings != null) {
            for (Component component : this.settings) {
                if (component != null) {
                    component.mouseReleased(mouseX, mouseY, button);
                }
            }
        }
    }

    public boolean isHovered(final double mouseX, final double mouseY) {
        if (this.parent == null) return false;
        return mouseX > this.parent.getX() && 
               mouseX < this.parent.getX() + this.parent.getWidth() && 
               mouseY > this.parent.getY() + this.offset && 
               mouseY < this.parent.getY() + this.offset + this.parent.getHeight();
    }
}
