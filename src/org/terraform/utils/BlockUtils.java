package org.terraform.utils;

import org.bukkit.Axis;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.block.data.Rail;
import org.bukkit.block.data.Rail.Shape;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.Leaves;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.SimpleChunkLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.utils.FastNoise.NoiseType;

import java.util.*;

public class BlockUtils {
    // N
    //W E
    // S
    public static final List<BlockFace> xzPlaneBlockFaces = Arrays.asList(
            BlockFace.NORTH, BlockFace.NORTH_EAST, BlockFace.EAST, BlockFace.SOUTH_EAST, BlockFace.SOUTH, BlockFace.SOUTH_WEST, BlockFace.WEST, BlockFace.NORTH_WEST
    );
    public static final BlockFace[] BLOCK_FACES = BlockFace.values();
    public static final BlockFace[] xzDiagonalPlaneBlockFaces = {BlockFace.NORTH_EAST, BlockFace.SOUTH_EAST, BlockFace.SOUTH_WEST, BlockFace.NORTH_WEST};
    public static final Material[] stoneBricks = {Material.STONE_BRICKS, Material.MOSSY_STONE_BRICKS, Material.CRACKED_STONE_BRICKS};
    public static final Material[] stoneBrickSlabs = {Material.STONE_BRICK_SLAB, Material.MOSSY_STONE_BRICK_SLAB};
    public static final BlockFace[] directBlockFaces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
    public static final BlockFace[] sixBlockFaces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN};
    public static final Set<Material> stoneLike = EnumSet.of(
            Material.STONE, Material.COBBLESTONE, Material.GRANITE, Material.ANDESITE, Material.DIORITE, Material.GRAVEL, Material.COAL_ORE, Material.IRON_ORE, Material.GOLD_ORE,
            Material.DIAMOND_ORE, Material.EMERALD_ORE, Material.REDSTONE_ORE, Material.LAPIS_ORE, Material.SNOW_BLOCK, Material.PACKED_ICE, Material.BLUE_ICE
    );
    public static final Material[] ores = {
            Material.COAL_ORE, Material.IRON_ORE, Material.GOLD_ORE, Material.DIAMOND_ORE, Material.EMERALD_ORE, Material.REDSTONE_ORE, Material.LAPIS_ORE,
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

    public static Material stoneBrick(Random rand) {
        return GenUtils.randMaterial(rand, stoneBricks);
    }

    public static Material stoneBrickSlab(Random rand) {
        return GenUtils.randMaterial(rand, stoneBrickSlabs);
    }

    public static BlockFace getXZPlaneBlockFace(Random rand) {
        return xzPlaneBlockFaces.get(rand.nextInt(8));
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

    public static BlockFace getDirectBlockFace(Random rand) {
        return directBlockFaces[rand.nextInt(4)];
    }

    public static Material getWoodForBiome(BiomeBank biome, String wood) {
        switch (biome) {
            case BADLANDS:
            case SAVANNA:
            case DESERT_MOUNTAINS:
            case DESERT:
            case BADLANDS_MOUNTAINS:
                return Material.getMaterial("ACACIA_" + wood);
            case BIRCH_MOUNTAINS:
                return Material.getMaterial("BIRCH_" + wood);
            case COLD_OCEAN:
            case WARM_OCEAN:
            case SWAMP:
            case PLAINS:
            case OCEAN:
            case MUDFLATS:
            case LUKEWARM_OCEAN:
            case FOREST:
                return Material.getMaterial("OAK_" + wood);
            case FROZEN_OCEAN:
            case TAIGA:
            case SNOWY_WASTELAND:
            case SNOWY_TAIGA:
            case SNOWY_MOUNTAINS:
            case ROCKY_MOUNTAINS:
            case ROCKY_BEACH:
            case ICE_SPIKES:
                return Material.getMaterial("SPRUCE_" + wood);
            case SANDY_BEACH:
            case JUNGLE:
                return Material.getMaterial("JUNGLE_" + wood);
            default:
                break;
        }
        return Material.getMaterial("OAK_" + wood);
    }

    public static Material pickFlower() {
        return GenUtils.randMaterial(Material.DANDELION,
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
                Material.PINK_TULIP);
    }

    public static Material pickTallFlower() {
        return GenUtils.randMaterial(Material.LILAC,
                Material.ROSE_BUSH,
                Material.PEONY,
                Material.LARGE_FERN,
                Material.SUNFLOWER);
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
            block.getRelative(0,1,0).setType(type);
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

    public static void setVines(PopulatorDataAbstract data, TerraformWorld tw, int x, int y, int z, int maxLength) {
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
        while (!data.getType(x, y, z).isSolid() && height > 0) {
            data.setType(x, y, z, GenUtils.randMaterial(type));
            height--;
            y--;
        }
    }

    public static boolean isStoneLike(Material mat) {
        return isDirtLike(mat) || stoneLike.contains(mat);
    }

    public static boolean isDirtLike(Material mat) {
        switch (mat) {
            case DIRT:
            case GRASS_BLOCK:
            case PODZOL:
            case GRASS_PATH:
            case COARSE_DIRT:
            case MYCELIUM:
                return true;
            default:
                return false;
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

    public static int spawnPillar(Random rand, PopulatorDataAbstract data, int x, int y, int z, Material type, int minHeight, int maxHeight) {
        int height = GenUtils.randInt(rand, minHeight, maxHeight);
        for (int i = 0; i < height; i++) data.setType(x, y + i, z, type);
        return height;
    }

    public static void generateClayDeposit(int x, int y, int z, PopulatorDataAbstract data, Random random) {
        //CLAY DEPOSIT
        int length = GenUtils.randInt(4, 8);
        int nx = x;
        int ny = y;
        int nz = z;
        while (length-- > 0) {
            if (data.getType(nx, ny, nz) == Material.SAND ||
                    data.getType(nx, ny, nz) == Material.GRAVEL ||
                    isDirtLike(data.getType(nx, ny, nz)))
                data.setType(nx, ny, nz, Material.CLAY);

            switch (random.nextInt(5)) {  // The direction chooser
                case 0:
                    nx++;
                    break;
                case 1:
                    ny++;
                    break;
                case 2:
                    nz++;
                    break;
                case 3:
                    nx--;
                    break;
                case 4:
                    ny--;
                    break;
                case 5:
                    nz--;
                    break;
            }
            if (ny > y) ny = y;
            if (ny < 2) ny = 2;
        }
    }

    
    public static void generateOceanClay(int x, int y, int z, PopulatorDataAbstract data, Random random) {
        //CLAY DEPOSIT
        int length = GenUtils.randInt(18, 39);
        int nx = x;
        int ny = y;
        int nz = z;
        while (length-- > 0) {
            if (data.getType(nx, ny, nz) == Material.SAND ||
                    data.getType(nx, ny, nz) == Material.GRAVEL ||
                    isDirtLike(data.getType(nx, ny, nz)))
                data.setType(nx, ny, nz, Material.CLAY);

            switch (random.nextInt(5)) {  // The direction chooser
                case 0:
                    nx++;
                    break;
                case 1:
                    ny++;
                    break;
                case 2:
                    nz++;
                    break;
                case 3:
                    nx--;
                    break;
                case 4:
                    ny--;
                    break;
                case 5:
                    nz--;
                    break;
            }
            if (ny > y) ny = y;
            if (ny < 2) ny = 2;
        }
    }
    
    public static void generateSandOcean(int x, int y, int z, PopulatorDataAbstract data, Random random) {
        //CLAY DEPOSIT
        int length = GenUtils.randInt(18, 39);
        int nx = x;
        int ny = y;
        int nz = z;
        while (length-- > 0) {
            if (data.getType(nx, ny, nz) == Material.GRAVEL ||
                    data.getType(nx, ny, nz) == Material.GRAVEL ||
                    isDirtLike(data.getType(nx, ny, nz)))
                data.setType(nx, ny, nz, Material.SAND);

            switch (random.nextInt(5)) {  // The direction chooser
                case 0:
                    nx++;
                    break;
                case 1:
                    ny++;
                    break;
                case 2:
                    nz++;
                    break;
                case 3:
                    nx--;
                    break;
                case 4:
                    ny--;
                    break;
                case 5:
                    nz--;
                    break;
            }
            if (ny > y) ny = y;
            if (ny < 2) ny = 2;
        }
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
            if (base.getType() == Material.AIR || base.getType() == Material.CAVE_AIR) return;
            base.setType(Material.WATER);
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
                        if (rel.getType() != Material.AIR && rel.getType() != Material.CAVE_AIR) {
                            rel.setType(Material.WATER);
                        }
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
            //block.setReplaceType(ReplaceType.ALL);
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
                    //double radiusSquared = Math.pow(trueRadius+noise.GetNoise(rel.getX(), rel.getY(), rel.getZ())*2,2);
                    double equationResult = Math.pow(x, 2) / Math.pow(rX, 2)
                            + Math.pow(y, 2) / Math.pow(rY, 2)
                            + Math.pow(z, 2) / Math.pow(rZ, 2);
                    if (equationResult <= 1 + 0.7 * noise.GetNoise(rel.getX(), rel.getY(), rel.getZ())) {
                        //if(rel.getLocation().distanceSquared(block.getLocation()) <= radiusSquared){
                        if (toReplace.contains(Material.BARRIER)) { //Blacklist
                            if (!toReplace.contains(rel.getType()))
                                if (block.getType() != Material.WATER || waterToAir)
                                    rel.setType(Material.CAVE_AIR);

                        } else if (toReplace.contains(rel.getType())) { //Whitelist
                            if (block.getType() != Material.WATER || waterToAir)
                                rel.setType(Material.CAVE_AIR);

                        } else if (!rel.getType().isSolid()) {
                            if (block.getType() != Material.WATER || waterToAir)
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

    public static void correctMultifacingData(SimpleBlock target) {
        if (!(target.getBlockData() instanceof MultipleFacing)) {
            if (Version.isAtLeast(16.1) && target.getType().name().endsWith(("_WALL"))) {
                org.terraform.coregen.v1_16_R1.BlockDataFixer.correctSurroundingWallData(target);
            }
            return;
        }

        MultipleFacing data = (MultipleFacing) target.getBlockData();
        for (BlockFace face : data.getAllowedFaces()) {
            Material type = target.getRelative(face).getType();
            data.setFace(face, type.isSolid() && !type.name().contains("PRESSURE_PLATE"));
        }
        target.setBlockData(data);
    }

    public static void correctSurroundingMultifacingData(SimpleBlock target) {
        if (!(target.getBlockData() instanceof MultipleFacing)) {
            if (Version.isAtLeast(16.1) && target.getType().name().endsWith(("_WALL"))) {
                org.terraform.coregen.v1_16_R1.BlockDataFixer.correctSurroundingWallData(target);
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
}
