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
    private Map<Module, Float> moduleAnimations = new HashMap<>();
    private Module hoveredModule = null;
    private String hoverDescription = "";
    private int hoverDescriptionX = 0, hoverDescriptionY = 0;
    private long hoverStartTime = 0;

    private static final Color TRANSPARENT = new Color(0, 0, 0, 0);

    // Professional, clean color scheme with high contrast
    private final Color BACKGROUND_DARK = new Color(12, 12, 16, 245);
    private final Color PANEL_BG = new Color(22, 22, 28, 250);
    private final Color ACCENT = new Color(88, 166, 255, 255);
    private final Color ACCENT_HOVER = new Color(108, 186, 255, 255);
    private final Color ACCENT_GLOW = new Color(88, 166, 255, 25);
    private final Color SELECTION_BG = new Color(88, 166, 255, 20);
    private final Color TEXT_WHITE = new Color(255, 255, 255, 245);
    private final Color TEXT_GRAY = new Color(200, 200, 210, 255);
    private final Color TEXT_DARK_GRAY = new Color(140, 140, 160, 255);
    private final Color SEARCH_BG = new Color(32, 32, 40, 255);
    private final Color HOVER_BG = new Color(88, 166, 255, 12);
    private final Color BORDER_LIGHT = new Color(55, 55, 70, 100);
    private final Color TOOLTIP_BG = new Color(28, 28, 36, 250);
    private final Color MODULE_ON_INDICATOR = new Color(76, 217, 100, 255);
    private final Color MODULE_OFF_INDICATOR = new Color(80, 80, 95, 255);

    // Optimized dimensions for perfect fitting
    private static final int SETTINGS_WIDTH = 350;
    private static final int CATEGORY_WIDTH = 165;
    private static final int MODULE_WIDTH = 385;
    private static final int HEADER_H = 50;
    private static final int ITEM_H = 36;
    private static final int PAD = 16;
    private static final int SPACING = 18;
    private static final int RADIUS = 10;
    private static final int TOTAL_W = SETTINGS_WIDTH + CATEGORY_WIDTH + MODULE_WIDTH + (SPACING * 2);
    private static final int TOTAL_H = 520;

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

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (Krypton.mc.currentScreen != this) return;
        
        if (Krypton.INSTANCE.screen != null) {
            Krypton.INSTANCE.screen.render(context, 0, 0, delta);
        }
        
        if (this.currentColor == null) {
            this.currentColor = new Color(0, 0, 0, 0);
        }
        
        int targetAlpha = skid.krypton.module.modules.client.Krypton.renderBackground.getValue() ? 200 : 0;
        if (this.currentColor.getAlpha() != targetAlpha) {
            this.currentColor = ColorUtil.a(0.05f, targetAlpha, this.currentColor);
        }
        
        if (Krypton.mc.currentScreen instanceof ClickGUI) {
            context.fill(0, 0, Krypton.mc.getWindow().getWidth(), Krypton.mc.getWindow().getHeight(), this.currentColor.getRGB());
        }
        
        RenderUtils.unscaledProjection();
        int scaledMouseX = (int)(mouseX * MinecraftClient.getInstance().getWindow().getScaleFactor());
        int scaledMouseY = (int)(mouseY * MinecraftClient.getInstance().getWindow().getScaleFactor());
        super.render(context, scaledMouseX, scaledMouseY, delta);
        
        renderBackgroundGradient(context);
        renderMainPanels(context, scaledMouseX, scaledMouseY);
        renderHoverDescription(context, scaledMouseX, scaledMouseY);
        
        RenderUtils.scaledProjection();
    }
    
    private void renderBackgroundGradient(DrawContext context) {
        int width = Krypton.mc.getWindow().getWidth();
        int height = Krypton.mc.getWindow().getHeight();
        
        if (skid.krypton.module.modules.client.Krypton.renderBackground.getValue()) {
            for (int i = 0; i < height; i++) {
                float ratio = (float)i / height;
                int alpha = (int)(70 * (1 - ratio * 0.5f));
                context.fill(0, i, width, i + 1, new Color(8, 8, 12, alpha).getRGB());
            }
        }
    }
    
    private void renderMainPanels(DrawContext context, int mouseX, int mouseY) {
        int screenW = Krypton.mc.getWindow().getWidth();
        int screenH = Krypton.mc.getWindow().getHeight();
        int baseX = (screenW - TOTAL_W) / 2;
        int baseY = (screenH - TOTAL_H) / 2;
        
        // Settings panel (right side)
        renderSettingsPanel(context, baseX, baseY, mouseX, mouseY);
        
        // Category panel (middle)
        renderCategoryPanel(context, baseX + SETTINGS_WIDTH + SPACING, baseY, mouseX, mouseY);
        
        // Module panel (left side)
        renderModulePanel(context, baseX + SETTINGS_WIDTH + SPACING + CATEGORY_WIDTH + SPACING, baseY, mouseX, mouseY);
        
        // Header (top)
        renderHeader(context, screenW, baseY);
    }
    
    private void renderHeader(DrawContext context, int screenW, int baseY) {
        String title = "URANIUM";
        String version = "v2.0";
        int titleX = screenW / 2 - TextRenderer.getWidth(title) / 2;
        
        TextRenderer.drawString(title, context, titleX, baseY - 28, toMCColor(TEXT_WHITE));
        TextRenderer.drawString(version, context, titleX + TextRenderer.getWidth(title) + 8, baseY - 26, toMCColor(TEXT_DARK_GRAY));
        
        // Accent underline
        int underlineY = baseY - 10;
        context.fill(titleX - 8, underlineY, titleX + TextRenderer.getWidth(title) + 8, underlineY + 2, toMCColor(ACCENT));
    }
    
    private void renderSettingsPanel(DrawContext context, int x, int y, int mouseX, int mouseY) {
        renderPanel(context, x, y, SETTINGS_WIDTH, TOTAL_H);
        
        // Panel header
        drawPanelHeader(context, x, y, "SETTINGS");
        
        if (selectedModule == null) {
            TextRenderer.drawCenteredString("Select a module", context, 
                x + SETTINGS_WIDTH / 2, y + TOTAL_H / 2, toMCColor(TEXT_DARK_GRAY));
            return;
        }
        
        int yOffset = y + HEADER_H + 12;
        
        // Module name
        TextRenderer.drawString(selectedModule.getName().toString(), context, 
            x + PAD, yOffset, toMCColor(ACCENT));
        yOffset += 28;
        
        for (Object setting : selectedModule.getSettings()) {
            if (!(setting instanceof Setting)) continue;
            
            Setting s = (Setting) setting;
            boolean hovered = isHovered(mouseX, mouseY, x, yOffset, SETTINGS_WIDTH, ITEM_H);
            
            if (hovered && !draggingSlider) {
                renderHoverHighlight(context, x + 4, yOffset, SETTINGS_WIDTH - 8, ITEM_H);
            }
            
            // Setting name with better visibility
            TextRenderer.drawString(s.getName().toString(), context, 
                x + PAD, yOffset + 10, toMCColor(TEXT_GRAY));
            
            renderSettingControl(context, setting, x, yOffset, mouseX, mouseY);
            
            yOffset += ITEM_H + 6;
        }
    }
    
    private void renderSettingControl(DrawContext context, Object setting, int x, int y, int mouseX, int mouseY) {
        int controlX = x + SETTINGS_WIDTH - PAD - 10;
        
        if (setting instanceof BooleanSetting) {
            BooleanSetting bool = (BooleanSetting) setting;
            boolean value = bool.getValue();
            
            // Toggle switch
            int toggleW = 48, toggleH = 24;
            int toggleX = controlX - toggleW;
            int toggleY = y + 6;
            
            // Background
            Color bgColor = value ? ACCENT : new Color(45, 45, 55, 255);
            RenderUtils.renderRoundedQuad(context.getMatrices(), bgColor,
                toggleX, toggleY, toggleX + toggleW, toggleY + toggleH, 12, 12, 12, 12, 30);
            
            // Handle
            int handleX = value ? toggleX + toggleW - 18 : toggleX + 4;
            RenderUtils.renderRoundedQuad(context.getMatrices(), Color.WHITE,
                handleX, toggleY + 3, handleX + 14, toggleY + toggleH - 3, 10, 10, 10, 10, 30);
                
        } else if (setting instanceof NumberSetting) {
            NumberSetting num = (NumberSetting) setting;
            String valueText = String.format("%.2f", num.getValue());
            
            // Value display
            int valueWidth = TextRenderer.getWidth(valueText);
            TextRenderer.drawString(valueText, context, 
                controlX - valueWidth - 8, y + 10, toMCColor(ACCENT));
            
            // Slider
            int sliderW = 120, sliderH = 4;
            int sliderX = controlX - sliderW - valueWidth - 12;
            int sliderY = y + 16;
            
            // Track
            RenderUtils.renderRoundedQuad(context.getMatrices(), new Color(45, 45, 55, 255),
                sliderX, sliderY, sliderX + sliderW, sliderY + sliderH, 2, 2, 2, 2, 20);
            
            // Progress
            double progress = (num.getValue() - num.getMin()) / (num.getMax() - num.getMin());
            int progressW = (int)(sliderW * progress);
            if (progressW > 0) {
                RenderUtils.renderRoundedQuad(context.getMatrices(), ACCENT,
                    sliderX, sliderY, sliderX + progressW, sliderY + sliderH, 2, 2, 2, 2, 20);
            }
            
            // Handle
            int handleX = sliderX + progressW - 3;
            RenderUtils.renderRoundedQuad(context.getMatrices(), Color.WHITE,
                handleX, sliderY - 3, handleX + 6, sliderY + sliderH + 3, 4, 4, 4, 4, 30);
                
        } else if (setting instanceof ModeSetting) {
            ModeSetting<?> mode = (ModeSetting<?>) setting;
            String value = mode.getValue().toString();
            int valueW = TextRenderer.getWidth(value);
            int pillX = controlX - valueW - 20;
            int pillY = y + 4;
            
            RenderUtils.renderRoundedQuad(context.getMatrices(), ACCENT,
                pillX, pillY, pillX + valueW + 20, pillY + 24, 12, 12, 12, 12, 30);
            
            TextRenderer.drawString(value, context, 
                pillX + 10, pillY + 8, toMCColor(Color.WHITE));
        }
    }
    
    private void renderCategoryPanel(DrawContext context, int x, int y, int mouseX, int mouseY) {
        renderPanel(context, x, y, CATEGORY_WIDTH, TOTAL_H);
        drawPanelHeader(context, x, y, "CATEGORIES");
        
        int yOffset = y + HEADER_H + 8;
        
        for (Category category : Category.values()) {
            boolean selected = category == selectedCategory;
            boolean hovered = isHovered(mouseX, mouseY, x, yOffset, CATEGORY_WIDTH, ITEM_H);
            
            if (selected) {
                // Left accent bar
                context.fill(x + 2, yOffset + 8, x + 4, yOffset + ITEM_H - 8, toMCColor(ACCENT));
                renderSelectionHighlight(context, x + 6, yOffset, CATEGORY_WIDTH - 12, ITEM_H);
            } else if (hovered) {
                renderHoverHighlight(context, x + 6, yOffset, CATEGORY_WIDTH - 12, ITEM_H);
            }
            
            Color textColor = selected ? ACCENT : TEXT_GRAY;
            TextRenderer.drawString(category.name.toString(), context,
                x + PAD + 8, yOffset + 10, toMCColor(textColor));
            
            yOffset += ITEM_H + 4;
        }
    }
    
    private void renderModulePanel(DrawContext context, int x, int y, int mouseX, int mouseY) {
        renderPanel(context, x, y, MODULE_WIDTH, TOTAL_H);
        drawPanelHeader(context, x, y, selectedCategory.name.toString().toUpperCase());
        
        // Search bar
        int searchY = y + HEADER_H + 4;
        int searchW = MODULE_WIDTH - PAD * 2;
        int searchH = 32;
        
        RenderUtils.renderRoundedQuad(context.getMatrices(), SEARCH_BG,
            x + PAD, searchY, x + PAD + searchW, searchY + searchH, 16, 16, 16, 16, 30);
        
        // Search icon
        TextRenderer.drawString("🔍", context, x + PAD + 10, searchY + 8, toMCColor(TEXT_DARK_GRAY));
        
        String searchText = searchQuery.isEmpty() ? "Search modules..." : searchQuery;
        Color searchColor = searchQuery.isEmpty() ? TEXT_DARK_GRAY : TEXT_GRAY;
        TextRenderer.drawString(searchText, context, x + PAD + 32, searchY + 9, toMCColor(searchColor));
        
        if (searchFocused && (System.currentTimeMillis() / 500) % 2 == 0) {
            int cursorX = x + PAD + 32 + TextRenderer.getWidth(searchQuery);
            context.fill(cursorX, searchY + 6, cursorX + 1, searchY + 26, toMCColor(ACCENT));
        }
        
        // Modules list
        List<Module> modules = Krypton.INSTANCE.getModuleManager().a(selectedCategory);
        int yOffset = searchY + searchH + 12;
        int maxModules = (TOTAL_H - (yOffset - y) - 12) / (ITEM_H + 4);
        
        hoveredModule = null;
        
        for (int i = 0; i < Math.min(modules.size(), maxModules); i++) {
            Module module = modules.get(i);
            
            if (!searchQuery.isEmpty() && !module.getName().toString().toLowerCase().contains(searchQuery.toLowerCase())) {
                continue;
            }
            
            boolean selected = module == selectedModule;
            boolean hovered = isHovered(mouseX, mouseY, x, yOffset, MODULE_WIDTH, ITEM_H);
            boolean enabled = module.isEnabled();
            
            if (hovered) {
                hoveredModule = module;
                if (System.currentTimeMillis() - hoverStartTime > 300) {
                    hoverDescription = module.getDescription() != null ? module.getDescription() : "";
                    hoverDescriptionX = mouseX + 12;
                    hoverDescriptionY = mouseY + 8;
                }
            }
            
            // Background
            if (selected) {
                renderSelectionHighlight(context, x + 4, yOffset, MODULE_WIDTH - 8, ITEM_H);
            } else if (hovered) {
                renderHoverHighlight(context, x + 4, yOffset, MODULE_WIDTH - 8, ITEM_H);
            }
            
            // Module name with enhanced visibility
            Color nameColor = enabled ? ACCENT : TEXT_WHITE;
            TextRenderer.drawString(module.getName().toString(), context,
                x + PAD, yOffset + 10, toMCColor(nameColor));
            
            // Status indicator
            Color indicatorColor = enabled ? MODULE_ON_INDICATOR : MODULE_OFF_INDICATOR;
            int indicatorX = x + MODULE_WIDTH - PAD - 12;
            RenderUtils.renderRoundedQuad(context.getMatrices(), indicatorColor,
                indicatorX, yOffset + 12, indicatorX + 8, yOffset + 24, 4, 4, 4, 4, 30);
            
            if (enabled) {
                RenderUtils.renderRoundedQuad(context.getMatrices(), ACCENT_GLOW,
                    indicatorX - 2, yOffset + 10, indicatorX + 10, yOffset + 26, 6, 6, 6, 6, 30);
            }
            
            yOffset += ITEM_H + 4;
        }
    }
    
    private void renderHoverDescription(DrawContext context, int mouseX, int mouseY) {
        if (hoveredModule != null && !hoverDescription.isEmpty() && System.currentTimeMillis() - hoverStartTime > 300) {
            int textW = TextRenderer.getWidth(hoverDescription);
            int tooltipW = textW + 24;
            int tooltipH = 28;
            int tooltipX = Math.min(mouseX + 12, Krypton.mc.getWindow().getWidth() - tooltipW - 10);
            int tooltipY = mouseY + 16;
            
            RenderUtils.renderRoundedQuad(context.getMatrices(), TOOLTIP_BG,
                tooltipX, tooltipY, tooltipX + tooltipW, tooltipY + tooltipH, 6, 6, 6, 6, 50);
            
            TextRenderer.drawString(hoverDescription, context,
                tooltipX + 12, tooltipY + 8, toMCColor(TEXT_GRAY));
        } else {
            hoverStartTime = System.currentTimeMillis();
        }
    }
    
    private void renderPanel(DrawContext context, int x, int y, int width, int height) {
        // Shadow
        for (int i = 1; i <= 4; i++) {
            int alpha = 20 - i * 3;
            context.fill(x - i, y - i, x + width + i, y + height + i, new Color(0, 0, 0, alpha).getRGB());
        }
        
        RenderUtils.renderRoundedQuad(context.getMatrices(), PANEL_BG,
            x, y, x + width, y + height, RADIUS, RADIUS, RADIUS, RADIUS, 50);
        
        // Border
        RenderUtils.renderRoundedQuad(context.getMatrices(), BORDER_LIGHT,
            x, y, x + width, y + height, RADIUS, RADIUS, RADIUS, RADIUS, 20);
    }
    
    private void drawPanelHeader(DrawContext context, int x, int y, String title) {
        context.fill(x, y, x + (title.equals("SETTINGS") ? SETTINGS_WIDTH : 
                     title.equals("CATEGORIES") ? CATEGORY_WIDTH : MODULE_WIDTH), 
                     y + HEADER_H, toMCColor(new Color(28, 28, 35, 255)));
        
        TextRenderer.drawString(title, context, x + PAD, y + 18, toMCColor(TEXT_WHITE));
        
        // Bottom border
        context.fill(x, y + HEADER_H - 1, x + (title.equals("SETTINGS") ? SETTINGS_WIDTH : 
                    title.equals("CATEGORIES") ? CATEGORY_WIDTH : MODULE_WIDTH), 
                    y + HEADER_H, toMCColor(ACCENT));
    }
    
    private void renderSelectionHighlight(DrawContext context, int x, int y, int w, int h) {
        RenderUtils.renderRoundedQuad(context.getMatrices(), SELECTION_BG, x, y, x + w, y + h, 6, 6, 6, 6, 30);
    }
    
    private void renderHoverHighlight(DrawContext context, int x, int y, int w, int h) {
        RenderUtils.renderRoundedQuad(context.getMatrices(), HOVER_BG, x, y, x + w, y + h, 6, 6, 6, 6, 30);
    }
    
    private boolean isHovered(int mouseX, int mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }
    
    private boolean isSearchBarHovered(int mouseX, int mouseY) {
        int screenW = Krypton.mc.getWindow().getWidth();
        int screenH = Krypton.mc.getWindow().getHeight();
        int baseX = (screenW - TOTAL_W) / 2;
        int baseY = (screenH - TOTAL_H) / 2;
        int moduleX = baseX + SETTINGS_WIDTH + SPACING + CATEGORY_WIDTH + SPACING;
        int searchX = moduleX + PAD;
        int searchY = baseY + HEADER_H + 4;
        int searchW = MODULE_WIDTH - PAD * 2;
        int searchH = 32;
        
        return isHovered(mouseX, mouseY, searchX, searchY, searchW, searchH);
    }
    
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int scaledX = (int)(mouseX * MinecraftClient.getInstance().getWindow().getScaleFactor());
        int scaledY = (int)(mouseY * MinecraftClient.getInstance().getWindow().getScaleFactor());
        
        int screenW = Krypton.mc.getWindow().getWidth();
        int screenH = Krypton.mc.getWindow().getHeight();
        int baseX = (screenW - TOTAL_W) / 2;
        int baseY = (screenH - TOTAL_H) / 2;
        
        // Search focus
        searchFocused = isSearchBarHovered(scaledX, scaledY);
        
        // Category clicks
        int catX = baseX + SETTINGS_WIDTH + SPACING;
        int catY = baseY + HEADER_H + 8;
        for (Category category : Category.values()) {
            if (isHovered(scaledX, scaledY, catX, catY, CATEGORY_WIDTH, ITEM_H)) {
                selectedCategory = category;
                selectedModule = null;
                return true;
            }
            catY += ITEM_H + 4;
        }
        
        // Module clicks
        int moduleX = baseX + SETTINGS_WIDTH + SPACING + CATEGORY_WIDTH + SPACING;
        int moduleY = baseY + HEADER_H + 4 + 32 + 12;
        List<Module> modules = Krypton.INSTANCE.getModuleManager().a(selectedCategory);
        
        for (Module module : modules) {
            if (!searchQuery.isEmpty() && !module.getName().toString().toLowerCase().contains(searchQuery.toLowerCase())) {
                continue;
            }
            
            if (isHovered(scaledX, scaledY, moduleX, moduleY, MODULE_WIDTH, ITEM_H)) {
                if (button == 0) {
                    module.toggle();
                } else if (button == 1) {
                    selectedModule = module;
                }
                return true;
            }
            moduleY += ITEM_H + 4;
        }
        
        // Settings clicks
        if (selectedModule != null) {
            int settingsX = baseX;
            int settingsY = baseY + HEADER_H + 12 + 28;
            
            for (Object setting : selectedModule.getSettings()) {
                if (!(setting instanceof Setting)) continue;
                
                if (isHovered(scaledX, scaledY, settingsX, settingsY, SETTINGS_WIDTH, ITEM_H)) {
                    handleSettingClick(setting, button, scaledX, scaledY, settingsX, settingsY);
                    return true;
                }
                settingsY += ITEM_H + 6;
            }
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    private void handleSettingClick(Object setting, int button, int mouseX, int mouseY, int panelX, int settingY) {
        if (setting instanceof BooleanSetting) {
            ((BooleanSetting) setting).toggle();
        } else if (setting instanceof ModeSetting) {
            ModeSetting<?> mode = (ModeSetting<?>) setting;
            if (button == 0) mode.cycleUp();
            else if (button == 1) mode.cycleDown();
        } else if (setting instanceof NumberSetting) {
            NumberSetting num = (NumberSetting) setting;
            int sliderY = settingY + 16;
            int controlX = panelX + SETTINGS_WIDTH - PAD - 10;
            String valueText = String.format("%.2f", num.getValue());
            int valueWidth = TextRenderer.getWidth(valueText);
            int sliderW = 120;
            int sliderX = controlX - sliderW - valueWidth - 12;
            
            if (isHovered(mouseX, mouseY, sliderX, sliderY - 5, sliderW, 14)) {
                draggingSlider = true;
                draggingSliderSetting = (Setting) setting;
                updateSliderValue(num, mouseX, sliderX, sliderX + sliderW);
            }
        }
    }
    
    private void updateSliderValue(NumberSetting setting, int mouseX, int startX, int endX) {
        double progress = Math.max(0, Math.min(1, (double)(mouseX - startX) / (endX - startX)));
        double newValue = setting.getMin() + progress * (setting.getMax() - setting.getMin());
        setting.getValue(MathUtil.roundToNearest(newValue, setting.getFormat()));
    }
    
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (draggingSlider && draggingSliderSetting instanceof NumberSetting) {
            int scaledX = (int)(mouseX * MinecraftClient.getInstance().getWindow().getScaleFactor());
            int screenW = Krypton.mc.getWindow().getWidth();
            int screenH = Krypton.mc.getWindow().getHeight();
            int baseX = (screenW - TOTAL_W) / 2;
            
            int controlX = baseX + SETTINGS_WIDTH - PAD - 10;
            String valueText = String.format("%.2f", ((NumberSetting) draggingSliderSetting).getValue());
            int valueWidth = TextRenderer.getWidth(valueText);
            int sliderW = 120;
            int sliderX = controlX - sliderW - valueWidth - 12;
            
            updateSliderValue((NumberSetting) draggingSliderSetting, scaledX, sliderX, sliderX + sliderW);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }
    
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (draggingSlider) {
            draggingSlider = false;
            draggingSliderSetting = null;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }
    
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (searchFocused) {
            if (keyCode == 259 && !searchQuery.isEmpty()) {
                searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
                return true;
            } else if (keyCode == 256) {
                searchFocused = false;
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    
    public boolean charTyped(char chr, int modifiers) {
        if (searchFocused && (Character.isLetterOrDigit(chr) || chr == ' ')) {
            searchQuery += chr;
            return true;
        }
        return super.charTyped(chr, modifiers);
    }
    
    public boolean shouldPause() {
        return false;
    }
    
    public void close() {
        Krypton.INSTANCE.getModuleManager().getModuleByClass(skid.krypton.module.modules.client.Krypton.class).setEnabled(false);
        onGuiClose();
    }
    
    public void onGuiClose() {
        Krypton.mc.setScreenAndRender(Krypton.INSTANCE.screen);
        currentColor = null;
        searchFocused = false;
        draggingSlider = false;
        draggingSliderSetting = null;
        hoveredModule = null;
        hoverDescription = "";
    }
}
