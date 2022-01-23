package org.terraform.utils;

import org.bukkit.Axis;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.block.data.Rail;
import org.bukkit.block.data.Rail.Shape;
import org.bukkit.block.data.Rotatable;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.block.data.type.Bed;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.block.data.type.Stairs;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.SimpleChunkLocation;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.main.config.TConfigOption;
import org.terraform.utils.blockdata.StairBuilder;
import org.terraform.utils.blockdata.fixers.v1_16_R1_BlockDataFixer;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.FastNoise.NoiseType;
import org.terraform.utils.version.OneOneSevenBlockHandler;
import org.terraform.utils.version.OneOneSixBlockHandler;
import org.terraform.utils.version.Version;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class BlockUtils {
    // N
    //W E
    // S
    public static final List<BlockFace> xzPlaneBlockFaces = Arrays.asList(
            BlockFace.NORTH, BlockFace.NORTH_EAST, BlockFace.EAST, BlockFace.SOUTH_EAST, BlockFace.SOUTH, BlockFace.SOUTH_WEST, BlockFace.WEST, BlockFace.NORTH_WEST
    );
    
    public static final List<Material> wetMaterials = Arrays.asList(
    		Material.WATER,
    		Material.KELP_PLANT,
    		Material.SEAGRASS,
    		Material.TALL_SEAGRASS
    );
    
    public static final BlockFace[] flatBlockFaces3x3 = {
            BlockFace.SELF, BlockFace.NORTH, BlockFace.NORTH_EAST, BlockFace.EAST, BlockFace.SOUTH_EAST, BlockFace.SOUTH, BlockFace.SOUTH_WEST, BlockFace.WEST, BlockFace.NORTH_WEST
    };

    public static final BlockFace[] BLOCK_FACES = BlockFace.values();
    public static final BlockFace[] xzDiagonalPlaneBlockFaces = {BlockFace.NORTH_EAST, BlockFace.SOUTH_EAST, BlockFace.SOUTH_WEST, BlockFace.NORTH_WEST};
    public static final Material[] stoneBricks = {Material.STONE_BRICKS, Material.MOSSY_STONE_BRICKS, Material.CRACKED_STONE_BRICKS};
    public static final Material[] stoneBrickSlabs = {Material.STONE_BRICK_SLAB, Material.MOSSY_STONE_BRICK_SLAB};
    public static final BlockFace[] directBlockFaces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};

    public static final BlockFace[][] cornerBlockFaces = {
            {BlockFace.NORTH, BlockFace.EAST},
            {BlockFace.NORTH, BlockFace.WEST},
            {BlockFace.SOUTH, BlockFace.EAST},
            {BlockFace.SOUTH, BlockFace.WEST},
    };

    public static final BlockFace[] sixBlockFaces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN};
    public static final Set<Material> stoneLike = EnumSet.of(
            Material.STONE, Material.COBBLESTONE,
            Material.GRANITE, Material.ANDESITE,
            Material.DIORITE, Material.GRAVEL,
            Material.CLAY,
            OneOneSevenBlockHandler.COPPER_ORE, 
            OneOneSevenBlockHandler.DEEPSLATE,
            OneOneSevenBlockHandler.TUFF,
            OneOneSevenBlockHandler.CALCITE,
            OneOneSevenBlockHandler.BUDDING_AMETHYST,
            OneOneSevenBlockHandler.AMETHYST_BLOCK,
            OneOneSevenBlockHandler.DRIPSTONE_BLOCK,
            OneOneSixBlockHandler.SMOOTH_BASALT,
            Material.LAPIS_ORE, Material.SNOW_BLOCK,
            Material.PACKED_ICE, Material.BLUE_ICE
    );
    
    

    public static final Set<Material> badlandsStoneLike = EnumSet.of(
            Material.STONE, Material.COBBLESTONE,
            Material.GRANITE, Material.ANDESITE,
            Material.DIORITE, Material.GRAVEL,
            Material.CLAY,
            Material.COAL_ORE, Material.IRON_ORE,
            Material.GOLD_ORE, Material.DIAMOND_ORE,
            Material.EMERALD_ORE, Material.REDSTONE_ORE,
            Material.LAPIS_ORE, 
            OneOneSevenBlockHandler.COPPER_ORE, 
            OneOneSevenBlockHandler.DEEPSLATE,
            OneOneSevenBlockHandler.TUFF,
            OneOneSevenBlockHandler.CALCITE,
            OneOneSevenBlockHandler.BUDDING_AMETHYST,
            OneOneSevenBlockHandler.AMETHYST_BLOCK,
            OneOneSevenBlockHandler.DRIPSTONE_BLOCK,
            OneOneSixBlockHandler.SMOOTH_BASALT,
            Material.SNOW_BLOCK,
            Material.PACKED_ICE, Material.BLUE_ICE,
            Material.TERRACOTTA, Material.ORANGE_TERRACOTTA,
            Material.RED_TERRACOTTA, Material.BROWN_TERRACOTTA,
            Material.YELLOW_TERRACOTTA, Material.RED_SAND,
            Material.DIRT, Material.PODZOL, Material.GRASS_BLOCK, Material.MYCELIUM,
            OneOneSevenBlockHandler.ROOTED_DIRT, OneOneSevenBlockHandler.DIRT_PATH()
    );
    public static final Material[] ores = {
            Material.COAL_ORE, 
            Material.IRON_ORE, 
            Material.GOLD_ORE, 
            Material.DIAMOND_ORE, 
            Material.EMERALD_ORE, 
            Material.REDSTONE_ORE, 
            Material.LAPIS_ORE,
            OneOneSevenBlockHandler.COPPER_ORE,
            OneOneSevenBlockHandler.deepSlateVersion(Material.COAL_ORE),
            OneOneSevenBlockHandler.deepSlateVersion(Material.IRON_ORE),
            OneOneSevenBlockHandler.deepSlateVersion(Material.GOLD_ORE),
            OneOneSevenBlockHandler.deepSlateVersion(Material.DIAMOND_ORE),
            OneOneSevenBlockHandler.deepSlateVersion(Material.EMERALD_ORE),
            OneOneSevenBlockHandler.deepSlateVersion(Material.REDSTONE_ORE),
            OneOneSevenBlockHandler.deepSlateVersion(Material.LAPIS_ORE),
            OneOneSevenBlockHandler.deepSlateVersion(OneOneSevenBlockHandler.COPPER_ORE),
    };
    private static final Material[] TALL_FLOWER = {Material.LILAC, Material.ROSE_BUSH, Material.PEONY, Material.LARGE_FERN, Material.SUNFLOWER};
    private static final Material[] FLOWER = {Material.DANDELION,
            Material.POPPY,
            Material.WHITE_TULIP,
            Material.ORANGE_TULIP,
            Material.RED_TULIP,
            Material.PINK_TULIP,
            Material.BLUE_ORCHID,
            Material.ALLIUM,
            Material.AZURE_BLUET,
            Material.OXEYE_DAISY,
            Material.CORNFLOWER,
            Material.LILY_OF_THE_VALLEY,
            Material.PINK_TULIP
    };

    private static final Material[] POTTED = {
            Material.POTTED_DANDELION,
            Material.POTTED_POPPY,
            Material.POTTED_WHITE_TULIP,
            Material.POTTED_ORANGE_TULIP,
            Material.POTTED_RED_TULIP,
            Material.POTTED_PINK_TULIP,
            Material.POTTED_BLUE_ORCHID,
            Material.POTTED_ALLIUM,
            Material.POTTED_AZURE_BLUET,
            Material.POTTED_OXEYE_DAISY,
            Material.POTTED_CORNFLOWER,
            Material.POTTED_LILY_OF_THE_VALLEY,
            Material.POTTED_PINK_TULIP
    };

    private static final Material[] WOOLS = {
            Material.WHITE_WOOL,
            Material.BLACK_WOOL,
            Material.BLUE_WOOL,
            Material.BROWN_WOOL,
            Material.CYAN_WOOL,
            Material.GRAY_WOOL,
            Material.GREEN_WOOL,
            Material.LIGHT_BLUE_WOOL,
            Material.LIGHT_GRAY_WOOL,
            Material.LIME_WOOL,
            Material.MAGENTA_WOOL,
            Material.ORANGE_WOOL,
            Material.PINK_WOOL,
            Material.PURPLE_WOOL,
            Material.RED_WOOL,
            Material.YELLOW_WOOL
    };
    
    private static final Material[] BED = {
            Material.WHITE_BED,
            Material.BLACK_BED,
            Material.BLUE_BED,
            Material.BROWN_BED,
            Material.CYAN_BED,
            Material.GRAY_BED,
            Material.GREEN_BED,
            Material.LIGHT_BLUE_BED,
            Material.LIGHT_GRAY_BED,
            Material.LIME_BED,
            Material.MAGENTA_BED,
            Material.ORANGE_BED,
            Material.PINK_BED,
            Material.PURPLE_BED,
            Material.RED_BED,
            Material.YELLOW_BED
    };

    public static final Material[] GLAZED_TERRACOTTA = {
            Material.WHITE_GLAZED_TERRACOTTA,
            Material.BLACK_GLAZED_TERRACOTTA,
            Material.BLUE_GLAZED_TERRACOTTA,
            Material.BROWN_GLAZED_TERRACOTTA,
            Material.CYAN_GLAZED_TERRACOTTA,
            Material.GRAY_GLAZED_TERRACOTTA,
            Material.GREEN_GLAZED_TERRACOTTA,
            Material.LIGHT_BLUE_GLAZED_TERRACOTTA,
            Material.LIGHT_GRAY_GLAZED_TERRACOTTA,
            Material.LIME_GLAZED_TERRACOTTA,
            Material.MAGENTA_GLAZED_TERRACOTTA,
            Material.ORANGE_GLAZED_TERRACOTTA,
            Material.PINK_GLAZED_TERRACOTTA,
            Material.PURPLE_GLAZED_TERRACOTTA,
            Material.RED_GLAZED_TERRACOTTA,
            Material.YELLOW_GLAZED_TERRACOTTA
    };

    public static boolean isDirectBlockFace(BlockFace facing) {
        switch (facing) {
            case NORTH:
            case SOUTH:
            case EAST:
            case WEST:
                return true;
            default:
                return false;
        }
    }

    /**
     * @return rotates original block face (NSEW only) clockwise the specified number of times
     */
    @SuppressWarnings("incomplete-switch")
    public static BlockFace rotateFace(BlockFace original, int times) {
        for (int i = 0; i < times; i++) {
            switch (original) {
                case NORTH:
                    original = BlockFace.EAST;
                    break;
                case EAST:
                    original = BlockFace.SOUTH;
                    break;
                case SOUTH:
                    original = BlockFace.WEST;
                    break;
                case WEST:
                    original = BlockFace.NORTH;
                    break;
            }
        }
        return original;
    }
    
    /**
     * @return rotates original block face (XZ plane only) clockwise the specified number of times
     */
    @SuppressWarnings("incomplete-switch")
    public static BlockFace rotateXZPlaneBlockFace(BlockFace original, int times) {
        //	N
    	//W + E
    	//	S
    	for (int i = 0; i < times; i++) {
            switch (original) {
                case NORTH:
                    original = BlockFace.NORTH_EAST;
                    break;
                case NORTH_EAST:
                    original = BlockFace.EAST;
                    break;
                case EAST:
                    original = BlockFace.SOUTH_EAST;
                    break;
                case SOUTH_EAST:
                    original = BlockFace.SOUTH;
                    break;
                case SOUTH:
                    original = BlockFace.SOUTH_WEST;
                    break;
                case SOUTH_WEST:
                    original = BlockFace.WEST;
                    break;
                case WEST:
                    original = BlockFace.NORTH_WEST;
                    break;
                case NORTH_WEST:
                    original = BlockFace.NORTH;
                    break;
            }
        }
        return original;
    }
    
    public static BlockFace[] getRandomBlockfaceAxis(Random rand) {
    	if(rand.nextInt(2) == 0)
    		return new BlockFace[] {BlockFace.NORTH, BlockFace.SOUTH};
    	else
    		return new BlockFace[] {BlockFace.WEST, BlockFace.EAST};
    }

    public static Material stoneBrick(Random rand) {
        return GenUtils.randMaterial(rand, stoneBricks);
    }

    public static Material stoneBrickSlab(Random rand) {
        return GenUtils.randMaterial(rand, stoneBrickSlabs);
    }

    public static BlockFace getXZPlaneBlockFace(Random rand) {
        return xzPlaneBlockFaces.get(rand.nextInt(8));
    }
    
    public static BlockFace getBlockFaceFromAxis(Axis ax) {
    	switch(ax) {
    	case X:
    		return BlockFace.EAST;
    	case Z:
    		return BlockFace.SOUTH;
    	case Y:
    		return BlockFace.UP;
    	}
		return null;
    }

    public static Axis getAxisFromBlockFace(BlockFace face) {
        switch (face) {
            case NORTH:
            case SOUTH:
                return Axis.Z;
            case EAST:
            case WEST:
                return Axis.X;
            case UP:
            case DOWN:
                return Axis.Y;
            default:
                throw new IllegalArgumentException("Invalid block facing for axis: " + face);
        }
    }
    
    public static Axis getPerpendicularHorizontalPlaneAxis(Axis x) {
    	switch(x) {
    	case X:
    		return Axis.Z;
    	case Z:
    		return Axis.X;
    	default:
    		return x;
    	}
    }

    public static BlockFace getDirectBlockFace(Random rand) {
        return directBlockFaces[rand.nextInt(4)];
    }
    
    public static BlockFace getSixBlockFace(Random rand) {
    	return sixBlockFaces[rand.nextInt(6)];
    }
    
    public static Material pickWool() {
        return GenUtils.randMaterial(WOOLS);
    }

    public static Material pickBed() {
        return GenUtils.randMaterial(BED);
    }

    public static Material pickFlower() {
        return GenUtils.randMaterial(FLOWER);
    }

    public static Material pickPottedPlant() {
        return GenUtils.randMaterial(POTTED);
    }

    public static Material pickTallFlower() {
        return GenUtils.randMaterial(TALL_FLOWER);
    }

    public static void dropDownBlock(SimpleBlock block) {
        if (block.getType().isSolid()) {
            Material type = block.getType();
            block.setType(Material.CAVE_AIR);
            int depth = 0;
            while (!block.getType().isSolid()) {
                block = block.getRelative(0, -1, 0);
                depth++;
                if (depth > 50) return;
            }
            block.getRelative(0, 1, 0).setType(type);
        }
    }

    public static void horizontalGlazedTerracotta(PopulatorDataAbstract data, int x, int y, int z, Material glazedTerracotta) {
        Directional terracotta = (Directional) Bukkit.createBlockData(glazedTerracotta);
        terracotta.setFacing(BlockFace.NORTH);
        data.setBlockData(x, y, z, terracotta);

        terracotta = (Directional) Bukkit.createBlockData(glazedTerracotta);
        terracotta.setFacing(BlockFace.EAST);
        data.setBlockData(x + 1, y, z, terracotta);

        terracotta = (Directional) Bukkit.createBlockData(glazedTerracotta);
        terracotta.setFacing(BlockFace.WEST);
        data.setBlockData(x, y, z + 1, terracotta);

        terracotta = (Directional) Bukkit.createBlockData(glazedTerracotta);
        terracotta.setFacing(BlockFace.SOUTH);
        data.setBlockData(x + 1, y, z + 1, terracotta);
    }

    public static void setVines(PopulatorDataAbstract data, int x, int y, int z, int maxLength) {
        SimpleBlock rel = new SimpleBlock(data, x, y, z);
        for (BlockFace face : directBlockFaces) {
            MultipleFacing dir = (MultipleFacing) Bukkit.createBlockData(Material.VINE);
            dir.setFace(face.getOppositeFace(), true);
            SimpleBlock vine = rel.getRelative(face);
            if (vine.getType().isSolid()) continue;

            vine.setType(Material.VINE);
            vine.setBlockData(dir);
            for (int i = 0; i < GenUtils.randInt(1, maxLength); i++) {
                vine.getRelative(0, -i, 0).setType(Material.VINE);
                vine.getRelative(0, -i, 0).setBlockData(dir);
            }
        }
    }

    public static double distanceSquared(float x1, float y1, float z1, float x2, float y2, float z2) {
        return Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2) + Math.pow(z2 - z1, 2);
    }

    public static void setDownUntilSolid(int x, int y, int z, PopulatorDataAbstract data, Material... type) {
        while (!data.getType(x, y, z).isSolid()) {
            data.setType(x, y, z, GenUtils.randMaterial(type));
            y--;
        }
    }

    public static void downPillar(int x, int y, int z, int height, PopulatorDataAbstract data, Material... type) {
        while (!data.getType(x, y, z).isSolid() && height > TerraformGeneratorPlugin.injector.getMinY()) {
            data.setType(x, y, z, GenUtils.randMaterial(type));
            height--;
            y--;
        }
    }

    public static boolean isStoneLike(Material mat) {
        return isDirtLike(mat) || stoneLike.contains(mat) || mat.toString().endsWith("_ORE");
    }

    public static boolean isDirtLike(Material mat) {
        switch (mat) {
            case DIRT:
            case GRASS_BLOCK:
            case PODZOL:
            case COARSE_DIRT:
            case MYCELIUM:
                return true;
            default:
                return mat == OneOneSevenBlockHandler.DIRT_PATH() ||
                		mat == OneOneSevenBlockHandler.ROOTED_DIRT;
        }
    }

    public static void setPersistentLeaves(PopulatorDataAbstract data, int x, int y, int z) {
        setPersistentLeaves(data, x, y, z, Material.OAK_LEAVES);
    }

    public static void setPersistentLeaves(PopulatorDataAbstract data, int x, int y, int z, Material type) {
        data.setType(x, y, z, Material.OAK_LEAVES);
        Leaves bd = (Leaves) Bukkit.createBlockData(type);
        bd.setPersistent(true);
        data.setBlockData(x, y, z, bd);
    }

    public static void setDoublePlant(PopulatorDataAbstract data, int x, int y, int z, Material doublePlant) {
        Bisected d = ((Bisected) Bukkit.createBlockData(doublePlant));
        d.setHalf(Half.BOTTOM);
        data.setBlockData(x, y, z, d);

        d = ((Bisected) Bukkit.createBlockData(doublePlant));
        d.setHalf(Half.TOP);
        data.setBlockData(x, y + 1, z, d);
    }

    public static boolean isSameChunk(Block a, Block b) {
        return SimpleChunkLocation.of(a).equals(SimpleChunkLocation.of(b));
    }

    public static boolean areAdjacentChunksLoaded(Chunk middle) {
        for (int nx = -1; nx <= 1; nx++) {
            for (int nz = -1; nz <= 1; nz++) {
                int x = middle.getX() + nx;
                int z = middle.getZ() + nz;
                if (!middle.getWorld().isChunkLoaded(x, z)) return false;
            }
        }
        return true;
    }

    public static Material getTerracotta(int height) {
        int mapped = (height + 10) % 17;

        if (mapped == 2 || mapped == 9 || mapped == 13 || mapped == 16) return Material.TERRACOTTA;
        if (mapped == 4 || mapped == 5 || mapped == 12 || mapped == 15) return Material.RED_TERRACOTTA;
        if (mapped == 6) return Material.YELLOW_TERRACOTTA;
        if (mapped == 8) return Material.BROWN_TERRACOTTA;

        return Material.ORANGE_TERRACOTTA;
    }

    public static int spawnPillar(Random rand, PopulatorDataAbstract data, int x, int y, int z, Material type, int minHeight, int maxHeight) {
        int height = GenUtils.randInt(rand, minHeight, maxHeight);
        for (int i = 0; i < height; i++) data.setType(x, y + i, z, type);
        return height;
    }

    public static void generateClayDeposit(int x, int y, int z, PopulatorDataAbstract data, Random random) {
        //CLAY DEPOSIT
    	replaceCircularPatch(random.nextInt(9999), TConfigOption.BIOME_CLAY_DEPOSIT_SIZE.getFloat(), new SimpleBlock(data,x,y,z), Material.CLAY);

    }

    public static void vineUp(SimpleBlock base, int maxLength) {
        for (BlockFace face : directBlockFaces) {
            MultipleFacing dir = (MultipleFacing) Bukkit.createBlockData(Material.VINE);
            dir.setFace(face.getOppositeFace(), true);
            SimpleBlock vine = base.getRelative(face);
            if (vine.getType().isSolid()) continue;

            vine.setType(Material.VINE);
            vine.setBlockData(dir);
            for (int i = 1; i < GenUtils.randInt(1, maxLength); i++) {
                SimpleBlock relative = vine.getRelative(0, -i, 0);
                if (relative.getType() != Material.AIR) break;
                relative.setType(Material.VINE);
                relative.setBlockData(dir);
            }
        }
    }
    
    /**
     * Places a noise-fuzzed circle of the defined material
     * @param seed
     * @param radius
     * @param base
     * @param type
     */
    public static void replaceCircle(int seed, float radius, SimpleBlock base, Material... type) {
    	if (radius <= 0) return;
        if (radius <= 0.5) {
            //block.setReplaceType(ReplaceType.ALL);
            base.setType(GenUtils.randMaterial(new Random(seed), type));
            return;
        }
        
        FastNoise noise = new FastNoise(seed);
        noise.SetNoiseType(NoiseType.Simplex);
        noise.SetFrequency(0.09f);
        
        for (float x = -radius; x <= radius; x++) {
            for (float z = -radius; z <= radius; z++) {
                SimpleBlock rel = base.getRelative(Math.round(x), 0, Math.round(z));
                
                //double radiusSquared = Math.pow(trueRadius+noise.GetNoise(rel.getX(), rel.getY(), rel.getZ())*2,2);
                double equationResult = Math.pow(x, 2) / Math.pow(radius, 2)
                        + Math.pow(z, 2) / Math.pow(radius, 2);
                if (equationResult <= 1 + 0.7 * noise.GetNoise(rel.getX(), rel.getZ())) {
                    //if(rel.getLocation().distanceSquared(block.getLocation()) <= radiusSquared){          
                    rel.lsetType(GenUtils.randMaterial(type));
                }
            }
        }
        
    }

    /**
     * Replaces the highest ground with a noise-fuzzed circle of the defined material
     * @param seed
     * @param radius
     * @param base
     * @param type
     */
    public static void replaceCircularPatch(int seed, float radius, SimpleBlock base, Material... type) {
    	if (radius <= 0) return;
        if (radius <= 0.5) {
            //block.setReplaceType(ReplaceType.ALL);
            base.setType(GenUtils.randMaterial(new Random(seed), type));
            return;
        }
        
        FastNoise noise = new FastNoise(seed);
        noise.SetNoiseType(NoiseType.Simplex);
        noise.SetFrequency(0.09f);
        
        for (float x = -radius; x <= radius; x++) {
            for (float z = -radius; z <= radius; z++) {
                SimpleBlock rel = base.getRelative(Math.round(x), 0, Math.round(z));
                rel = rel.getGround();
                
                //double radiusSquared = Math.pow(trueRadius+noise.GetNoise(rel.getX(), rel.getY(), rel.getZ())*2,2);
                double equationResult = Math.pow(x, 2) / Math.pow(radius, 2)
                        + Math.pow(z, 2) / Math.pow(radius, 2);
                if (equationResult <= 1 + 0.7 * noise.GetNoise(rel.getX(), rel.getZ())) {
                    //if(rel.getLocation().distanceSquared(block.getLocation()) <= radiusSquared){          
                    rel.setType(GenUtils.randMaterial(type));
                }
            }
        }
        
    }
    
    public static void replaceSphere(int seed, float radius, SimpleBlock base, boolean hardReplace, Material... type) {
        if (radius > 0) replaceSphere(seed, radius, radius, radius, base, hardReplace, type);
    }

    public static void replaceSphere(int seed, float rX, float rY, float rZ, SimpleBlock block, boolean hardReplace, Material... type) {
        replaceSphere(seed, rX, rY, rZ, block, hardReplace, false, type);
    }

    public static void replaceWaterSphere(int seed, float radius, SimpleBlock base) {
        if (radius <= 0) return;
        if (radius <= 0.5) {
            //block.setReplaceType(ReplaceType.ALL);
        	if (base.getY() <= TerraformGenerator.seaLevel) 
        		base.setType(Material.WATER);
	        else
	        	base.setType(Material.AIR);
            return;
        }

        FastNoise noise = new FastNoise(seed);
        noise.SetNoiseType(NoiseType.Simplex);
        noise.SetFrequency(0.09f);

        for (float x = -radius; x <= radius; x++) {
            for (float y = -radius; y <= radius; y++) {
                for (float z = -radius; z <= radius; z++) {

                    SimpleBlock rel = base.getRelative(Math.round(x), Math.round(y), Math.round(z));
                    //double radiusSquared = Math.pow(trueRadius+noise.GetNoise(rel.getX(), rel.getY(), rel.getZ())*2,2);
                    double equationResult = Math.pow(x, 2) / Math.pow(radius, 2)
                            + Math.pow(y, 2) / Math.pow(radius, 2)
                            + Math.pow(z, 2) / Math.pow(radius, 2);
                    if (equationResult <= 1 + 0.7 * noise.GetNoise(rel.getX(), rel.getY(), rel.getZ())) {
                        //if(rel.getLocation().distanceSquared(block.getLocation()) <= radiusSquared){
                        if (rel.getY() <= TerraformGenerator.seaLevel) 
                            rel.setType(Material.WATER);
                        else
                            rel.setType(Material.AIR);
                    }
                }
            }
        }
    }

    /**
     * Put barrier in toReplace to hard replace all solid blocks.
     */
    public static void carveCaveAir(int seed, float rX, float rY, float rZ, SimpleBlock block, boolean waterToAir, Collection<Material> toReplace) {
        if (rX <= 0 && rY <= 0 && rZ <= 0) return;
        if (rX <= 0.5 && rY <= 0.5 && rZ <= 0.5) {
            if (waterToAir || block.getType() != Material.WATER) block.setType(Material.CAVE_AIR);
            return;
        }

        FastNoise noise = new FastNoise(seed);
        noise.SetNoiseType(NoiseType.Simplex);
        noise.SetFrequency(0.09f);

        for (float x = -rX; x <= rX; x++) {
            for (float y = -rY; y <= rY; y++) {
                for (float z = -rZ; z <= rZ; z++) {
                    SimpleBlock rel = block.getRelative(Math.round(x), Math.round(y), Math.round(z));
                    double equationResult = Math.pow(x, 2) / Math.pow(rX, 2)
                            + Math.pow(y, 2) / Math.pow(rY, 2)
                            + Math.pow(z, 2) / Math.pow(rZ, 2);
                    if (equationResult <= 1 + 0.7 * noise.GetNoise(rel.getX(), rel.getY(), rel.getZ())) {
                        if (toReplace.contains(Material.BARRIER)) { //Blacklist
                            if (!toReplace.contains(rel.getType()))
                                if (rel.getType() != Material.WATER || waterToAir)
                                    rel.setType(Material.CAVE_AIR);

                        } else if (toReplace.contains(rel.getType())) { //Whitelist
                            if (rel.getType() != Material.WATER || waterToAir)
                                rel.setType(Material.CAVE_AIR);

                        } else if (!rel.getType().isSolid()) {
                            if (rel.getType() != Material.WATER || waterToAir)
                                rel.setType(Material.CAVE_AIR);
                        }
                    }
                }
            }
        }
    }

    public static void replaceSphere(int seed, float rX, float rY, float rZ, SimpleBlock block, boolean hardReplace, boolean snowy, Material... type) {
        if (rX <= 0 && rY <= 0 && rZ <= 0) return;
        if (rX <= 0.5 && rY <= 0.5 && rZ <= 0.5) {
            //block.setReplaceType(ReplaceType.ALL);
            block.setType(GenUtils.randMaterial(new Random(seed), type));
            return;
        }

        Random rand = new Random(seed);
        FastNoise noise = new FastNoise(seed);
        noise.SetNoiseType(NoiseType.Simplex);
        noise.SetFrequency(0.09f);

        for (float x = -rX; x <= rX; x++) {
            for (float y = -rY; y <= rY; y++) {
                for (float z = -rZ; z <= rZ; z++) {
                    SimpleBlock rel = block.getRelative(Math.round(x), Math.round(y), Math.round(z));
                    //double radiusSquared = Math.pow(trueRadius+noise.GetNoise(rel.getX(), rel.getY(), rel.getZ())*2,2);
                    double equationResult = Math.pow(x, 2) / Math.pow(rX, 2)
                            + Math.pow(y, 2) / Math.pow(rY, 2)
                            + Math.pow(z, 2) / Math.pow(rZ, 2);
                    if (equationResult <= 1 + 0.7 * noise.GetNoise(rel.getX(), rel.getY(), rel.getZ())) {
                        //if(rel.getLocation().distanceSquared(block.getLocation()) <= radiusSquared){
                        if (hardReplace || !rel.getType().isSolid()) {
                            rel.setType(GenUtils.randMaterial(rand, type));
                            if (snowy) {
                                rel.getRelative(0, 1, 0).lsetType(Material.SNOW);
                            }
                        }
                    }
                }
            }
        }
    }

    public static void replaceUpperSphere(int seed, float rX, float rY, float rZ, SimpleBlock block, boolean hardReplace, Material... type) {
        if (rX <= 0 && rY <= 0 && rZ <= 0) return;
        if (rX <= 0.5 && rY <= 0.5 && rZ <= 0.5) {
            //block.setReplaceType(ReplaceType.ALL);
            block.setType(GenUtils.randMaterial(new Random(seed), type));
            return;
        }

        Random rand = new Random(seed);
        FastNoise noise = new FastNoise(seed);
        noise.SetNoiseType(NoiseType.Simplex);
        noise.SetFrequency(0.09f);

        for (float x = -rX; x <= rX; x++) {
            for (float y = 0; y <= rY; y++) {
                for (float z = -rZ; z <= rZ; z++) {
                    SimpleBlock rel = block.getRelative(Math.round(x), Math.round(y), Math.round(z));
                    //double radiusSquared = Math.pow(trueRadius+noise.GetNoise(rel.getX(), rel.getY(), rel.getZ())*2,2);
                    double equationResult = Math.pow(x, 2) / Math.pow(rX, 2)
                            + Math.pow(y, 2) / Math.pow(rY, 2)
                            + Math.pow(z, 2) / Math.pow(rZ, 2);
                    if (equationResult <= 1 + 0.7 * noise.GetNoise(rel.getX(), rel.getY(), rel.getZ())) {
                        //if(rel.getLocation().distanceSquared(block.getLocation()) <= radiusSquared){
                        if (hardReplace || !rel.getType().isSolid()) rel.setType(GenUtils.randMaterial(rand, type));
                        //rel.setReplaceType(ReplaceType.ALL);
                    }
                }
            }
        }
    }
    
    public static void replaceLowerSphere(int seed, float rX, float rY, float rZ, SimpleBlock block, boolean hardReplace, Material... type) {
        if (rX <= 0 && rY <= 0 && rZ <= 0) return;
        if (rX <= 0.5 && rY <= 0.5 && rZ <= 0.5) {
            //block.setReplaceType(ReplaceType.ALL);
            block.setType(GenUtils.randMaterial(new Random(seed), type));
            return;
        }

        Random rand = new Random(seed);
        FastNoise noise = new FastNoise(seed);
        noise.SetNoiseType(NoiseType.Simplex);
        noise.SetFrequency(0.09f);

        for (float x = -rX; x <= rX; x++) {
            for (float y = -rY; y <= 0; y++) {
                for (float z = -rZ; z <= rZ; z++) {
                    SimpleBlock rel = block.getRelative(Math.round(x), Math.round(y), Math.round(z));
                    //double radiusSquared = Math.pow(trueRadius+noise.GetNoise(rel.getX(), rel.getY(), rel.getZ())*2,2);
                    double equationResult = Math.pow(x, 2) / Math.pow(rX, 2)
                            + Math.pow(y, 2) / Math.pow(rY, 2)
                            + Math.pow(z, 2) / Math.pow(rZ, 2);
                    if (equationResult <= 1 + 0.7 * noise.GetNoise(rel.getX(), rel.getY(), rel.getZ())) {
                        //if(rel.getLocation().distanceSquared(block.getLocation()) <= radiusSquared){
                        if (hardReplace || !rel.getType().isSolid()) rel.setType(GenUtils.randMaterial(rand, type));
                        //rel.setReplaceType(ReplaceType.ALL);
                    }
                }
            }
        }
    }

    public static BlockFace[] getAdjacentFaces(BlockFace original) {
        //   N
        //W    E
        //   S
        switch (original) {
            case EAST:
                return new BlockFace[]{BlockFace.SOUTH, BlockFace.NORTH};
            case NORTH:
                return new BlockFace[]{BlockFace.EAST, BlockFace.WEST};
            case SOUTH:
                return new BlockFace[]{BlockFace.WEST, BlockFace.EAST};
            default:
                return new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH};
        }
    }

    public static BlockFace getTurnBlockFace(Random rand, BlockFace original) {
        return getAdjacentFaces(original)[GenUtils.randInt(rand, 0, 1)];
    }

    public static BlockFace getLeft(BlockFace original) {
        return getAdjacentFaces(original)[0];
    }

    public static BlockFace getRight(BlockFace original) {
        return getAdjacentFaces(original)[1];
    }

    public static void correctMultifacingData(SimpleBlock target) {
        if (!(target.getBlockData() instanceof MultipleFacing)) {
            if (Version.isAtLeast(16.1) && target.getType().name().endsWith(("_WALL"))) {
                v1_16_R1_BlockDataFixer.correctSurroundingWallData(target);
            }
            return;
        }

        MultipleFacing data = (MultipleFacing) target.getBlockData();
        for (BlockFace face : data.getAllowedFaces()) {
            Material type = target.getRelative(face).getType();
            boolean facing = type.isSolid()
                    && !type.toString().endsWith("PRESSURE_PLATE")
            		&& !type.toString().contains("BANNER")
                    && !Tag.SLABS.isTagged(type)
                    && !Tag.TRAPDOORS.isTagged(type);
            if(target.getType().toString().endsWith("GLASS_PANE")
            		&& (Tag.FENCE_GATES.isTagged(type)
            				|| Tag.FENCES.isTagged(type)))
            	facing = false;
            data.setFace(face, facing);
            if(Tag.STAIRS.isTagged(type)) {
            	Stairs stairs = (Stairs) target.getRelative(face).getBlockData();
            	if(stairs.getFacing() == face.getOppositeFace())
            		data.setFace(face, true);
            	else
            		data.setFace(face, false);
            }
        }
        target.setBlockData(data);
    }
    
    public static boolean isExposedToNonSolid(SimpleBlock target) {
    	for(BlockFace face:directBlockFaces) {
    		if(!target.getRelative(face).getType().isSolid())
    			return true;
    	}
    	return false;
    }

    public static boolean isExposedToMaterial(SimpleBlock target, Material mat) {
    	for(BlockFace face:directBlockFaces) {
    		if(target.getRelative(face).getType() == mat)
    			return true;
    	}
    	return false;
    }

    /**
     * Correct fencse for example
     */
    public static void correctSurroundingMultifacingData(SimpleBlock target) {
        if (!(target.getBlockData() instanceof MultipleFacing)) {
            if (Version.isAtLeast(16.1) && Tag.WALLS.isTagged(target.getType())) {
                v1_16_R1_BlockDataFixer.correctSurroundingWallData(target);
            }
            return;
        }

        correctMultifacingData(target);
        MultipleFacing data = (MultipleFacing) target.getBlockData();
        for (BlockFace face : data.getAllowedFaces()) {
            if (target.getRelative(face).getBlockData() instanceof MultipleFacing) {
                correctMultifacingData(target.getRelative(face));
            }
        }
    }

    public static void correctStairData(SimpleBlock target) {
        if (!(target.getBlockData() instanceof Stairs)) {
            return;
        }

        Stairs data = (Stairs) target.getBlockData();
        BlockFace left = BlockUtils.getLeft(data.getFacing());
        BlockFace right = BlockUtils.getRight(data.getFacing());

        //Left is a stair and right isn't
        if (Tag.STAIRS.isTagged(target.getRelative(left).getType())
                && !Tag.STAIRS.isTagged(target.getRelative(right).getType())) {

            //Only adjust if the left side has the same facing.
            if (((Stairs) target.getRelative(left).getBlockData()).getFacing() == data.getFacing()) {

                //Back is a stair
                if (Tag.STAIRS.isTagged(target.getRelative(data.getFacing()).getType())) {

                    //Only set if the back stair is facing a valid location
                    if (((Stairs) target.getRelative(data.getFacing()).getBlockData()).getFacing()
                            == getLeft(data.getFacing()))
                        data.setShape(Stairs.Shape.OUTER_RIGHT);

                    //Front is a stair
                } else if (Tag.STAIRS.isTagged(target.getRelative(data.getFacing().getOppositeFace()).getType())) {

                    //Only set if the front stair is facing a valid location
                    if (((Stairs) target.getRelative(data.getFacing().getOppositeFace()).getBlockData()).getFacing()
                            == getRight(data.getFacing()))
                        data.setShape(Stairs.Shape.INNER_RIGHT);
                }
            }

            //Right is a stair and left isn't.
        } else if (!Tag.STAIRS.isTagged(target.getRelative(left).getType())
                && Tag.STAIRS.isTagged(target.getRelative(right).getType())) {

            //Only adjust if the right side has the same facing.
            if (((Stairs) target.getRelative(right).getBlockData()).getFacing() == data.getFacing()) {

                //Back is a stair
                if (Tag.STAIRS.isTagged(target.getRelative(data.getFacing()).getType())) {

                    //Only set if the back stair is facing a valid location
                    if (((Stairs) target.getRelative(data.getFacing()).getBlockData()).getFacing()
                            == getRight(data.getFacing()))
                        data.setShape(Stairs.Shape.OUTER_LEFT);

                    //Front is a stair
                } else if (Tag.STAIRS.isTagged(target.getRelative(data.getFacing().getOppositeFace()).getType())) {

                    //Only set if the front stair is facing a valid location
                    if (((Stairs) target.getRelative(data.getFacing().getOppositeFace()).getBlockData()).getFacing()
                            == getLeft(data.getFacing()))
                        data.setShape(Stairs.Shape.INNER_LEFT);
                }
            }

            //Right is a stair and left isn't.
        }
        target.setBlockData(data);
    }

    public static void correctSurroundingStairData(SimpleBlock target) {
        if (!(target.getBlockData() instanceof Stairs)) {
            return;
        }

        correctStairData(target);
        Stairs data = (Stairs) target.getBlockData();
        for (BlockFace face : getAdjacentFaces(data.getFacing())) {
            if (target.getRelative(face).getBlockData() instanceof Stairs) {
                correctStairData(target.getRelative(face));
            }
        }
    }

    private static boolean isMushroom(SimpleBlock target) {
        Material material = target.getType();
        return material == Material.BROWN_MUSHROOM_BLOCK || material == Material.RED_MUSHROOM_BLOCK;
    }

    public static void correctMushroomData(SimpleBlock target) {
        if (!isMushroom(target)) return;
        MultipleFacing data = (MultipleFacing) target.getBlockData();
        for (BlockFace face : data.getAllowedFaces()) {
            data.setFace(face, !isMushroom(target.getRelative(face)));
        }

        target.setBlockData(data);
    }

    public static void correctSurroundingMushroomData(SimpleBlock target) {
        correctMushroomData(target);
        for (BlockFace face : sixBlockFaces) correctMushroomData(target.getRelative(face));
    }

    public static void placeDoor(PopulatorDataAbstract data, Material mat, int x, int y, int z, BlockFace dir) {
        data.setType(x, y, z, mat);
        data.setType(x, y + 1, z, mat);
        Door door = (Door) Bukkit.createBlockData(mat);
        door.setFacing(dir);
        door.setHalf(Half.BOTTOM);
        data.setBlockData(x, y, z, door);

        door = (Door) Bukkit.createBlockData(mat);
        door.setFacing(dir);
        door.setHalf(Half.TOP);
        data.setBlockData(x, y + 1, z, door);
    }

    public static void placeBed(SimpleBlock block, Material mat, BlockFace dir) {
        if(BlockUtils.isAir(block.getType()) && BlockUtils.isAir(block.getRelative(dir).getType())) 
        {
        	Bed bed = (Bed) Bukkit.createBlockData(mat);
            bed.setFacing(dir.getOppositeFace());
            bed.setPart(Bed.Part.HEAD);
            block.setBlockData(bed);

            bed = (Bed) Bukkit.createBlockData(mat);
            bed.setFacing(dir.getOppositeFace());
            bed.setPart(Bed.Part.FOOT);
            block.getRelative(dir).setBlockData(bed);
        }
    }

    public static void placeRail(SimpleBlock block, Material mat) {
        Rail rail = (Rail) Bukkit.createBlockData(mat);
        Set<BlockFace> faces = EnumSet.noneOf(BlockFace.class);
        BlockFace upperFace = null;
        for (BlockFace face : BlockUtils.directBlockFaces) {
            SimpleBlock relative = block.getRelative(face);
            if (relative.getType().name().contains("RAIL")) faces.add(face);
            if (relative.getRelative(0, 1, 0).getType().name().contains("RAIL")) upperFace = face;
        }

        if (upperFace != null) {
            switch (upperFace) {
                case NORTH:
                    rail.setShape(Shape.ASCENDING_NORTH);
                    break;
                case SOUTH:
                    rail.setShape(Shape.ASCENDING_SOUTH);
                    break;
                case EAST:
                    rail.setShape(Shape.ASCENDING_EAST);
                    break;
                case WEST:
                    rail.setShape(Shape.ASCENDING_WEST);
                    break;
                default:
                    break;
            }
        } else if (!faces.isEmpty()) {
            if (faces.contains(BlockFace.NORTH) && faces.contains(BlockFace.EAST)) {
                rail.setShape(Shape.NORTH_EAST);
            } else if (faces.contains(BlockFace.NORTH) && faces.contains(BlockFace.WEST)) {
                rail.setShape(Shape.NORTH_WEST);
            } else if (faces.contains(BlockFace.SOUTH) && faces.contains(BlockFace.EAST)) {
                rail.setShape(Shape.SOUTH_EAST);
            } else if (faces.contains(BlockFace.NORTH) || faces.contains(BlockFace.SOUTH)) {
                rail.setShape(Shape.NORTH_SOUTH);
            } else if (faces.contains(BlockFace.EAST) || faces.contains(BlockFace.WEST)) {
                rail.setShape(Shape.EAST_WEST);
            }
        }

        block.setBlockData(rail);
    }

    public static void correctSurroundingRails(SimpleBlock target) {
        if (!(target.getBlockData() instanceof Rail)) return;

        placeRail(target, target.getType());
        for (BlockFace face : BlockUtils.directBlockFaces) {
            SimpleBlock relative = target.getRelative(face);
            if (relative.getBlockData() instanceof Rail)
                placeRail(relative, relative.getType());
            if (target.getRelative(face).getRelative(0, -1, 0).getBlockData() instanceof Rail)
                placeRail(relative.getRelative(0, -1, 0), target.getRelative(0, -1, 0).getRelative(face).getType());
        }
    }

    public static boolean emitsLight(Material mat) {
        switch (mat) {
            case TORCH:
            case SEA_PICKLE:
            case SEA_LANTERN:
            case GLOWSTONE:
            case LANTERN:
            case LAVA:
            case CAMPFIRE:
            case REDSTONE_LAMP:
            case FIRE:
                return true;
            default:
                return false;
        }
    }

    public static BlockData infestStone(BlockData mat) {
        switch (mat.getMaterial()) {
            case STONE_BRICKS:
                return Bukkit.createBlockData(Material.INFESTED_STONE_BRICKS);
            case MOSSY_STONE_BRICKS:
                return Bukkit.createBlockData(Material.INFESTED_MOSSY_STONE_BRICKS);
            case CRACKED_STONE_BRICKS:
                return Bukkit.createBlockData(Material.INFESTED_CRACKED_STONE_BRICKS);
            case CHISELED_STONE_BRICKS:
                return Bukkit.createBlockData(Material.INFESTED_CHISELED_STONE_BRICKS);
            case COBBLESTONE:
                return Bukkit.createBlockData(Material.INFESTED_COBBLESTONE);
            case STONE:
                return Bukkit.createBlockData(Material.INFESTED_STONE);
            default:
                return mat;
        }
    }

    public static void stairwayUntilSolid(SimpleBlock start, BlockFace extensionDir, Material[] downTypes, Material... stairTypes) {
        while (!start.getType().isSolid()) {
            new StairBuilder(stairTypes)
                    .setFacing(extensionDir.getOppositeFace())
                    .apply(start);
            BlockUtils.setDownUntilSolid(
                    start.getX(),
                    start.getY() - 1,
                    start.getZ(),
                    start.getPopData(),
                    downTypes);
            start = start.getRelative(extensionDir).getRelative(0, -1, 0);
        }
    }
    
    public static boolean isAir(Material mat) {
    	return mat.toString().endsWith("AIR");
    }

    public static BlockData getRandomBarrel() {
        Directional barrel = (Directional) Bukkit.createBlockData(Material.BARREL);
        barrel.setFacing(BlockUtils.sixBlockFaces[GenUtils.randInt(0, BlockUtils.sixBlockFaces.length - 1)]);
        return barrel;
    }
    
    public static void angledStairwayUntilSolid(SimpleBlock start, BlockFace extensionDir, Material[] downTypes, Material... stairTypes) {
        int threshold = 5;
    	while (!start.getType().isSolid()) {
    		
    		if(threshold == 0)
    			extensionDir = BlockUtils.getTurnBlockFace(new Random(), extensionDir);
            
    		new StairBuilder(stairTypes)
                    .setFacing(extensionDir.getOppositeFace())
                    .apply(start);
            BlockUtils.setDownUntilSolid(
                    start.getX(),
                    start.getY() - 1,
                    start.getZ(),
                    start.getPopData(),
                    downTypes);
            threshold--;
            start = start.getRelative(extensionDir).getRelative(0, -1, 0);
        }
    }
    
    /**
     * Checks if the target is in a wet material, or if the material it is
     * in is waterlogged.
     * @param target
     * @return
     */
    public static boolean isWet(SimpleBlock target) {
    	return BlockUtils.wetMaterials.contains(target.getType()) || 
        		(target.getBlockData() instanceof Waterlogged
            			&& ((Waterlogged) target.getBlockData()).isWaterlogged());
    }
    
    public static float yawFromBlockFace(BlockFace face) {
    	switch(face) {
		case EAST:
			return -90.0f;
		case NORTH:
			return 180.0f;
		case SOUTH:
			return 0.0f;
		case WEST:
			return 90.0f;
		default:
			return 180.0f;
    	}
    }
    
    public static void randRotateBlockData(Random rand, BlockData data) {
    	if(data instanceof Directional) {
    		Set<BlockFace> faces = ((Directional) data).getFaces();
    		((Directional) data).setFacing(faces.stream().skip((int) (faces.size() * rand.nextDouble())).findAny().get());
    	}else if(data instanceof Rotatable) {
    		((Rotatable) data).setRotation(BlockUtils.getXZPlaneBlockFace(rand));
    	}
    }
    
    public static boolean isOre(Material mat) {
    	for(Material ore:ores)
    		if(ore == mat) return true;
    	return false;
    }
}
