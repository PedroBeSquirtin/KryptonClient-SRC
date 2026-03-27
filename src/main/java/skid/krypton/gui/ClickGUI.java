package skid.krypton.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import skid.krypton.Krypton;
import skid.krypton.module.Category;
import skid.krypton.module.Module;
import skid.krypton.module.setting.*;
import skid.krypton.utils.*;

import java.awt.*;
import java.util.*;
import java.util.List;

public final class ClickGUI extends Screen {
    private Category selectedCategory;
    private Module selectedModule;
    private String searchQuery;
    public Color currentColor;
    private boolean searchFocused;
    private boolean draggingSlider;
    private Setting draggingSliderSetting;
    private float animationProgress = 0f;
    private Map<Module, Float> moduleAnimations = new HashMap<>();

    private static final Color TRANSPARENT = new Color(0, 0, 0, 0);

    // Modern Uranium Color Scheme
    private final Color BACKGROUND_COLOR = new Color(8, 8, 12, 240);
    private final Color PANEL_COLOR = new Color(15, 15, 22, 245);
    private final Color ACCENT_COLOR = new Color(0, 230, 180, 255); // Fresh teal
    private final Color ACCENT_DARK = new Color(0, 180, 140, 255);
    private final Color ACCENT_GLOW = new Color(0, 230, 180, 80);
    private final Color SELECTED_COLOR = new Color(0, 230, 180, 30);
    private final Color TEXT_COLOR = new Color(235, 235, 245, 255);
    private final Color TEXT_SECONDARY = new Color(150, 150, 170, 255);
    private final Color SEARCH_BG = new Color(25, 25, 35, 255);
    private final Color HOVER_COLOR = new Color(0, 230, 180, 15);
    private final Color MODULE_ON_COLOR = new Color(0, 230, 180, 255);
    private final Color MODULE_OFF_COLOR = new Color(45, 45, 60, 255);

    // Modern Layout with more spacing
    private static final int SETTINGS_PANEL_WIDTH = 340;
    private static final int CATEGORY_PANEL_WIDTH = 160;
    private static final int MODULE_PANEL_WIDTH = 380;
    private static final int HEADER_HEIGHT = 60;
    private static final int ITEM_HEIGHT = 40;
    private static final int PADDING = 20;
    private static final int PANEL_SPACING = 24;
    private static final int CORNER_RADIUS = 16;
    private static final int TOTAL_WIDTH = SETTINGS_PANEL_WIDTH + CATEGORY_PANEL_WIDTH + MODULE_PANEL_WIDTH + (PANEL_SPACING * 2);
    private static final int TOTAL_HEIGHT = 540;

    public ClickGUI() {
        super(Text.empty());
        this.selectedCategory = Category.COMBAT;
        this.searchQuery = "";
        this.searchFocused = false;
        this.draggingSlider = false;
        this.draggingSliderSetting = null;
    }

    private static int toMCColor(Color c) {
        return net.minecraft.util.math.ColorHelper.Argb.getArgb(c.getAlpha(), c.getRed(), c.getGreen(), c.getBlue());
    }

    public boolean isDraggingAlready() {
        return this.draggingSlider;
    }

    public void setTooltip(final CharSequence tooltipText, final int tooltipX, final int tooltipY) {
        // Modern tooltip handling
    }

    public void setInitialFocus() {
        if (this.client == null) return;
        super.setInitialFocus();
    }

