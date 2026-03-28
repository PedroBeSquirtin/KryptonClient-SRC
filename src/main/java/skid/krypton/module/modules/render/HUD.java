package skid.krypton.module.modules.render;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;
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
    private static final Color SPAWNER_COLOR = new Color(200, 80, 200, 200);

    // SETTINGS
    private final BooleanSetting showWatermark = new BooleanSetting("Watermark", true);
    private final BooleanSetting showInfo = new BooleanSetting("Info", true);
    private final BooleanSetting showModules = new BooleanSetting("Modules", true);
    private final BooleanSetting showTime = new BooleanSetting("Time", true);
    private final BooleanSetting showCoordinates = new BooleanSetting("Coordinates", true);
    private final BooleanSetting showRadar = new BooleanSetting("Radar", true);
    private final BooleanSetting showPotions = new BooleanSetting("Potions", true);
    private final BooleanSetting showBlockEntities = new BooleanSetting("Show Block Entities", true);
    private final NumberSetting radarSize = new NumberSetting("Radar Size", 100, 250, 180, 5);
    private final NumberSetting radarRange = new NumberSetting("Radar Range", 10, 80, 40, 5);
    
    private final ModeSetting<ModuleListSorting> moduleSortingMode =
            new ModeSetting<>("Sort Mode", ModuleListSorting.LENGTH, ModuleListSorting.class);

    public HUD() {
        super("HUD", "Clean HUD", -1, Category.RENDER);

        this.addSettings(
                showWatermark, showInfo, showModules, showTime, showCoordinates,
                showRadar, radarSize, radarRange, showPotions, showBlockEntities, moduleSortingMode
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

    // RADAR - Static + that reaches edges, rotating N E S W
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
        
        // Draw thin + crosshair that reaches the edges
        int armLength = size / 2;
        
        // Draw thin horizontal line (left-right) - 1 pixel thick
        for (int i = -armLength; i <= armLength; i++) {
            ctx.fill(centerX + i, centerY, centerX + i + 1, centerY + 1, CROSSHAIR_COLOR.getRGB());
        }
        
        // Draw thin vertical line (up-down) - 1 pixel thick
        for (int i = -armLength; i <= armLength; i++) {
            ctx.fill(centerX, centerY + i, centerX + 1, centerY + i + 1, CROSSHAIR_COLOR.getRGB());
        }
        
        // Draw center dot
        ctx.fill(centerX - 1, centerY - 1, centerX + 1, centerY + 1, URANIUM_GREEN.getRGB());
        
        // Draw rotating cardinal directions (N, E, S, W)
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
            
            double dx = ent.getX() - mc.player.getX();
            double dz = ent.getZ() - mc.player.getZ();
            double distance = Math.sqrt(dx * dx + dz * dz);
            
            if (distance > range) continue;
            
            double angle = Math.atan2(dz, dx);
            double relAngle = angle - rad;
            double rotatedX = Math.cos(relAngle) * distance;
            double rotatedZ = Math.sin(relAngle) * distance;
            
            int px = (int)(centerX + (rotatedX / range) * (size / 2 - 12));
            int py = (int)(centerY + (rotatedZ / range) * (size / 2 - 12));
            
            if (px > x + 6 && px < x + size - 6 && py > y + 6 && py < y + size - 6) {
                PlayerEntity player = (PlayerEntity) ent;
                RenderUtils.renderCircle(ctx.getMatrices(), new Color(80, 200, 80, 200), px, py, 3, 12);
                
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
        
        // Draw block entities - FIXED: removed isLoaded() check
        if (showBlockEntities.getValue()) {
            for (int chunkX = -range; chunkX <= range; chunkX++) {
                for (int chunkZ = -range; chunkZ <= range; chunkZ++) {
                    WorldChunk chunk = mc.world.getChunkManager().getWorldChunk(mc.player.getChunkPos().x + chunkX, mc.player.getChunkPos().z + chunkZ);
                    if (chunk != null) {
                        for (BlockPos pos : chunk.getBlockEntityPositions()) {
                            BlockEntity blockEntity = mc.world.getBlockEntity(pos);
                            if (blockEntity == null) continue;
                            
                            double dx = pos.getX() + 0.5 - mc.player.getX();
                            double dz = pos.getZ() + 0.5 - mc.player.getZ();
                            double distance = Math.sqrt(dx * dx + dz * dz);
                            
                            if (distance > range) continue;
                            
                            double angle = Math.atan2(dz, dx);
                            double relAngle = angle - rad;
                            double rotatedX = Math.cos(relAngle) * distance;
                            double rotatedZ = Math.sin(relAngle) * distance;
                            
                            int px = (int)(centerX + (rotatedX / range) * (size / 2 - 12));
                            int py = (int)(centerY + (rotatedZ / range) * (size / 2 - 12));
                            
                            if (px > x + 4 && px < x + size - 4 && py > y + 4 && py < y + size - 4) {
                                Color blockColor = getBlockEntityColor(blockEntity);
                                if (blockColor != null) {
                                    RenderUtils.renderCircle(ctx.getMatrices(), blockColor, px, py, 2, 10);
                                }
                            }
                        }
                    }
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
    
    private Color getBlockEntityColor(BlockEntity blockEntity) {
        if (blockEntity instanceof MobSpawnerBlockEntity) {
            return SPAWNER_COLOR;
        }
        return null;
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

    // Potions
    private void renderPotions(DrawContext ctx) {
        List<StatusEffectInstance> effects = new ArrayList<>(mc.player.getStatusEffects());
        if (effects.isEmpty()) return;
        
        int padding = 10;
        int lineHeight = 18;
        int x = 12;
        int y = getTopLeftHeight() + (int) radarSize.getValue() + 16 + 32;
        
        int potionCount = effects.size();
        int bgHeight = potionCount * lineHeight + padding;
        int maxWidth = 0;
        
        for (StatusEffectInstance effect : effects) {
            int durationSeconds = effect.getDuration() / 20;
            String text = formatPotionName(effect) + " " + formatDuration(durationSeconds);
            int w = TextRenderer.getWidth(text);
            if (w > maxWidth) maxWidth = w;
        }
        
        int bgWidth = maxWidth + padding * 2;
        
        // Background
        RenderUtils.renderRoundedQuad(ctx.getMatrices(), BG_DARK, x, y, x + bgWidth, y + bgHeight, 8, 8, 8, 8, 50);
        
        // Potion list with colored dots
        int textY = y + padding / 2 + 2;
        for (StatusEffectInstance effect : effects) {
            int durationSeconds = effect.getDuration() / 20;
            String text = formatPotionName(effect) + " " + formatDuration(durationSeconds);
            
            // Draw colored dot
            RenderUtils.renderCircle(ctx.getMatrices(), getPotionColor(effect), x + padding + 4, textY + 5, 4, 12);
            
            // Draw text
            TextRenderer.drawString(text, ctx, x + padding + 12, textY, TEXT_GRAY.getRGB());
            textY += lineHeight;
        }
    }
    
    private String formatDuration(int seconds) {
        if (seconds >= 3600) {
            int hours = seconds / 3600;
            int minutes = (seconds % 3600) / 60;
            return hours + "h " + minutes + "m";
        } else if (seconds >= 60) {
            int minutes = seconds / 60;
            int remainingSeconds = seconds % 60;
            return minutes + "m " + remainingSeconds + "s";
        } else {
            return seconds + "s";
        }
    }

    private String formatPotionName(StatusEffectInstance effect) {
        String name = effect.getEffectType().value().getName().getString();
        int amplifier = effect.getAmplifier();
        if (amplifier > 0) {
            String roman = getRomanNumeral(amplifier + 1);
            name = name + " " + roman;
        }
        return name;
    }

    private String getRomanNumeral(int num) {
        switch (num) {
            case 1: return "I";
            case 2: return "II";
            case 3: return "III";
            case 4: return "IV";
            case 5: return "V";
            default: return String.valueOf(num);
        }
    }

    private Color getPotionColor(StatusEffectInstance effect) {
        String name = effect.getEffectType().value().getName().getString().toLowerCase();
        if (name.contains("speed")) return new Color(100, 200, 255, 200);
        if (name.contains("slowness")) return new Color(100, 100, 150, 200);
        if (name.contains("haste")) return new Color(200, 200, 100, 200);
        if (name.contains("strength")) return new Color(255, 100, 100, 200);
        if (name.contains("jump boost")) return new Color(100, 255, 100, 200);
        if (name.contains("regeneration")) return new Color(255, 100, 200, 200);
        if (name.contains("resistance")) return new Color(200, 150, 100, 200);
        if (name.contains("fire resistance")) return new Color(255, 150, 50, 200);
        if (name.contains("water breathing")) return new Color(50, 150, 255, 200);
        if (name.contains("invisibility")) return new Color(150, 150, 150, 200);
        if (name.contains("night vision")) return new Color(100, 200, 100, 200);
        if (name.contains("poison")) return new Color(100, 150, 50, 200);
        if (name.contains("wither")) return new Color(50, 50, 50, 200);
        if (name.contains("absorption")) return new Color(255, 200, 100, 200);
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
            if (entry != null) {
                int ping = entry.getLatency();
                return ping + " ms";
            }
        }
        return "0 ms";
    }

    public enum ModuleListSorting {
        LENGTH, ALPHABETICAL, CATEGORY
    }
}
