package skid.krypton.module.modules.render;

import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import skid.krypton.event.EventListener;
import skid.krypton.event.events.Render3DEvent;
import skid.krypton.module.Category;
import skid.krypton.module.Module;
import skid.krypton.module.modules.client.Krypton;
import skid.krypton.module.setting.BooleanSetting;
import skid.krypton.module.setting.ModeSetting;
import skid.krypton.module.setting.NumberSetting;
import skid.krypton.utils.*;

import java.awt.*;
import java.util.function.Supplier;

public final class PlayerESP extends Module {
    
    // ESP Types
    public enum ESPType {
        BOX, OUTLINE, GLOW, CHAMS, WIREFRAME
    }
    
    // Main Settings
    private final ModeSetting<ESPType> espType = new ModeSetting<>(EncryptedString.of("ESP Type"), ESPType.BOX, ESPType.class);
    private final NumberSetting red = new NumberSetting(EncryptedString.of("Red"), 0, 255, 80, 1);
    private final NumberSetting green = new NumberSetting(EncryptedString.of("Green"), 0, 255, 200, 1);
    private final NumberSetting blue = new NumberSetting(EncryptedString.of("Blue"), 0, 255, 80, 1);
    private final NumberSetting alpha = new NumberSetting(EncryptedString.of("Alpha"), 0, 255, 150, 1);
    private final NumberSetting lineWidth = new NumberSetting(EncryptedString.of("Line Width"), 1, 5, 2, 0.5);
    
    // Additional Features
    private final BooleanSetting tracers = new BooleanSetting(EncryptedString.of("Tracers"), false);
    private final BooleanSetting healthBar = new BooleanSetting(EncryptedString.of("Health Bar"), true);
    private final BooleanSetting nameTag = new BooleanSetting(EncryptedString.of("Name Tag"), true);
    private final BooleanSetting distance = new BooleanSetting(EncryptedString.of("Distance"), true);
    private final BooleanSetting teamCheck = new BooleanSetting(EncryptedString.of("Team Check"), true);
    private final BooleanSetting invisibleCheck = new BooleanSetting(EncryptedString.of("Invisible Check"), false);
    
    // Glow Settings
    private final NumberSetting glowIntensity = new NumberSetting(EncryptedString.of("Glow Intensity"), 1, 10, 3, 1);
    
    // Chams Settings
    private final BooleanSetting chamsWireframe = new BooleanSetting(EncryptedString.of("Wireframe Mode"), false);
    
    // Color Modes
    private final ModeSetting colorMode = new ModeSetting(EncryptedString.of("Color Mode"), "Custom", new String[]{"Custom", "Health", "Distance", "Team"});
    
    public PlayerESP() {
        super(EncryptedString.of("Player ESP"), EncryptedString.of("Advanced player rendering ESP"), -1, Category.RENDER);
        
        // Main Settings
        this.addSettings(this.espType, this.red, this.green, this.blue, this.alpha, this.lineWidth, this.colorMode);
        
        // Additional Features
        this.addSettings(this.tracers, this.healthBar, this.nameTag, this.distance, this.teamCheck, this.invisibleCheck);
        
        // Glow & Chams
        this.addSettings(this.glowIntensity, this.chamsWireframe);
    }
    
    @Override
    public void onEnable() {
        super.onEnable();
    }
    
    @Override
    public void onDisable() {
        super.onDisable();
    }
    
    @EventListener
    public void onRender3D(final Render3DEvent event) {
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player) continue;
            
            // Team check
            if (teamCheck.getValue() && isTeammate(player)) continue;
            
            // Invisible check
            if (invisibleCheck.getValue() && player.isInvisible()) continue;
            