    public void render(final DrawContext drawContext, final int n, final int n2, final float n3) {
        if (Krypton.mc.currentScreen == this) {
            if (Krypton.INSTANCE.screen != null) {
                Krypton.INSTANCE.screen.render(drawContext, 0, 0, n3);
            }
            
            // Smooth animation
            animationProgress += (0.1f - animationProgress) * 0.1f;
            
            if (this.currentColor == null) {
                this.currentColor = new Color(0, 0, 0, 0);
            } else {
                this.currentColor = new Color(0, 0, 0, this.currentColor.getAlpha());
            }

            int targetAlpha = skid.krypton.module.modules.client.Krypton.renderBackground.getValue() ? 180 : 0;
            if (this.currentColor.getAlpha() != targetAlpha) {
                this.currentColor = ColorUtil.a(0.05f, targetAlpha, this.currentColor);
            }
            
            if (Krypton.mc.currentScreen instanceof ClickGUI) {
                drawContext.fill(0, 0, Krypton.mc.getWindow().getWidth(), Krypton.mc.getWindow().getHeight(), this.currentColor.getRGB());
            }
            
            RenderUtils.unscaledProjection();
            final int scaledMouseX = n * (int) MinecraftClient.getInstance().getWindow().getScaleFactor();
            final int scaledMouseY = n2 * (int) MinecraftClient.getInstance().getWindow().getScaleFactor();
            super.render(drawContext, scaledMouseX, scaledMouseY, n3);

            this.renderBackground(drawContext);
            this.renderModernHeader(drawContext);
            this.renderSettingsPanel(drawContext, scaledMouseX, scaledMouseY);
            this.renderCategoryPanel(drawContext, scaledMouseX, scaledMouseY);
            this.renderModulePanel(drawContext, scaledMouseX, scaledMouseY);

            RenderUtils.scaledProjection();
        }
    }

    private void renderBackground(final DrawContext drawContext) {
        final int screenWidth = Krypton.mc.getWindow().getWidth();
        final int screenHeight = Krypton.mc.getWindow().getHeight();

        if (skid.krypton.module.modules.client.Krypton.renderBackground.getValue()) {
            // Gradient background
            for (int i = 0; i < screenHeight; i++) {
                float ratio = (float) i / screenHeight;
                int alpha = (int) (80 * (1 - ratio));
                drawContext.fill(0, i, screenWidth, i + 1, new Color(0, 0, 0, alpha).getRGB());
            }
        }
    }

    private void renderModernHeader(final DrawContext drawContext) {
        final int screenWidth = Krypton.mc.getWindow().getWidth();
        final int headerHeight = 80;
        
        // Glow effect
        RenderUtils.renderRoundedQuad(drawContext.getMatrices(), ACCENT_GLOW,
                screenWidth / 2 - 200, 20, screenWidth / 2 + 200, 70, 40, 40, 40, 40, 50);
        
        // Title with gradient effect
        String title = "URANIUM";
        String subtitle = "PREMIUM CLIENT";
        
        for (int i = 0; i < title.length(); i++) {
            float progress = (float) i / title.length();
            Color gradientColor = lerpColor(ACCENT_COLOR, ACCENT_DARK, progress);
            TextRenderer.drawString(String.valueOf(title.charAt(i)), drawContext,
                    screenWidth / 2 - 60 + (i * 14), 35, toMCColor(gradientColor));
        }
        
        TextRenderer.drawString(subtitle, drawContext,
                screenWidth / 2 - 40, 55, toMCColor(TEXT_SECONDARY));
    }

    private void renderSettingsPanel(final DrawContext drawContext, final int mouseX, final int mouseY) {
        final int screenWidth = Krypton.mc.getWindow().getWidth();
        final int screenHeight = Krypton.mc.getWindow().getHeight();
        final int startX = (screenWidth - TOTAL_WIDTH) / 2;
        final int startY = (screenHeight - TOTAL_HEIGHT) / 2 + 20;
        final int endX = startX + SETTINGS_PANEL_WIDTH;
        final int endY = startY + TOTAL_HEIGHT;

        // Panel with shadow
        renderShadow(drawContext, startX, startY, SETTINGS_PANEL_WIDTH, TOTAL_HEIGHT);
        RenderUtils.renderRoundedQuad(drawContext.getMatrices(), PANEL_COLOR,
                startX, startY, endX, endY, CORNER_RADIUS, CORNER_RADIUS, CORNER_RADIUS, CORNER_RADIUS, 200);

        // Header
        drawVerticalGradient(drawContext, startX, startY, SETTINGS_PANEL_WIDTH, HEADER_HEIGHT, ACCENT_COLOR, ACCENT_DARK);
        TextRenderer.drawString("MODULE SETTINGS", drawContext,
                startX + PADDING, startY + 22, toMCColor(Color.WHITE));

        if (this.selectedModule != null) {
            int yOffset = startY + HEADER_HEIGHT + PADDING;
            
            // Module name display
            TextRenderer.drawString(this.selectedModule.getName().toString().toUpperCase(), drawContext,
                    startX + PADDING, yOffset - 10, toMCColor(ACCENT_COLOR));

            for (Object setting : this.selectedModule.getSettings()) {
                if (setting instanceof Setting) {
                    final Setting s = (Setting) setting;
                    final boolean isHovered = this.isHoveredInRect(mouseX, mouseY, startX, yOffset, SETTINGS_PANEL_WIDTH, ITEM_HEIGHT);

                    if (isHovered && !this.draggingSlider) {
                        RenderUtils.renderRoundedQuad(drawContext.getMatrices(), HOVER_COLOR,
                                startX + 8, yOffset, endX - 8, yOffset + ITEM_HEIGHT, 8, 8, 8, 8, 30);
                    }

                    TextRenderer.drawString(s.getName().toString(), drawContext,
                            startX + PADDING, yOffset + 12, toMCColor(TEXT_COLOR));

                    this.renderModernSettingValue(drawContext, setting, startX, endX, yOffset, mouseX, mouseY);

                    yOffset += ITEM_HEIGHT + 8;
                }
            }
        } else {
            TextRenderer.drawCenteredString("SELECT A MODULE", drawContext,
                    startX + SETTINGS_PANEL_WIDTH / 2, startY + 200, toMCColor(TEXT_SECONDARY));
        }
    }

