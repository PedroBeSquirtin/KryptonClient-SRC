package skid.krypton.module.modules.render;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.util.math.MathHelper;
import skid.krypton.Krypton;
import skid.krypton.event.EventListener;
import skid.krypton.event.events.Render2DEvent;
import skid.krypton.module.Category;
import skid.krypton.module.Module;
import skid.krypton.module.setting.*;
import skid.krypton.utils.*;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public final class HUD extends Module {

    private static final CharSequence watermarkText = "Uranium Client";
    private static final SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss");

    // SETTINGS
    private final BooleanSetting showWatermark = new BooleanSetting("Watermark", true);
    private final BooleanSetting showInfo = new BooleanSetting("Info", true);
    private final BooleanSetting showModules = new BooleanSetting("Modules", true);
    private final BooleanSetting showTime = new BooleanSetting("Time", true);
    private final BooleanSetting showCoordinates = new BooleanSetting("Coordinates", true);
    private final BooleanSetting showRadar = new BooleanSetting("Radar", true);
    private final BooleanSetting showPotions = new BooleanSetting("Potions", true);
    private final BooleanSetting showNotifications = new BooleanSetting("Notifications", true);

    private final NumberSetting opacity = new NumberSetting("Opacity", 0.0, 1.0, 0.8f, 0.05f);
    private final NumberSetting cornerRadius = new NumberSetting("Corner Radius", 0.0, 10.0, 5.0, 0.5);

    // Use the existing ModuleListSorting enum
    private final ModeSetting<ModuleListSorting> moduleSortingMode =
            new ModeSetting<>("Sort Mode", ModuleListSorting.LENGTH, ModuleListSorting.class);

    private final BooleanSetting enableRainbowEffect = new BooleanSetting("Rainbow", false);
    private final NumberSetting rainbowSpeed = new NumberSetting("Rainbow Speed", 0.1f, 10.0, 2.0, 0.1f);

    private final Color primaryColor = new Color(65, 185, 255);
    private final Color secondaryColor = new Color(255, 110, 230);

    // NOTIFICATIONS
    private final List<String> notifications = new ArrayList<>();

    public HUD() {
        super("HUD", "Custom HUD", -1, Category.RENDER);

        this.addSettings(
                showWatermark, showInfo, showModules, showTime, showCoordinates,
                showRadar, showPotions, showNotifications,
                opacity, cornerRadius, moduleSortingMode,
                enableRainbowEffect, rainbowSpeed
        );
    }

    @EventListener
    public void onRender2D(Render2DEvent e) {
        if (mc.currentScreen != Krypton.INSTANCE.GUI) {
            DrawContext ctx = e.context;
            int w = mc.getWindow().getWidth();
            int h = mc.getWindow().getHeight();

            RenderUtils.unscaledProjection();

            if (showWatermark.getValue()) renderWatermark(ctx);
            if (showInfo.getValue() && mc.player != null) renderInfo(ctx);
            if (showTime.getValue()) renderTime(ctx, w);
            if (showCoordinates.getValue() && mc.player != null) renderCoordinates(ctx, h);
            if (showRadar.getValue()) renderRadar(ctx);
            if (showPotions.getValue() && mc.player != null) renderPotions(ctx);
            if (showNotifications.getValue()) renderNotifications(ctx);
            if (showModules.getValue()) renderModules(ctx, w);

            RenderUtils.scaledProjection();
        }
    }

    // WATERMARK
    private void renderWatermark(DrawContext ctx) {
        int width = TextRenderer.getWidth(watermarkText);
        RenderUtils.renderRoundedQuad(ctx.getMatrices(),
                bg(), 5, 5, 5 + width + 8, 25, cornerRadius.getValue(), 15);

        TextRenderer.drawString(watermarkText, ctx, 8, 8, getColor(0).getRGB());
    }

    // INFO
    private void renderInfo(DrawContext ctx) {
        String fps = "FPS: " + mc.getCurrentFps() + " | ";
        String ping = getPingInfo();
        String server = mc.getCurrentServerEntry() == null ? "Singleplayer" : mc.getCurrentServerEntry().address;

        String text = fps + ping + server;

        int width = TextRenderer.getWidth(text);

        RenderUtils.renderRoundedQuad(ctx.getMatrices(),
                bg(), 5, 30, 5 + width + 10, 50, cornerRadius.getValue(), 15);

        TextRenderer.drawString(text, ctx, 10, 33, primaryColor.getRGB());
    }

    // TIME
    private void renderTime(DrawContext ctx, int w) {
        String time = timeFormatter.format(new Date());
        int width = TextRenderer.getWidth(time);

        int x = w / 2;

        RenderUtils.renderRoundedQuad(ctx.getMatrices(),
                bg(), x - width / 2 - 4, 5, x + width / 2 + 6, 25, cornerRadius.getValue(), 15);

        TextRenderer.drawString(time, ctx, x - width / 2, 8, getColor(0).getRGB());
    }

    // COORDS
    private void renderCoordinates(DrawContext ctx, int h) {
        String coords = String.format("X: %.1f Y: %.1f Z: %.1f",
                mc.player.getX(), mc.player.getY(), mc.player.getZ());

        int width = TextRenderer.getWidth(coords);

        RenderUtils.renderRoundedQuad(ctx.getMatrices(),
                bg(), 5, h - 25, 5 + width + 10, h - 5, cornerRadius.getValue(), 15);

        TextRenderer.drawString(coords, ctx, 10, h - 22, primaryColor.getRGB());
    }

    // RADAR
    private void renderRadar(DrawContext ctx) {
        int size = 80;
        int x = 5;
        int y = 60;

        RenderUtils.renderRoundedQuad(ctx.getMatrices(),
                bg(), x, y, x + size, y + size, cornerRadius.getValue(), 15);

        int cx = x + size / 2;
        int cy = y + size / 2;

        for (Entity ent : mc.world.getEntities()) {
            if (ent == mc.player) continue;

            double dx = ent.getX() - mc.player.getX();
            double dz = ent.getZ() - mc.player.getZ();

            int px = (int)(cx + dx * 2);
            int py = (int)(cy + dz * 2);

            if (px > x && px < x + size && py > y && py < y + size) {
                ctx.fill(px, py, px + 2, py + 2, Color.RED.getRGB());
            }
        }
    }

    // POTIONS - Fixed to work with your Minecraft version
    private void renderPotions(DrawContext ctx) {
        int x = 5;
        int y = 145;
        int offset = 0;

        for (StatusEffectInstance effect : mc.player.getStatusEffects()) {
            // Get potion name - using getTranslationKey() which is more reliable
            String effectName = effect.getEffectType().getTranslationKey();
            // Remove the "effect.minecraft." prefix
            effectName = effectName.replace("effect.minecraft.", "");
            // Capitalize first letter
            effectName = effectName.substring(0, 1).toUpperCase() + effectName.substring(1);
            
            String text = effectName + " " + (effect.getDuration() / 20) + "s";

            int width = TextRenderer.getWidth(text);

            RenderUtils.renderRoundedQuad(ctx.getMatrices(),
                    bg(), x, y + offset, x + width + 10, y + offset + 18,
                    cornerRadius.getValue(), 15);

            TextRenderer.drawString(text, ctx, x + 5, y + offset + 4, primaryColor.getRGB());

            offset += 20;
        }
    }

    // NOTIFICATIONS
    private void renderNotifications(DrawContext ctx) {
        int y = 200;

        for (String notif : notifications) {
            int width = TextRenderer.getWidth(notif);

            RenderUtils.renderRoundedQuad(ctx.getMatrices(),
                    bg(), 5, y, 10 + width, y + 20,
                    cornerRadius.getValue(), 15);

            TextRenderer.drawString(notif, ctx, 8, y + 5, secondaryColor.getRGB());

            y += 25;
        }
    }

    // MODULE LIST
    private void renderModules(DrawContext ctx, int w) {
        List<Module> list = getSortedModules();
        int y = 5;

        for (Module m : list) {
            if (!m.isEnabled()) continue;

            String name = m.getName().toString();
            int width = TextRenderer.getWidth(name);

            int x = w - width - 10;

            RenderUtils.renderRoundedQuad(ctx.getMatrices(),
                    bg(), x - 3, y, w - 5, y + 18, cornerRadius.getValue(), 15);

            TextRenderer.drawString(name, ctx, x, y + 4, getColor(list.indexOf(m)).getRGB());

            y += 22;
        }
    }

    // HELPERS
    private Color bg() {
        return new Color(35, 35, 35, (int)(opacity.getFloatValue() * 255));
    }

    private Color getColor(int i) {
        return enableRainbowEffect.getValue()
                ? ColorUtil.a((int)(rainbowSpeed.getValue()) + i, 1)
                : primaryColor;
    }

    private List<Module> getSortedModules() {
        List<Module> modules = Krypton.INSTANCE.getModuleManager().b();
        List<Module> sorted = new ArrayList<>(modules);
        
        switch (moduleSortingMode.getValue()) {
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
            PlayerListEntry entry = mc.getNetworkHandler()
                    .getPlayerListEntry(mc.player.getUuid());
            return entry != null ? "Ping: " + entry.getLatency() + "ms | " : "Ping: N/A | ";
        }
        return "Ping: N/A | ";
    }

    public void addNotification(String message) {
        notifications.add(message);
        while (notifications.size() > 5) {
            notifications.remove(0);
        }
    }

    // This enum must exist in your client - it's already there from your original code
    enum ModuleListSorting {
        LENGTH, ALPHABETICAL, CATEGORY
    }
}
