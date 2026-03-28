package skid.krypton.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import skid.krypton.Krypton;
import skid.krypton.gui.components.ModuleButton;
import skid.krypton.module.Category;
import skid.krypton.module.Module;
import skid.krypton.utils.*;
import skid.krypton.utils.TextRenderer;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public final class CategoryWindow {
    public List<ModuleButton> moduleButtons;
    public int x;
    public int y;
    private final int width;
    private final int height;
    public Color currentColor;
    private final Category category;
    public boolean dragging = false;
    public boolean extended;
    public ClickGUI parent;
    private float hoverAnimation;
    
    // Clean Green Color Scheme
    private final Color BG_COLOR = new Color(18, 25, 18, 245);
    private final Color HEADER_COLOR = new Color(25, 35, 25, 255);
    private final Color ACCENT_GREEN = new Color(120, 255, 120, 255); // Brighter!
    private final Color HOVER_GREEN = new Color(120, 255, 120, 25);
    private final Color BORDER_COLOR = new Color(70, 100, 70, 120);
    
    // Simple text icons that WILL display
    private final String COMBAT_ICON = "[S]";
    private final String MISC_ICON = "[M]";
    private final String DONUT_ICON = "[D]";
    private final String RENDER_ICON = "[E]";
    private final String CLIENT_ICON = "[C]";

    public CategoryWindow(final int x, final int y, final int width, final int height, final Category category, final ClickGUI parent) {
        this.moduleButtons = new ArrayList<>();
        this.hoverAnimation = 0.0f;
        this.x = x;
        this.y = y;
        this.width = width;
        this.extended = true;
        this.height = height;
        this.category = category;
        this.parent = parent;

        try {
            List<Module> modules = Krypton.INSTANCE.getModuleManager().a(category);
            int offset = height;

            for (Module module : modules) {
                if (module != null) {
                    ModuleButton button = new ModuleButton(this, module, offset);
                    this.moduleButtons.add(button);
                    offset += height;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void render(final DrawContext context, final int mouseX, final int mouseY, final float delta) {
        try {
            int targetAlpha = skid.krypton.module.modules.client.Krypton.windowAlpha.getIntValue();
            Color targetColor = new Color(18, 25, 18, targetAlpha);
            
            if (this.currentColor == null) {
                this.currentColor = new Color(18, 25, 18, 0);
            } else {
                this.currentColor = ColorUtil.a(0.05f, targetColor, this.currentColor);
            }
            
            float hoverTarget = this.isHovered(mouseX, mouseY) ? 1.0F : 0.0F;
            this.hoverAnimation = (float) MathUtil.approachValue(delta * 0.1f, this.hoverAnimation, hoverTarget);
            
            Color panelBg = ColorUtil.a(BG_COLOR, HOVER_GREEN, this.hoverAnimation);
            float topRadius = 10.0F;
            float bottomRadius = this.extended ? 0.0F : 10.0F;
            
            // Main panel
            RenderUtils.renderRoundedQuad(context.getMatrices(), panelBg, 
                this.x, this.y, this.x + this.width, this.y + this.height, 
                topRadius, topRadius, bottomRadius, bottomRadius, 50.0);
            
            // Header
            context.fill(this.x, this.y, this.x + this.width, this.y + 32, HEADER_COLOR.getRGB());
            
            // Get icon for category - using simple text icons that WILL display
            String icon = getCategoryIcon(this.category);
            String categoryName = this.category.name.toString();
            String fullText = icon + " " + categoryName;
            
            // Calculate text position - perfectly centered
            int textX = this.x + (this.width - getTextWidth(fullText)) / 2;
            int textY = this.y + 11;
            
            // Draw text with bright green
            drawText(context, fullText, textX, textY, ACCENT_GREEN.getRGB());
            
            // Bottom accent line
            context.fill(this.x, this.y + 31, this.x + this.width, this.y + 32, ACCENT_GREEN.getRGB());
            
            // Border
            RenderUtils.renderRoundedQuad(context.getMatrices(), BORDER_COLOR,
                this.x, this.y, this.x + this.width, this.y + this.height,
                topRadius, topRadius, bottomRadius, bottomRadius, 30.0);
            
            this.updateButtons(delta);
            
            if (this.extended && this.moduleButtons != null) {
                this.renderModuleButtons(context, mouseX, mouseY, delta);
            }
        } catch (Exception e) {
            e.printStackTrace();
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
    
    private String getCategoryIcon(Category category) {
        if (category == null || category.name == null) return "[?]";
        String name = category.name.toString().toLowerCase();
        switch (name) {
            case "combat": return COMBAT_ICON;
            case "misc": return MISC_ICON;
            case "donut": return DONUT_ICON;
            case "render": return RENDER_ICON;
            case "client": return CLIENT_ICON;
            default: return "[?]";
        }
    }

    private void renderModuleButtons(final DrawContext context, final int mouseX, final int mouseY, final float delta) {
        if (this.moduleButtons == null) return;
        for (ModuleButton module : this.moduleButtons) {
            if (module != null) {
                module.render(context, mouseX, mouseY, delta);
            }
        }
    }

    public void keyPressed(final int keyCode, final int scanCode, final int modifiers) {
        if (keyCode <= 0) return;
        if (this.moduleButtons == null) return;
        for (ModuleButton moduleButton : this.moduleButtons) {
            if (moduleButton != null) {
                moduleButton.keyPressed(keyCode, scanCode, modifiers);
            }
        }
    }

    public void onGuiClose() {
        this.currentColor = null;
        if (this.moduleButtons != null) {
            for (ModuleButton moduleButton : this.moduleButtons) {
                if (moduleButton != null) {
                    moduleButton.onGuiClose();
                }
            }
        }
    }

    public void mouseClicked(final double mouseX, final double mouseY, final int button) {
        if (this.isHovered(mouseX, mouseY)) {
            if (button == 1) {
                this.extended = !this.extended;
            }
        }
        if (this.extended && this.moduleButtons != null) {
            for (ModuleButton moduleButton : this.moduleButtons) {
                if (moduleButton != null) {
                    moduleButton.mouseClicked(mouseX, mouseY, button);
                }
            }
        }
    }

    public void mouseDragged(final double mouseX, final double mouseY, final int button, final double deltaX, final double deltaY) {
        if (this.extended && this.moduleButtons != null) {
            for (ModuleButton moduleButton : this.moduleButtons) {
                if (moduleButton != null) {
                    moduleButton.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
                }
            }
        }
    }

    public void updateButtons(final float delta) {
        if (this.moduleButtons == null) return;
        int currentHeight = this.height;
        for (final ModuleButton button : this.moduleButtons) {
            if (button == null) continue;
            final Animation animation = button.animation;
            double targetHeight;
            if (button.extended) {
                targetHeight = this.height * (button.settings != null ? button.settings.size() + 1 : 1);
            } else {
                targetHeight = this.height;
            }
            animation.animate(0.5 * delta, targetHeight);
            final double animHeight = animation.getAnimation();
            button.offset = currentHeight;
            currentHeight += (int) animHeight;
        }
    }

    public void mouseReleased(final double mouseX, final double mouseY, final int button) {
        if (this.moduleButtons != null) {
            for (ModuleButton moduleButton : this.moduleButtons) {
                if (moduleButton != null) {
                    moduleButton.mouseReleased(mouseX, mouseY, button);
                }
            }
        }
    }

    public void mouseScrolled(final double mouseX, final double mouseY, final double horizontalAmount, final double verticalAmount) {}

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public boolean isHovered(final double mouseX, final double mouseY) {
        return mouseX > this.x && mouseX < this.x + this.width && mouseY > this.y && mouseY < this.y + 32;
    }
}
