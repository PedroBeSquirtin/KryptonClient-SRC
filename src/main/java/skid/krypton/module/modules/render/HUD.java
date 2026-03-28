package skid.krypton.module.modules.render;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import skid.krypton.Krypton;
import skid.krypton.event.EventListener;
import skid.krypton.event.events.Render2DEvent;
import skid.krypton.module.Category;
import skid.krypton.module.Module;
import skid.krypton.module.setting.BooleanSetting;
import skid.krypton.module.setting.ModeSetting;
import skid.krypton.module.setting.NumberSetting;
import skid.krypton.utils.*;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public final class HUD extends Module {

    private static final SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss");
    
    // Uranium Green Color Scheme
    private static final Color URANIUM_GREEN = new Color(80, 200, 80, 255);
    private static final Color BG_DARK = new Color(15, 18, 15, 200);
    private static final Color TEXT_WHITE = new Color(255, 255, 255, 255);
    private static final Color TEXT_GRAY = new Color(170, 180, 170, 255);
    
    // Potion Colors
    private static final Color SPEED_COLOR = new Color(100, 200, 255, 200);
    private static final Color STRENGTH_COLOR = new Color(255, 100, 100, 200);
    private static final Color JUMP_BOOST_COLOR = new Color(100, 255, 100, 200);
    private static final Color REGENERATION_COLOR = new Color(255, 100, 200, 200);
    private static final Color RESISTANCE_COLOR = new Color(200, 150, 100, 200);
    private static final Color FIRE_RESISTANCE_COLOR = new Color(255, 150, 50, 200);
    private static final Color WATER_BREATHING_COLOR = new Color(50, 150, 255, 200);
    private static final Color INVISIBILITY_COLOR = new Color(150, 150, 150, 200);
    private static final Color NIGHT_VISION_COLOR = new Color(100, 200, 100, 200);
    private static final Color POISON_COLOR = new Color(100, 150, 50, 200);
    private static final Color WITHER_COLOR = new Color(50, 50, 50, 200);
    private static final Color ABSORPTION_COLOR = new Color(255, 200, 100, 200);

    // SETTINGS
    private final BooleanSetting showWatermark = new BooleanSetting("Watermark", true);
    private final BooleanSetting showInfo = new BooleanSetting("Info", true);
    private final BooleanSetting showModules = new BooleanSetting("Modules", true);
    private final BooleanSetting showTime = new BooleanSetting("Time", true);
    private final BooleanSetting showCoordinates = new BooleanSetting("Coordinates", true);
    private final BooleanSetting showRadar = new BooleanSetting("Radar", true);
    private final BooleanSetting showPotions = new BooleanSetting("Potions", true);
    private final NumberSetting radarSize = new NumberSetting("Radar Size", 80, 200, 120, 5);
    private final NumberSetting radarRange = new NumberSetting("Radar Range", 10, 50, 25, 5);
    
    private final ModeSetting<ModuleListSorting> moduleSortingMode =
            new ModeSetting<>("Sort Mode", ModuleListSorting.LENGTH, ModuleListSorting.class);

    public HUD() {
        super("HUD", "Clean HUD", -1, Category.RENDER);

        this.addSettings(
                showWatermark, showInfo, showModules, showTime, showCoordinates,
                showRadar, radarSize, radarRange, showPotions, moduleSortingMode
        );
    }

    @EventListener
    public void onRender2D(Render2DEvent e) {
        if (mc.currentScreen != Krypton.INSTANCE.GUI && mc.player != null) {
            DrawContext ctx = e.context;
            int w = mc.getWindow().getWidth();
            int h = mc.getWindow().getHeight();

            RenderUtils.unscaledProjection();

            // TOP RIGHT - Modules
            if (showModules.getValue()) {
                renderModules(ctx, w, h);
            }
            
            // TOP LEFT - Watermark and Info
            if (showWatermark.getValue() || showInfo.getValue()) {
                renderTopLeft(ctx);
            }
            
            // TOP CENTER - Time
            if (showTime.getValue()) {
                renderTime(ctx, w);
            }
            
            // LEFT SIDE - Radar (below watermark)
            if (showRadar.getValue()) {
                renderRadar(ctx);
            }
            
            // LEFT SIDE - Coordinates (below radar)
            if (showCoordinates.getValue()) {
                renderCoordinates(ctx);
            }
            
            // LEFT SIDE - Potions (below coordinates)
            if (showPotions.getValue()) {
                renderPotions(ctx);
            }

            RenderUtils.scaledProjection();
        }
    }

    // TOP RIGHT - Enabled Modules
    private void renderModules(DrawContext ctx, int screenWidth, int screenHeight) {
        List<Module> list = getSortedModules();
        int padding = 10;
        int lineHeight = 14;
        int y = 12;
        int moduleCount = 0;
        
        for (Module m : list) {
            if (m.isEnabled()) moduleCount++;
        }
        
        if (moduleCount == 0) return;
        
        int bgHeight = moduleCount * lineHeight + padding;
        int maxWidth = 0;
        
        for (Module m : list) {
            if (m.isEnabled()) {
                String name = m.getName().toString();
                int w = TextRenderer.getWidth(name);
                if (w > maxWidth) maxWidth = w;
            }
        }
        
        int bgWidth = maxWidth + padding * 2;
        int x = screenWidth - bgWidth - 12;
        
        // Background
        RenderUtils.renderRoundedQuad(ctx.getMatrices(), BG_DARK, x, y, x + bgWidth, y + bgHeight, 8, 8, 8, 8, 50);
        
        // Module names
        int textY = y + padding / 2 + 2;
        for (Module m : list) {
            if (m.isEnabled()) {
                String name = m.getName().toString();
                TextRenderer.drawString(name, ctx, x + padding, textY, URANIUM_GREEN.getRGB());
                textY += lineHeight;
            }
        }
    }

    // TOP LEFT - Watermark + Info (FPS + Ping)
    private void renderTopLeft(DrawContext ctx) {
        String line1 = "";
        String line2 = "";
        
        if (showWatermark.getValue()) {
            line1 = "URANIUM";
        }
        
        if (showInfo.getValue()) {
            String fps = mc.getCurrentFps() + " FPS";
            String ping = getPingInfo();
            line2 = fps + "  " + ping;
        }
        
        int padding = 10;
        int lineHeight = 14;
        int bgWidth = 0;
        int bgHeight = 0;
        
        if (!line1.isEmpty() && !line2.isEmpty()) {
            bgWidth = Math.max(TextRenderer.getWidth(line1), TextRenderer.getWidth(line2)) + padding * 2;
            bgHeight = lineHeight * 2 + padding;
        } else if (!line1.isEmpty()) {
            bgWidth = TextRenderer.getWidth(line1) + padding * 2;
            bgHeight = lineHeight + padding;
        } else if (!line2.isEmpty()) {
            bgWidth = TextRenderer.getWidth(line2) + padding * 2;
            bgHeight = lineHeight + padding;
        }
        
        int x = 12;
        int y = 12;
        
        // Background
        RenderUtils.renderRoundedQuad(ctx.getMatrices(), BG_DARK, x, y, x + bgWidth, y + bgHeight, 8, 8, 8, 8, 50);
        
        // Text
        int textY = y + padding / 2 + 2;
        if (!line1.isEmpty()) {
            TextRenderer.drawString(line1, ctx, x + padding, textY, URANIUM_GREEN.getRGB());
            textY += lineHeight;
        }
        if (!line2.isEmpty()) {
            TextRenderer.drawString(line2, ctx, x + padding, textY, TEXT_GRAY.getRGB());
        }
    }

    // TOP CENTER - Time
    private void renderTime(DrawContext ctx, int screenWidth) {
        String time = timeFormatter.format(new Date());
        int width = TextRenderer.getWidth(time);
        int padding = 14;
        int bgWidth = width + padding * 2;
        int bgHeight = 22;
        int x = (screenWidth - bgWidth) / 2;
        int y = 12;
        
        // Background
        RenderUtils.renderRoundedQuad(ctx.getMatrices(), BG_DARK, x, y, x + bgWidth, y + bgHeight, 8, 8, 8, 8, 50);
        
        // Text
        TextRenderer.drawString(time, ctx, x + padding, y + 6, TEXT_WHITE.getRGB());
    }

    // RADAR - Left side (shows player dots with names)
    private void renderRadar(DrawContext ctx) {
        int size = (int) radarSize.getValue();
        int range = (int) radarRange.getValue();
        int x = 12;
        int y = getTopLeftHeight() + 12;
        
        // Background
        RenderUtils.renderRoundedQuad(ctx.getMatrices(), BG_DARK, x, y, x + size, y + size, 8, 8, 8, 8, 50);
        
        // Center of radar (player position)
        int centerX = x + size / 2;
        int centerY = y + size / 2;
        
        // Draw center dot (player)
        ctx.fill(centerX - 3, centerY - 3, centerX + 3, centerY + 3, URANIUM_GREEN.getRGB());
        
        // Draw other players
        for (Entity ent : mc.world.getPlayers()) {
            if (ent == mc.player) continue;
            
            double dx = ent.getX() - mc.player.getX();
            double dz = ent.getZ() - mc.player.getZ();
            double distance = Math.sqrt(dx * dx + dz * dz);
            
            if (distance > range) continue;
            
            // Scale to radar size
            int px = (int)(centerX + (dx / range) * (size / 2 - 8));
            int py = (int)(centerY + (dz / range) * (size / 2 - 8));
            
            // Check bounds
            if (px > x + 4 && px < x + size - 4 && py > y + 4 && py < y + size - 4) {
                PlayerEntity player = (PlayerEntity) ent;
                
                // Draw player dot
                RenderUtils.renderCircle(ctx.getMatrices(), new Color(80, 200, 80, 200), px, py, 4, 12);
                
                // Draw player name
                String name = player.getName().getString();
                int nameWidth = TextRenderer.getWidth(name);
                int nameX = px - nameWidth / 2;
                int nameY = py + 8;
                
                // Name background
                if (nameX > x + 2 && nameX + nameWidth < x + size - 2) {
                    RenderUtils.renderRoundedQuad(ctx.getMatrices(), new Color(0, 0, 0, 150), nameX - 2, nameY - 2, nameX + nameWidth + 2, nameY + 10, 4, 4, 4, 4, 30);
                    TextRenderer.drawString(name, ctx, nameX, nameY, TEXT_WHITE.getRGB());
                }
            }
        }
        
        // Draw border
        RenderUtils.renderRoundedOutline(ctx, new Color(80, 200, 80, 100), 
            x, y, x + size, y + size, 8, 8, 8, 8, 2, 5);
        
        // Draw range circles
        int step = range / 4;
        for (int r = step; r <= range; r += step) {
            int radius = (int)((double)r / range * (size / 2 - 4));
            RenderUtils.renderCircle(ctx.getMatrices(), new Color(80, 200, 80, 50), centerX, centerY, radius, 36);
        }
    }

    // BOTTOM LEFT - Coordinates (below radar)
    private void renderCoordinates(DrawContext ctx) {
        String coords = String.format("X: %.0f  Y: %.0f  Z: %.0f",
                mc.player.getX(), mc.player.getY(), mc.player.getZ());
        
        int width = TextRenderer.getWidth(coords);
        int padding = 14;
        int bgWidth = width + padding * 2;
        int bgHeight = 22;
        int x = 12;
        int y = getTopLeftHeight() + (int) radarSize.getValue() + 24;
        
        // Background
        RenderUtils.renderRoundedQuad(ctx.getMatrices(), BG_DARK, x, y, x + bgWidth, y + bgHeight, 8, 8, 8, 8, 50);
        
        // Text
        TextRenderer.drawString(coords, ctx, x + padding, y + 6, TEXT_GRAY.getRGB());
    }

    // POTIONS - Left side (below coordinates) with icons
    private void renderPotions(DrawContext ctx) {
        List<StatusEffectInstance> effects = new ArrayList<>(mc.player.getStatusEffects());
        if (effects.isEmpty()) return;
        
        int padding = 10;
        int lineHeight = 16;
        int iconSize = 16;
        int x = 12;
        int y = getTopLeftHeight() + (int) radarSize.getValue() + 24 + 28;
        
        int potionCount = effects.size();
        int bgHeight = potionCount * lineHeight + padding;
        int maxWidth = 0;
        
        for (StatusEffectInstance effect : effects) {
            String effectName = effect.getEffectType().value().getName().getString();
            String text = effectName + " " + (effect.getDuration() / 20) + "s";
            int w = TextRenderer.getWidth(text);
            if (w > maxWidth) maxWidth = w;
        }
        
        int bgWidth = maxWidth + iconSize + padding * 2;
        
        // Background
        RenderUtils.renderRoundedQuad(ctx.getMatrices(), BG_DARK, x, y, x + bgWidth, y + bgHeight, 8, 8, 8, 8, 50);
        
        // Potion list with icons
        int textY = y + padding / 2 + 3;
        for (StatusEffectInstance effect : effects) {
            String effectName = effect.getEffectType().value().getName().getString();
            int duration = effect.getDuration() / 20;
            String text = effectName + " " + duration + "s";
            Color effectColor = getPotionColor(effect);
            
            // Draw potion icon (colored circle)
            RenderUtils.renderCircle(ctx.getMatrices(), effectColor, x + padding + 8, textY + 5, 6, 12);
            
            // Draw text
            TextRenderer.drawString(text, ctx, x + padding + iconSize + 4, textY, TEXT_GRAY.getRGB());
            textY += lineHeight;
        }
    }

    private Color getPotionColor(StatusEffectInstance effect) {
        String name = effect.getEffectType().value().getName().getString().toLowerCase();
        
        if (name.contains("speed")) return SPEED_COLOR;
        if (name.contains("strength")) return STRENGTH_COLOR;
        if (name.contains("jump boost")) return JUMP_BOOST_COLOR;
        if (name.contains("regeneration")) return REGENERATION_COLOR;
        if (name.contains("resistance")) return RESISTANCE_COLOR;
        if (name.contains("fire resistance")) return FIRE_RESISTANCE_COLOR;
        if (name.contains("water breathing")) return WATER_BREATHING_COLOR;
        if (name.contains("invisibility")) return INVISIBILITY_COLOR;
        if (name.contains("night vision")) return NIGHT_VISION_COLOR;
        if (name.contains("poison")) return POISON_COLOR;
        if (name.contains("wither")) return WITHER_COLOR;
        if (name.contains("absorption")) return ABSORPTION_COLOR;
        
        return new Color(200, 200, 200, 200);
    }

    private int getTopLeftHeight() {
        int height = 0;
        if (showWatermark.getValue() || showInfo.getValue()) {
            if (showWatermark.getValue() && showInfo.getValue()) {
                height = 12 + 10 + 14 + 14 + 10;
            } else if (showWatermark.getValue() || showInfo.getValue()) {
                height = 12 + 10 + 14 + 10;
            }
        }
        return height;
    }

    private List<Module> getSortedModules() {
        List<Module> modules = Krypton.INSTANCE.getModuleManager().b();
        List<Module> sorted = new ArrayList<>(modules);
        
        ModuleListSorting mode = (ModuleListSorting) moduleSortingMode.getValue();
        
        switch (mode) {
            case LENGTH:
                sorted.sort((a, b) -> Integer.compare(b.getName().length(), a.getName().length()));
                break;
            case ALPHABETICAL:
                sorted.sort((a, b) -> a.getName().toString().compareToIgnoreCase(b.getName().toString()));
                break;
            case CATEGORY:
                sorted.sort((a, b) -> a.getCategory().compareTo(b.getCategory()));
                break;
        }
        return sorted;
    }

    private String getPingInfo() {
        if (mc.getNetworkHandler() != null && mc.player != null) {
            PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
            return entry != null ? entry.getLatency() + " ms" : "0 ms";
        }
        return "0 ms";
    }

    public enum ModuleListSorting {
        LENGTH, ALPHABETICAL, CATEGORY
    }
}
