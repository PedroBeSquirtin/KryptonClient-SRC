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
import skid.krypton.module.setting.ColorSetting;
import skid.krypton.module.setting.ModeSetting;
import skid.krypton.module.setting.NumberSetting;
import skid.krypton.utils.BlockUtil;
import skid.krypton.utils.EncryptedString;
import skid.krypton.utils.RenderUtils;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public final class BlockESP extends Module {
    
    // General Settings
    private final NumberSetting alpha = new NumberSetting(EncryptedString.of("Alpha"), 50, 255, 150, 1);
    private final BooleanSetting tracers = new BooleanSetting(EncryptedString.of("Tracers"), false).setDescription(EncryptedString.of("Draws a line from player to blocks"));
    private final BooleanSetting outline = new BooleanSetting(EncryptedString.of("Outline"), true).setDescription(EncryptedString.of("Draws outline around blocks"));
    
    // Block Selection Mode
    private final ModeSetting selectionMode = new ModeSetting(EncryptedString.of("Selection Mode"), "All Blocks", new String[]{"All Blocks", "Selected Blocks", "Whitelist", "Blacklist"});
    
    // Custom Block Selection (using block IDs)
    private final ModeSetting block1 = new ModeSetting(EncryptedString.of("Block 1"), "None", getBlockList());
    private final ModeSetting block2 = new ModeSetting(EncryptedString.of("Block 2"), "None", getBlockList());
    private final ModeSetting block3 = new ModeSetting(EncryptedString.of("Block 3"), "None", getBlockList());
    private final ModeSetting block4 = new ModeSetting(EncryptedString.of("Block 4"), "None", getBlockList());
    private final ModeSetting block5 = new ModeSetting(EncryptedString.of("Block 5"), "None", getBlockList());
    private final ModeSetting block6 = new ModeSetting(EncryptedString.of("Block 6"), "None", getBlockList());
    
    // Colors for custom blocks
    private final ColorSetting color1 = new ColorSetting(EncryptedString.of("Color 1"), new Color(255, 0, 0, 150));
    private final ColorSetting color2 = new ColorSetting(EncryptedString.of("Color 2"), new Color(0, 255, 0, 150));
    private final ColorSetting color3 = new ColorSetting(EncryptedString.of("Color 3"), new Color(0, 0, 255, 150));
    private final ColorSetting color4 = new ColorSetting(EncryptedString.of("Color 4"), new Color(255, 255, 0, 150));
    private final ColorSetting color5 = new ColorSetting(EncryptedString.of("Color 5"), new Color(255, 0, 255, 150));
    private final ColorSetting color6 = new ColorSetting(EncryptedString.of("Color 6"), new Color(0, 255, 255, 150));
    
    // Block Categories
    private final BooleanSetting ores = new BooleanSetting(EncryptedString.of("Ores"), true);
    private final BooleanSetting storage = new BooleanSetting(EncryptedString.of("Storage Blocks"), true);
    private final BooleanSetting utility = new BooleanSetting(EncryptedString.of("Utility Blocks"), true);
    private final BooleanSetting redstone = new BooleanSetting(EncryptedString.of("Redstone"), true);
    private final BooleanSetting plants = new BooleanSetting(EncryptedString.of("Plants"), false);
    private final BooleanSetting liquids = new BooleanSetting(EncryptedString.of("Liquids"), false);
    
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
    private final BooleanSetting netherGoldOre = new BooleanSetting(EncryptedString.of("Nether Gold Ore"), true);
    
    // Storage Colors
    private final ColorSetting chestColor = new ColorSetting(EncryptedString.of("Chest Color"), new Color(200, 100, 0, 150));
    private final ColorSetting enderChestColor = new ColorSetting(EncryptedString.of("Ender Chest Color"), new Color(100, 0, 200, 150));
    private final ColorSetting shulkerColor = new ColorSetting(EncryptedString.of("Shulker Color"), new Color(150, 0, 200, 150));
    private final ColorSetting barrelColor = new ColorSetting(EncryptedString.of("Barrel Color"), new Color(255, 100, 100, 150));
    
    // Utility Colors
    private final ColorSetting furnaceColor = new ColorSetting(EncryptedString.of("Furnace Color"), new Color(100, 100, 100, 150));
    private final ColorSetting spawnerColor = new ColorSetting(EncryptedString.of("Spawner Color"), new Color(150, 100, 200, 150));
    private final ColorSetting enchantColor = new ColorSetting(EncryptedString.of("Enchant Table Color"), new Color(100, 100, 255, 150));
    private final ColorSetting beaconColor = new ColorSetting(EncryptedString.of("Beacon Color"), new Color(0, 200, 200, 150));
    private final ColorSetting anvilColor = new ColorSetting(EncryptedString.of("Anvil Color"), new Color(150, 150, 150, 150));
    
    // Redstone Colors
    private final ColorSetting pistonColor = new ColorSetting(EncryptedString.of("Piston Color"), new Color(100, 200, 100, 150));
    private final ColorSetting dropperColor = new ColorSetting(EncryptedString.of("Dropper Color"), new Color(150, 150, 150, 150));
    private final ColorSetting dispenserColor = new ColorSetting(EncryptedString.of("Dispenser Color"), new Color(150, 150, 150, 150));
    private final ColorSetting observerColor = new ColorSetting(EncryptedString.of("Observer Color"), new Color(200, 200, 100, 150));
    
    // Map to store custom block colors
    private final Map<Block, Color> customBlockColors = new HashMap<>();
    
    public BlockESP() {
        super(EncryptedString.of("Block ESP"), EncryptedString.of("Highlights blocks of your choice"), -1, Category.RENDER);
        
        // General Settings
        this.addSettings(this.alpha, this.tracers, this.outline, this.selectionMode);
        
        // Custom Block Selection
        this.addSettings(this.block1, this.block2, this.block3, this.block4, this.block5, this.block6);
        this.addSettings(this.color1, this.color2, this.color3, this.color4, this.color5, this.color6);
        
        // Block Categories
        this.addSettings(this.ores, this.storage, this.utility, this.redstone, this.plants, this.liquids);
        
        // Individual Ores
        this.addSettings(this.diamondOre, this.goldOre, this.ironOre, this.coalOre, this.emeraldOre, 
                        this.lapisOre, this.redstoneOre, this.netherQuartzOre, this.ancientDebris, this.netherGoldOre);
        
        // Storage
        this.addSettings(this.chestColor, this.enderChestColor, this.shulkerColor, this.barrelColor);
        
        // Utility
        this.addSettings(this.furnaceColor, this.spawnerColor, this.enchantColor, this.beaconColor, this.anvilColor);
        
        // Redstone
        this.addSettings(this.pistonColor, this.dropperColor, this.dispenserColor, this.observerColor);
        
        initCustomBlockColors();
    }
    
    private String[] getBlockList() {
        return new String[]{
            "None", "Diamond Ore", "Gold Ore", "Iron Ore", "Coal Ore", "Emerald Ore", 
            "Lapis Ore", "Redstone Ore", "Nether Quartz Ore", "Ancient Debris", "Nether Gold Ore",
            "Chest", "Ender Chest", "Shulker Box", "Barrel", "Furnace", "Blast Furnace", 
            "Smoker", "Spawner", "Enchanting Table", "Beacon", "Anvil", "Piston", "Sticky Piston",
            "Dropper", "Dispenser", "Observer", "Hopper", "Note Block", "Jukebox", "Brewing Stand",
            "Cauldron", "Flower Pot", "Beehive", "Bee Nest", "Composter", "Loom", "Grindstone",
            "Stonecutter", "Cartography Table", "Smithing Table", "Lectern", "Bell", "Conduit"
        };
    }
    
    private void initCustomBlockColors() {
        customBlockColors.put(Blocks.DIAMOND_ORE, new Color(100, 200, 255, 150));
        customBlockColors.put(Blocks.GOLD_ORE, new Color(255, 200, 0, 150));
        customBlockColors.put(Blocks.IRON_ORE, new Color(200, 200, 200, 150));
        customBlockColors.put(Blocks.COAL_ORE, new Color(50, 50, 50, 150));
        customBlockColors.put(Blocks.EMERALD_ORE, new Color(0, 255, 0, 150));
        customBlockColors.put(Blocks.LAPIS_ORE, new Color(0, 0, 255, 150));
        customBlockColors.put(Blocks.REDSTONE_ORE, new Color(255, 0, 0, 150));
        customBlockColors.put(Blocks.NETHER_QUARTZ_ORE, new Color(255, 255, 255, 150));
        customBlockColors.put(Blocks.ANCIENT_DEBRIS, new Color(100, 50, 0, 150));
        customBlockColors.put(Blocks.NETHER_GOLD_ORE, new Color(255, 150, 0, 150));
    }
    
    private Block getBlockFromName(String name) {
        if (name.equals("None")) return null;
        
        try {
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
                case "Chest": return Blocks.CHEST;
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
                case "Note Block": return Blocks.NOTE_BLOCK;
                case "Jukebox": return Blocks.JUKEBOX;
                case "Brewing Stand": return Blocks.BREWING_STAND;
                case "Cauldron": return Blocks.CAULDRON;
                case "Flower Pot": return Blocks.FLOWER_POT;
                case "Beehive": return Blocks.BEEHIVE;
                case "Bee Nest": return Blocks.BEE_NEST;
                case "Composter": return Blocks.COMPOSTER;
                case "Loom": return Blocks.LOOM;
                case "Grindstone": return Blocks.GRINDSTONE;
                case "Stonecutter": return Blocks.STONECUTTER;
                case "Cartography Table": return Blocks.CARTOGRAPHY_TABLE;
                case "Smithing Table": return Blocks.SMITHING_TABLE;
                case "Lectern": return Blocks.LECTERN;
                case "Bell": return Blocks.BELL;
                case "Conduit": return Blocks.CONDUIT;
                default: return null;
            }
        } catch (Exception e) {
            return null;
        }
    }
    
    private Color getBlockColor(Block block, BlockPos pos) {
        BlockEntity blockEntity = mc.world.getBlockEntity(pos);
        
        // Check custom selected blocks first
        if (selectionMode.getValue().equals("Selected Blocks")) {
            Block selected1 = getBlockFromName(block1.getValue());
            Block selected2 = getBlockFromName(block2.getValue());
            Block selected3 = getBlockFromName(block3.getValue());
            Block selected4 = getBlockFromName(block4.getValue());
            Block selected5 = getBlockFromName(block5.getValue());
            Block selected6 = getBlockFromName(block6.getValue());
            
            if (block == selected1) return color1.getColor();
            if (block == selected2) return color2.getColor();
            if (block == selected3) return color3.getColor();
            if (block == selected4) return color4.getColor();
            if (block == selected5) return color5.getColor();
            if (block == selected6) return color6.getColor();
            return null;
        }
        
        // Check block entities
        if (blockEntity != null) {
            if (blockEntity instanceof ChestBlockEntity && !(blockEntity instanceof TrappedChestBlockEntity)) return chestColor.getColor();
            if (blockEntity instanceof TrappedChestBlockEntity) return chestColor.getColor();
            if (blockEntity instanceof EnderChestBlockEntity) return enderChestColor.getColor();
            if (blockEntity instanceof ShulkerBoxBlockEntity) return shulkerColor.getColor();
            if (blockEntity instanceof BarrelBlockEntity) return barrelColor.getColor();
            if (blockEntity instanceof FurnaceBlockEntity) return furnaceColor.getColor();
            if (blockEntity instanceof MobSpawnerBlockEntity) return spawnerColor.getColor();
            if (blockEntity instanceof EnchantingTableBlockEntity) return enchantColor.getColor();
            if (blockEntity instanceof BeaconBlockEntity) return beaconColor.getColor();
        }
        
        // Check ores
        if (ores.getValue()) {
            if (block == Blocks.DIAMOND_ORE && diamondOre.getValue()) return customBlockColors.getOrDefault(Blocks.DIAMOND_ORE, new Color(100, 200, 255, alpha.getIntValue()));
            if (block == Blocks.GOLD_ORE && goldOre.getValue()) return customBlockColors.getOrDefault(Blocks.GOLD_ORE, new Color(255, 200, 0, alpha.getIntValue()));
            if (block == Blocks.IRON_ORE && ironOre.getValue()) return customBlockColors.getOrDefault(Blocks.IRON_ORE, new Color(200, 200, 200, alpha.getIntValue()));
            if (block == Blocks.COAL_ORE && coalOre.getValue()) return customBlockColors.getOrDefault(Blocks.COAL_ORE, new Color(50, 50, 50, alpha.getIntValue()));
            if (block == Blocks.EMERALD_ORE && emeraldOre.getValue()) return customBlockColors.getOrDefault(Blocks.EMERALD_ORE, new Color(0, 255, 0, alpha.getIntValue()));
            if (block == Blocks.LAPIS_ORE && lapisOre.getValue()) return customBlockColors.getOrDefault(Blocks.LAPIS_ORE, new Color(0, 0, 255, alpha.getIntValue()));
            if (block == Blocks.REDSTONE_ORE && redstoneOre.getValue()) return customBlockColors.getOrDefault(Blocks.REDSTONE_ORE, new Color(255, 0, 0, alpha.getIntValue()));
            if (block == Blocks.NETHER_QUARTZ_ORE && netherQuartzOre.getValue()) return customBlockColors.getOrDefault(Blocks.NETHER_QUARTZ_ORE, new Color(255, 255, 255, alpha.getIntValue()));
            if (block == Blocks.ANCIENT_DEBRIS && ancientDebris.getValue()) return customBlockColors.getOrDefault(Blocks.ANCIENT_DEBRIS, new Color(100, 50, 0, alpha.getIntValue()));
            if (block == Blocks.NETHER_GOLD_ORE && netherGoldOre.getValue()) return customBlockColors.getOrDefault(Blocks.NETHER_GOLD_ORE, new Color(255, 150, 0, alpha.getIntValue()));
        }
        
        // Check pistons
        if (redstone.getValue() && (block == Blocks.PISTON || block == Blocks.STICKY_PISTON)) return pistonColor.getColor();
        
        // Check other redstone components
        if (redstone.getValue()) {
            if (block == Blocks.DROPPER) return dropperColor.getColor();
            if (block == Blocks.DISPENSER) return dispenserColor.getColor();
            if (block == Blocks.OBSERVER) return observerColor.getColor();
        }
        
        return null;
    }
    
    private boolean shouldRenderBlock(Block block) {
        if (selectionMode.getValue().equals("All Blocks")) return true;
        
        if (selectionMode.getValue().equals("Selected Blocks")) {
            Block selected1 = getBlockFromName(block1.getValue());
            Block selected2 = getBlockFromName(block2.getValue());
            Block selected3 = getBlockFromName(block3.getValue());
            Block selected4 = getBlockFromName(block4.getValue());
            Block selected5 = getBlockFromName(block5.getValue());
            Block selected6 = getBlockFromName(block6.getValue());
            
            return block == selected1 || block == selected2 || block == selected3 || 
                   block == selected4 || block == selected5 || block == selected6;
        }
        
        return true;
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
                
                if (!shouldRenderBlock(block)) continue;
                
                Color color = getBlockColor(block, blockPos);
                if (color == null) continue;
                
                // Render filled box
                RenderUtils.renderFilledBox(matrices, 
                    blockPos.getX() + 0.05, blockPos.getY() + 0.05, blockPos.getZ() + 0.05,
                    blockPos.getX() + 0.95, blockPos.getY() + 0.95, blockPos.getZ() + 0.95,
                    new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha.getIntValue()));
                
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