    private void renderModernSettingValue(final DrawContext drawContext, final Object setting, 
                                          final int startX, final int endX, final int yOffset, 
                                          final int mouseX, final int mouseY) {
        if (setting instanceof BooleanSetting) {
            final BooleanSetting boolSetting = (BooleanSetting) setting;
            final boolean value = boolSetting.getValue();
            
            // Modern toggle switch
            final int toggleX = endX - 60;
            final int toggleY = yOffset + 8;
            final int toggleWidth = 50;
            final int toggleHeight = 24;
            
            // Background
            Color bgColor = value ? ACCENT_COLOR : new Color(50, 50, 65, 255);
            RenderUtils.renderRoundedQuad(drawContext.getMatrices(), bgColor,
                    toggleX, toggleY, toggleX + toggleWidth, toggleY + toggleHeight, 12, 12, 12, 12, 30);
            
            // Handle
            int handleX = value ? toggleX + toggleWidth - 20 : toggleX + 4;
            RenderUtils.renderRoundedQuad(drawContext.getMatrices(), Color.WHITE,
                    handleX, toggleY + 2, handleX + 16, toggleY + toggleHeight - 2, 10, 10, 10, 10, 30);
                    
        } else if (setting instanceof NumberSetting) {
            final NumberSetting numSetting = (NumberSetting) setting;
            final String valueText = String.format("%.1f", numSetting.getValue());
            
            // Value display
            TextRenderer.drawString(valueText, drawContext,
                    endX - PADDING - 40, yOffset + 12, toMCColor(ACCENT_COLOR));
            
            // Modern slider
            final int sliderY = yOffset + 30;
            final int sliderStartX = startX + PADDING;
            final int sliderEndX = endX - PADDING - 45;
            final int sliderWidth = sliderEndX - sliderStartX;
            
            // Track
            RenderUtils.renderRoundedQuad(drawContext.getMatrices(), new Color(40, 40, 50, 255),
                    sliderStartX, sliderY, sliderEndX, sliderY + 4, 2, 2, 2, 2, 20);
            
            // Progress
            final double progress = (numSetting.getValue() - numSetting.getMin()) / (numSetting.getMax() - numSetting.getMin());
            final int progressWidth = (int) (sliderWidth * progress);
            if (progressWidth > 0) {
                RenderUtils.renderRoundedQuad(drawContext.getMatrices(), ACCENT_COLOR,
                        sliderStartX, sliderY, sliderStartX + progressWidth, sliderY + 4, 2, 2, 2, 2, 20);
            }
            
            // Handle
            final int handleX = sliderStartX + progressWidth - 4;
            RenderUtils.renderRoundedQuad(drawContext.getMatrices(), Color.WHITE,
                    handleX, sliderY - 4, handleX + 8, sliderY + 10, 6, 6, 6, 6, 30);
                    
        } else if (setting instanceof ModeSetting) {
            final ModeSetting<?> modeSetting = (ModeSetting<?>) setting;
            final String valueText = modeSetting.getValue().toString();
            
            // Modern pill background
            int textWidth = TextRenderer.getWidth(valueText);
            int pillX = endX - PADDING - textWidth - 20;
            int pillY = yOffset + 6;
            
            RenderUtils.renderRoundedQuad(drawContext.getMatrices(), ACCENT_COLOR,
                    pillX, pillY, pillX + textWidth + 20, pillY + 22, 11, 11, 11, 11, 30);
            
            TextRenderer.drawString(valueText, drawContext,
                    pillX + 10, pillY + 7, toMCColor(Color.WHITE));
        }
    }

