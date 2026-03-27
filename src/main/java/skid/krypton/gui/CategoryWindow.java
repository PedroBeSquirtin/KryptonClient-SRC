package skid.krypton.gui;

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
    public boolean extended;
    public ClickGUI parent;
    private float hoverAnimation;
    
    // Brighter Uranium Green Color Scheme
    private final Color URANIUM_BG = new Color(20, 28, 20, 245);
    private final Color URANIUM_HEADER = new Color(28, 38, 28, 255);
    private final Color URANIUM_ACCENT = new Color(100, 255, 100, 255);
    private final Color URANIUM_HOVER = new Color(100, 255, 100, 30);
    private final Color URANIUM_BORDER = new Color(80, 120, 80, 150);
    
    // Icons for categories
    private final String COMBAT_ICON = "⚔️";
    private final String MOVEMENT_ICON = "🏃";
    private final String PLAYER_ICON = "👤";
    private final String RENDER_ICON = "👁️";
    private final String WORLD_ICON = "🌍";
    private final String CLIENT_ICON = "💎";

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

        final List<Module> modules = new ArrayList<>(Krypton.INSTANCE.getModuleManager().a(category));
        int offset = height;

        for (Module module : modules) {
            this.moduleButtons.add(new ModuleButton(this, module, offset));
            offset += height;
        }
    }

    public void render(final DrawContext context, final int mouseX, final int mouseY, final float delta) {
        int targetAlpha = skid.krypton.module.modules.client.Krypton.windowAlpha.getIntValue();
        Color targetColor = new Color(20, 28, 20, targetAlpha);
        
        if (this.currentColor == null) {
            this.currentColor = new Color(20, 28, 20, 0);
        } else {
            this.currentColor = ColorUtil.a(0.05f, targetColor, this.currentColor);
        }
        
        float hoverTarget = this.isHovered(mouseX, mouseY) ? 1.0F : 0.0F;
        this.hoverAnimation = (float) MathUtil.approachValue(delta * 0.1f, this.hoverAnimation, hoverTarget);
        
        renderShadow(context, this.x, this.y, this.width, this.height);
        
        Color panelBg = ColorUtil.a(new Color(20, 28, 20, this.currentColor.getAlpha()), URANIUM_HOVER, this.hoverAnimation);
        float topRadius = this.extended ? 8.0F : 8.0F;
        float bottomRadius = this.extended ? 0.0F : 8.0F;
        
        RenderUtils.renderRoundedQuad(context.getMatrices(), panelBg, 
            this.x, this.y, this.x + this.width, this.y + this.height, 
            topRadius, topRadius, bottomRadius, bottomRadius, 50.0);
        
        // Header with icon
        context.fill(this.x, this.y, this.x + this.width, this.y + 32, URANIUM_HEADER.getRGB());
        
        // Get icon for category
        String icon = getCategoryIcon(this.category);
        String categoryName = this.category.name.toString().toUpperCase();
        String fullText = icon + " " + categoryName;
        int textX = this.x + (this.width - TextRenderer.getWidth(fullText)) / 2;
        int textY = this.y + 10;
        
        // Shadow text effect
        TextRenderer.drawString(fullText, context, textX + 1, textY + 1, new Color(0, 0, 0, 100).getRGB());
        TextRenderer.drawString(fullText, context, textX, textY, URANIUM_ACCENT.getRGB());
        
        // Bottom accent line for header
        context.fill(this.x, this.y + 31, this.x + this.width, this.y + 32, URANIUM_ACCENT.getRGB());
        
        // Border
        RenderUtils.renderRoundedQuad(context.getMatrices(), URANIUM_BORDER,
            this.x, this.y, this.x + this.width, this.y + this.height,
            topRadius, topRadius, bottomRadius, bottomRadius, 30.0);
        
        this.updateButtons(delta);
        
        if (this.extended) {
            this.renderModuleButtons(context, mouseX, mouseY, delta);
        }
    }
    
    private String getCategoryIcon(Category category) {
        switch (category.name.toString().toLowerCase()) {
            case "combat": return COMBAT_ICON;
            case "movement": return MOVEMENT_ICON;
            case "player": return PLAYER_ICON;
            case "render": return RENDER_ICON;
            case "world": return WORLD_ICON;
            case "client": return CLIENT_ICON;
            default: return "◆";
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
        if (keyCode <= 0) return;
        for (ModuleButton moduleButton : this.moduleButtons) {
            moduleButton.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    public void onGuiClose() {
        this.currentColor = null;
        for (ModuleButton moduleButton : this.moduleButtons) {
            moduleButton.onGuiClose();
        }
    }

    public void mouseClicked(final double mouseX, final double mouseY, final int button) {
        if (this.isHovered(mouseX, mouseY)) {
            if (button == 1) {
                this.extended = !this.extended;
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
        for (ModuleButton moduleButton : this.moduleButtons) {
            moduleButton.mouseReleased(mouseX, mouseY, button);
        }
    }

    public void mouseScrolled(final double mouseX, final double mouseY, final double horizontalAmount, final double verticalAmount) {
        // No scrolling for fixed windows
    }

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
