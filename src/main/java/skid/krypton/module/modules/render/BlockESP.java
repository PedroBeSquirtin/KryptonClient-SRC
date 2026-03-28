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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class BlockESP extends Module {
    
    // ========== GENERAL SETTINGS ==========
    private final NumberSetting alpha = new NumberSetting(EncryptedString.of("Alpha"), 1, 255, 150, 1);
    private final BooleanSetting tracers = new BooleanSetting(EncryptedString.of("Tracers"), false);
    private final BooleanSetting outline = new BooleanSetting(EncryptedString.of("Outline"), true);
    private final BooleanSetting filledBox = new BooleanSetting(EncryptedString.of("Filled Box"), true);
    
    // ========== BLOCK SELECTION MODES ==========
    private final ModeSetting selectionMode = new ModeSetting(EncryptedString.of("Selection Mode"), "All Blocks", 
            new String[]{"All Blocks", "Ores", "Storage", "Utility", "Redstone", "Custom List"});
    
    // ========== CUSTOM BLOCK SELECTION (up to 10 custom blocks) ==========
    private final ModeSetting customBlock1 = new ModeSetting(EncryptedString.of("Custom Block 1"), "None", getAllBlockNames());
    private final ModeSetting customBlock2 = new ModeSetting(EncryptedString.of("Custom Block 2"), "None", getAllBlockNames());
    private final ModeSetting customBlock3 = new ModeSetting(EncryptedString.of("Custom Block 3"), "None", getAllBlockNames());
    private final ModeSetting customBlock4 = new ModeSetting(EncryptedString.of("Custom Block 4"), "None", getAllBlockNames());
    private final ModeSetting customBlock5 = new ModeSetting(EncryptedString.of("Custom Block 5"), "None", getAllBlockNames());
    private final ModeSetting customBlock6 = new ModeSetting(EncryptedString.of("Custom Block 6"), "None", getAllBlockNames());
    private final ModeSetting customBlock7 = new ModeSetting(EncryptedString.of("Custom Block 7"), "None", getAllBlockNames());
    private final ModeSetting customBlock8 = new ModeSetting(EncryptedString.of("Custom Block 8"), "None", getAllBlockNames());
    private final ModeSetting customBlock9 = new ModeSetting(EncryptedString.of("Custom Block 9"), "None", getAllBlockNames());
    private final ModeSetting customBlock10 = new ModeSetting(EncryptedString.of("Custom Block 10"), "None", getAllBlockNames());
    
    // ========== CUSTOM BLOCK COLORS (RGB for each) ==========
    private final NumberSetting block1Red = new NumberSetting(EncryptedString.of("Block 1 Red"), 0, 255, 255, 1);
    private final NumberSetting block1Green = new NumberSetting(EncryptedString.of("Block 1 Green"), 0, 255, 0, 1);
    private final NumberSetting block1Blue = new NumberSetting(EncryptedString.of("Block 1 Blue"), 0, 255, 0, 1);
    
    private final NumberSetting block2Red = new NumberSetting(EncryptedString.of("Block 2 Red"), 0, 255, 0, 1);
    private final NumberSetting block2Green = new NumberSetting(EncryptedString.of("Block 2 Green"), 0, 255, 255, 1);
    private final NumberSetting block2Blue = new NumberSetting(EncryptedString.of("Block 2 Blue"), 0, 255, 0, 1);
    
    private final NumberSetting block3Red = new NumberSetting(EncryptedString.of("Block 3 Red"), 0, 255, 0, 1);
    private final NumberSetting block3Green = new NumberSetting(EncryptedString.of("Block 3 Green"), 0, 255, 0, 1);
    private final NumberSetting block3Blue = new NumberSetting(EncryptedString.of("Block 3 Blue"), 0, 255, 255, 1);
    
    private final NumberSetting block4Red = new NumberSetting(EncryptedString.of("Block 4 Red"), 0, 255, 255, 1);
    private final NumberSetting block4Green = new NumberSetting(EncryptedString.of("Block 4 Green"), 0, 255, 255, 1);
    private final NumberSetting block4Blue = new NumberSetting(EncryptedString.of("Block 4 Blue"), 0, 255, 0, 1);
    
    private final NumberSetting block5Red = new NumberSetting(EncryptedString.of("Block 5 Red"), 0, 255, 255, 1);
    private final NumberSetting block5Green = new NumberSetting(EncryptedString.of("Block 5 Green"), 0, 255, 0, 1);
    private final NumberSetting block5Blue = new NumberSetting(EncryptedString.of("Block 5 Blue"), 0, 255, 255, 1);
    
    private final NumberSetting block6Red = new NumberSetting(EncryptedString.of("Block 6 Red"), 0, 255, 255, 1);
    private final NumberSetting block6Green = new NumberSetting(EncryptedString.of("Block 6 Green"), 0, 255, 255, 1);
    private final NumberSetting block6Blue = new NumberSetting(EncryptedString.of("Block 6 Blue"), 0, 255, 255, 1);
    
    private final NumberSetting block7Red = new NumberSetting(EncryptedString.of("Block 7 Red"), 0, 255, 255, 1);
    private final NumberSetting block7Green = new NumberSetting(EncryptedString.of("Block 7 Green"), 0, 255, 0, 1);
    private final NumberSetting block7Blue = new NumberSetting(EncryptedString.of("Block 7 Blue"), 0, 255, 255, 1);
    
    private final NumberSetting block8Red = new NumberSetting(EncryptedString.of("Block 8 Red"), 0, 255, 255, 1);
    private final NumberSetting block8Green = new NumberSetting(EncryptedString.of("Block 8 Green"), 0, 255, 255, 1);
    private final NumberSetting block8Blue = new NumberSetting(EncryptedString.of("Block 8 Blue"), 0, 255, 0, 1);
    
    private final NumberSetting block9Red = new NumberSetting(EncryptedString.of("Block 9 Red"), 0, 255, 255, 1);
    private final NumberSetting block9Green = new NumberSetting(EncryptedString.of("Block 9 Green"), 0, 255, 0, 1);
    private final NumberSetting block9Blue = new NumberSetting(EncryptedString.of("Block 9 Blue"), 0, 255, 255, 1);
    
    private final NumberSetting block10Red = new NumberSetting(EncryptedString.of("Block 10 Red"), 0, 255, 255, 1);
    private final NumberSetting block10Green = new NumberSetting(EncryptedString.of("Block 10 Green"), 0, 255, 255, 1);
    private final NumberSetting block10Blue = new NumberSetting(EncryptedString.of("Block 10 Blue"), 0, 255, 0, 1);
    
    // ========== ORE SETTINGS ==========
    private final BooleanSetting diamondOre = new BooleanSetting(EncryptedString.of("Diamond Ore"), true);
    private final BooleanSetting goldOre = new BooleanSetting(EncryptedString.of("Gold Ore"), true);
    private final BooleanSetting ironOre = new BooleanSetting(EncryptedString.of("Iron Ore"), true);
    private final BooleanSetting coalOre = new BooleanSetting(EncryptedString.of("Coal Ore"), true);
    private final BooleanSetting emeraldOre = new BooleanSetting(EncryptedString.of("Emerald Ore"), true);
    private final BooleanSetting lapisOre = new BooleanSetting(EncryptedString.of("Lapis Ore"), true);
    private final BooleanSetting redstoneOre = new BooleanSetting(EncryptedString.of("Redstone Ore"), true);
    private final BooleanSetting netherQuartzOre = new BooleanSetting(EncryptedString.of("Nether Quartz Ore"), true);
    private final BooleanSetting ancientDebris = new BooleanSetting(EncryptedString.of("Ancient Debris"), true);
    private final BooleanSetting netherGoldOre = new BooleanSetting(EncryptedString.of("Nether Gold Ore"), true);
    private final BooleanSetting copperOre = new BooleanSetting(EncryptedString.of("Copper Ore"), true);
    
    // ========== STORAGE SETTINGS ==========
    private final BooleanSetting chests = new BooleanSetting(EncryptedString.of("Chests"), true);
    private final BooleanSetting trappedChests = new BooleanSetting(EncryptedString.of("Trapped Chests"), true);
    private final BooleanSetting enderChests = new BooleanSetting(EncryptedString.of("Ender Chests"), true);
    private final BooleanSetting shulkerBoxes = new BooleanSetting(EncryptedString.of("Shulker Boxes"), true);
    private final BooleanSetting barrels = new BooleanSetting(EncryptedString.of("Barrels"), true);
    
    // ========== UTILITY SETTINGS ==========
    private final BooleanSetting furnaces = new BooleanSetting(EncryptedString.of("Furnaces"), true);
    private final BooleanSetting blastFurnaces = new BooleanSetting(EncryptedString.of("Blast Furnaces"), true);
    private final BooleanSetting smokers = new BooleanSetting(EncryptedString.of("Smokers"), true);
    private final BooleanSetting spawners = new BooleanSetting(EncryptedString.of("Spawners"), true);
    private final BooleanSetting enchantTables = new BooleanSetting(EncryptedString.of("Enchanting Tables"), true);
    private final BooleanSetting beacons = new BooleanSetting(EncryptedString.of("Beacons"), true);
    private final BooleanSetting anvils = new BooleanSetting(EncryptedString.of("Anvils"), true);
    
    // ========== REDSTONE SETTINGS ==========
    private final BooleanSetting pistons = new BooleanSetting(EncryptedString.of("Pistons"), true);
    private final BooleanSetting stickyPistons = new BooleanSetting(EncryptedString.of("Sticky Pistons"), true);
    private final BooleanSetting droppers = new BooleanSetting(EncryptedString.of("Droppers"), true);
    private final BooleanSetting dispensers = new BooleanSetting(EncryptedString.of("Dispensers"), true);
    private final BooleanSetting observers = new BooleanSetting(EncryptedString.of("Observers"), true);
    private final BooleanSetting hoppers = new BooleanSetting(EncryptedString.of("Hoppers"), true);
    
    // Map to store custom block colors
    private final Map<String, Color> customBlockColorMap = new HashMap<>();
    
    public BlockESP() {
        super(EncryptedString.of("Block ESP"), EncryptedString.of("Highlights any blocks with custom colors"), -1, Category.RENDER);
        
        // General Settings
        this.addSettings(this.alpha, this.tracers, this.outline, this.filledBox, this.selectionMode);
        
        // Custom Block Selection
        this.addSettings(this.customBlock1, this.customBlock2, this.customBlock3, this.customBlock4, this.customBlock5,
                        this.customBlock6, this.customBlock7, this.customBlock8, this.customBlock9, this.customBlock10);
        
        // Custom Block Colors
        this.addSettings(this.block1Red, this.block1Green, this.block1Blue);
        this.addSettings(this.block2Red, this.block2Green, this.block2Blue);
        this.addSettings(this.block3Red, this.block3Green, this.block3Blue);
        this.addSettings(this.block4Red, this.block4Green, this.block4Blue);
        this.addSettings(this.block5Red, this.block5Green, this.block5Blue);
        this.addSettings(this.block6Red, this.block6Green, this.block6Blue);
        this.addSettings(this.block7Red, this.block7Green, this.block7Blue);
        this.addSettings(this.block8Red, this.block8Green, this.block8Blue);
        this.addSettings(this.block9Red, this.block9Green, this.block9Blue);
        this.addSettings(this.block10Red, this.block10Green, this.block10Blue);
        
        // Ores
        this.addSettings(this.diamondOre, this.goldOre, this.ironOre, this.coalOre, this.emeraldOre,
                        this.lapisOre, this.redstoneOre, this.netherQuartzOre, this.ancientDebris, 
                        this.netherGoldOre, this.copperOre);
        
        // Storage
        this.addSettings(this.chests, this.trappedChests, this.enderChests, this.shulkerBoxes, this.barrels);
        
        // Utility
        this.addSettings(this.furnaces, this.blastFurnaces, this.smokers, this.spawners, this.enchantTables, 
                        this.beacons, this.anvils);
        
        // Redstone
        this.addSettings(this.pistons, this.stickyPistons, this.droppers, this.dispensers, this.observers, this.hoppers);
        
        initCustomColors();
    }
    
    private void initCustomColors() {
        customBlockColorMap.put("Diamond Ore", new Color(100, 200, 255, alpha.getIntValue()));
        customBlockColorMap.put("Gold Ore", new Color(255, 200, 0, alpha.getIntValue()));
        customBlockColorMap.put("Iron Ore", new Color(200, 200, 200, alpha.getIntValue()));
        customBlockColorMap.put("Coal Ore", new Color(50, 50, 50, alpha.getIntValue()));
        customBlockColorMap.put("Emerald Ore", new Color(0, 255, 0, alpha.getIntValue()));
        customBlockColorMap.put("Lapis Ore", new Color(0, 0, 255, alpha.getIntValue()));
        customBlockColorMap.put("Redstone Ore", new Color(255, 0, 0, alpha.getIntValue()));
        customBlockColorMap.put("Nether Quartz Ore", new Color(255, 255, 255, alpha.getIntValue()));
        customBlockColorMap.put("Ancient Debris", new Color(100, 50, 0, alpha.getIntValue()));
        customBlockColorMap.put("Nether Gold Ore", new Color(255, 150, 0, alpha.getIntValue()));
        customBlockColorMap.put("Copper Ore", new Color(200, 100, 50, alpha.getIntValue()));
    }
    
    private String[] getAllBlockNames() {
        return new String[]{
            "None", "Diamond Ore", "Gold Ore", "Iron Ore", "Coal Ore", "Emerald Ore", "Lapis Ore", 
            "Redstone Ore", "Nether Quartz Ore", "Ancient Debris", "Nether Gold Ore", "Copper Ore",
            "Chest", "Trapped Chest", "Ender Chest", "Shulker Box", "Barrel", "Furnace", "Blast Furnace", 
            "Smoker", "Spawner", "Enchanting Table", "Beacon", "Anvil", "Piston", "Sticky Piston",
            "Dropper", "Dispenser", "Observer", "Hopper"
        };
    }
    
    private Block getBlockFromName(String name) {
        if (name.equals("None")) return null;
        
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
            case "Nether Gold Ore": return Blocks.NETHER_GOLD_ORE;
            case "Copper Ore": return Blocks.COPPER_ORE;
            case "Chest": return Blocks.CHEST;
            case "Trapped Chest": return Blocks.TRAPPED_CHEST;
            case "Ender Chest": return Blocks.ENDER_CHEST;
            case "Shulker Box": return Blocks.SHULKER_BOX;
            case "Barrel": return Blocks.BARREL;
            case "Furnace": return Blocks.FURNACE;
            case "Blast Furnace": return Blocks.BLAST_FURNACE;
            case "Smoker": return Blocks.SMOKER;
            case "Spawner": return Blocks.SPAWNER;
            case "Enchanting Table": return Blocks.ENCHANTING_TABLE;
            case "Beacon": return Blocks.BEACON;
            case "Anvil": return Blocks.ANVIL;
            case "Piston": return Blocks.PISTON;
            case "Sticky Piston": return Blocks.STICKY_PISTON;
            case "Dropper": return Blocks.DROPPER;
            case "Dispenser": return Blocks.DISPENSER;
            case "Observer": return Blocks.OBSERVER;
            case "Hopper": return Blocks.HOPPER;
            default: return null;
        }
    }
    
    private Color getBlockColor(Block block, BlockPos pos) {
        BlockEntity blockEntity = mc.world.getBlockEntity(pos);
        
        // Custom Block List Mode
        if (selectionMode.getValue().equals("Custom List")) {
            Block[] customBlocks = {
                getBlockFromName(customBlock1.getValue()), getBlockFromName(customBlock2.getValue()),
                getBlockFromName(customBlock3.getValue()), getBlockFromName(customBlock4.getValue()),
                getBlockFromName(customBlock5.getValue()), getBlockFromName(customBlock6.getValue()),
                getBlockFromName(customBlock7.getValue()), getBlockFromName(customBlock8.getValue()),
                getBlockFromName(customBlock9.getValue()), getBlockFromName(customBlock10.getValue())
            };
            
            Color[] customColors = {
                new Color(block1Red.getIntValue(), block1Green.getIntValue(), block1Blue.getIntValue(), alpha.getIntValue()),
                new Color(block2Red.getIntValue(), block2Green.getIntValue(), block2Blue.getIntValue(), alpha.getIntValue()),
                new Color(block3Red.getIntValue(), block3Green.getIntValue(), block3Blue.getIntValue(), alpha.getIntValue()),
                new Color(block4Red.getIntValue(), block4Green.getIntValue(), block4Blue.getIntValue(), alpha.getIntValue()),
                new Color(block5Red.getIntValue(), block5Green.getIntValue(), block5Blue.getIntValue(), alpha.getIntValue()),
                new Color(block6Red.getIntValue(), block6Green.getIntValue(), block6Blue.getIntValue(), alpha.getIntValue()),
                new Color(block7Red.getIntValue(), block7Green.getIntValue(), block7Blue.getIntValue(), alpha.getIntValue()),
                new Color(block8Red.getIntValue(), block8Green.getIntValue(), block8Blue.getIntValue(), alpha.getIntValue()),
                new Color(block9Red.getIntValue(), block9Green.getIntValue(), block9Blue.getIntValue(), alpha.getIntValue()),
                new Color(block10Red.getIntValue(), block10Green.getIntValue(), block10Blue.getIntValue(), alpha.getIntValue())
            };
            
            for (int i = 0; i < customBlocks.length; i++) {
                if (customBlocks[i] != null && block == customBlocks[i]) {
                    return customColors[i];
                }
            }
            return null;
        }
        
        // Ores Mode
        if (selectionMode.getValue().equals("Ores")) {
            if (block == Blocks.DIAMOND_ORE && diamondOre.getValue()) return new Color(100, 200, 255, alpha.getIntValue());
            if (block == Blocks.GOLD_ORE && goldOre.getValue()) return new Color(255, 200, 0, alpha.getIntValue());
            if (block == Blocks.IRON_ORE && ironOre.getValue()) return new Color(200, 200, 200, alpha.getIntValue());
            if (block == Blocks.COAL_ORE && coalOre.getValue()) return new Color(50, 50, 50, alpha.getIntValue());
            if (block == Blocks.EMERALD_ORE && emeraldOre.getValue()) return new Color(0, 255, 0, alpha.getIntValue());
            if (block == Blocks.LAPIS_ORE && lapisOre.getValue()) return new Color(0, 0, 255, alpha.getIntValue());
            if (block == Blocks.REDSTONE_ORE && redstoneOre.getValue()) return new Color(255, 0, 0, alpha.getIntValue());
            if (block == Blocks.NETHER_QUARTZ_ORE && netherQuartzOre.getValue()) return new Color(255, 255, 255, alpha.getIntValue());
            if (block == Blocks.ANCIENT_DEBRIS && ancientDebris.getValue()) return new Color(100, 50, 0, alpha.getIntValue());
            if (block == Blocks.NETHER_GOLD_ORE && netherGoldOre.getValue()) return new Color(255, 150, 0, alpha.getIntValue());
            if (block == Blocks.COPPER_ORE && copperOre.getValue()) return new Color(200, 100, 50, alpha.getIntValue());
            return null;
        }
        
        // Storage Mode
        if (selectionMode.getValue().equals("Storage")) {
            if (blockEntity instanceof ChestBlockEntity && chests.getValue()) return new Color(200, 100, 0, alpha.getIntValue());
            if (blockEntity instanceof TrappedChestBlockEntity && trappedChests.getValue()) return new Color(200, 100, 0, alpha.getIntValue());
            if (blockEntity instanceof EnderChestBlockEntity && enderChests.getValue()) return new Color(100, 0, 200, alpha.getIntValue());
            if (blockEntity instanceof ShulkerBoxBlockEntity && shulkerBoxes.getValue()) return new Color(150, 0, 200, alpha.getIntValue());
            if (blockEntity instanceof BarrelBlockEntity && barrels.getValue()) return new Color(255, 100, 100, alpha.getIntValue());
            return null;
        }
        
        // Utility Mode
        if (selectionMode.getValue().equals("Utility")) {
            if (blockEntity instanceof FurnaceBlockEntity && furnaces.getValue()) return new Color(100, 100, 100, alpha.getIntValue());
            if (blockEntity instanceof BlastFurnaceBlockEntity && blastFurnaces.getValue()) return new Color(100, 100, 100, alpha.getIntValue());
            if (blockEntity instanceof SmokerBlockEntity && smokers.getValue()) return new Color(100, 100, 100, alpha.getIntValue());
            if (blockEntity instanceof MobSpawnerBlockEntity && spawners.getValue()) return new Color(150, 100, 200, alpha.getIntValue());
            if (blockEntity instanceof EnchantingTableBlockEntity && enchantTables.getValue()) return new Color(100, 100, 255, alpha.getIntValue());
            if (blockEntity instanceof BeaconBlockEntity && beacons.getValue()) return new Color(0, 200, 200, alpha.getIntValue());
            if ((block == Blocks.ANVIL || block == Blocks.CHIPPED_ANVIL || block == Blocks.DAMAGED_ANVIL) && anvils.getValue()) {
                return new Color(150, 150, 150, alpha.getIntValue());
            }
            return null;
        }
        
        // Redstone Mode
        if (selectionMode.getValue().equals("Redstone")) {
            if ((block == Blocks.PISTON && pistons.getValue()) || (block == Blocks.STICKY_PISTON && stickyPistons.getValue())) {
                return new Color(100, 200, 100, alpha.getIntValue());
            }
            if (block == Blocks.DROPPER && droppers.getValue()) return new Color(150, 150, 150, alpha.getIntValue());
            if (block == Blocks.DISPENSER && dispensers.getValue()) return new Color(150, 150, 150, alpha.getIntValue());
            if (block == Blocks.OBSERVER && observers.getValue()) return new Color(200, 200, 100, alpha.getIntValue());
            if (block == Blocks.HOPPER && hoppers.getValue()) return new Color(100, 100, 100, alpha.getIntValue());
            return null;
        }
        
        return null;
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
            for (BlockPos blockPos : BlockUtil.getBlockPositions(chunk)) {
                Block block = mc.world.getBlockState(blockPos).getBlock();
                
                // Skip air
                if (block == Blocks.AIR) continue;
                
                Color color = getBlockColor(block, blockPos);
                if (color == null) continue;
                
                // Render filled box
                if (filledBox.getValue()) {
                    RenderUtils.renderFilledBox(matrices, 
                        blockPos.getX() + 0.05, blockPos.getY() + 0.05, blockPos.getZ() + 0.05,
                        blockPos.getX() + 0.95, blockPos.getY() + 0.95, blockPos.getZ() + 0.95,
                        new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha.getIntValue()));
                }
                
                // Render outline
                if (outline.getValue()) {
                    RenderUtils.renderOutline(matrices,
                        blockPos.getX() + 0.05, blockPos.getY() + 0.05, blockPos.getZ() + 0.05,
                        blockPos.getX() + 0.95, blockPos.getY() + 0.95, blockPos.getZ() + 0.95,
                        new Color(color.getRed(), color.getGreen(), color.getBlue(), 255));
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