    private void renderCategoryPanel(final DrawContext drawContext, final int mouseX, final int mouseY) {
        final int screenWidth = Krypton.mc.getWindow().getWidth();
        final int screenHeight = Krypton.mc.getWindow().getHeight();
        final int startX = (screenWidth - TOTAL_WIDTH) / 2 + SETTINGS_PANEL_WIDTH + PANEL_SPACING;
        final int startY = (screenHeight - TOTAL_HEIGHT) / 2 + 20;
        final int endX = startX + CATEGORY_PANEL_WIDTH;
        final int endY = startY + TOTAL_HEIGHT;

        renderShadow(drawContext, startX, startY, CATEGORY_PANEL_WIDTH, TOTAL_HEIGHT);
        RenderUtils.renderRoundedQuad(drawContext.getMatrices(), PANEL_COLOR,
                startX, startY, endX, endY, CORNER_RADIUS, CORNER_RADIUS, CORNER_RADIUS, CORNER_RADIUS, 200);

        TextRenderer.drawString("CATEGORIES", drawContext, 
                startX + PADDING, startY + 22, toMCColor(ACCENT_COLOR));

        int yOffset = startY + HEADER_HEIGHT + PADDING;
        for (Category category : Category.values()) {
            final boolean isSelected = category == this.selectedCategory;
            final boolean isHovered = this.isHoveredInRect(mouseX, mouseY, startX, yOffset, CATEGORY_PANEL_WIDTH, ITEM_HEIGHT);

            if (isSelected) {
                // Left accent bar
                drawContext.fill(startX + 4, yOffset + 8, startX + 6, yOffset + ITEM_HEIGHT - 8, toMCColor(ACCENT_COLOR));
                RenderUtils.renderRoundedQuad(drawContext.getMatrices(), SELECTED_COLOR,
                        startX + 8, yOffset, endX - 8, yOffset + ITEM_HEIGHT, 8, 8, 8, 8, 30);
            } else if (isHovered) {
                RenderUtils.renderRoundedQuad(drawContext.getMatrices(), HOVER_COLOR,
                        startX + 8, yOffset, endX - 8, yOffset + ITEM_HEIGHT, 8, 8, 8, 8, 30);
            }

            Color textColor = isSelected ? ACCENT_COLOR : TEXT_COLOR;
            TextRenderer.drawString(category.name.toString(), drawContext,
                    startX + PADDING + 10, yOffset + 12, toMCColor(textColor));

            yOffset += ITEM_HEIGHT + 5;
        }
    }

