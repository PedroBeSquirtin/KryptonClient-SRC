package skid.krypton.module.modules.render;

import net.minecraft.block.entity.*;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.Entity;
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
import java.util.concurrent.ConcurrentHashMap;

public final class HUD extends Module {

    private static final SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss");
    
    // Uranium Green Color Scheme
    private static final Color URANIUM_GREEN = new Color(80, 200, 80, 255);
    private static final Color BG_DARK = new Color(15, 18, 15, 200);
    private static final Color TEXT_WHITE = new Color(255, 255, 255, 255);
    private static final Color TEXT_GRAY = new Color(170, 180, 170, 255);
    private static final Color CARDINAL_COLOR = new Color(80, 200, 80, 200);
    private static final Color CROSSHAIR_COLOR = new Color(80, 200, 80, 180);
    
    // Block Entity Colors
    private static final Color SPAWNER_COLOR = new Color(200, 80, 200, 200);
    private static final Color CHEST_COLOR = new Color(200, 150, 80, 200);
    private static final Color SHULKER_COLOR = new Color(150, 80, 200, 200);
    private static final Color BEACON_COLOR = new Color(80, 200, 200, 200);
    private static final Color BARREL_COLOR = new Color(200, 120, 80, 200);
    private static final Color PISTON_COLOR = new Color(100, 200, 100, 200);

    // SETTINGS
    private final BooleanSetting showWatermark = new BooleanSetting("Watermark", true);
    private final BooleanSetting showInfo = new BooleanSetting("Info", true);
    private final BooleanSetting showModules = new BooleanSetting("Modules", true);
    private final BooleanSetting showTime = new BooleanSetting("Time", true);
    private final BooleanSetting showCoordinates = new BooleanSetting("Coordinates", true);
    private final BooleanSetting showRadar = new BooleanSetting("Radar", true);
    private final BooleanSetting showBlockEntities = new BooleanSetting("Show Block Entities", true);
    private final NumberSetting radarSize = new NumberSetting("Radar Size", 100, 250, 180, 5);
    private final NumberSetting radarRange = new NumberSetting("Radar Range", 10, 80, 40, 5);
    
    private final ModeSetting<ModuleListSorting> moduleSortingMode =
            new ModeSetting<>("Sort Mode", ModuleListSorting.LENGTH, ModuleListSorting.class);

    // Block entity counter for display
    private final Map<String, Integer> blockEntityCounts = new ConcurrentHashMap<>();

    public HUD() {
        super("HUD", "Clean HUD", -1, Category.RENDER);

        this.addSettings(
                showWatermark, showInfo, showModules, showTime, showCoordinates,
                showRadar, radarSize, radarRange, showBlockEntities, moduleSortingMode
        );
    }

    @EventListener
    public void onRender2D(Render2DEvent e) {
        if (mc.currentScreen != Krypton.INSTANCE.GUI && mc.player != null) {
            DrawContext ctx = e.context;
            int w = mc.getWindow().getWidth();
            int h = mc.getWindow().getHeight();

            RenderUtils.unscaledProjection();

            // Reset block entity counts
            blockEntityCounts.clear();

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
            
            // LEFT SIDE - Block Entity Counter (right side of radar)
            if (showBlockEntities.getValue() && !blockEntityCounts.isEmpty()) {
                renderBlockEntityCounter(ctx);
            }
            
            // LEFT SIDE - Coordinates (below radar)
            if (showCoordinates.getValue()) {
                renderCoordinates(ctx);
            }
            
            // LEFT SIDE - Modules (below coordinates)
            if (showModules.getValue()) {
                renderModules(ctx);
            }

            RenderUtils.scaledProjection();
        }
    }

