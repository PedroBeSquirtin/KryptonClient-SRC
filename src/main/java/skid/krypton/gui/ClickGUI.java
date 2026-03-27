package skid.krypton.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import skid.krypton.Krypton;
import skid.krypton.font.Fonts;
import skid.krypton.module.Category;
import skid.krypton.utils.ColorUtil;
import skid.krypton.utils.RenderUtils;
import skid.krypton.utils.TextRenderer;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class ClickGUI extends Screen {
    public List<CategoryWindow> windows;
    public Color currentColor;
    private CharSequence tooltipText;
    private int tooltipX;
    private int tooltipY;
    
    // Uranium Green Color Scheme
    private final Color DESCRIPTION_BG = new Color(20, 25, 20, 245);
    private final Color ACCENT = new Color(80, 200, 80, 255);
    private final Color ACCENT_DARK = new Color(50, 150, 50, 255);
    private final Color ACCENT_GLOW = new Color(80, 200, 80, 60);
    private final Color ACCENT_HOVER = new Color(100, 220, 100, 255);
    private final Color HEADER_BG = new Color(15, 20, 15, 250);
    
    // Window positioning
    private static final int WINDOW_START_X = 50;
    private static final int WINDOW_START_Y = 50;
    private static final int WINDOW_WIDTH = 240;
    private static final int WINDOW_HEIGHT = 30;
    private static final int WINDOW_SPACING = 20;

    public ClickGUI() {
        super(Text.empty());
        this.windows = new ArrayList<>();
        this.tooltipText = null;
        
        int x = WINDOW_START_X;
        for (Category category : Category.values()) {
            this.windows.add(new CategoryWindow(x, WINDOW_START_Y, WINDOW_WIDTH, WINDOW_HEIGHT, category, this));
            x += WINDOW_WIDTH + WINDOW_SPACING;
        }
    }

    public boolean isDraggingAlready() {
        for (CategoryWindow window : this.windows) {
            if (window.dragging) {
                return true;
            }
        }
        return false;
    }

    public void setTooltip(final CharSequence tooltipText, final int tooltipX, final int tooltipY) {
        this.tooltipText = tooltipText;
        this.tooltipX = tooltipX;
        this.tooltipY = tooltipY;
    }

    public void setInitialFocus() {
        if (this.client == null) return;
        super.setInitialFocus();
    }

    public void render(final DrawContext drawContext, final int mouseX, final int mouseY, final float delta) {
        if (Krypton.mc.currentScreen == this) {
            if (Krypton.INSTANCE.screen != null) {
                Krypton.INSTANCE.screen.render(drawContext, 0, 0, delta);
            }
            
            if (this.currentColor == null) {
                this.currentColor = new Color(0, 0, 0, 0);
            } else {
                this.currentColor = new Color(0, 0, 0, this.currentColor.getAlpha());
            }
            
            int targetAlpha = skid.krypton.module.modules.client.Krypton.renderBackground.getValue() ? 200 : 0;
            if (this.currentColor.getAlpha() != targetAlpha) {
                this.currentColor = ColorUtil.a(0.05f, targetAlpha, this.currentColor);
            }
            
            if (Krypton.mc.currentScreen instanceof ClickGUI) {
                drawContext.fill(0, 0, Krypton.mc.getWindow().getWidth(), Krypton.mc.getWindow().getHeight(), this.currentColor.getRGB());
            }
            
            RenderUtils.unscaledProjection();
            
            int scaledMouseX = (int)(mouseX * MinecraftClient.getInstance().getWindow().getScaleFactor());
            int scaledMouseY = (int)(mouseY * MinecraftClient.getInstance().getWindow().getScaleFactor());
            super.render(drawContext, scaledMouseX, scaledMouseY, delta);
            
            renderUraniumHeader(drawContext);
            
            for (CategoryWindow window : this.windows) {
                window.render(drawContext, scaledMouseX, scaledMouseY, delta);
                window.updatePosition(scaledMouseX, scaledMouseY, delta);
            }
            
            if (this.tooltipText != null) {
                renderModernTooltip(drawContext, this.tooltipText, this.tooltipX, this.tooltipY);
                this.tooltipText = null;
            }
            
            RenderUtils.scaledProjection();
        }
    }
    
    private void renderUraniumHeader(DrawContext context) {
        int screenWidth = Krypton.mc.getWindow().getWidth();
        int headerY = 12;
        
        String logo = "URANIUM";
        int logoWidth = Fonts.FONT.getStringWidth(logo);
        int logoX = screenWidth / 2 - logoWidth / 2;
        
        RenderUtils.renderRoundedQuad(context.getMatrices(), ACCENT_GLOW,
            logoX - 25, headerY - 8, logoX + logoWidth + 25, headerY + 32, 25, 25, 25, 25, 50);
        
        for (int i = 0; i < logo.length(); i++) {
            float progress = (float) i / logo.length();
            Color gradientColor = lerpColor(ACCENT, ACCENT_DARK, progress);
            Fonts.FONT.drawString(context.getMatrices(), String.valueOf(logo.charAt(i)), 
                logoX + (i * 14), headerY + 8, gradientColor.getRGB());
        }
        
        String version = "URANIUM EDITION";
        int versionWidth = Fonts.FONT.getStringWidth(version);
        Fonts.FONT.drawString(context.getMatrices(), version,
            screenWidth / 2 - versionWidth / 2, headerY + 32, new Color(140, 180, 140, 255).getRGB());
        
        context.fill(logoX - 25, headerY + 48, logoX + logoWidth + 25, headerY + 49, ACCENT.getRGB());
    }
    
    private void renderModernTooltip(DrawContext context, CharSequence text, int x, int y) {
        if (text == null || text.length() == 0) return;
        
        int textWidth = Fonts.FONT.getStringWidth(text);
        int screenWidth = Krypton.mc.getWindow().getWidth();
        
        if (x + textWidth + 20 > screenWidth) {
            x = screenWidth - textWidth - 20;
        }
        
        int tooltipX = x - 8;
        int tooltipY = y - 8;
        int tooltipWidth = textWidth + 20;
        int tooltipHeight = 26;
        
        for (int i = 1; i <= 3; i++) {
            int alpha = 30 - i * 8;
            RenderUtils.renderRoundedQuad(context.getMatrices(), new Color(0, 0, 0, alpha),
                tooltipX + i, tooltipY + i, tooltipX + tooltipWidth + i, tooltipY + tooltipHeight + i,
                6, 6, 6, 6, 30);
        }
        
        RenderUtils.renderRoundedQuad(context.getMatrices(), DESCRIPTION_BG,
            tooltipX, tooltipY, tooltipX + tooltipWidth, tooltipY + tooltipHeight,
            8, 8, 8, 8, 50);
        
        RenderUtils.renderRoundedQuad(context.getMatrices(), new Color(80, 200, 80, 60),
            tooltipX, tooltipY, tooltipX + tooltipWidth, tooltipY + tooltipHeight,
            8, 8, 8, 8, 30);
        
        Fonts.FONT.drawString(context.getMatrices(), text, x, y + 4, new Color(220, 240, 220, 255).getRGB());
    }
    
    private Color lerpColor(Color c1, Color c2, float t) {
        int r = (int)(c1.getRed() + (c2.getRed() - c1.getRed()) * t);
        int g = (int)(c1.getGreen() + (c2.getGreen() - c1.getGreen()) * t);
        int b = (int)(c1.getBlue() + (c2.getBlue() - c1.getBlue()) * t);
        return new Color(r, g, b);
    }
    
    private int toMCColor(Color c) {
        return net.minecraft.util.math.ColorHelper.Argb.getArgb(c.getAlpha(), c.getRed(), c.getGreen(), c.getBlue());
    }

    public boolean keyPressed(final int keyCode, final int scanCode, final int modifiers) {
        // Handle escape key to close GUI
        if (keyCode == 256) { // ESC key
            this.close();
            return true;
        }
        
        // Pass key events to windows
        for (CategoryWindow window : this.windows) {
            window.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
        double scaledX = mouseX * MinecraftClient.getInstance().getWindow().getScaleFactor();
        double scaledY = mouseY * MinecraftClient.getInstance().getWindow().getScaleFactor();
        
        for (CategoryWindow window : this.windows) {
            window.mouseClicked(scaledX, scaledY, button);
        }
        return super.mouseClicked(scaledX, scaledY, button);
    }

    public boolean mouseDragged(final double mouseX, final double mouseY, final int button, final double deltaX, final double deltaY) {
        double scaledX = mouseX * MinecraftClient.getInstance().getWindow().getScaleFactor();
        double scaledY = mouseY * MinecraftClient.getInstance().getWindow().getScaleFactor();
        
        for (CategoryWindow window : this.windows) {
            window.mouseDragged(scaledX, scaledY, button, deltaX, deltaY);
        }
        return super.mouseDragged(scaledX, scaledY, button, deltaX, deltaY);
    }

    public boolean mouseReleased(final double mouseX, final double mouseY, final int button) {
        double scaledX = mouseX * MinecraftClient.getInstance().getWindow().getScaleFactor();
        double scaledY = mouseY * MinecraftClient.getInstance().getWindow().getScaleFactor();
        
        for (CategoryWindow window : this.windows) {
            window.mouseReleased(scaledX, scaledY, button);
        }
        return super.mouseReleased(scaledX, scaledY, button);
    }

    public boolean mouseScrolled(final double mouseX, final double mouseY, final double horizontalAmount, final double verticalAmount) {
        double scaledY = mouseY * MinecraftClient.getInstance().getWindow().getScaleFactor();
        
        for (CategoryWindow window : this.windows) {
            window.mouseScrolled(mouseX, scaledY, horizontalAmount, verticalAmount);
        }
        return super.mouseScrolled(mouseX, scaledY, horizontalAmount, verticalAmount);
    }

    public boolean shouldPause() {
        return false;
    }

    public void close() {
        Krypton.INSTANCE.getModuleManager().getModuleByClass(skid.krypton.module.modules.client.Krypton.class).setEnabled(false);
        this.onGuiClose();
    }

    public void onGuiClose() {
        Krypton.mc.setScreenAndRender(Krypton.INSTANCE.screen);
        this.currentColor = null;
        for (CategoryWindow window : this.windows) {
            window.onGuiClose();
        }
    }
}