    private void renderModulePanel(final DrawContext drawContext, final int mouseX, final int mouseY) {
        final int screenWidth = Krypton.mc.getWindow().getWidth();
        final int screenHeight = Krypton.mc.getWindow().getHeight();
        final int startX = (screenWidth - TOTAL_WIDTH) / 2 + SETTINGS_PANEL_WIDTH + PANEL_SPACING + CATEGORY_PANEL_WIDTH + PANEL_SPACING;
        final int startY = (screenHeight - TOTAL_HEIGHT) / 2 + 20;
        final int endX = startX + MODULE_PANEL_WIDTH;
        final int endY = startY + TOTAL_HEIGHT;

        renderShadow(drawContext, startX, startY, MODULE_PANEL_WIDTH, TOTAL_HEIGHT);
        RenderUtils.renderRoundedQuad(drawContext.getMatrices(), PANEL_COLOR,
                startX, startY, endX, endY, CORNER_RADIUS, CORNER_RADIUS, CORNER_RADIUS, CORNER_RADIUS, 200);

        // Category header
        TextRenderer.drawString(this.selectedCategory.name.toString().toUpperCase(), drawContext,
                startX + PADDING, startY + 22, toMCColor(ACCENT_COLOR));

        // Modern search bar
        final int searchY = startY + HEADER_HEIGHT - 5;
        final int searchStartX = startX + PADDING;
        final int searchEndX = endX - PADDING;
        
        RenderUtils.renderRoundedQuad(drawContext.getMatrices(), SEARCH_BG,
                searchStartX, searchY, searchEndX, searchY + 36, 18, 18, 18, 18, 30);
        
        // Search icon
        TextRenderer.drawString("🔍", drawContext, searchStartX + 12, searchY + 10, toMCColor(TEXT_SECONDARY));
        
        final String searchText = this.searchQuery.isEmpty() ? "Search modules..." : this.searchQuery;
        final Color searchTextColor = this.searchQuery.isEmpty() ? TEXT_SECONDARY : TEXT_COLOR;
        TextRenderer.drawString(searchText, drawContext, searchStartX + 32, searchY + 12, toMCColor(searchTextColor));

        if (this.searchFocused && System.currentTimeMillis() % 1000 < 500) {
            final int cursorX = searchStartX + 32 + TextRenderer.getWidth(this.searchQuery);
            drawContext.fill(cursorX, searchY + 8, cursorX + 2, searchY + 28, toMCColor(ACCENT_COLOR));
        }

        final List<Module> modules = Krypton.INSTANCE.getModuleManager().a(this.selectedCategory);
        int yOffset = startY + HEADER_HEIGHT + 45;

        for (Module module : modules) {
            if (!this.searchQuery.isEmpty() && !module.getName().toString().toLowerCase().contains(this.searchQuery.toLowerCase())) {
                continue;
            }

            final boolean isSelected = module == this.selectedModule;
            final boolean isHovered = this.isHoveredInRect(mouseX, mouseY, startX, yOffset, MODULE_PANEL_WIDTH, ITEM_HEIGHT);
            final boolean isEnabled = module.isEnabled();
            
            // Animation for module items
            moduleAnimations.putIfAbsent(module, 0f);
            float target = isHovered ? 1f : 0f;
            float current = moduleAnimations.get(module);
            current += (target - current) * 0.2f;
            moduleAnimations.put(module, current);

            if (isSelected) {
                RenderUtils.renderRoundedQuad(drawContext.getMatrices(), SELECTED_COLOR,
                        startX + 8, yOffset, endX - 8, yOffset + ITEM_HEIGHT, 8, 8, 8, 8, 30);
            } else if (current > 0.01f) {
                int alpha = (int) (15 * current);
                RenderUtils.renderRoundedQuad(drawContext.getMatrices(), new Color(0, 230, 180, alpha),
                        startX + 8, yOffset, endX - 8, yOffset + ITEM_HEIGHT, 8, 8, 8, 8, 30);
            }

            // Module name
            Color textColor = isEnabled ? ACCENT_COLOR : TEXT_COLOR;
            TextRenderer.drawString(module.getName().toString(), drawContext,
                    startX + PADDING, yOffset + 12, toMCColor(textColor));
            
            // Module description (if any)
            if (module.getDescription() != null && !module.getDescription().isEmpty()) {
                TextRenderer.drawString(module.getDescription(), drawContext,
                        startX + PADDING, yOffset + 28, toMCColor(TEXT_SECONDARY));
            }

            // Modern status indicator
            Color indicatorColor = isEnabled ? ACCENT_COLOR : MODULE_OFF_COLOR;
            RenderUtils.renderRoundedQuad(drawContext.getMatrices(), indicatorColor,
                    endX - 30, yOffset + 12, endX - 18, yOffset + 28, 6, 6, 6, 6, 30);
            
            if (isEnabled) {
                // Glow effect for enabled modules
                RenderUtils.renderRoundedQuad(drawContext.getMatrices(), ACCENT_GLOW,
                        endX - 32, yOffset + 10, endX - 16, yOffset + 30, 8, 8, 8, 8, 30);
            }

            yOffset += ITEM_HEIGHT + 5;
        }
    }

    private void renderShadow(DrawContext context, int x, int y, int width, int height) {
        for (int i = 0; i < 8; i++) {
            int alpha = (int) (20 * (1 - (float) i / 8));
            context.fill(x - i, y - i, x + width + i, y + height + i, new Color(0, 0, 0, alpha).getRGB());
        }
    }

