package skid.krypton.module.modules.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.util.Formatting;
import skid.krypton.Krypton;
import skid.krypton.event.EventListener;
import skid.krypton.event.events.PacketEvent;
import skid.krypton.event.events.Render2DEvent;
import skid.krypton.event.events.TickEvent;
import skid.krypton.module.Category;
import skid.krypton.module.Module;
import skid.krypton.module.setting.BooleanSetting;
import skid.krypton.module.setting.ModeSetting;
import skid.krypton.module.setting.NumberSetting;
import skid.krypton.utils.EncryptedString;
import skid.krypton.utils.Fonts;
import skid.krypton.utils.RenderUtils;
import skid.krypton.utils.TextRenderer;

import java.awt.*;
import java.util.Random;

public final class Uranium extends Module {
    
    // Fixed Uranium Green Color - cannot be changed
    private static final Color URANIUM_GREEN = new Color(80, 200, 80, 255);
    private static final Color URANIUM_GREEN_DARK = new Color(50, 150, 50, 255);
    private static final Color URANIUM_GREEN_GLOW = new Color(80, 200, 80, 100);
    
    // Settings that don't affect colors
    private final BooleanSetting watermark = new BooleanSetting(EncryptedString.of("Watermark"), true);
    private final BooleanSetting arrayList = new BooleanSetting(EncryptedString.of("ArrayList"), true);
    private final BooleanSetting coordinates = new BooleanSetting(EncryptedString.of("Coordinates"), true);
    private final BooleanSetting fps = new BooleanSetting(EncryptedString.of("FPS"), true);
    private final BooleanSetting ping = new BooleanSetting(EncryptedString.of("Ping"), true);
    private final BooleanSetting serverIP = new BooleanSetting(EncryptedString.of("Server IP"), true);
    private final BooleanSetting welcomeMessage = new BooleanSetting(EncryptedString.of("Welcome Message"), true);
    private final ModeSetting<String> arrayListMode = new ModeSetting<>(EncryptedString.of("ArrayList Mode"), "Right", new String[]{"Right", "Left"});
    private final NumberSetting arrayListSpacing = new NumberSetting(EncryptedString.of("ArrayList Spacing"), 1, 5, 2, 1);
    
    // Removed all color settings
    
    private final Random random = new Random();
    private String welcomeText = "";
    private long welcomeTime = 0;
    
    public Uranium() {
        super(EncryptedString.of("Uranium"), EncryptedString.of("Client settings and visual enhancements"), -1, Category.CLIENT);
        
        this.addSettings(
            this.watermark, this.arrayList, this.coordinates, this.fps, this.ping, 
            this.serverIP, this.welcomeMessage, this.arrayListMode, this.arrayListSpacing
        );
        
        this.setEnabled(true);
    }
    
    @Override
    public void onEnable() {
        super.onEnable();
        
        if (welcomeMessage.getValue() && welcomeText.isEmpty()) {
            String[] greetings = {
                "Uranium Client loaded!", 
                "Welcome to Uranium!", 
                "Uranium Client - Premium Experience",
                "Uranium is ready!",
                "Stay bright with Uranium!"
            };
            welcomeText = greetings[random.nextInt(greetings.length)];
            welcomeTime = System.currentTimeMillis();
        }
    }
    
    @EventListener
    public void onRender2D(Render2DEvent event) {
        DrawContext context = event.context;
        int width = mc.getWindow().getWidth();
        int height = mc.getWindow().getHeight();
        
        if (watermark.getValue()) {
            renderWatermark(context, width, height);
        }
        
        if (arrayList.getValue()) {
            renderArrayList(context, width, height);
        }
        
        if (welcomeMessage.getValue() && !welcomeText.isEmpty() && System.currentTimeMillis() - welcomeTime < 5000) {
            renderWelcomeMessage(context, width, height);
        }
    }
    