            renderPlayerESP(event, player);
        }
    }
    
    private void renderPlayerESP(Render3DEvent event, PlayerEntity player) {
        Camera camera = RenderUtils.getCamera();
        if (camera != null) {
            MatrixStack matrices = event.matrixStack;
            matrices.push();
            Vec3d cameraPos = RenderUtils.getCameraPos();
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0f));
            matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        }
        
        // Get interpolated position for smooth rendering
        double x = MathHelper.lerp(RenderTickCounter.ONE.getTickDelta(true), player.prevX, player.getX());
        double y = MathHelper.lerp(RenderTickCounter.ONE.getTickDelta(true), player.prevY, player.getY());
        double z = MathHelper.lerp(RenderTickCounter.ONE.getTickDelta(true), player.prevZ, player.getZ());
        
        float width = player.getWidth() / 2.0f;
        float height = player.getHeight();
        
        // Get color based on selected mode
        Color color = getPlayerColor(player);
        Color fillColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha.getIntValue());
        
        // Render based on ESP type
        switch (espType.getValue()) {
            case BOX:
                renderBoxESP(event.matrixStack, x, y, z, width, height, fillColor, color);
                break;
            case OUTLINE:
                renderOutlineESP(event.matrixStack, player, x, y, z, width, height, color);
                break;
            case GLOW:
                renderGlowESP(event.matrixStack, player, x, y, z, width, height, color);
                break;
            case CHAMS:
                renderChamsESP(event.matrixStack, player, x, y, z, width, height, fillColor);
                break;
            case WIREFRAME:
                renderWireframeESP(event.matrixStack, player, x, y, z, width, height, color);
                break;
        }
        
        // Render health bar
        if (healthBar.getValue()) {
            renderHealthBar(event.matrixStack, x, y, z, width, height, player);
        }
        
        // Render tracers
        if (tracers.getValue()) {
            renderTracer(event.matrixStack, player);
        }
        
        // Render name tag and distance in 3D space
        if (nameTag.getValue() || distance.getValue()) {
            renderNameTag(event.matrixStack, player, x, y, z);
        }
        
        if (camera != null) {
            event.matrixStack.pop();
        }
    }
    
    private void renderBoxESP(MatrixStack matrices, double x, double y, double z, float width, float height, Color fillColor, Color outlineColor) {
        // Filled box
        RenderUtils.renderFilledBox(matrices, 
            (float)(x - width), (float)y, (float)(z - width),
            (float)(x + width), (float)(y + height), (float)(z + width),
            fillColor);
        
        // Outline
        renderOutlineESP(matrices, null, x, y, z, width, height, outlineColor);
    }
    
    private void renderOutlineESP(MatrixStack matrices, PlayerEntity player, double x, double y, double z, float width, float height, Color color) {
        float x1 = (float)(x - width);
        float y1 = (float)y;
        float z1 = (float)(z - width);
        float x2 = (float)(x + width);
        float y2 = (float)(y + height);
        float z2 = (float)(z + width);
        
        // Draw all 12 edges of the box
        drawLine(matrices, x1, y1, z1, x2, y1, z1, color);
        drawLine(matrices, x1, y1, z1, x1, y2, z1, color);
        drawLine(matrices, x1, y1, z1, x1, y1, z2, color);
        drawLine(matrices, x2, y1, z1, x2, y2, z1, color);
        drawLine(matrices, x2, y1, z1, x2, y1, z2, color);
        drawLine(matrices, x1, y2, z1, x2, y2, z1, color);
        drawLine(matrices, x1, y2, z1, x1, y2, z2, color);
        drawLine(matrices, x1, y1, z2, x2, y1, z2, color);
        drawLine(matrices, x1, y1, z2, x1, y2, z2, color);
        drawLine(matrices, x2, y1, z2, x2, y2, z2, color);
        drawLine(matrices, x1, y2, z2, x2, y2, z2, color);
        drawLine(matrices, x2, y2, z1, x2, y2, z2, color);
    }
    
    private void renderGlowESP(MatrixStack matrices, PlayerEntity player, double x, double y, double z, float width, float height, Color color) {
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        
        for (int i = 1; i <= glowIntensity.getIntValue(); i++) {
            float alpha = 0.15f / i;
            Color glowColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(alpha * 255));
            
            float scale = 1.0f + (i * 0.05f);
            float x1 = (float)(x - width * scale);
            float y1 = (float)y;
            float z1 = (float)(z - width * scale);
            float x2 = (float)(x + width * scale);
            float y2 = (float)(y + height * scale);
            float z2 = (float)(z + width * scale);
            
            RenderUtils.renderFilledBox(matrices, x1, y1, z1, x2, y2, z2, glowColor);
        }
        
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }
    
    private void renderChamsESP(MatrixStack matrices, PlayerEntity player, double x, double y, double z, float width, float height, Color color) {
        GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);
        GL11.glPolygonOffset(1.0f, 1.0f);
        
        RenderUtils.renderFilledBox(matrices, 
            (float)(x - width), (float)y, (float)(z - width),
            (float)(x + width), (float)(y + height), (float)(z + width),
            color);
        
        if (chamsWireframe.getValue()) {
            GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
            RenderUtils.renderFilledBox(matrices, 
                (float)(x - width), (float)y, (float)(z - width),
                (float)(x + width), (float)(y + height), (float)(z + width),
                new Color(255, 255, 255, 200));
            GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
        }
        
        GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);
    }
    
    private void renderWireframeESP(MatrixStack matrices, PlayerEntity player, double x, double y, double z, float width, float height, Color color) {
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
        GL11.glLineWidth(lineWidth.getFloatValue());
        
        RenderUtils.renderFilledBox(matrices, 
            (float)(x - width), (float)y, (float)(z - width),
            (float)(x + width), (float)(y + height), (float)(z + width),
            color);
        
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
        GL11.glLineWidth(1.0f);
    }
    
    private void renderHealthBar(MatrixStack matrices, double x, double y, double z, float width, float height, PlayerEntity player) {
        float healthPercent = player.getHealth() / player.getMaxHealth();
        int healthColor = Color.HSBtoRGB(healthPercent / 3.0f, 1.0f, 1.0f);
        Color healthBarColor = new Color(healthColor);
        
        float barWidth = width * 2;
        float barHeight = 0.1f;
        float barX = (float)(x - barWidth / 2);
        float barY = (float)(y + height + 0.1f);
        float barZ = (float)z;
        
        // Background
        RenderUtils.renderFilledBox(matrices, barX, barY, barZ, barX + barWidth, barY + barHeight, barZ + 0.05f, new Color(0, 0, 0, 150));
        // Health fill
        RenderUtils.renderFilledBox(matrices, barX, barY, barZ, barX + (barWidth * healthPercent), barY + barHeight, barZ + 0.05f, healthBarColor);
    }
    
    private void renderTracer(MatrixStack matrices, PlayerEntity player) {
        Vec3d playerPos = player.getLerpedPos(RenderTickCounter.ONE.getTickDelta(true));
        Vec3d cameraPos = RenderUtils.getCameraPos();
        
        GL11.glLineWidth(lineWidth.getFloatValue());
        GL11.glDepthFunc(GL11.GL_ALWAYS);
        
        RenderUtils.renderLine(matrices, getPlayerColor(player), cameraPos, playerPos);
        
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        GL11.glLineWidth(1.0f);
    }
    
    private void renderNameTag(MatrixStack matrices, PlayerEntity player, double x, double y, double z) {
        matrices.push();
        matrices.translate(x, y + player.getHeight() + 0.3, z);
        
        // Always face camera
        Camera camera = mc.gameRenderer.getCamera();
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        
        String tag = "";
        if (nameTag.getValue()) {
            tag = player.getName().getString();
        }
        if (distance.getValue()) {
            double dist = mc.player.distanceTo(player);
            if (nameTag.getValue()) {
                tag += " §7(" + (int)dist + "m)";
            } else {
                tag = "§7" + (int)dist + "m";
            }
        }
        
        matrices.scale(0.025f, 0.025f, 0.025f);
        
        int textWidth = mc.textRenderer.getWidth(tag);
        int textHeight = mc.textRenderer.fontHeight;
        
        // Background
        RenderUtils.renderRoundedQuad(matrices, new Color(0, 0, 0, 100), 
            -textWidth / 2 - 2, -textHeight / 2 - 2, 
            textWidth / 2 + 2, textHeight / 2 + 2, 
            3, 3, 3, 3, 50);
        
        // Text
        mc.textRenderer.draw(matrices, tag, -textWidth / 2, -textHeight / 2, getPlayerColor(player).getRGB());
        
        matrices.pop();
    }
    
    private Color getPlayerColor(PlayerEntity player) {
        if (colorMode.getValue().equals("Health")) {
            float healthPercent = player.getHealth() / player.getMaxHealth();
            return Color.getHSBColor(healthPercent / 3.0f, 1.0f, 1.0f);
        } else if (colorMode.getValue().equals("Distance")) {
            float distance = (float) mc.player.distanceTo(player);
            float maxDistance = 50.0f;
            float percent = Math.min(1.0f, distance / maxDistance);
            return new Color(80, 200, 80).darker().brighter().brighter();
        } else if (colorMode.getValue().equals("Team")) {
            return new Color(80, 200, 80, alpha.getIntValue());
        } else {
            // Custom color with Uranium Green default
            return new Color(red.getIntValue(), green.getIntValue(), blue.getIntValue(), alpha.getIntValue());
        }
    }
    
    private boolean isTeammate(PlayerEntity player) {
        if (mc.player.getScoreboardTeam() != null && player.getScoreboardTeam() != null) {
            return mc.player.getScoreboardTeam().isEqual(player.getScoreboardTeam());
        }
        return false;
    }
    
    private void drawLine(MatrixStack matrices, float x1, float y1, float z1, float x2, float y2, float z2, Color color) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        
        buffer.vertex(matrix, x1, y1, z1).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        buffer.vertex(matrix, x2, y2, z2).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        
        BufferRenderer.drawWithGlobalProgram(buffer.end());
    }
}