    private void drawVerticalGradient(DrawContext context, int x, int y, int width, int height, Color top, Color bottom) {
        for (int i = 0; i < height; i++) {
            float ratio = (float) i / height;
            Color color = lerpColor(top, bottom, ratio);
            context.fill(x, y + i, x + width, y + i + 1, toMCColor(color));
        }
    }

    private Color lerpColor(Color c1, Color c2, float t) {
        int r = (int) (c1.getRed() + (c2.getRed() - c1.getRed()) * t);
        int g = (int) (c1.getGreen() + (c2.getGreen() - c1.getGreen()) * t);
        int b = (int) (c1.getBlue() + (c2.getBlue() - c1.getBlue()) * t);
        return new Color(r, g, b);
    }

    private boolean isHoveredInRect(final int mouseX, final int mouseY, final int x, final int y, final int width, final int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    private boolean isSearchBarHovered(final int mouseX, final int mouseY) {
        final int screenWidth = Krypton.mc.getWindow().getWidth();
        final int screenHeight = Krypton.mc.getWindow().getHeight();
        final int startX = (screenWidth - TOTAL_WIDTH) / 2 + SETTINGS_PANEL_WIDTH + PANEL_SPACING + CATEGORY_PANEL_WIDTH + PANEL_SPACING;
        final int startY = (screenHeight - TOTAL_HEIGHT) / 2 + 20;
        final int searchY = startY + HEADER_HEIGHT - 5;
        final int searchStartX = startX + PADDING;
        final int searchEndX = startX + MODULE_PANEL_WIDTH - PADDING;
        
        return this.isHoveredInRect(mouseX, mouseY, searchStartX, searchY, searchEndX - searchStartX, 36);
    }

    public boolean keyPressed(final int keyCode, final int scanCode, final int modifiers) {
        if (this.searchFocused) {
            if (keyCode == 259 && !this.searchQuery.isEmpty()) {
                this.searchQuery = this.searchQuery.substring(0, this.searchQuery.length() - 1);
                return true;
            } else if (keyCode == 256) {
                this.searchFocused = false;
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public boolean charTyped(final char chr, final int modifiers) {
        if (this.searchFocused && (Character.isLetterOrDigit(chr) || chr == ' ')) {
            this.searchQuery += chr;
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
        final double scaledMouseX = mouseX * MinecraftClient.getInstance().getWindow().getScaleFactor();
        final double scaledMouseY = mouseY * MinecraftClient.getInstance().getWindow().getScaleFactor();

        final int screenWidth = Krypton.mc.getWindow().getWidth();
        final int screenHeight = Krypton.mc.getWindow().getHeight();

        if (this.isSearchBarHovered((int) scaledMouseX, (int) scaledMouseY)) {
            this.searchFocused = true;
            return true;
        } else {
            this.searchFocused = false;
        }

        // Category panel clicks
        final int categoryStartX = (screenWidth - TOTAL_WIDTH) / 2 + SETTINGS_PANEL_WIDTH + PANEL_SPACING;
        final int categoryStartY = (screenHeight - TOTAL_HEIGHT) / 2 + 20 + HEADER_HEIGHT + PADDING;
        int categoryY = categoryStartY;

        for (Category category : Category.values()) {
            if (this.isHoveredInRect((int) scaledMouseX, (int) scaledMouseY, categoryStartX, categoryY, CATEGORY_PANEL_WIDTH, ITEM_HEIGHT)) {
                this.selectedCategory = category;
                this.selectedModule = null;
                return true;
            }
            categoryY += ITEM_HEIGHT + 5;
        }

        // Module panel clicks
        final int modulePanelStartX = (screenWidth - TOTAL_WIDTH) / 2 + SETTINGS_PANEL_WIDTH + PANEL_SPACING + CATEGORY_PANEL_WIDTH + PANEL_SPACING;
        final int modulePanelStartY = (screenHeight - TOTAL_HEIGHT) / 2 + 20 + HEADER_HEIGHT + 45;
        final List<Module> modules = Krypton.INSTANCE.getModuleManager().a(this.selectedCategory);
        int moduleY = modulePanelStartY;

        for (Module module : modules) {
            if (!this.searchQuery.isEmpty() && !module.getName().toString().toLowerCase().contains(this.searchQuery.toLowerCase())) {
                continue;
            }

            if (this.isHoveredInRect((int) scaledMouseX, (int) scaledMouseY, modulePanelStartX, moduleY, MODULE_PANEL_WIDTH, ITEM_HEIGHT)) {
                if (button == 0) {
                    module.toggle();
                } else if (button == 1) {
                    this.selectedModule = module;
                }
                return true;
            }
            moduleY += ITEM_HEIGHT + 5;
        }

        // Settings panel clicks
        if (this.selectedModule != null) {
            final int settingsPanelStartX = (screenWidth - TOTAL_WIDTH) / 2;
            final int settingsPanelStartY = (screenHeight - TOTAL_HEIGHT) / 2 + 20 + HEADER_HEIGHT + PADDING;
            int settingY = settingsPanelStartY;

            for (Object setting : this.selectedModule.getSettings()) {
                if (setting instanceof Setting) {
                    if (this.isHoveredInRect((int) scaledMouseX, (int) scaledMouseY, settingsPanelStartX, settingY, SETTINGS_PANEL_WIDTH, ITEM_HEIGHT)) {
                        this.handleModernSettingClick(setting, button, (int) scaledMouseX, (int) scaledMouseY, settingsPanelStartX, settingY);
                        return true;
                    }
                    settingY += ITEM_HEIGHT + 8;
                }
            }
        }

        return super.mouseClicked(scaledMouseX, scaledMouseY, button);
    }

    private void handleModernSettingClick(final Object setting, final int button, final int mouseX, final int mouseY, final int panelX, final int settingY) {
        if (setting instanceof BooleanSetting) {
            final BooleanSetting boolSetting = (BooleanSetting) setting;
            if (button == 0) {
                boolSetting.toggle();
            }
        } else if (setting instanceof ModeSetting) {
            final ModeSetting<?> modeSetting = (ModeSetting<?>) setting;
            if (button == 0) {
                modeSetting.cycleUp();
            } else if (button == 1) {
                modeSetting.cycleDown();
            }
        } else if (setting instanceof NumberSetting) {
            final NumberSetting numSetting = (NumberSetting) setting;
            final int sliderY = settingY + 30;
            final int sliderStartX = panelX + PADDING;
            final int sliderEndX = panelX + SETTINGS_PANEL_WIDTH - PADDING - 45;
            
            if (this.isHoveredInRect(mouseX, mouseY, sliderStartX, sliderY - 5, sliderEndX - sliderStartX, 14)) {
                this.draggingSlider = true;
                this.draggingSliderSetting = (Setting) setting;
                this.updateSliderValue(numSetting, mouseX, sliderStartX, sliderEndX);
            }
        }
    }

    private void updateSliderValue(final NumberSetting setting, final int mouseX, final int sliderStartX, final int sliderEndX) {
        final double progress = Math.max(0.0, Math.min(1.0, (double) (mouseX - sliderStartX) / (sliderEndX - sliderStartX)));
        final double newValue = setting.getMin() + progress * (setting.getMax() - setting.getMin());
        setting.getValue(MathUtil.roundToNearest(newValue, setting.getFormat()));
    }

    public boolean mouseDragged(final double mouseX, final double mouseY, final int button, final double deltaX, final double deltaY) {
        if (this.draggingSlider && this.draggingSliderSetting != null) {
            final double scaledMouseX = mouseX * MinecraftClient.getInstance().getWindow().getScaleFactor();
            final int screenWidth = Krypton.mc.getWindow().getWidth();
            final int screenHeight = Krypton.mc.getWindow().getHeight();
            final int settingsPanelStartX = (screenWidth - TOTAL_WIDTH) / 2;
            final int sliderStartX = settingsPanelStartX + PADDING;
            final int sliderEndX = settingsPanelStartX + SETTINGS_PANEL_WIDTH - PADDING - 45;
            
            if (this.draggingSliderSetting instanceof NumberSetting) {
                this.updateSliderValue((NumberSetting) this.draggingSliderSetting, (int) scaledMouseX, sliderStartX, sliderEndX);
            }
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    public boolean mouseReleased(final double mouseX, final double mouseY, final int button) {
        if (this.draggingSlider) {
            this.draggingSlider = false;
            this.draggingSliderSetting = null;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
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
        this.searchFocused = false;
        this.draggingSlider = false;
        this.draggingSliderSetting = null;
    }
}
