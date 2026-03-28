package skid.krypton.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import skid.krypton.Krypton;
import skid.krypton.module.Category;
import skid.krypton.utils.ColorUtil;
import skid.krypton.utils.RenderUtils;
import skid.krypton.utils.TextRenderer;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public final class ClickGUI extends Screen {
    public List<CategoryWindow> windows;
    public Color currentColor;
    private CharSequence tooltipText;
    private int tooltipX;
    private int tooltipY;
    
    private final Color DESCRIPTION_BG = new Color(20, 28, 20, 245);
    private final Color ACCENT_GREEN = new Color(120, 255, 120, 255);
    
    private static final int WINDOW_START_X = 50;
    private static final int WINDOW_START_Y = 40;
    private static final int WINDOW_WIDTH = 240;
    private static final int WINDOW_HEIGHT = 30;
    private static final int WINDOW_SPACING = 20;

    public ClickGUI() {
        super(Text.empty());
        this.windows = new ArrayList<>();
        this.tooltipText = null;
        
        try {
            int x = WINDOW_START_X;
            for (Category category : Category.values()) {
                this.windows.add(new CategoryWindow(x, WINDOW_START_Y, WINDOW_WIDTH, WINDOW_HEIGHT, category, this));
                x += WINDOW_WIDTH + WINDOW_SPACING;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isDraggingAlready() {
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
        try {
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
                
                for (CategoryWindow window : this.windows) {
                    try {
                        window.render(drawContext, scaledMouseX, scaledMouseY, delta);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                
                if (this.tooltipText != null) {
                    renderModernTooltip(drawContext, this.tooltipText, this.tooltipX, this.tooltipY);
                    this.tooltipText = null;
                }
                
                RenderUtils.scaledProjection();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void renderModernTooltip(DrawContext context, CharSequence text, int x, int y) {
        try {
            if (text == null || text.length() == 0) return;
            
            int textWidth = TextRenderer.getWidth(text);
            int screenWidth = Krypton.mc.getWindow().getWidth();
            
            if (x + textWidth + 20 > screenWidth) {
                x = screenWidth - textWidth - 20;
            }
            
            int tooltipX = x - 8;
            int tooltipY = y - 5;
            int tooltipWidth = textWidth + 20;
            int tooltipHeight = 24;
            
            // Background
            RenderUtils.renderRoundedQuad(context.getMatrices(), DESCRIPTION_BG,
                tooltipX, tooltipY, tooltipX + tooltipWidth, tooltipY + tooltipHeight,
                8, 8, 8, 8, 50);
            
            // Border
            RenderUtils.renderRoundedQuad(context.getMatrices(), new Color(120, 255, 120, 80),
                tooltipX, tooltipY, tooltipX + tooltipWidth, tooltipY + tooltipHeight,
                8, 8, 8, 8, 30);
            
            // Text - perfectly centered vertically in the bubble
            int textYPos = tooltipY + (tooltipHeight - 8) / 2;
            TextRenderer.drawString(text, context, x, textYPos, new Color(255, 255, 255, 255).getRGB());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean keyPressed(final int keyCode, final int scanCode, final int modifiers) {
        try {
            if (keyCode == 256) {
                this.close();
                return true;
            }
            
            for (CategoryWindow window : this.windows) {
                window.keyPressed(keyCode, scanCode, modifiers);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
        try {
            double scaledX = mouseX * MinecraftClient.getInstance().getWindow().getScaleFactor();
            double scaledY = mouseY * MinecraftClient.getInstance().getWindow().getScaleFactor();
            
            for (CategoryWindow window : this.windows) {
                window.mouseClicked(scaledX, scaledY, button);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public boolean mouseDragged(final double mouseX, final double mouseY, final int button, final double deltaX, final double deltaY) {
        try {
            double scaledX = mouseX * MinecraftClient.getInstance().getWindow().getScaleFactor();
            double scaledY = mouseY * MinecraftClient.getInstance().getWindow().getScaleFactor();
            
            for (CategoryWindow window : this.windows) {
                window.mouseDragged(scaledX, scaledY, button, deltaX, deltaY);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    public boolean mouseReleased(final double mouseX, final double mouseY, final int button) {
        try {
            double scaledX = mouseX * MinecraftClient.getInstance().getWindow().getScaleFactor();
            double scaledY = mouseY * MinecraftClient.getInstance().getWindow().getScaleFactor();
            
            for (CategoryWindow window : this.windows) {
                window.mouseReleased(scaledX, scaledY, button);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    public boolean mouseScrolled(final double mouseX, final double mouseY, final double horizontalAmount, final double verticalAmount) {
        try {
            double scaledY = mouseY * MinecraftClient.getInstance().getWindow().getScaleFactor();
            
            for (CategoryWindow window : this.windows) {
                window.mouseScrolled(mouseX, scaledY, horizontalAmount, verticalAmount);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    public boolean shouldPause() {
        return false;
    }

    public void close() {
        try {
            Krypton.INSTANCE.getModuleManager().getModuleByClass(skid.krypton.module.modules.client.Krypton.class).setEnabled(false);
            this.onGuiClose();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onGuiClose() {
        try {
            Krypton.mc.setScreenAndRender(Krypton.INSTANCE.screen);
            this.currentColor = null;
            for (CategoryWindow window : this.windows) {
                window.onGuiClose();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
