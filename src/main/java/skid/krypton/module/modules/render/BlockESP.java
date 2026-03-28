package skid.krypton.module.modules.render;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.*;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.WorldChunk;
import skid.krypton.event.EventListener;
import skid.krypton.event.events.Render3DEvent;
import skid.krypton.module.Category;
import skid.krypton.module.Module;
import skid.krypton.module.setting.BooleanSetting;
import skid.krypton.module.setting.ModeSetting;
import skid.krypton.module.setting.NumberSetting;
import skid.krypton.utils.BlockUtil;
import skid.krypton.utils.EncryptedString;
import skid.krypton.utils.RenderUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public final class BlockESP extends Module {
    
    // General Settings
    private final NumberSetting alpha = new NumberSetting(EncryptedString.of("Alpha"), 1, 255, 125, 1);
    private final BooleanSetting tracers = new BooleanSetting(EncryptedString.of("Tracers"), false);
    private final BooleanSetting outline = new BooleanSetting(EncryptedString.of("Outline"), true);
    
    // Block Categories
    private final BooleanSetting ores = new BooleanSetting(EncryptedString.of("Ores"), true);
    private final BooleanSetting storage = new BooleanSetting(EncryptedString.of("Storage"), true);
    private final BooleanSetting utility = new BooleanSetting(EncryptedString.of("Utility"), true);
    private final BooleanSetting redstone = new BooleanSetting(EncryptedString.of("Redstone"), true);
    
    // Individual Ore Toggles
    private final BooleanSetting diamondOre = new BooleanSetting(EncryptedString.of("Diamond Ore"), true);
    private final BooleanSetting goldOre = new BooleanSetting(EncryptedString.of("Gold Ore"), true);
    private final BooleanSetting ironOre = new BooleanSetting(EncryptedString.of("Iron Ore"), true);
    private final BooleanSetting coalOre = new BooleanSetting(EncryptedString.of("Coal Ore"), true);
    private final BooleanSetting emeraldOre = new BooleanSetting(EncryptedString.of("Emerald Ore"), true);
    private final BooleanSetting lapisOre = new BooleanSetting(EncryptedString.of("Lapis Ore"), true);
    private final BooleanSetting redstoneOre = new BooleanSetting(EncryptedString.of("Redstone Ore"), true);
    private final BooleanSetting netherQuartzOre = new BooleanSetting(EncryptedString.of("Nether Quartz Ore"), true);
    private final BooleanSetting ancientDebris = new BooleanSetting(EncryptedString.of("Ancient Debris"), true);
    
    // Storage Toggles
    private final BooleanSetting chests = new BooleanSetting(EncryptedString.of("Chests"), true);
    private final BooleanSetting enderChests = new BooleanSetting(EncryptedString.of("Ender Chests"), true);
    private final BooleanSetting shulkers = new BooleanSetting(EncryptedString.of("Shulkers"), true);
    private final BooleanSetting barrels = new BooleanSetting(EncryptedString.of("Barrels"), true);
    
    // Utility Toggles
    private final BooleanSetting furnaces = new BooleanSetting(EncryptedString.of("Furnaces"), true);
    private final BooleanSetting spawners = new BooleanSetting(EncryptedString.of("Spawners"), true);
    private final BooleanSetting enchantTables = new BooleanSetting(EncryptedString.of("Enchant Tables"), true);
    private final BooleanSetting beacons = new BooleanSetting(EncryptedString.of("Beacons"), true);
    
    // Redstone Toggles
    private final BooleanSetting pistons = new BooleanSetting(EncryptedString.of("Pistons"), true);
    
    // Custom Block Selection (using ModeSetting with String values)
    private final ModeSetting customBlock1 = new ModeSetting(EncryptedString.of("Custom Block 1"), "None", 
            new String[]{"None", "Diamond Ore", "Gold Ore", "Iron Ore", "Chest", "Spawner"});
    private final NumberSetting customRed1 = new NumberSetting(EncryptedString.of("Red 1"), 0, 255, 255, 1);
    private final NumberSetting customGreen1 = new NumberSetting(EncryptedString.of("Green 1"), 0, 255, 0, 1);
    private final NumberSetting customBlue1 = new NumberSetting(EncryptedString.of("Blue 1"), 0, 255, 0, 1);
    
    private final ModeSetting customBlock2 = new ModeSetting(EncryptedString.of("Custom Block 2"), "None", 
            new String[]{"None", "Emerald Ore", "Lapis Ore", "Redstone Ore", "Ender Chest", "Shulker Box"});
    private final NumberSetting customRed2 = new NumberSetting(EncryptedString.of("Red 2"), 0, 255, 0, 1);
    private final NumberSetting customGreen2 = new NumberSetting(EncryptedString.of("Green 2"), 0, 255, 255, 1);
    private final NumberSetting customBlue2 = new NumberSetting(EncryptedString.of("Blue 2"), 0, 255, 0, 1);
    
    private final ModeSetting customBlock3 = new ModeSetting(EncryptedString.of("Custom Block 3"), "None", 
            new String[]{"None", "Nether Quartz Ore", "Ancient Debris", "Barrel", "Furnace", "Beacon"});
    private final NumberSetting customRed3 = new NumberSetting(EncryptedString.of("Red 3"), 0, 255, 0, 1);
    private final NumberSetting customGreen3 = new NumberSetting(EncryptedString.of("Green 3"), 0, 255, 0, 1);
    private final NumberSetting customBlue3 = new NumberSetting(EncryptedString.of("Blue 3"), 0, 255, 255, 1);
    
    public BlockESP() {
        super(EncryptedString.of("Block ESP"), EncryptedString.of("Renders blocks through walls"), -1, Category.RENDER);
        
        // Add general settings
        this.addSettings(this.alpha, this.tracers, this.outline);
        this.addSettings(this.ores, this.storage, this.utility, this.redstone);
        
        // Add ore settings
        this.addSettings(this.diamondOre, this.goldOre, this.ironOre, this.coalOre, this.emeraldOre,
                        this.lapisOre, this.redstoneOre, this.netherQuartzOre, this.ancientDebris);
        
        // Add storage settings
        this.addSettings(this.chests, this.enderChests, this.shulkers, this.barrels);
        
        // Add utility settings
        this.addSettings(this.furnaces, this.spawners, this.enchantTables, this.beacons);
        
        // Add redstone settings
        this.addSettings(this.pistons);
        
        // Add custom blocks
        this.addSettings(this.customBlock1, this.customRed1, this.customGreen1, this.customBlue1);
        this.addSettings(this.customBlock2, this.customRed2, this.customGreen2, this.customBlue2);
        this.addSettings(this.customBlock3, this.customRed3, this.customGreen3, this.customBlue3);
    }
    
    private Color getBlockColor(Block block, BlockPos pos) {
        BlockEntity blockEntity = mc.world.getBlockEntity(pos);
        int a = alpha.getIntValue();
        
        // Custom Block 1
        if (!customBlock1.getValue().equals("None")) {
            Block customBlock = getBlockFromName(customBlock1.getValue());
            if (customBlock != null && block == customBlock) {
                return new Color(customRed1.getIntValue(), customGreen1.getIntValue(), customBlue1.getIntValue(), a);
            }
        }
        
        // Custom Block 2
        if (!customBlock2.getValue().equals("None")) {
            Block customBlock = getBlockFromName(customBlock2.getValue());
            if (customBlock != null && block == customBlock) {
                return new Color(customRed2.getIntValue(), customGreen2.getIntValue(), customBlue2.getIntValue(), a);
            }
        }
        
        // Custom Block 3
        if (!customBlock3.getValue().equals("None")) {
            Block customBlock = getBlockFromName(customBlock3.getValue());
            if (customBlock != null && block == customBlock) {
                return new Color(customRed3.getIntValue(), customGreen3.getIntValue(), customBlue3.getIntValue(), a);
            }
        }
        
        // Ores
        if (ores.getValue()) {
            if (block == Blocks.DIAMOND_ORE && diamondOre.getValue()) return new Color(100, 200, 255, a);
            if (block == Blocks.GOLD_ORE && goldOre.getValue()) return new Color(255, 200, 0, a);
            if (block == Blocks.IRON_ORE && ironOre.getValue()) return new Color(200, 200, 200, a);
            if (block == Blocks.COAL_ORE && coalOre.getValue()) return new Color(50, 50, 50, a);
            if (block == Blocks.EMERALD_ORE && emeraldOre.getValue()) return new Color(0, 255, 0, a);
            if (block == Blocks.LAPIS_ORE && lapisOre.getValue()) return new Color(0, 0, 255, a);
            if (block == Blocks.REDSTONE_ORE && redstoneOre.getValue()) return new Color(255, 0, 0, a);
            if (block == Blocks.NETHER_QUARTZ_ORE && netherQuartzOre.getValue()) return new Color(255, 255, 255, a);
            if (block == Blocks.ANCIENT_DEBRIS && ancientDebris.getValue()) return new Color(100, 50, 0, a);
        }
        
        // Storage
        if (storage.getValue()) {
            if (blockEntity instanceof ChestBlockEntity && chests.getValue()) return new Color(200, 100, 0, a);
            if (blockEntity instanceof EnderChestBlockEntity && enderChests.getValue()) return new Color(100, 0, 200, a);
            if (blockEntity instanceof ShulkerBoxBlockEntity && shulkers.getValue()) return new Color(150, 0, 200, a);
            if (blockEntity instanceof BarrelBlockEntity && barrels.getValue()) return new Color(255, 100, 100, a);
        }
        
        // Utility
        if (utility.getValue()) {
            if (blockEntity instanceof FurnaceBlockEntity && furnaces.getValue()) return new Color(100, 100, 100, a);
            if (blockEntity instanceof MobSpawnerBlockEntity && spawners.getValue()) return new Color(150, 100, 200, a);
            if (blockEntity instanceof EnchantingTableBlockEntity && enchantTables.getValue()) return new Color(100, 100, 255, a);
            if (blockEntity instanceof BeaconBlockEntity && beacons.getValue()) return new Color(0, 200, 200, a);
        }
        
        // Redstone
        if (redstone.getValue()) {
            if ((block == Blocks.PISTON || block == Blocks.STICKY_PISTON) && pistons.getValue()) {
                return new Color(100, 200, 100, a);
            }
        }
        
        return null;
    }
    
    private Block getBlockFromName(String name) {
        switch (name) {
            case "Diamond Ore": return Blocks.DIAMOND_ORE;
            case "Gold Ore": return Blocks.GOLD_ORE;
            case "Iron Ore": return Blocks.IRON_ORE;
            case "Coal Ore": return Blocks.COAL_ORE;
            case "Emerald Ore": return Blocks.EMERALD_ORE;
            case "Lapis Ore": return Blocks.LAPIS_ORE;
            case "Redstone Ore": return Blocks.REDSTONE_ORE;
            case "Nether Quartz Ore": return Blocks.NETHER_QUARTZ_ORE;
            case "Ancient Debris": return Blocks.ANCIENT_DEBRIS;
            case "Chest": return Blocks.CHEST;
            case "Ender Chest": return Blocks.ENDER_CHEST;
            case "Shulker Box": return Blocks.SHULKER_BOX;
            case "Barrel": return Blocks.BARREL;
            case "Furnace": return Blocks.FURNACE;
            case "Spawner": return Blocks.SPAWNER;
            case "Enchant Table": return Blocks.ENCHANTING_TABLE;
            case "Beacon": return Blocks.BEACON;
            default: return null;
        }
    }
    
    @EventListener
    public void onRender3D(final Render3DEvent event) {
        renderBlocks(event);
    }
    
    private void renderBlocks(Render3DEvent event) {
        Camera cam = RenderUtils.getCamera();
        if (cam == null) return;
        
        Vec3d camPos = RenderUtils.getCameraPos();
        MatrixStack matrices = event.matrixStack;
        matrices.push();
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(cam.getPitch()));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(cam.getYaw() + 180.0f));
        matrices.translate(-camPos.x, -camPos.y, -camPos.z);
        
        for (WorldChunk chunk : BlockUtil.getLoadedChunks().toList()) {
            for (BlockPos blockPos : chunk.getBlockEntityPositions()) {
                Block block = mc.world.getBlockState(blockPos).getBlock();
                Color color = getBlockColor(block, blockPos);
                
                if (color == null) continue;
                
                // Render filled box
                float x1 = blockPos.getX() + 0.1f;
                float y1 = blockPos.getY() + 0.05f;
                float z1 = blockPos.getZ() + 0.1f;
                float x2 = blockPos.getX() + 0.9f;
                float y2 = blockPos.getY() + 0.85f;
                float z2 = blockPos.getZ() + 0.9f;
                
                RenderUtils.renderFilledBox(matrices, x1, y1, z1, x2, y2, z2, color);
                
                // Render outline
                if (outline.getValue()) {
                    RenderUtils.renderOutlineBox(matrices, x1, y1, z1, x2, y2, z2, new Color(color.getRed(), color.getGreen(), color.getBlue(), 255));
                }
                
                // Render tracer
                if (tracers.getValue()) {
                    RenderUtils.renderLine(matrices, new Color(color.getRed(), color.getGreen(), color.getBlue(), 255),
                        camPos, new Vec3d(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5));
                }
            }
        }
        
        matrices.pop();
    }
}