    // LEFT SIDE - Enabled Modules (under coordinates)
    private void renderModules(DrawContext ctx) {
        List<Module> list = getSortedModules();
        int padding = 12;
        int lineHeight = 16;
        int x = 12;
        int y = getTopLeftHeight() + (int) radarSize.getValue() + 16 + 32;
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

    // RADAR - FIXED: Arrow points where you're looking, cardinal directions correct
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
        
        // Get player's facing direction (yaw in degrees)
        // Minecraft: 0 = South, 90 = West, 180 = North, 270 = East
        float yaw = mc.player.getYaw();
        // Adjust so that the direction you're facing goes to the TOP of the radar
        // When facing North (180°), it should show North at the top
        // So we need to rotate the radar by (yaw - 180)
        double rad = Math.toRadians(-yaw + 180);
        
        // Draw + crosshair with arrow head on top line
        int armLength = size / 2 - 8;
        
        // Draw the top line of the + (with arrow head at the end - points where you're looking)
        for (int i = 0; i <= armLength; i++) {
            int px = centerX;
            int py = centerY - i;
            ctx.fill(px, py, px + 1, py + 1, CROSSHAIR_COLOR.getRGB());
        }
        
        // Draw arrow head at the top
        int arrowTipX = centerX;
        int arrowTipY = centerY - armLength;
        ctx.fill(arrowTipX - 2, arrowTipY, arrowTipX + 3, arrowTipY + 1, CROSSHAIR_COLOR.getRGB());
        ctx.fill(arrowTipX - 1, arrowTipY - 1, arrowTipX + 2, arrowTipY + 2, CROSSHAIR_COLOR.getRGB());
        ctx.fill(arrowTipX, arrowTipY - 2, arrowTipX + 1, arrowTipY + 3, CROSSHAIR_COLOR.getRGB());
        
        // Draw bottom line
        for (int i = 0; i <= armLength; i++) {
            int px = centerX;
            int py = centerY + i;
            ctx.fill(px, py, px + 1, py + 1, CROSSHAIR_COLOR.getRGB());
        }
        
        // Draw left line
        for (int i = 0; i <= armLength; i++) {
            int px = centerX - i;
            int py = centerY;
            ctx.fill(px, py, px + 1, py + 1, CROSSHAIR_COLOR.getRGB());
        }
        
        // Draw right line
        for (int i = 0; i <= armLength; i++) {
            int px = centerX + i;
            int py = centerY;
            ctx.fill(px, py, px + 1, py + 1, CROSSHAIR_COLOR.getRGB());
        }
        
        // Draw center dot (player)
        ctx.fill(centerX - 1, centerY - 1, centerX + 1, centerY + 1, URANIUM_GREEN.getRGB());
        
        // Draw cardinal directions - FIXED: Correct orientation
        int compassDistance = size / 2 - 15;
        
        // Calculate positions for cardinal directions based on rotation
        // North (0° relative) goes to the top when facing North
        int northX = centerX + (int)(Math.sin(rad) * compassDistance);
        int northY = centerY - (int)(Math.cos(rad) * compassDistance);
        int southX = centerX - (int)(Math.sin(rad) * compassDistance);
        int southY = centerY + (int)(Math.cos(rad) * compassDistance);
        int eastX = centerX + (int)(Math.cos(rad) * compassDistance);
        int eastY = centerY + (int)(Math.sin(rad) * compassDistance);
        int westX = centerX - (int)(Math.cos(rad) * compassDistance);
        int westY = centerY - (int)(Math.sin(rad) * compassDistance);
        
        // Draw the cardinal directions
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
            
            // Calculate angle relative to your facing direction
            double angle = Math.atan2(dz, dx);
            double relAngle = angle - Math.toRadians(yaw);
            
            // Convert to radar coordinates with forward direction going to TOP
            double radarX = Math.sin(relAngle) * distance;
            double radarY = -Math.cos(relAngle) * distance;
            
            int px = (int)(centerX + (radarX / range) * (size / 2 - 12));
            int py = (int)(centerY + (radarY / range) * (size / 2 - 12));
            
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
        
        // Draw block entities (all Y levels)
        if (showBlockEntities.getValue()) {
            for (int chunkX = -range; chunkX <= range; chunkX++) {
                for (int chunkZ = -range; chunkZ <= range; chunkZ++) {
                    WorldChunk chunk = mc.world.getChunkManager().getWorldChunk(mc.player.getChunkPos().x + chunkX, mc.player.getChunkPos().z + chunkZ);
                    if (chunk != null) {
                        for (BlockPos pos : chunk.getBlockEntityPositions()) {
                            BlockEntity blockEntity = mc.world.getBlockEntity(pos);
                            if (blockEntity == null) continue;
                            
                            // Check Y level - show all from -64 to 320
                            if (pos.getY() < -64 || pos.getY() > 320) continue;
                            
                            // Only show specific block types
                            if (!shouldShowBlockEntity(blockEntity)) continue;
                            
                            double dx = pos.getX() + 0.5 - mc.player.getX();
                            double dz = pos.getZ() + 0.5 - mc.player.getZ();
                            double distance = Math.sqrt(dx * dx + dz * dz);
                            
                            if (distance > range) continue;
                            
                            // Count block entities for display
                            String entityType = getBlockEntityName(blockEntity);
                            blockEntityCounts.merge(entityType, 1, Integer::sum);
                            
                            // Calculate angle relative to your facing direction
                            double angle = Math.atan2(dz, dx);
                            double relAngle = angle - Math.toRadians(yaw);
                            
                            // Convert to radar coordinates with forward direction going to TOP
                            double radarX = Math.sin(relAngle) * distance;
                            double radarY = -Math.cos(relAngle) * distance;
                            
                            int px = (int)(centerX + (radarX / range) * (size / 2 - 12));
                            int py = (int)(centerY + (radarY / range) * (size / 2 - 12));
                            
                            if (px > x + 4 && px < x + size - 4 && py > y + 4 && py < y + size - 4) {
                                Color blockColor = getBlockEntityColor(blockEntity);
                                if (blockColor != null) {
                                    // Draw colored dot
                                    RenderUtils.renderCircle(ctx.getMatrices(), blockColor, px, py, 3, 12);
                                    
                                    // Draw the first letter of the block type
                                    String firstLetter = getBlockEntityIcon(blockEntity);
                                    TextRenderer.drawString(firstLetter, ctx, px - 3, py - 4, TEXT_WHITE.getRGB());
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
    
    private boolean shouldShowBlockEntity(BlockEntity blockEntity) {
        return blockEntity instanceof MobSpawnerBlockEntity ||
               blockEntity instanceof ChestBlockEntity ||
               blockEntity instanceof ShulkerBoxBlockEntity ||
               blockEntity instanceof BeaconBlockEntity ||
               blockEntity instanceof BarrelBlockEntity ||
               blockEntity instanceof PistonBlockEntity;
    }
    
    private String getBlockEntityName(BlockEntity blockEntity) {
        if (blockEntity instanceof MobSpawnerBlockEntity) return "Spawner";
        if (blockEntity instanceof ChestBlockEntity) return "Chest";
        if (blockEntity instanceof ShulkerBoxBlockEntity) return "Shulker";
        if (blockEntity instanceof BeaconBlockEntity) return "Beacon";
        if (blockEntity instanceof BarrelBlockEntity) return "Barrel";
        if (blockEntity instanceof PistonBlockEntity) return "Piston";
        return "Block";
    }
    
    private String getBlockEntityIcon(BlockEntity blockEntity) {
        if (blockEntity instanceof MobSpawnerBlockEntity) return "S";
        if (blockEntity instanceof ChestBlockEntity) return "C";
        if (blockEntity instanceof ShulkerBoxBlockEntity) return "B";
        if (blockEntity instanceof BeaconBlockEntity) return "B";
        if (blockEntity instanceof BarrelBlockEntity) return "R";
        if (blockEntity instanceof PistonBlockEntity) return "P";
        return "?";
    }
    
    private Color getBlockEntityColor(BlockEntity blockEntity) {
        if (blockEntity instanceof MobSpawnerBlockEntity) return SPAWNER_COLOR;
        if (blockEntity instanceof ChestBlockEntity) return CHEST_COLOR;
        if (blockEntity instanceof ShulkerBoxBlockEntity) return SHULKER_COLOR;
        if (blockEntity instanceof BeaconBlockEntity) return BEACON_COLOR;
        if (blockEntity instanceof BarrelBlockEntity) return BARREL_COLOR;
        if (blockEntity instanceof PistonBlockEntity) return PISTON_COLOR;
        return TEXT_GRAY;
    }
    
    // Block Entity Counter (right side of radar)
    private void renderBlockEntityCounter(DrawContext ctx) {
        int radarSizeVal = (int) radarSize.getValue();
        int x = 12 + radarSizeVal + 12;
        int y = getTopLeftHeight() + 12;
        
        int padding = 8;
        int lineHeight = 14;
        int counterCount = blockEntityCounts.size();
        
        if (counterCount == 0) return;
        
        int bgHeight = counterCount * lineHeight + padding;
        int maxWidth = 0;
        
        for (Map.Entry<String, Integer> entry : blockEntityCounts.entrySet()) {
            String text = entry.getKey() + ": " + entry.getValue();
            int w = TextRenderer.getWidth(text);
            if (w > maxWidth) maxWidth = w;
        }
        
        int bgWidth = maxWidth + padding * 2;
        
        // Background
        RenderUtils.renderRoundedQuad(ctx.getMatrices(), BG_DARK, x, y, x + bgWidth, y + bgHeight, 8, 8, 8, 8, 50);
        
        // Counter list
        int textY = y + padding / 2 + 2;
        for (Map.Entry<String, Integer> entry : blockEntityCounts.entrySet()) {
            String text = entry.getKey() + ": " + entry.getValue();
            Color blockColor = getBlockEntityColorByName(entry.getKey());
            TextRenderer.drawString(text, ctx, x + padding, textY, blockColor.getRGB());
            textY += lineHeight;
        }
    }
    
    private Color getBlockEntityColorByName(String name) {
        switch (name) {
            case "Spawner": return SPAWNER_COLOR;
            case "Chest": return CHEST_COLOR;
            case "Shulker": return SHULKER_COLOR;
            case "Beacon": return BEACON_COLOR;
            case "Barrel": return BARREL_COLOR;
            case "Piston": return PISTON_COLOR;
            default: return TEXT_GRAY;
        }
    }

    // Coordinates (below radar)
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
