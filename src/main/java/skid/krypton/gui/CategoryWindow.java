package skid.krypton.gui;

import net.minecraft.client.gui.DrawContext;
import skid.krypton.Krypton;
import skid.krypton.font.Fonts;
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
    public boolean dragging;
    public boolean extended;
    private int dragX;
    private int dragY;
    private int prevX;
    private int prevY;
    public ClickGUI parent;
    private float hoverAnimation;
    
    // Uranium Green Color Scheme
    private final Color URANIUM_BG = new Color(18, 23, 18, 245);
    private final Color URANIUM_HEADER = new Color(23, 30, 23, 255);
    private final Color URANIUM_ACCENT = new Color(80, 200, 80, 255);
    private final Color URANIUM_ACCENT_DARK = new Color(50, 150, 50, 255);
    private final Color URANIUM_HOVER = new Color(80, 200, 80, 20);
    private final Color URANIUM_BORDER = new Color(50, 70, 50, 100);
    private final Color URANIUM_SHADOW = new Color(0, 0, 0, 60);

    public CategoryWindow(final int x, final int y, final int width, final int height, final Category category, final ClickGUI parent) {
        this.moduleButtons = new ArrayList<>();
        this.hoverAnimation = 0.0f;
        this.x = x;
        this.y = y;
        this.width = width;
        this.dragging = false;
        this.extended = true;
        this.height = height;
        this.category = category;
        this.parent = parent;
        this.prevX = x;
        this.prevY = y;

        final List<Module> modules = new ArrayList<>(Krypton.INSTANCE.getModuleManager().a(category));
        int offset = height;

        for (Module module : modules) {
            this.moduleButtons.add(new ModuleButton(this, module, offset));
            offset += height;
        }
    }

    public void render(final DrawContext context, final int mouseX, final int mouseY, final float delta) {
        // Update background color with animation
        int targetAlpha = skid.krypton.module.modules.client.Krypton.windowAlpha.getIntValue();
        Color targetColor = new Color(18, 23, 18, targetAlpha);
        
        if (this.currentColor == null) {
            this.currentColor = new Color(18, 23, 18, 0);
        } else {
            this.currentColor = ColorUtil.a(0.05f, targetColor, this.currentColor);
        }
        
        // Hover animation
        float hoverTarget = this.isHovered(mouseX, mouseY) && !this.dragging ? 1.0F : 0.0F;
        this.hoverAnimation = (float) MathUtil.approachValue(delta * 0.1f, this.hoverAnimation, hoverTarget);
        
        // Render shadow
        renderShadow(context, this.prevX, this.prevY, this.width, this.height);
        
        // Main panel background
        Color panelBg = ColorUtil.a(new Color(18, 23, 18, this.currentColor.getAlpha()), URANIUM_HOVER, this.hoverAnimation);
        float topRadius = this.extended ? 8.0F : 8.0F;
        float bottomRadius = this.extended ? 0.0F : 8.0F;
        
        RenderUtils.renderRoundedQuad(context.getMatrices(), panelBg, 
            this.prevX, this.prevY, this.prevX + this.width, this.prevY + this.height, 
            topRadius, topRadius, bottomRadius, bottomRadius, 50.0);
        
        // Header
        context.fill(this.prevX, this.prevY, this.prevX + this.width, this.prevY + 28, URANIUM_HEADER.getRGB());
        
        // Category name with Uranium styling using new font
        String categoryName = this.category.name.toString().toUpperCase();
        int textX = this.prevX + (this.width - Fonts.FONT.getStringWidth(categoryName)) / 2;
        int textY = this.prevY + 9;
        
        // Shadow text effect
        Fonts.FONT.drawString(context.getMatrices(), categoryName, textX + 1, textY + 1, new Color(0, 0, 0, 100).getRGB());
        Fonts.FONT.drawString(context.getMatrices(), categoryName, textX, textY, URANIUM_ACCENT.getRGB());
        
        // Bottom accent line for header
        context.fill(this.prevX, this.prevY + 27, this.prevX + this.width, this.prevY + 28, URANIUM_ACCENT.getRGB());
        
        // Border
        RenderUtils.renderRoundedQuad(context.getMatrices(), URANIUM_BORDER,
            this.prevX, this.prevY, this.prevX + this.width, this.prevY + this.height,
            topRadius, topRadius, bottomRadius, bottomRadius, 30.0);
        
        this.updateButtons(delta);
        
        if (this.extended) {
            this.renderModuleButtons(context, mouseX, mouseY, delta);
        }
    }
    
    private void renderShadow(DrawContext context, int x, int y, int width, int height) {
        for (int i = 1; i <= 4; i++) {
            int alpha = 25 - i * 5;
            context.fill(x - i, y - i, x + width + i, y + height + i, new Color(0, 0, 0, alpha).getRGB());
        }
    }

    private void renderModuleButtons(final DrawContext context, final int mouseX, final int mouseY, final float delta) {
        for (ModuleButton module : this.moduleButtons) {
            module.render(context, mouseX, mouseY, delta);
        }
    }

    public void keyPressed(final int keyCode, final int scanCode, final int modifiers) {
        for (ModuleButton moduleButton : this.moduleButtons) {
            moduleButton.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    public void onGuiClose() {
        this.currentColor = null;
        for (ModuleButton moduleButton : this.moduleButtons) {
            moduleButton.onGuiClose();
        }
        this.dragging = false;
    }

    public void mouseClicked(final double mouseX, final double mouseY, final int button) {
        if (this.isHovered(mouseX, mouseY)) {
            switch (button) {
                case 0:
                    if (!this.parent.isDraggingAlready()) {
                        this.dragging = true;
                        this.dragX = (int) (mouseX - this.x);
                        this.dragY = (int) (mouseY - this.y);
                    }
                    break;
                case 1:
                    this.extended = !this.extended;
                    break;
            }
        }
        if (this.extended) {
            for (ModuleButton moduleButton : this.moduleButtons) {
                moduleButton.mouseClicked(mouseX, mouseY, button);
            }
        }
    }

    public void mouseDragged(final double mouseX, final double mouseY, final int button, final double deltaX, final double deltaY) {
        if (this.extended) {
            for (ModuleButton moduleButton : this.moduleButtons) {
                moduleButton.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
            }
        }
    }

    public void updateButtons(final float delta) {
        int currentHeight = this.height;
        for (final ModuleButton button : this.moduleButtons) {
            final Animation animation = button.animation;
            double targetHeight;
            if (button.extended) {
                targetHeight = this.height * (button.settings.size() + 1);
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
        if (button == 0 && this.dragging) {
            this.dragging = false;
        }
        for (ModuleButton moduleButton : this.moduleButtons) {
            moduleButton.mouseReleased(mouseX, mouseY, button);
        }
    }

    public void mouseScrolled(final double mouseX, final double mouseY, final double horizontalAmount, final double verticalAmount) {
        this.prevX = this.x;
        this.prevY = this.y;
        this.prevY += (int) (verticalAmount * 20.0);
        this.setY((int) (this.y + verticalAmount * 20.0));
    }

    public int getX() {
        return this.prevX;
    }

    public int getY() {
        return this.prevY;
    }

    public void setY(final int y) {
        this.y = y;
    }

    public void setX(final int x) {
        this.x = x;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public boolean isHovered(final double mouseX, final double mouseY) {
        return mouseX > this.x && mouseX < this.x + this.width && mouseY > this.y && mouseY < this.y + this.height;
    }

    public boolean isPrevHovered(final double mouseX, final double mouseY) {
        return mouseX > this.prevX && mouseX < this.prevX + this.width && mouseY > this.prevY && mouseY < this.prevY + this.height;
    }

    public void updatePosition(final double mouseX, final double mouseY, final float delta) {
        this.prevX = this.x;
        this.prevY = this.y;
        if (this.dragging) {
            // Direct 1:1 dragging - no lag, instant response
            this.x = (int) (mouseX - this.dragX);
            this.y = (int) (mouseY - this.dragY);
        }
    }
}
