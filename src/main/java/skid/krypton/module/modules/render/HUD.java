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
    private static final Color CARDINAL_COLOR = new Color(80, 200, 80, 180);
    
    // Potion Textures (Minecraft default potion icons)
    private static final Identifier SPEED_ICON = new Identifier("textures/mob_effect/speed.png");
    private static final Identifier SLOWNESS_ICON = new Identifier("textures/mob_effect/slowness.png");
    private static final Identifier HASTE_ICON = new Identifier("textures/mob_effect/haste.png");
    private static final Identifier STRENGTH_ICON = new Identifier("textures/mob_effect/strength.png");
    private static final Identifier JUMP_BOOST_ICON = new Identifier("textures/mob_effect/jump_boost.png");
    private static final Identifier REGENERATION_ICON = new Identifier("textures/mob_effect/regeneration.png");
    private static final Identifier RESISTANCE_ICON = new Identifier("textures/mob_effect/resistance.png");
    private static final Identifier FIRE_RESISTANCE_ICON = new Identifier("textures/mob_effect/fire_resistance.png");
    private static final Identifier WATER_BREATHING_ICON = new Identifier("textures/mob_effect/water_breathing.png");
    private static final Identifier INVISIBILITY_ICON = new Identifier("textures/mob_effect/invisibility.png");
    private static final Identifier NIGHT_VISION_ICON = new Identifier("textures/mob_effect/night_vision.png");
    private static final Identifier POISON_ICON = new Identifier("textures/mob_effect/poison.png");
    private static final Identifier WITHER_ICON = new Identifier("textures/mob_effect/wither.png");
    private static final Identifier ABSORPTION_ICON = new Identifier("textures/mob_effect/absorption.png");
    private static final Identifier HEALTH_BOOST_ICON = new Identifier("textures/mob_effect/health_boost.png");
    private static final Identifier LUCK_ICON = new Identifier("textures/mob_effect/luck.png");

    // SETTINGS
    private final BooleanSetting showWatermark = new BooleanSetting("Watermark", true);
    private final BooleanSetting showInfo = new BooleanSetting("Info", true);
    private final BooleanSetting showModules = new BooleanSetting("Modules", true);
    private final BooleanSetting showTime = new BooleanSetting("Time", true);
    private final BooleanSetting showCoordinates = new BooleanSetting("Coordinates", true);
    private final BooleanSetting showRadar = new BooleanSetting("Radar", true);
    private final BooleanSetting showPotions = new BooleanSetting("Potions", true);
    private final BooleanSetting hideVanillaPotions = new BooleanSetting("Hide Vanilla Potions", true);
    private final NumberSetting radarSize = new NumberSetting("Radar Size", 100, 250, 180, 5);
    private final NumberSetting radarRange = new NumberSetting("Radar Range", 10, 60, 30, 5);
    
    private final ModeSetting<ModuleListSorting> moduleSortingMode =
            new ModeSetting<>("Sort Mode", ModuleListSorting.LENGTH, ModuleListSorting.class);

    public HUD() {
        super("HUD", "Clean HUD", -1, Category.RENDER);

        this.addSettings(
                showWatermark, showInfo, showModules, showTime, showCoordinates,
                showRadar, radarSize, radarRange, showPotions, hideVanillaPotions, moduleSortingMode
        );
    }

    @Override
    public void onEnable() {
        super.onEnable();
        // Hide vanilla potion effects when module is enabled
        if (hideVanillaPotions.getValue()) {
            mc.options.getEffectAmplifier().setValue(0);
            mc.options.getEffectDuration().setValue(false);
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        // Restore vanilla potion effects
        mc.options.getEffectAmplifier().setValue(1);
        mc.options.getEffectDuration().setValue(true);
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
        int padding = 8;
        int lineHeight = 12;
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

    // RADAR - Larger with cardinal directions and crosshair
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
        
        // Draw crosshair (shows direction you're facing)
        float yaw = mc.player.getYaw();
        double rad = Math.toRadians(yaw);
        int crosshairLength = size / 4;
        int crosshairX = centerX + (int)(Math.sin(rad) * crosshairLength);
        int crosshairY = centerY + (int)(Math.cos(rad) * crosshairLength);
        
        // Draw crosshair line
        RenderUtils.renderLine(ctx.getMatrices(), new Color(80, 200, 80, 200), 
            new Vec3d(centerX, centerY, 0), new Vec3d(crosshairX, crosshairY, 0));
        
        // Draw center dot (player)
        ctx.fill(centerX - 3, centerY - 3, centerX + 3, centerY + 3, URANIUM_GREEN.getRGB());
        
        // Draw cardinal directions (N, E, S, W)
        TextRenderer.drawString("N", ctx, centerX - 4, centerY - size / 2 + 10, CARDINAL_COLOR.getRGB());
        TextRenderer.drawString("S", ctx, centerX - 4, centerY + size / 2 - 20, CARDINAL_COLOR.getRGB());
        TextRenderer.drawString("E", ctx, centerX + size / 2 - 20, centerY - 5, CARDINAL_COLOR.getRGB());
        TextRenderer.drawString("W", ctx, centerX - size / 2 + 10, centerY - 5, CARDINAL_COLOR.getRGB());
        
        // Draw other players with smaller names
        for (Entity ent : mc.world.getPlayers()) {
            if (ent == mc.player) continue;
            
            double dx = ent.getX() - mc.player.getX();
            double dz = ent.getZ() - mc.player.getZ();
            double distance = Math.sqrt(dx * dx + dz * dz);
            
            if (distance > range) continue;
            
            // Scale to radar size
            int px = (int)(centerX + (dx / range) * (size / 2 - 12));
            int py = (int)(centerY + (dz / range) * (size / 2 - 12));
            
            // Check bounds
            if (px > x + 6 && px < x + size - 6 && py > y + 6 && py < y + size - 6) {
                PlayerEntity player = (PlayerEntity) ent;
                
                // Draw player dot (less green, more subtle)
                RenderUtils.renderCircle(ctx.getMatrices(), new Color(80, 200, 80, 180), px, py, 3, 12);
                
                // Draw player name (smaller)
                String name = player.getName().getString();
                int nameWidth = TextRenderer.getWidth(name);
                int nameX = px - nameWidth / 2;
                int nameY = py + 6;
                
                // Name background (semi-transparent)
                if (nameX > x + 2 && nameX + nameWidth < x + size - 2) {
                    RenderUtils.renderRoundedQuad(ctx.getMatrices(), new Color(0, 0, 0, 120), nameX - 2, nameY - 2, nameX + nameWidth + 2, nameY + 8, 3, 3, 3, 3, 30);
                    TextRenderer.drawString(name, ctx, nameX, nameY, TEXT_GRAY.getRGB());
                }
            }
        }
        
        // Draw border
        RenderUtils.renderRoundedOutline(ctx, new Color(80, 200, 80, 150), 
            x, y, x + size, y + size, 8, 8, 8, 8, 2, 5);
        
        // Draw range circles (less prominent)
        int step = range / 4;
        for (int r = step; r <= range; r += step) {
            int radius = (int)((double)r / range * (size / 2 - 6));
            RenderUtils.renderCircle(ctx.getMatrices(), new Color(80, 200, 80, 40), centerX, centerY, radius, 36);
        }
    }

    // BOTTOM LEFT - Coordinates
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

    // POTIONS - With Minecraft potion icons
    private void renderPotions(DrawContext ctx) {
        List<StatusEffectInstance> effects = new ArrayList<>(mc.player.getStatusEffects());
        if (effects.isEmpty()) return;
        
        int padding = 8;
        int lineHeight = 22;
        int iconSize = 18;
        int x = 12;
        int y = getTopLeftHeight() + (int) radarSize.getValue() + 16 + 28;
        
        int potionCount = effects.size();
        int bgHeight = potionCount * lineHeight + padding;
        int maxWidth = 0;
        
        for (StatusEffectInstance effect : effects) {
            String text = formatPotionName(effect) + " " + (effect.getDuration() / 20);
            int w = TextRenderer.getWidth(text);
            if (w > maxWidth) maxWidth = w;
        }
        
        int bgWidth = maxWidth + iconSize + padding * 2;
        
        // Background
        RenderUtils.renderRoundedQuad(ctx.getMatrices(), BG_DARK, x, y, x + bgWidth, y + bgHeight, 8, 8, 8, 8, 50);
        
        // Potion list with icons
        int textY = y + padding / 2 + 2;
        for (StatusEffectInstance effect : effects) {
            String effectName = formatPotionName(effect);
            int duration = effect.getDuration() / 20;
            String text = effectName + " " + duration;
            
            // Draw potion icon
            Identifier icon = getPotionIcon(effect);
            ctx.getMatrices().push();
            ctx.getMatrices().translate(x + padding, textY, 0);
            ctx.getMatrices().scale(0.9f, 0.9f, 1);
            ctx.drawTexture(icon, 0, 0, 0, 0, 18, 18, 18, 18);
            ctx.getMatrices().pop();
            
            // Draw text
            TextRenderer.drawString(text, ctx, x + padding + iconSize + 4, textY + 3, TEXT_GRAY.getRGB());
            textY += lineHeight;
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

    private Identifier getPotionIcon(StatusEffectInstance effect) {
        String name = effect.getEffectType().value().getName().getString().toLowerCase();
        
        if (name.contains("speed")) return SPEED_ICON;
        if (name.contains("slowness")) return SLOWNESS_ICON;
        if (name.contains("haste")) return HASTE_ICON;
        if (name.contains("strength")) return STRENGTH_ICON;
        if (name.contains("jump boost")) return JUMP_BOOST_ICON;
        if (name.contains("regeneration")) return REGENERATION_ICON;
        if (name.contains("resistance")) return RESISTANCE_ICON;
        if (name.contains("fire resistance")) return FIRE_RESISTANCE_ICON;
        if (name.contains("water breathing")) return WATER_BREATHING_ICON;
        if (name.contains("invisibility")) return INVISIBILITY_ICON;
        if (name.contains("night vision")) return NIGHT_VISION_ICON;
        if (name.contains("poison")) return POISON_ICON;
        if (name.contains("wither")) return WITHER_ICON;
        if (name.contains("absorption")) return ABSORPTION_ICON;
        if (name.contains("health boost")) return HEALTH_BOOST_ICON;
        if (name.contains("luck")) return LUCK_ICON;
        
        return SPEED_ICON; // Default
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
