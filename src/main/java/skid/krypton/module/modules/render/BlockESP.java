package skid.krypton.module.modules.render;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.WorldChunk;
import skid.krypton.event.EventListener;
import skid.krypton.event.events.Render3DEvent;
import skid.krypton.module.Category;
import skid.krypton.module.Module;
import skid.krypton.module.setting.BooleanSetting;
import skid.krypton.module.setting.NumberSetting;
import skid.krypton.utils.BlockUtil;
import skid.krypton.utils.EncryptedString;
import skid.krypton.utils.RenderUtils;

import java.awt.*;
import java.util.*;
import java.util.List;

public final class BlockESP extends Module {

    // 🔥 SETTINGS PER BLOCK
    private static class BlockSettings {
        BooleanSetting enabled;
        BooleanSetting tracer;
        NumberSetting red, green, blue, alpha;

        BlockSettings(String name) {
            enabled = new BooleanSetting(EncryptedString.of(name + " Enabled"), false);
            tracer = new BooleanSetting(EncryptedString.of(name + " Tracer"), false);

            red = new NumberSetting(EncryptedString.of(name + " Red"), 0, 255, 255, 1);
            green = new NumberSetting(EncryptedString.of(name + " Green"), 0, 255, 255, 1);
            blue = new NumberSetting(EncryptedString.of(name + " Blue"), 0, 255, 255, 1);
            alpha = new NumberSetting(EncryptedString.of(name + " Alpha"), 0, 255, 120, 1);
        }

        Color getColor() {
            return new Color(
                    red.getIntValue(),
                    green.getIntValue(),
                    blue.getIntValue(),
                    alpha.getIntValue()
            );
        }
    }

    // 🔥 BLOCK SETTINGS
    private final Map<Block, BlockSettings> blockSettings = new HashMap<>();

    // 🔥 CACHE (THIS IS THE PERFORMANCE BOOST)
    private final Map<Block, List<BlockPos>> blockCache = new HashMap<>();

    private long lastScan = 0;

    public BlockESP() {
        super(
                EncryptedString.of("Block ESP"),
                EncryptedString.of("Fast selected block ESP"),
                -1,
                Category.RENDER
        );

        // 🔥 REGISTER ALL BLOCKS
        for (Block block : Registries.BLOCK) {
            String name = block.getName().getString();

            BlockSettings settings = new BlockSettings(name);
            blockSettings.put(block, settings);
            blockCache.put(block, new ArrayList<>());

            this.addSettings(
                    settings.enabled,
                    settings.tracer,
                    settings.red,
                    settings.green,
                    settings.blue,
                    settings.alpha
            );
        }
    }

    @EventListener
    public void onRender3D(Render3DEvent event) {

        // 🔥 ONLY SCAN EVERY 500ms
        if (System.currentTimeMillis() - lastScan > 500) {
            scanBlocks();
            lastScan = System.currentTimeMillis();
        }

        renderBlocks(event);
    }

    // 🔥 SCAN ONLY ENABLED BLOCKS
    private void scanBlocks() {

        // clear old cache
        blockCache.values().forEach(List::clear);

        for (WorldChunk chunk : BlockUtil.getLoadedChunks().toList()) {

            BlockPos min = chunk.getPos().getStartPos();
            BlockPos max = chunk.getPos().getEndPos();

            for (BlockPos pos : BlockPos.iterate(min, max)) {

                BlockState state = mc.world.getBlockState(pos);
                Block block = state.getBlock();

                BlockSettings settings = blockSettings.get(block);
                if (settings == null || !settings.enabled.getValue()) continue;

                // 🔥 ADD ONLY SELECTED BLOCKS
                blockCache.get(block).add(pos.toImmutable());
            }
        }
    }

    private void renderBlocks(Render3DEvent event) {
        Camera cam = RenderUtils.getCamera();

        if (cam != null) {
            Vec3d camPos = RenderUtils.getCameraPos();
            MatrixStack matrices = event.matrixStack;

            matrices.push();
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(cam.getPitch()));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(cam.getYaw() + 180.0f));
            matrices.translate(-camPos.x, -camPos.y, -camPos.z);
        }

        // 🔥 RENDER ONLY CACHED BLOCKS
        for (Map.Entry<Block, List<BlockPos>> entry : blockCache.entrySet()) {

            Block block = entry.getKey();
            BlockSettings settings = blockSettings.get(block);

            if (settings == null || !settings.enabled.getValue()) continue;

            Color color = settings.getColor();

            for (BlockPos pos : entry.getValue()) {

                // 🔥 DISTANCE LIMIT (HUGE FPS BOOST)
                if (mc.player.getPos().distanceTo(Vec3d.ofCenter(pos)) > 64) continue;

                RenderUtils.renderFilledBox(
                        event.matrixStack,
                        pos.getX() + 0.1F,
                        pos.getY() + 0.05F,
                        pos.getZ() + 0.1F,
                        pos.getX() + 0.9F,
                        pos.getY() + 0.95F,
                        pos.getZ() + 0.9F,
                        color
                );

                if (settings.tracer.getValue() && mc.crosshairTarget != null) {
                    RenderUtils.renderLine(
                            event.matrixStack,
                            new Color(color.getRed(), color.getGreen(), color.getBlue(), 255),
                            mc.crosshairTarget.getPos(),
                            new Vec3d(
                                    pos.getX() + 0.5,
                                    pos.getY() + 0.5,
                                    pos.getZ() + 0.5
                            )
                    );
                }
            }
        }

        event.matrixStack.pop();
    }
}
