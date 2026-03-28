package skid.krypton.module.modules.render;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
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
    private static final Color RADAR_PLAYER = new Color(80, 200, 80, 200);
    private static final Color RADAR_ENTITY = new Color(200, 80, 80, 180);

    // SETTINGS
    private final BooleanSetting showWatermark = new BooleanSetting("Watermark", true);
    private final BooleanSetting showInfo = new BooleanSetting("Info", true);
    private final BooleanSetting showModules = new BooleanSetting("Modules", true);
    private final BooleanSetting showTime = new BooleanSetting("Time", true);
    private final BooleanSetting showCoordinates = new BooleanSetting("Coordinates", true);
    private final BooleanSetting showRadar = new BooleanSetting("Radar", true);
    private final BooleanSetting showPotions = new BooleanSetting("Potions", true);
    private final NumberSetting radarSize = new NumberSetting("Radar Size", 50, 150, 90, 5);
    
    private final ModeSetting<ModuleListSorting> moduleSortingMode =
            new ModeSetting<>("Sort Mode", ModuleListSorting.LENGTH, ModuleListSorting.class);

    public HUD() {
        super("HUD", "Clean HUD", -1, Category.RENDER);

        this.addSettings(
                showWatermark, showInfo, showModules, showTime, showCoordinates,
                showRadar, radarSize, showPotions, moduleSortingMode
        );
    }

    @EventListener
    public void onRender2D(Render2DEvent e) {
        if (mc.currentScreen != Krypton.INSTANCE.GUI && mc.player != null) {
            DrawContext ctx = e.context;
            int w = mc.getWindow().getWidth();
            int h = mc.getWindow().getHeight();

            RenderUtils.unscaledProjection();

            // Top Left - Watermark and Info
            if (showWatermark.getValue() || showInfo.getValue()) {
                renderTopLeft(ctx);
            }
            
            // Top Center - Time
            if (showTime.getValue()) {
                renderTime(ctx, w);
            }
            
            // Bottom Left - Coordinates
            if (showCoordinates.getValue()) {
                renderCoordinates(ctx, h);
            }
            
            // Bottom Right - Modules
            if (showModules.getValue()) {
                renderModules(ctx, w, h);
            }
            
            // Right side above modules - Potions
            if (showPotions.getValue()) {
                renderPotions(ctx, w, h);
            }
            
            // Bottom Right above modules - Radar
            if (showRadar.getValue()) {
                renderRadar(ctx, w, h);
            }

            RenderUtils.scaledProjection();
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

    // BOTTOM LEFT - Coordinates
    private void renderCoordinates(DrawContext ctx, int screenHeight) {
        String coords = String.format("X: %.0f  Y: %.0f  Z: %.0f",
                mc.player.getX(), mc.player.getY(), mc.player.getZ());
        
        int width = TextRenderer.getWidth(coords);
        int padding = 14;
        int bgWidth = width + padding * 2;
        int bgHeight = 22;
        int x = 12;
        int y = screenHeight - bgHeight - 12;
        
        // Background
        RenderUtils.renderRoundedQuad(ctx.getMatrices(), BG_DARK, x, y, x + bgWidth, y + bgHeight, 8, 8, 8, 8, 50);
        
        // Text
        TextRenderer.drawString(coords, ctx, x + padding, y + 6, TEXT_GRAY.getRGB());
    }

    // RADAR - Bottom right above modules
    private void renderRadar(DrawContext ctx, int screenWidth, int screenHeight) {
        int size = (int) radarSize.getValue();
        int padding = 10;
        
        // Calculate position above modules
        int modulesHeight = 0;
        if (showModules.getValue()) {
            List<Module> modules = getSortedModules();
            int moduleCount = 0;
            for (Module m : modules) {
                if (m.isEnabled()) moduleCount++;
            }
            modulesHeight = moduleCount * 14 + 12;
        }
        
        int potionsHeight = 0;
        if (showPotions.getValue()) {
            potionsHeight = mc.player.getStatusEffects().size() * 14 + 12;
        }
        
        int x = screenWidth - size - 12;
        int y = screenHeight - size - modulesHeight - potionsHeight - 20;
        
        // Background
        RenderUtils.renderRoundedQuad(ctx.getMatrices(), BG_DARK, x, y, x + size, y + size, 8, 8, 8, 8, 50);
        
        // Center of radar (player position)
        int centerX = x + size / 2;
        int centerY = y + size / 2;
        
        // Draw center dot (player)
        ctx.fill(centerX - 2, centerY - 2, centerX + 2, centerY + 2, URANIUM_GREEN.getRGB());
        
        // Draw other entities
        double range = 20.0; // Radar range in blocks
        for (Entity ent : mc.world.getEntities()) {
            if (ent == mc.player) continue;
            
            double dx = ent.getX() - mc.player.getX();
            double dz = ent.getZ() - mc.player.getZ();
            double distance = Math.sqrt(dx * dx + dz * dz);
            
            if (distance > range) continue;
            
            // Scale to radar size
            int px = (int)(centerX + (dx / range) * (size / 2 - 4));
            int py = (int)(centerY + (dz / range) * (size / 2 - 4));
            
            // Check bounds
            if (px > x + 2 && px < x + size - 2 && py > y + 2 && py < y + size - 2) {
                // Different color for players vs mobs
                Color entColor = ent.isPlayer() ? RADAR_PLAYER : RADAR_ENTITY;
                ctx.fill(px - 1, py - 1, px + 1, py + 1, entColor.getRGB());
            }
        }
        
        // Draw border
        RenderUtils.renderOutline(ctx.getMatrices(), x, y, x + size, y + size, new Color(80, 200, 80, 100));
    }

    // BOTTOM RIGHT - Enabled Modules
    private void renderModules(DrawContext ctx, int screenWidth, int screenHeight) {
        List<Module> list = getSortedModules();
        int padding = 10;
        int lineHeight = 14;
        int y = screenHeight - 12;
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
        int startY = y - bgHeight;
        
        RenderUtils.renderRoundedQuad(ctx.getMatrices(), BG_DARK, x, startY, x + bgWidth, y, 8, 8, 8, 8, 50);
        
        int textY = startY + padding / 2 + 2;
        for (Module m : list) {
            if (m.isEnabled()) {
                String name = m.getName().toString();
                TextRenderer.drawString(name, ctx, x + padding, textY, URANIUM_GREEN.getRGB());
                textY += lineHeight;
            }
        }
    }

    // POTIONS - Right side above modules
    private void renderPotions(DrawContext ctx, int screenWidth, int screenHeight) {
        List<StatusEffectInstance> effects = new ArrayList<>(mc.player.getStatusEffects());
        if (effects.isEmpty()) return;
        
        int padding = 10;
        int lineHeight = 14;
        int y = screenHeight - 12;
        
        int modulesHeight = 0;
        if (showModules.getValue()) {
            List<Module> modules = getSortedModules();
            int moduleCount = 0;
            for (Module m : modules) {
                if (m.isEnabled()) moduleCount++;
            }
            modulesHeight = moduleCount * lineHeight + padding;
        }
        
        int potionCount = effects.size();
        int bgHeight = potionCount * lineHeight + padding;
        int maxWidth = 0;
        
        for (StatusEffectInstance effect : effects) {
            String effectName = effect.getEffectType().value().getName().getString();
            String text = effectName + " " + (effect.getDuration() / 20);
            int w = TextRenderer.getWidth(text);
            if (w > maxWidth) maxWidth = w;
        }
        
        int bgWidth = maxWidth + padding * 2;
        int x = screenWidth - bgWidth - 12;
        int startY = y - modulesHeight - bgHeight - 5;
        
        RenderUtils.renderRoundedQuad(ctx.getMatrices(), BG_DARK, x, startY, x + bgWidth, startY + bgHeight, 8, 8, 8, 8, 50);
        
        int textY = startY + padding / 2 + 2;
        for (StatusEffectInstance effect : effects) {
            String effectName = effect.getEffectType().value().getName().getString();
            int duration = effect.getDuration() / 20;
            String text = effectName + " " + duration;
            TextRenderer.drawString(text, ctx, x + padding, textY, TEXT_GRAY.getRGB());
            textY += lineHeight;
        }
        
        // Optional: Add "POTIONS" header
        TextRenderer.drawString("POTIONS", ctx, x + padding, startY + padding / 2 - 10, URANIUM_GREEN.getRGB());
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
