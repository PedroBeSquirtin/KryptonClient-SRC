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

    // Custom transparent constant (since java.awt.Color has no TRANSPARENT)
    private static final Color TRANSPARENT = new Color(0, 0, 0, 0);

    // Color scheme
    private final Color BACKGROUND_COLOR = new Color(20, 20, 25, 200);
    private final Color PANEL_COLOR = new Color(30, 30, 35, 255);
    private final Color ACCENT_COLOR = new Color(138, 43, 226, 255);
    private final Color SELECTED_COLOR = new Color(138, 43, 226, 180);
    private final Color TEXT_COLOR = new Color(220, 220, 220, 255);
    private final Color SEARCH_BG = new Color(40, 40, 45, 255);
    private final Color HOVER_COLOR = new Color(60, 60, 70, 255);

    // Layout constants - made bigger and centered
    private static final int SETTINGS_PANEL_WIDTH = 320;
    private static final int CATEGORY_PANEL_WIDTH = 180;
    private static final int MODULE_PANEL_WIDTH = 350;
    private static final int HEADER_HEIGHT = 45;
    private static final int ITEM_HEIGHT = 32;
    private static final int PADDING = 15;
    private static final int PANEL_SPACING = 20;
    private static final int TOTAL_WIDTH = SETTINGS_PANEL_WIDTH + CATEGORY_PANEL_WIDTH + MODULE_PANEL_WIDTH + (PANEL_SPACING * 2);
    private static final int TOTAL_HEIGHT = 500;

    public ClickGUI() {
        super(Text.empty());
        this.selectedCategory = Category.COMBAT;
        this.searchQuery = "";
        this.searchFocused = false;
        this.draggingSlider = false;
        this.draggingSliderSetting = null;
    }

    // Utility to convert java.awt.Color to MC color int
    private static int toMCColor(Color c) {
        return net.minecraft.util.math.ColorHelper.Argb.getArgb(c.getAlpha(), c.getRed(), c.getGreen(), c.getBlue());
    }

    public boolean isDraggingAlready() {
        return this.draggingSlider;
    }

    public void setTooltip(final CharSequence tooltipText, final int tooltipX, final int tooltipY) {
        // Modern tooltip handling can be added here
    }

    public void setInitialFocus() {
        if (this.client == null) {
            return;
        }
        super.setInitialFocus();
    }

    public void render(final DrawContext drawContext, final int n, final int n2, final float n3) {
        if (Krypton.mc.currentScreen == this) {
            if (Krypton.INSTANCE.screen != null) {
                Krypton.INSTANCE.screen.render(drawContext, 0, 0, n3);
            }
            if (this.currentColor == null) {
                this.currentColor = new Color(0, 0, 0, 0);
            } else {
                this.currentColor = new Color(0, 0, 0, this.currentColor.getAlpha());
            }

            int n4;
            if (skid.krypton.module.modules.client.Krypton.renderBackground.getValue()) {
                n4 = 200;
            } else {
                n4 = 0;
            }
            if (this.currentColor.getAlpha() != n4) {
                int n5;
                if (skid.krypton.module.modules.client.Krypton.renderBackground.getValue()) {
                    n5 = 200;
                } else {
                    n5 = 0;
                }
                this.currentColor = ColorUtil.a(0.05f, n5, this.currentColor);
            }
            if (Krypton.mc.currentScreen instanceof ClickGUI) {
                drawContext.fill(0, 0, Krypton.mc.getWindow().getWidth(), Krypton.mc.getWindow().getHeight(), this.currentColor.getRGB());
            }
            RenderUtils.unscaledProjection();
            final int n6 = n * (int) MinecraftClient.getInstance().getWindow().getScaleFactor();
            final int n7 = n2 * (int) MinecraftClient.getInstance().getWindow().getScaleFactor();
            super.render(drawContext, n6, n7, n3);

            this.renderBackground(drawContext);
            this.renderSettingsPanel(drawContext, n6, n7);
            this.renderCategoryPanel(drawContext, n6, n7);
            this.renderModulePanel(drawContext, n6, n7);

            RenderUtils.scaledProjection();
        }
    }

    private void renderBackground(final DrawContext drawContext) {
        final int screenWidth = Krypton.mc.getWindow().getWidth();
        final int screenHeight = Krypton.mc.getWindow().getHeight();

        if (skid.krypton.module.modules.client.Krypton.renderBackground.getValue()) {
            drawContext.fill(0, 0, screenWidth, screenHeight, toMCColor(new Color(0, 0, 0, 100)));
        }
    }

    private void renderSettingsPanel(final DrawContext drawContext, final int mouseX, final int mouseY) {
        final int screenWidth = Krypton.mc.getWindow().getWidth();
        final int screenHeight = Krypton.mc.getWindow().getHeight();
        final int startX = (screenWidth - TOTAL_WIDTH) / 2;
        final int startY = (screenHeight - TOTAL_HEIGHT) / 2;
        final int endX = startX + SETTINGS_PANEL_WIDTH;
        final int endY = startY + TOTAL_HEIGHT;

        RenderUtils.renderRoundedQuad(drawContext.getMatrices(), PANEL_COLOR,
                startX, startY, endX, endY, 8.0, 8.0, 8.0, 8.0, 50.0);

        if (this.selectedModule != null) {
            final String headerText = "SETTINGS: " + this.selectedModule.getName().toString().toUpperCase();
            TextRenderer.drawString(headerText, drawContext, startX + PADDING, startY + 15, toMCColor(ACCENT_COLOR));

            int yOffset = startY + HEADER_HEIGHT + PADDING;

            for (Object setting : this.selectedModule.getSettings()) {
                if (setting instanceof Setting) {
                    final Setting s = (Setting) setting;
                    final boolean isHovered = this.isHoveredInRect(mouseX, mouseY, startX, yOffset, SETTINGS_PANEL_WIDTH, ITEM_HEIGHT);

                    if (isHovered && !this.draggingSlider) {
                        RenderUtils.renderRoundedQuad(drawContext.getMatrices(), HOVER_COLOR,
                                startX + 5, yOffset, endX - 5, yOffset + ITEM_HEIGHT, 4.0, 4.0, 4.0, 4.0, 30.0);
                    }

                    TextRenderer.drawString(s.getName().toString().toUpperCase(), drawContext,
                            startX + PADDING, yOffset + 8, toMCColor(TEXT_COLOR));

                    this.renderSettingValue(drawContext, setting, startX, endX, yOffset, mouseX, mouseY);

                    yOffset += ITEM_HEIGHT + 5;
                }
            }
        } else {
            TextRenderer.drawCenteredString("SELECT A MODULE", drawContext,
                    startX + SETTINGS_PANEL_WIDTH / 2, startY + 100, toMCColor(new Color(120, 120, 120, 255)));
        }
    }

    private void renderSettingValue(final DrawContext drawContext, final Object setting, final int startX, final int endX, final int yOffset, final int mouseX, final int mouseY) {
        if (setting instanceof BooleanSetting) {
            final BooleanSetting boolSetting = (BooleanSetting) setting;
            final String valueText = boolSetting.getValue() ? "ON" : "OFF";
            final Color valueColor = boolSetting.getValue() ? ACCENT_COLOR : new Color(120, 120, 120, 255);
            TextRenderer.drawString(valueText, drawContext,
                    endX - PADDING - TextRenderer.getWidth(valueText), yOffset + 8, toMCColor(valueColor));
        } else if (setting instanceof NumberSetting) {
            final NumberSetting numSetting = (NumberSetting) setting;
            final String valueText = String.format("%.2f", numSetting.getValue());
            TextRenderer.drawString(valueText, drawContext,
                    endX - PADDING - TextRenderer.getWidth(valueText), yOffset + 8, toMCColor(ACCENT_COLOR));
            
            // Render slider
            final int sliderY = yOffset + 20;
            final int sliderStartX = startX + PADDING;
            final int sliderEndX = endX - PADDING;
            final int sliderWidth = sliderEndX - sliderStartX;
            
            // Slider track
            RenderUtils.renderRoundedQuad(drawContext.getMatrices(), new Color(60, 60, 65, 255),
                    sliderStartX, sliderY, sliderEndX, sliderY + 4, 2.0, 2.0, 2.0, 2.0, 20.0);
            
            // Slider progress
            final double progress = (numSetting.getValue() - numSetting.getMin()) / (numSetting.getMax() - numSetting.getMin());
            final int progressWidth = (int) (sliderWidth * progress);
            if (progressWidth > 0) {
                RenderUtils.renderRoundedQuad(drawContext.getMatrices(), ACCENT_COLOR,
                        sliderStartX, sliderY, sliderStartX + progressWidth, sliderY + 4, 2.0, 2.0, 2.0, 2.0, 20.0);
            }
            
            // Slider handle
            final int handleX = sliderStartX + progressWidth - 3;
            RenderUtils.renderRoundedQuad(drawContext.getMatrices(), Color.WHITE,
                    handleX, sliderY - 2, handleX + 6, sliderY + 8, 3.0, 3.0, 3.0, 3.0, 15.0);
        } else if (setting instanceof ModeSetting) {
            final ModeSetting<?> modeSetting = (ModeSetting<?>) setting;
            final String valueText = modeSetting.getValue().toString();
            TextRenderer.drawString(valueText, drawContext,
                    endX - PADDING - TextRenderer.getWidth(valueText), yOffset + 8, toMCColor(ACCENT_COLOR));
        } else if (setting instanceof BindSetting) {
            final BindSetting bindSetting = (BindSetting) setting;
            final String valueText = bindSetting.getValue() == -1 ? "NONE" : KeyUtils.getKey(bindSetting.getValue()).toString();
            TextRenderer.drawString(valueText, drawContext,
                    endX - PADDING - TextRenderer.getWidth(valueText), yOffset + 8, toMCColor(ACCENT_COLOR));
        } else if (setting instanceof StringSetting) {
            final StringSetting stringSetting = (StringSetting) setting;
            final String valueText = stringSetting.getValue().isEmpty() ? "EMPTY" : stringSetting.getValue();
            TextRenderer.drawString(valueText, drawContext,
                    endX - PADDING - TextRenderer.getWidth(valueText), yOffset + 8, toMCColor(ACCENT_COLOR));
        } else if (setting instanceof MinMaxSetting) {
            final MinMaxSetting minMaxSetting = (MinMaxSetting) setting;
            final String valueText = String.format("%.1f - %.1f", minMaxSetting.getCurrentMin(), minMaxSetting.getCurrentMax());
            TextRenderer.drawString(valueText, drawContext,
                    endX - PADDING - TextRenderer.getWidth(valueText), yOffset + 8, toMCColor(ACCENT_COLOR));
            
            // Render dual slider
            final int sliderY = yOffset + 20;
            final int sliderStartX = startX + PADDING;
            final int sliderEndX = endX - PADDING;
            final int sliderWidth = sliderEndX - sliderStartX;
            
            // Slider track
            RenderUtils.renderRoundedQuad(drawContext.getMatrices(), new Color(60, 60, 65, 255),
                    sliderStartX, sliderY, sliderEndX, sliderY + 4, 2.0, 2.0, 2.0, 2.0, 20.0);
            
            // Slider progress between min and max
            final double minProgress = (minMaxSetting.getCurrentMin() - minMaxSetting.getMinValue()) / (minMaxSetting.getMaxValue() - minMaxSetting.getMinValue());
            final double maxProgress = (minMaxSetting.getCurrentMax() - minMaxSetting.getMinValue()) / (minMaxSetting.getMaxValue() - minMaxSetting.getMinValue());
            final int minX = sliderStartX + (int) (sliderWidth * minProgress);
            final int maxX = sliderStartX + (int) (sliderWidth * maxProgress);
            
            if (maxX > minX) {
                RenderUtils.renderRoundedQuad(drawContext.getMatrices(), ACCENT_COLOR,
                        minX, sliderY, maxX, sliderY + 4, 2.0, 2.0, 2.0, 2.0, 20.0);
            }
            
            // Slider handles
            RenderUtils.renderRoundedQuad(drawContext.getMatrices(), Color.WHITE,
                    minX - 3, sliderY - 2, minX + 3, sliderY + 8, 3.0, 3.0, 3.0, 3.0, 15.0);
            RenderUtils.renderRoundedQuad(drawContext.getMatrices(), Color.WHITE,
                    maxX - 3, sliderY - 2, maxX + 3, sliderY + 8, 3.0, 3.0, 3.0, 3.0, 15.0);
        }
    }

    private void renderCategoryPanel(final DrawContext drawContext, final int mouseX, final int mouseY) {
        final int screenWidth = Krypton.mc.getWindow().getWidth();
        final int screenHeight = Krypton.mc.getWindow().getHeight();
        final int startX = (screenWidth - TOTAL_WIDTH) / 2 + SETTINGS_PANEL_WIDTH + PANEL_SPACING;
        final int startY = (screenHeight - TOTAL_HEIGHT) / 2;
        final int endX = startX + CATEGORY_PANEL_WIDTH;
        final int endY = startY + TOTAL_HEIGHT;

        RenderUtils.renderRoundedQuad(drawContext.getMatrices(), PANEL_COLOR,
                startX, startY, endX, endY, 8.0, 8.0, 8.0, 8.0, 50.0);

        TextRenderer.drawString("Krypton+", drawContext, startX + PADDING, startY + 15, toMCColor(ACCENT_COLOR));

        int yOffset = startY + HEADER_HEIGHT + PADDING;
        for (Category category : Category.values()) {
            final boolean isSelected = category == this.selectedCategory;
            final boolean isHovered = this.isHoveredInRect(mouseX, mouseY, startX, yOffset, CATEGORY_PANEL_WIDTH, ITEM_HEIGHT);

            Color bgColor = isSelected ? SELECTED_COLOR : (isHovered ? HOVER_COLOR : TRANSPARENT);
            Color textColor = isSelected ? Color.WHITE : TEXT_COLOR;

            if (bgColor != TRANSPARENT) {
                RenderUtils.renderRoundedQuad(drawContext.getMatrices(), bgColor,
                        startX + 5, yOffset, endX - 5, yOffset + ITEM_HEIGHT, 6.0, 6.0, 6.0, 6.0, 30.0);
            }

            TextRenderer.drawString(category.name.toString().toUpperCase(), drawContext,
                    startX + PADDING, yOffset + 8, toMCColor(textColor));

            yOffset += ITEM_HEIGHT + 5;
        }
    }

    private void renderModulePanel(final DrawContext drawContext, final int mouseX, final int mouseY) {
        final int screenWidth = Krypton.mc.getWindow().getWidth();
        final int screenHeight = Krypton.mc.getWindow().getHeight();
        final int startX = (screenWidth - TOTAL_WIDTH) / 2 + SETTINGS_PANEL_WIDTH + PANEL_SPACING + CATEGORY_PANEL_WIDTH + PANEL_SPACING;
        final int startY = (screenHeight - TOTAL_HEIGHT) / 2;
        final int endX = startX + MODULE_PANEL_WIDTH;
        final int endY = startY + TOTAL_HEIGHT;

        RenderUtils.renderRoundedQuad(drawContext.getMatrices(), PANEL_COLOR,
                startX, startY, endX, endY, 8.0, 8.0, 8.0, 8.0, 50.0);

        final String categoryTitle = "CATEGORY: " + this.selectedCategory.name.toString().toUpperCase();
        TextRenderer.drawString(categoryTitle, drawContext, startX + PADDING, startY + 15, toMCColor(TEXT_COLOR));

        // Search bar
        final int searchY = startY + HEADER_HEIGHT - 15;
        final int searchStartX = startX + PADDING;
        final int searchEndX = endX - PADDING;
        final Color searchBgColor = this.searchFocused ? new Color(50, 50, 55, 255) : SEARCH_BG;
        
        RenderUtils.renderRoundedQuad(drawContext.getMatrices(), searchBgColor,
                searchStartX, searchY, searchEndX, searchY + 25, 6.0, 6.0, 6.0, 6.0, 30.0);

        final String searchText = this.searchQuery.isEmpty() ? "SEARCH..." : this.searchQuery;
        final Color searchTextColor = this.searchQuery.isEmpty() ? new Color(120, 120, 120, 255) : TEXT_COLOR;
        TextRenderer.drawString(searchText, drawContext, searchStartX + 10, searchY + 8, toMCColor(searchTextColor));

        // Cursor for search
        if (this.searchFocused && System.currentTimeMillis() % 1000 < 500) {
            final int cursorX = searchStartX + 10 + TextRenderer.getWidth(this.searchQuery);
            drawContext.fill(cursorX, searchY + 5, cursorX + 1, searchY + 20, toMCColor(TEXT_COLOR));
        }

        final List<Module> modules = Krypton.INSTANCE.getModuleManager().a(this.selectedCategory);
        int yOffset = startY + HEADER_HEIGHT + 25;

        for (Module module : modules) {
            if (!this.searchQuery.isEmpty() && !module.getName().toString().toLowerCase().contains(this.searchQuery.toLowerCase())) {
                continue;
            }

            final boolean isSelected = module == this.selectedModule;
            final boolean isHovered = this.isHoveredInRect(mouseX, mouseY, startX, yOffset, MODULE_PANEL_WIDTH, ITEM_HEIGHT);
            final boolean isEnabled = module.isEnabled();

            Color bgColor = isSelected ? SELECTED_COLOR : (isHovered ? HOVER_COLOR : TRANSPARENT);
            Color textColor = isEnabled ? ACCENT_COLOR : TEXT_COLOR;

            if (bgColor != TRANSPARENT) {
                RenderUtils.renderRoundedQuad(drawContext.getMatrices(), bgColor,
                        startX + 5, yOffset, endX - 5, yOffset + ITEM_HEIGHT, 6.0, 6.0, 6.0, 6.0, 30.0);
            }

            TextRenderer.drawString(module.getName().toString().toUpperCase(), drawContext,
                    startX + PADDING, yOffset + 8, toMCColor(textColor));

            final Color indicatorColor = isEnabled ? ACCENT_COLOR : new Color(80, 80, 85, 255);
            RenderUtils.renderRoundedQuad(drawContext.getMatrices(), indicatorColor,
                    endX - 25, yOffset + 8, endX - 10, yOffset + 24, 3.0, 3.0, 3.0, 3.0, 20.0);

            yOffset += ITEM_HEIGHT + 3;
        }
    }

    private boolean isHoveredInRect(final int mouseX, final int mouseY, final int x, final int y, final int width, final int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    private boolean isSearchBarHovered(final int mouseX, final int mouseY) {
        final int screenWidth = Krypton.mc.getWindow().getWidth();
        final int screenHeight = Krypton.mc.getWindow().getHeight();
        final int startX = (screenWidth - TOTAL_WIDTH) / 2 + SETTINGS_PANEL_WIDTH + PANEL_SPACING + CATEGORY_PANEL_WIDTH + PANEL_SPACING;
        final int startY = (screenHeight - TOTAL_HEIGHT) / 2;
        final int searchY = startY + HEADER_HEIGHT - 15;
        final int searchStartX = startX + PADDING;
        final int searchEndX = startX + MODULE_PANEL_WIDTH - PADDING;
        
        return this.isHoveredInRect(mouseX, mouseY, searchStartX, searchY, searchEndX - searchStartX, 25);
    }

    public boolean keyPressed(final int keyCode, final int scanCode, final int modifiers) {
        if (this.searchFocused) {
            if (keyCode == 259 && !this.searchQuery.isEmpty()) { // Backspace
                this.searchQuery = this.searchQuery.substring(0, this.searchQuery.length() - 1);
                return true;
            } else if (keyCode == 256) { // Escape
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

        // Check search bar click
        if (this.isSearchBarHovered((int) scaledMouseX, (int) scaledMouseY)) {
            this.searchFocused = true;
            return true;
        } else {
            this.searchFocused = false;
        }

        // Category panel clicks
        final int categoryStartX = (screenWidth - TOTAL_WIDTH) / 2 + SETTINGS_PANEL_WIDTH + PANEL_SPACING;
        final int categoryStartY = (screenHeight - TOTAL_HEIGHT) / 2 + HEADER_HEIGHT + PADDING;
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
        final int modulePanelStartY = (screenHeight - TOTAL_HEIGHT) / 2 + HEADER_HEIGHT + 25;
        final List<Module> modules = Krypton.INSTANCE.getModuleManager().a(this.selectedCategory);
        int moduleY = modulePanelStartY;

        for (Module module : modules) {
            if (!this.searchQuery.isEmpty() && !module.getName().toString().toLowerCase().contains(this.searchQuery.toLowerCase())) {
                continue;
            }

            if (this.isHoveredInRect((int) scaledMouseX, (int) scaledMouseY, modulePanelStartX, moduleY, MODULE_PANEL_WIDTH, ITEM_HEIGHT)) {
                if (button == 0) { // Left click - toggle module
                    module.toggle();
                } else if (button == 1) { // Right click - select for settings
                    this.selectedModule = module;
                }
                return true;
            }
            moduleY += ITEM_HEIGHT + 3;
        }

        // Settings panel clicks
        if (this.selectedModule != null) {
            final int settingsPanelStartX = (screenWidth - TOTAL_WIDTH) / 2;
            final int settingsPanelStartY = (screenHeight - TOTAL_HEIGHT) / 2 + HEADER_HEIGHT + PADDING;
            int settingY = settingsPanelStartY;

            for (Object setting : this.selectedModule.getSettings()) {
                if (setting instanceof Setting) {
                    if (this.isHoveredInRect((int) scaledMouseX, (int) scaledMouseY, settingsPanelStartX, settingY, SETTINGS_PANEL_WIDTH, ITEM_HEIGHT)) {
                        this.handleSettingClick(setting, button, (int) scaledMouseX, (int) scaledMouseY, settingsPanelStartX, settingY);
                        return true;
                    }
                    settingY += ITEM_HEIGHT + 5;
                }
            }
        }

        return super.mouseClicked(scaledMouseX, scaledMouseY, button);
    }

    private void handleSettingClick(final Object setting, final int button, final int mouseX, final int mouseY, final int panelX, final int settingY) {
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
            final int sliderY = settingY + 20;
            final int sliderStartX = panelX + PADDING;
            final int sliderEndX = panelX + SETTINGS_PANEL_WIDTH - PADDING;
            
            if (this.isHoveredInRect(mouseX, mouseY, sliderStartX, sliderY - 5, sliderEndX - sliderStartX, 14)) {
                this.draggingSlider = true;
                this.draggingSliderSetting = (Setting) setting;
                this.updateSliderValue(numSetting, mouseX, sliderStartX, sliderEndX);
            }
        } else if (setting instanceof MinMaxSetting) {
            final MinMaxSetting minMaxSetting = (MinMaxSetting) setting;
            final int sliderY = settingY + 20;
            final int sliderStartX = panelX + PADDING;
            final int sliderEndX = panelX + SETTINGS_PANEL_WIDTH - PADDING;
            
            if (this.isHoveredInRect(mouseX, mouseY, sliderStartX, sliderY - 5, sliderEndX - sliderStartX, 14)) {
                this.draggingSlider = true;
                this.draggingSliderSetting = (Setting) setting;
                this.updateMinMaxSliderValue(minMaxSetting, mouseX, sliderStartX, sliderEndX);
            }
        }
    }

    private void updateSliderValue(final NumberSetting setting, final int mouseX, final int sliderStartX, final int sliderEndX) {
        final double progress = Math.max(0.0, Math.min(1.0, (double) (mouseX - sliderStartX) / (sliderEndX - sliderStartX)));
        final double newValue = setting.getMin() + progress * (setting.getMax() - setting.getMin());
        setting.getValue(MathUtil.roundToNearest(newValue, setting.getFormat()));
    }

    private void updateMinMaxSliderValue(final MinMaxSetting setting, final int mouseX, final int sliderStartX, final int sliderEndX) {
        final double progress = Math.max(0.0, Math.min(1.0, (double) (mouseX - sliderStartX) / (sliderEndX - sliderStartX)));
        final double newValue = setting.getMinValue() + progress * (setting.getMaxValue() - setting.getMinValue());
        
        final double currentMinProgress = (setting.getCurrentMin() - setting.getMinValue()) / (setting.getMaxValue() - setting.getMinValue());
        final double currentMaxProgress = (setting.getCurrentMax() - setting.getMinValue()) / (setting.getMaxValue() - setting.getMinValue());
        
        // Determine which handle is closer
        if (Math.abs(progress - currentMinProgress) < Math.abs(progress - currentMaxProgress)) {
            setting.setCurrentMin(Math.min(newValue, setting.getCurrentMax()));
        } else {
            setting.setCurrentMax(Math.max(newValue, setting.getCurrentMin()));
        }
    }

    public boolean mouseDragged(final double mouseX, final double mouseY, final int button, final double deltaX, final double deltaY) {
        if (this.draggingSlider && this.draggingSliderSetting != null) {
            final double scaledMouseX = mouseX * MinecraftClient.getInstance().getWindow().getScaleFactor();
            final int screenWidth = Krypton.mc.getWindow().getWidth();
            final int screenHeight = Krypton.mc.getWindow().getHeight();
            final int settingsPanelStartX = (screenWidth - TOTAL_WIDTH) / 2;
            final int sliderStartX = settingsPanelStartX + PADDING;
            final int sliderEndX = settingsPanelStartX + SETTINGS_PANEL_WIDTH - PADDING;
            
            if (this.draggingSliderSetting instanceof NumberSetting) {
                this.updateSliderValue((NumberSetting) this.draggingSliderSetting, (int) scaledMouseX, sliderStartX, sliderEndX);
            } else if (this.draggingSliderSetting instanceof MinMaxSetting) {
                this.updateMinMaxSliderValue((MinMaxSetting) this.draggingSliderSetting, (int) scaledMouseX, sliderStartX, sliderEndX);
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

    static {
    }
}