    private void renderWatermark(DrawContext context, int width, int height) {
        String watermarkText = "URANIUM";
        int textWidth = TextRenderer.getWidth(watermarkText);
        int x = 5;
        int y = 5;
        int padding = 8;
        int bgWidth = textWidth + padding * 2;
        int bgHeight = 20;
        
        // Background with uranium green accent
        RenderUtils.renderRoundedQuad(context.getMatrices(), new Color(15, 18, 15, 200), x, y, x + bgWidth, y + bgHeight, 6, 6, 6, 6, 50);
        
        // Draw text with uranium green
        TextRenderer.drawString(watermarkText, context, x + padding, y + 6, URANIUM_GREEN.getRGB());
        
        // Small accent line
        context.fill(x + 2, y + 2, x + 4, y + bgHeight - 2, URANIUM_GREEN.getRGB());
    }
    
    private void renderArrayList(DrawContext context, int width, int height) {
        java.util.List<Module> modules = Krypton.INSTANCE.getModuleManager().b();
        int y = 5;
        int spacing = arrayListSpacing.getIntValue() + 10;
        int x = arrayListMode.getValue().equals("Right") ? width - 10 : 10;
        int alignment = arrayListMode.getValue().equals("Right") ? -1 : 1;
        
        for (Module module : modules) {
            if (module.isEnabled() && module != this) {
                String name = module.getName();
                int textWidth = TextRenderer.getWidth(name);
                int bgX = arrayListMode.getValue().equals("Right") ? x - textWidth - 8 : x;
                int bgWidth = textWidth + 16;
                
                // Background
                RenderUtils.renderRoundedQuad(context.getMatrices(), new Color(15, 18, 15, 180), 
                    bgX, y, bgX + bgWidth, y + 18, 4, 4, 4, 4, 50);
                
                // Left accent bar
                int accentX = arrayListMode.getValue().equals("Right") ? bgX : bgX + bgWidth - 3;
                context.fill(accentX, y + 2, accentX + 2, y + 16, URANIUM_GREEN.getRGB());
                
                // Module name in uranium green
                int textX = arrayListMode.getValue().equals("Right") ? bgX + 8 : bgX + 8;
                TextRenderer.drawString(name, context, textX, y + 5, URANIUM_GREEN.getRGB());
                
                y += spacing;
            }
        }
    }
    
    private void renderWelcomeMessage(DrawContext context, int width, int height) {
        int textWidth = TextRenderer.getWidth(welcomeText);
        int x = (width - textWidth) / 2 - 10;
        int y = height / 3;
        int padding = 12;
        int bgWidth = textWidth + padding * 2;
        int bgHeight = 28;
        
        // Animated fade out
        float alpha = Math.min(1.0f, (5000 - (System.currentTimeMillis() - welcomeTime)) / 2000f);
        if (alpha < 0) alpha = 0;
        
        Color bgColor = new Color(15, 18, 15, (int)(200 * alpha));
        RenderUtils.renderRoundedQuad(context.getMatrices(), bgColor, x, y, x + bgWidth, y + bgHeight, 8, 8, 8, 8, 50);
        
        Color textColor = new Color(URANIUM_GREEN.getRed(), URANIUM_GREEN.getGreen(), URANIUM_GREEN.getBlue(), (int)(255 * alpha));
        TextRenderer.drawString(welcomeText, context, x + padding, y + 10, textColor.getRGB());
    }
    
    @EventListener
    public void onTick(TickEvent event) {
        if (welcomeMessage.getValue() && welcomeText.isEmpty() && !welcomeMessage.getValue()) {
            String[] greetings = {
                "Uranium Client loaded!", 
                "Welcome to Uranium!", 
                "Uranium Client - Premium Experience",
                "Uranium is ready!",
                "Stay bright with Uranium!"
            };
            welcomeText = greetings[random.nextInt(greetings.length)];
            welcomeTime = System.currentTimeMillis();
        }
    }
    
    @EventListener
    public void onPacket(PacketEvent event) {
        // No color-related code here
    }
    
    // Override any color getters to always return uranium green
    public static Color getUraniumGreen() {
        return URANIUM_GREEN;
    }
    
    public static Color getUraniumGreenDark() {
        return URANIUM_GREEN_DARK;
    }
    
    public static Color getUraniumGreenGlow() {
        return URANIUM_GREEN_GLOW;
    }
}
