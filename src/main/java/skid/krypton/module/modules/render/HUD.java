package skid.krypton.module.modules.render;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
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
    private static final Color CARDINAL_COLOR = new Color(80, 200, 80, 200);
    private static final Color CROSSHAIR_COLOR = new Color(80, 200, 80, 180);

    // SETTINGS
    private final BooleanSetting showWatermark = new BooleanSetting("Watermark", true);
    private final BooleanSetting showInfo = new BooleanSetting("Info", true);
    private final BooleanSetting showModules = new BooleanSetting("Modules", true);
    private final BooleanSetting showTime = new BooleanSetting("Time", true);
    private final BooleanSetting showCoordinates = new BooleanSetting("Coordinates", true);
    private final BooleanSetting showRadar = new BooleanSetting("Radar", true);
    private final BooleanSetting showPotions = new BooleanSetting("Potions", true);
    private final NumberSetting radarSize = new NumberSetting("Radar Size", 100, 250, 180, 5);
    private final NumberSetting radarRange = new NumberSetting("Radar Range", 10, 80, 40, 5);
    private final NumberSetting potionSize = new NumberSetting("Potion Size", 50, 150, 80, 5);
    
    private final ModeSetting<ModuleListSorting> moduleSortingMode =
            new ModeSetting<>("Sort Mode", ModuleListSorting.LENGTH, ModuleListSorting.class);

    public HUD() {
        super("HUD", "Clean HUD", -1, Category.RENDER);

        this.addSettings(
                showWatermark, showInfo, showModules, showTime, showCoordinates,
                showRadar, radarSize, radarRange, showPotions, potionSize, moduleSortingMode
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
            
            // LEFT SIDE - Radar
            if (showRadar.getValue()) {
                renderRadar(ctx);
            }
            
            // LEFT SIDE - Coordinates (below radar)
            if (showCoordinates.getValue()) {
                renderCoordinates(ctx);
            }
            
            // LEFT SIDE - Potions (below coordinates, using Minecraft's built-in potion HUD moved)
            if (showPotions.getValue()) {
                renderVanillaPotions(ctx);
            }

            RenderUtils.scaledProjection();
        }
    }

    // TOP RIGHT - Enabled Modules (fixed spacing)
    private void renderModules(DrawContext ctx, int screenWidth, int screenHeight) {
        List<Module> list = getSortedModules();
        int padding = 12;
        int lineHeight = 16;
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
        int textY = y + padding / 2 + 3;
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

    // RADAR - With rotating cardinal directions and + crosshair
    private void renderRadar(DrawContext ctx) {
        int size = (int) radarSize.getValue();
        int range = (int) radarRange.getValue();
        int x = 12;
        int y = getTopLeftHeight() + 12;
        
        // Background
        RenderUtils.renderRoundedQuad(ctx.getMatrices(), BG_DARK, x, y, x + size, y + size, 8, 8, 8, 8, 50);
        
        // Center of radar
        int centerX = x + size / 2;
        int centerY = y + size / 2;
        
        // Get player's facing direction
        float yaw = mc.player.getYaw();
        double rad = Math.toRadians(yaw);
        
        // Draw + crosshair (rotates with player direction)
        int armLength = size / 3;
        
        // Calculate rotated arm endpoints
        int rightX = centerX + (int)(Math.sin(rad) * armLength);
        int rightY = centerY + (int)(Math.cos(rad) * armLength);
        int leftX = centerX - (int)(Math.sin(rad) * armLength);
        int leftY = centerY - (int)(Math.cos(rad) * armLength);
        int upX = centerX + (int)(Math.cos(rad) * armLength);
        int upY = centerY - (int)(Math.sin(rad) * armLength);
        int downX = centerX - (int)(Math.cos(rad) * armLength);
        int downY = centerY + (int)(Math.sin(rad) * armLength);
        
        // Draw the + crosshair
        for (int i = 0; i <= 20; i++) {
            float t = i / 20f;
            // Horizontal arm
            int hx = (int)(centerX + (rightX - centerX) * t);
            int hy = (int)(centerY + (rightY - centerY) * t);
            ctx.fill(hx, hy, hx + 2, hy + 2, CROSSHAIR_COLOR.getRGB());
            
            int hx2 = (int)(centerX + (leftX - centerX) * t);
            int hy2 = (int)(centerY + (leftY - centerY) * t);
            ctx.fill(hx2, hy2, hx2 + 2, hy2 + 2, CROSSHAIR_COLOR.getRGB());
            
            // Vertical arm
            int vx = (int)(centerX + (upX - centerX) * t);
            int vy = (int)(centerY + (upY - centerY) * t);
            ctx.fill(vx, vy, vx + 2, vy + 2, CROSSHAIR_COLOR.getRGB());
            
            int vx2 = (int)(centerX + (downX - centerX) * t);
            int vy2 = (int)(centerY + (downY - centerY) * t);
            ctx.fill(vx2, vy2, vx2 + 2, vy2 + 2, CROSSHAIR_COLOR.getRGB());
        }
        
        // Draw center dot
        ctx.fill(centerX - 2, centerY - 2, centerX + 2, centerY + 2, URANIUM_GREEN.getRGB());
        
        // Draw rotating cardinal directions
        int compassDistance = size / 2 - 15;
        int northX = centerX + (int)(Math.cos(rad) * compassDistance);
        int northY = centerY - (int)(Math.sin(rad) * compassDistance);
        int southX = centerX - (int)(Math.cos(rad) * compassDistance);
        int southY = centerY + (int)(Math.sin(rad) * compassDistance);
        int eastX = centerX + (int)(Math.sin(rad) * compassDistance);
        int eastY = centerY + (int)(Math.cos(rad) * compassDistance);
        int westX = centerX - (int)(Math.sin(rad) * compassDistance);
        int westY = centerY - (int)(Math.cos(rad) * compassDistance);
        
        TextRenderer.drawString("N", ctx, northX - 4, northY - 5, CARDINAL_COLOR.getRGB());
        TextRenderer.drawString("S", ctx, southX - 4, southY - 5, CARDINAL_COLOR.getRGB());
        TextRenderer.drawString("E", ctx, eastX - 4, eastY - 5, CARDINAL_COLOR.getRGB());
        TextRenderer.drawString("W", ctx, westX - 4, westY - 5, CARDINAL_COLOR.getRGB());
        
        // Draw other players
        for (Entity ent : mc.world.getPlayers()) {
            if (ent == mc.player) continue;
            
            // Calculate relative position in world coordinates
            double dx = ent.getX() - mc.player.getX();
            double dz = ent.getZ() - mc.player.getZ();
            double distance = Math.sqrt(dx * dx + dz * dz);
            
            if (distance > range) continue;
            
            // Rotate coordinates based on player facing
            double angle = Math.atan2(dz, dx);
            double relAngle = angle - rad;
            double rotatedX = Math.cos(relAngle) * distance;
            double rotatedZ = Math.sin(relAngle) * distance;
            
            // Scale to radar size
            int px = (int)(centerX + (rotatedX / range) * (size / 2 - 12));
            int py = (int)(centerY + (rotatedZ / range) * (size / 2 - 12));
            
            // Check bounds
            if (px > x + 6 && px < x + size - 6 && py > y + 6 && py < y + size - 6) {
                PlayerEntity player = (PlayerEntity) ent;
                
                // Draw player dot
                RenderUtils.renderCircle(ctx.getMatrices(), new Color(80, 200, 80, 200), px, py, 3, 12);
                
                // Draw player name
                String name = player.getName().getString();
                int nameWidth = TextRenderer.getWidth(name);
                int nameX = px - nameWidth / 2;
                int nameY = py + 8;
                
                if (nameX > x + 2 && nameX + nameWidth < x + size - 2) {
                    RenderUtils.renderRoundedQuad(ctx.getMatrices(), new Color(0, 0, 0, 120), nameX - 2, nameY - 2, nameX + nameWidth + 2, nameY + 10, 3, 3, 3, 3, 30);
                    TextRenderer.drawString(name, ctx, nameX, nameY, TEXT_WHITE.getRGB());
                }
            }
        }
        
        // Draw border
        RenderUtils.renderRoundedOutline(ctx, new Color(80, 200, 80, 150), 
            x, y, x + size, y + size, 8, 8, 8, 8, 2, 5);
        
        // Draw range circles
        int step = range / 4;
        for (int r = step; r <= range; r += step) {
            int radius = (int)((double)r / range * (size / 2 - 6));
            RenderUtils.renderCircle(ctx.getMatrices(), new Color(80, 200, 80, 40), centerX, centerY, radius, 36);
        }
    }

    // Coordinates
    private void renderCoordinates(DrawContext ctx) {
        String coords = String.format("X: %.0f  Y: %.0f  Z: %.0f",
                mc.player.getX(), mc.player.getY(), mc.player.getZ());
        
        int width = TextRenderer.getWidth(coords);
        int padding = 14;
        int bgWidth = width + padding * 2;
        int bgHeight = 22;
        int x = 12;
        int y = getTopLeftHeight() + (int) radarSize.getValue() + 16;
        
        // Background
        RenderUtils.renderRoundedQuad(ctx.getMatrices(), BG_DARK, x, y, x + bgWidth, y + bgHeight, 8, 8, 8, 8, 50);
        
        // Text
        TextRenderer.drawString(coords, ctx, x + padding, y + 6, TEXT_GRAY.getRGB());
    }

    // Vanilla Potions - Moved to custom position
    private void renderVanillaPotions(DrawContext ctx) {
        List<StatusEffectInstance> effects = new ArrayList<>(mc.player.getStatusEffects());
        if (effects.isEmpty()) return;
        
        int size = (int) potionSize.getValue();
        int x = 12;
        int y = getTopLeftHeight() + (int) radarSize.getValue() + 16 + 32;
        
        // Draw each potion using Minecraft's built-in rendering but at custom position
        int potionY = y;
        for (StatusEffectInstance effect : effects) {
            // Draw potion background
            ctx.getMatrices().push();
            ctx.getMatrices().translate(x, potionY, 0);
            ctx.getMatrices().scale(0.8f, 0.8f, 1);
            
            // Draw the vanilla potion HUD element
            int duration = effect.getDuration() / 20;
            String name = effect.getEffectType().value().getName().getString();
            String text = name + " " + duration;
            
            // Simple background for each potion
            int textWidth = TextRenderer.getWidth(text);
            RenderUtils.renderRoundedQuad(ctx.getMatrices(), BG_DARK, 0, 0, textWidth + 16, 24, 6, 6, 6, 6, 50);
            
            // Draw potion effect icon (colored circle)
            RenderUtils.renderCircle(ctx.getMatrices(), getPotionColor(effect), 8, 12, 8, 16);
            
            // Draw text
            TextRenderer.drawString(text, ctx, 20, 6, TEXT_GRAY.getRGB());
            
            ctx.getMatrices().pop();
            potionY += 28;
        }
    }

    private Color getPotionColor(StatusEffectInstance effect) {
        String name = effect.getEffectType().value().getName().getString().toLowerCase();
        if (name.contains("speed")) return new Color(100, 200, 255, 200);
        if (name.contains("strength")) return new Color(255, 100, 100, 200);
        if (name.contains("jump boost")) return new Color(100, 255, 100, 200);
        if (name.contains("regeneration")) return new Color(255, 100, 200, 200);
        if (name.contains("resistance")) return new Color(200, 150, 100, 200);
        if (name.contains("fire resistance")) return new Color(255, 150, 50, 200);
        if (name.contains("water breathing")) return new Color(50, 150, 255, 200);
        if (name.contains("invisibility")) return new Color(150, 150, 150, 200);
        if (name.contains("night vision")) return new Color(100, 200, 100, 200);
        if (name.contains("poison")) return new Color(100, 150, 50, 200);
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
