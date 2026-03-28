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
import skid.krypton.module.setting.NumberSetting;
import skid.krypton.utils.BlockUtil;
import skid.krypton.utils.EncryptedString;
import skid.krypton.utils.RenderUtils;

import java.awt.*;
import java.util.*;
import java.util.List;

public final class BlockESP extends Module {

    // 🔥 GLOBAL ALPHA
    private final NumberSetting alpha =
            new NumberSetting(EncryptedString.of("Alpha"), 0, 255, 120, 1);

    // 🔥 SELECTED BLOCKS
    private final Set<Block> selectedBlocks = new HashSet<>();

    // 🔥 PER BLOCK DATA (color + tracer)
    private final Map<Block, BlockData> blockDataMap = new HashMap<>();

    // 🔥 CACHE
    private final Map<Block, List<BlockPos>> blockCache = new HashMap<>();

    private long lastScan = 0;

    public BlockESP() {
        super(
                EncryptedString.of("Block ESP"),
                EncryptedString.of("Clean block selection ESP"),
                -1,
                Category.RENDER
        );

        this.addSettings(alpha);

        // init all blocks
        for (Block block : Registries.BLOCK) {
            blockDataMap.put(block, new BlockData());
            blockCache.put(block, new ArrayList<>());
        }
    }

    // 🔥 BLOCK DATA (PER BLOCK SETTINGS)
    public static class BlockData {
        public int r = 255, g = 255, b = 255;
        public boolean tracer = false;

        public Color getColor(int alpha) {
            return new Color(r, g, b, alpha);
        }
    }

    // 🔥 CALLED FROM YOUR GUI
    public void toggleBlock(Block block) {
        if (selectedBlocks.contains(block)) {
            selectedBlocks.remove(block);
        } else {
            selectedBlocks.add(block);
        }
    }

    public Set<Block> getSelectedBlocks() {
        return selectedBlocks;
    }

    public BlockData getBlockData(Block block) {
        return blockDataMap.get(block);
    }

    // 🔥 DISPLAY TEXT: "Blocks: X Blocks"
    public String getBlockCountText() {
        return "Blocks: " + selectedBlocks.size() + " Blocks";
    }

    @EventListener
    public void onRender3D(Render3DEvent event) {

        if (System.currentTimeMillis() - lastScan > 500) {
            scanBlocks();
            lastScan = System.currentTimeMillis();
        }

        renderBlocks(event);
    }

    // 🔥 ONLY SCAN SELECTED BLOCKS
    private void scanBlocks() {

        blockCache.values().forEach(List::clear);

        if (selectedBlocks.isEmpty()) return;

        for (WorldChunk chunk : BlockUtil.getLoadedChunks().toList()) {

            int startX = chunk.getPos().getStartX();
            int startZ = chunk.getPos().getStartZ();

            BlockPos min = new BlockPos(startX, mc.world.getBottomY(), startZ);
            BlockPos max = new BlockPos(startX + 15, mc.world.getTopY() - 1, startZ + 15);

            for (BlockPos pos : BlockPos.iterate(min, max)) {

                BlockState state = mc.world.getBlockState(pos);
                Block block = state.getBlock();

                if (!selectedBlocks.contains(block)) continue;

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

        for (Block block : selectedBlocks) {

            List<BlockPos> positions = blockCache.get(block);
            if (positions == null) continue;

            BlockData data = blockDataMap.get(block);
            Color color = data.getColor(alpha.getIntValue());

            for (BlockPos pos : positions) {

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

                if (data.tracer && mc.crosshairTarget != null) {
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
