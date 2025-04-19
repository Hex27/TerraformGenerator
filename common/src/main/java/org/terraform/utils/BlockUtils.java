package org.terraform.utils;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.*;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.Rail.Shape;
import org.bukkit.block.data.type.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.SimpleChunkLocation;
import org.terraform.data.Wall;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.main.config.TConfig;
import org.terraform.small_items.PlantBuilder;
import org.terraform.utils.blockdata.StairBuilder;
import org.terraform.utils.blockdata.fixers.v1_16_R1_BlockDataFixer;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.FastNoise.NoiseType;
import org.terraform.utils.version.V_1_19;
import org.terraform.utils.version.V_1_20;
import org.terraform.utils.version.V_1_21_5;
import org.terraform.utils.version.Version;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;

public class BlockUtils {

    // This is needed as REPLACABLE_BY_TREES is a 1.20 tag.
    // Also this has mushrooms and saplings, snow and coral fans
    public static final EnumSet<Material> replacableByTrees = EnumSet.of(
            Material.ACACIA_SAPLING,
            V_1_21_5.BUSH,
            V_1_21_5.FIREFLY_BUSH,
            V_1_21_5.WILDFLOWERS,
            V_1_21_5.LEAF_LITTER,
            Material.DARK_OAK_SAPLING,
            Material.BIRCH_SAPLING,
            Material.SPRUCE_SAPLING,
            Material.JUNGLE_SAPLING,
            Material.OAK_SAPLING,
            V_1_20.CHERRY_SAPLING,
            Material.ACACIA_LEAVES,
            Material.AZALEA_LEAVES,
            Material.DARK_OAK_LEAVES,
            Material.BIRCH_LEAVES,
            Material.SPRUCE_LEAVES,
            Material.JUNGLE_LEAVES,
            Material.OAK_LEAVES,
            V_1_20.CHERRY_LEAVES,
            Material.FLOWERING_AZALEA_LEAVES,
            Material.BROWN_MUSHROOM,
            Material.RED_MUSHROOM,
            Material.GRASS,
            Material.FERN,
            Material.DEAD_BUSH,
            Material.VINE,
            Material.GLOW_LICHEN,
            Material.SUNFLOWER,
            Material.LILAC,
            Material.ROSE_BUSH,
            Material.PEONY,
            Material.TALL_GRASS,
            Material.LARGE_FERN,
            Material.HANGING_ROOTS,
            V_1_20.PITCHER_PLANT,
            Material.WATER,
            Material.AIR,
            Material.CAVE_AIR,
            Material.SEAGRASS,
            Material.TALL_SEAGRASS,
            Material.WARPED_ROOTS,
            Material.NETHER_SPROUTS,
            Material.CRIMSON_ROOTS,
            Material.SNOW,
            Material.BRAIN_CORAL_FAN,
            Material.BUBBLE_CORAL_FAN,
            Material.FIRE_CORAL_FAN,
            Material.HORN_CORAL_FAN,
            Material.TUBE_CORAL_FAN,
            Material.BRAIN_CORAL_WALL_FAN,
            Material.BUBBLE_CORAL_WALL_FAN,
            Material.FIRE_CORAL_WALL_FAN,
            Material.HORN_CORAL_WALL_FAN,
            Material.TUBE_CORAL_WALL_FAN,
            Material.DEAD_BRAIN_CORAL_FAN,
            Material.DEAD_BUBBLE_CORAL_FAN,
            Material.DEAD_FIRE_CORAL_FAN,
            Material.DEAD_HORN_CORAL_FAN,
            Material.DEAD_TUBE_CORAL_FAN,
            Material.DEAD_BRAIN_CORAL_WALL_FAN,
            Material.DEAD_BUBBLE_CORAL_WALL_FAN,
            Material.DEAD_FIRE_CORAL_WALL_FAN,
            Material.DEAD_HORN_CORAL_WALL_FAN,
            Material.DEAD_TUBE_CORAL_WALL_FAN
    );
    // N
    // W E
    // S
    public static final BlockFace[] xzPlaneBlockFaces = new BlockFace[] {
            BlockFace.NORTH,
            BlockFace.NORTH_EAST,
            BlockFace.EAST,
            BlockFace.SOUTH_EAST,
            BlockFace.SOUTH,
            BlockFace.SOUTH_WEST,
            BlockFace.WEST,
            BlockFace.NORTH_WEST
    };
    public static final EnumSet<Material> fluids = EnumSet.of(Material.WATER, Material.LAVA);
    public static final EnumSet<Material> wetMaterials = EnumSet.of(Material.WATER,
            Material.KELP_PLANT,
            Material.SEAGRASS,
            Material.TALL_SEAGRASS
    );
    public static final EnumSet<Material> amethysts = EnumSet.of(Material.AMETHYST_BLOCK,
            Material.AMETHYST_CLUSTER,
            Material.BUDDING_AMETHYST,
            Material.LARGE_AMETHYST_BUD,
            Material.MEDIUM_AMETHYST_BUD,
            Material.SMALL_AMETHYST_BUD
    );
    public static final BlockFace[] flatBlockFaces3x3 = {
            BlockFace.SELF,
            BlockFace.NORTH,
            BlockFace.NORTH_EAST,
            BlockFace.EAST,
            BlockFace.SOUTH_EAST,
            BlockFace.SOUTH,
            BlockFace.SOUTH_WEST,
            BlockFace.WEST,
            BlockFace.NORTH_WEST
    };
    public static final BlockFace[] BLOCK_FACES = BlockFace.values();
    public static final BlockFace[] xzDiagonalPlaneBlockFaces = {
            BlockFace.NORTH_EAST,
            BlockFace.SOUTH_EAST,
            BlockFace.SOUTH_WEST,
            BlockFace.NORTH_WEST
    };
    public static final Material[] stoneBricks = {
            Material.STONE_BRICKS,
            Material.MOSSY_STONE_BRICKS,
            Material.CRACKED_STONE_BRICKS
    };
    public static final Material[] stoneBrickSlabs = {Material.STONE_BRICK_SLAB, Material.MOSSY_STONE_BRICK_SLAB};
    public static final BlockFace[] directBlockFaces = {
            BlockFace.NORTH,
            BlockFace.SOUTH,
            BlockFace.EAST,
            BlockFace.WEST
    };
    public static final BlockFace[][] cornerBlockFaces = {
            {BlockFace.NORTH, BlockFace.EAST},
            {BlockFace.NORTH, BlockFace.WEST},
            {BlockFace.SOUTH, BlockFace.EAST},
            {BlockFace.SOUTH, BlockFace.WEST},
            };
    public static final BlockFace[] sixBlockFaces = {
            BlockFace.NORTH,
            BlockFace.SOUTH,
            BlockFace.EAST,
            BlockFace.WEST,
            BlockFace.UP,
            BlockFace.DOWN
    };
    /**
     * Everything here <strong>must</strong> be solid blocks that tend to generate
     * in the ground.
     */
    public static final EnumSet<Material> stoneLike = EnumSet.of(Material.STONE,
            Material.COBBLESTONE,
            Material.MOSSY_COBBLESTONE,
            Material.GRANITE,
            Material.ANDESITE,
            Material.DIORITE,
            Material.GRAVEL,
            Material.CLAY,
            Material.DEEPSLATE,
            Material.TUFF,
            Material.CALCITE,
            Material.BUDDING_AMETHYST,
            Material.AMETHYST_BLOCK,
            Material.DRIPSTONE_BLOCK,
            Material.SMOOTH_BASALT,
            Material.PACKED_ICE,
            Material.BLUE_ICE,
            Material.DIRT,
            Material.PODZOL,
            Material.GRASS_BLOCK,
            Material.MYCELIUM,
            Material.ROOTED_DIRT,
            Material.DIRT_PATH,
            V_1_19.SCULK
    );
    public static final EnumSet<Material> caveDecoratorMaterials = EnumSet.of(Material.ANDESITE_WALL,
            Material.DIORITE_WALL,
            Material.GRANITE_WALL,
            Material.COBBLESTONE_WALL,
            Material.MOSSY_COBBLESTONE_WALL,
            Material.MOSSY_COBBLESTONE_SLAB,
            Material.COBBLED_DEEPSLATE_WALL,
            Material.COBBLESTONE_SLAB,
            Material.STONE_SLAB,
            Material.COBBLED_DEEPSLATE_SLAB,
            Material.MOSS_BLOCK,
            Material.MOSS_CARPET,
            Material.CAVE_VINES,
            Material.CAVE_VINES_PLANT,
            Material.HANGING_ROOTS,
            Material.SPORE_BLOSSOM,
            Material.SMALL_DRIPLEAF,
            Material.AZALEA,
            Material.FLOWERING_AZALEA,
            Material.BIG_DRIPLEAF,
            Material.BIG_DRIPLEAF_STEM,
            Material.GRASS,
            Material.TALL_GRASS,
            Material.ICE,
            Material.PACKED_ICE,
            Material.DRIPSTONE_BLOCK,
            Material.POINTED_DRIPSTONE,
            Material.AMETHYST_CLUSTER,
            Material.BUDDING_AMETHYST,
            Material.GLOW_LICHEN,

            // ItemsAdder Blocks
            Material.NOTE_BLOCK,
            Material.SPAWNER,
            Material.BROWN_MUSHROOM_BLOCK,
            Material.RED_MUSHROOM_BLOCK
    );
    // This enumset gets populated more in initBlockUtils
    public static final EnumSet<Material> badlandsStoneLike = EnumSet.of(Material.TERRACOTTA,
            Material.ORANGE_TERRACOTTA,
            Material.RED_TERRACOTTA,
            Material.BROWN_TERRACOTTA,
            Material.YELLOW_TERRACOTTA,
            Material.RED_SAND
    );
    public static final EnumSet<Material> caveCarveReplace = EnumSet.of(Material.NOTE_BLOCK);
    // This enumset gets populated more in initBlockUtils
    public static final EnumSet<Material> ores = EnumSet.noneOf(Material.class);
    public static final EnumSet<Material> airs = EnumSet.of(Material.AIR, Material.CAVE_AIR);
    // This enumset gets populated more in initBlockUtils
    public static final EnumSet<Material> glassPanes = EnumSet.noneOf(Material.class);
    public static final Material[] WOOLS = {
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
    public static final Material[] TERRACOTTA = {
            Material.WHITE_TERRACOTTA,
            Material.BLACK_TERRACOTTA,
            Material.BLUE_TERRACOTTA,
            Material.BROWN_TERRACOTTA,
            Material.CYAN_TERRACOTTA,
            Material.GRAY_TERRACOTTA,
            Material.GREEN_TERRACOTTA,
            Material.LIGHT_BLUE_TERRACOTTA,
            Material.LIGHT_GRAY_TERRACOTTA,
            Material.LIME_TERRACOTTA,
            Material.MAGENTA_TERRACOTTA,
            Material.ORANGE_TERRACOTTA,
            Material.PINK_TERRACOTTA,
            Material.PURPLE_TERRACOTTA,
            Material.RED_TERRACOTTA,
            Material.YELLOW_TERRACOTTA
    };
    private static final PlantBuilder[] TALL_FLOWER = {
            PlantBuilder.LILAC,
            PlantBuilder.ROSE_BUSH,
            PlantBuilder.PEONY,
            PlantBuilder.LARGE_FERN,
            PlantBuilder.SUNFLOWER
    };
    private static final PlantBuilder[] FLOWER = {
            PlantBuilder.DANDELION,
            PlantBuilder.POPPY,
            PlantBuilder.WHITE_TULIP,
            PlantBuilder.ORANGE_TULIP,
            PlantBuilder.RED_TULIP,
            PlantBuilder.PINK_TULIP,
            PlantBuilder.BLUE_ORCHID,
            PlantBuilder.ALLIUM,
            PlantBuilder.AZURE_BLUET,
            PlantBuilder.OXEYE_DAISY,
            PlantBuilder.CORNFLOWER,
            PlantBuilder.LILY_OF_THE_VALLEY,
            };
    private static final PlantBuilder[] POTTED = {
            PlantBuilder.POTTED_DANDELION,
            PlantBuilder.POTTED_POPPY,
            PlantBuilder.POTTED_WHITE_TULIP,
            PlantBuilder.POTTED_ORANGE_TULIP,
            PlantBuilder.POTTED_RED_TULIP,
            PlantBuilder.POTTED_PINK_TULIP,
            PlantBuilder.POTTED_BLUE_ORCHID,
            PlantBuilder.POTTED_ALLIUM,
            PlantBuilder.POTTED_AZURE_BLUET,
            PlantBuilder.POTTED_OXEYE_DAISY,
            PlantBuilder.POTTED_CORNFLOWER,
            PlantBuilder.POTTED_LILY_OF_THE_VALLEY,
            };
    private static final Material[] CARPETS = {
            Material.WHITE_CARPET,
            Material.BLACK_CARPET,
            Material.BLUE_CARPET,
            Material.BROWN_CARPET,
            Material.CYAN_CARPET,
            Material.GRAY_CARPET,
            Material.GREEN_CARPET,
            Material.LIGHT_BLUE_CARPET,
            Material.LIGHT_GRAY_CARPET,
            Material.LIME_CARPET,
            Material.MAGENTA_CARPET,
            Material.ORANGE_CARPET,
            Material.PINK_CARPET,
            Material.PURPLE_CARPET,
            Material.RED_CARPET,
            Material.YELLOW_CARPET
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
    private static final HashMap<String, BlockData> deepslateMap = new HashMap<>();

    public static void initBlockUtils() {
        // init ores
        for (Material mat : Material.values()) {
            if (mat.toString().endsWith("_ORE")) {
                if (!mat.toString().contains("NETHER")) {
                    ores.add(mat);
                }
                stoneLike.add(mat);
            }
        }
        if(Version.isAtLeast(19)){
            caveDecoratorMaterials.add(V_1_19.SCULK);
            caveDecoratorMaterials.add(V_1_19.SCULK_SENSOR);
            caveDecoratorMaterials.add(V_1_19.SCULK_SHRIEKER);
            caveDecoratorMaterials.add(V_1_19.SCULK_VEIN);
            caveDecoratorMaterials.add(V_1_19.SCULK_CATALYST);
        }
        badlandsStoneLike.addAll(stoneLike);
        caveCarveReplace.addAll(badlandsStoneLike);
        caveCarveReplace.addAll(caveDecoratorMaterials);
        for(PlantBuilder pb:FLOWER)
            replacableByTrees.add(pb.material);

        // init glass panes
        for (Material mat : Material.values()) {
            if (mat.toString().endsWith("_GLASS_PANE")) {
                glassPanes.add(mat);
            }
        }
    }

    public static boolean isDirectBlockFace(@NotNull BlockFace facing) {
        return switch (facing) {
            case NORTH, SOUTH, EAST, WEST -> true;
            default -> false;
        };
    }

    /**
     * @return rotates original block face (NSEW only) clockwise the specified number of times
     */
    @SuppressWarnings("incomplete-switch")
    public static @NotNull BlockFace rotateFace(@NotNull BlockFace original, int times) {
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
    public static @NotNull BlockFace rotateXZPlaneBlockFace(@NotNull BlockFace original, int times) {
        //	N
        // W + E
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

    /**
     * @return rotates original block face (XZ plane only) clockwise the specified number of times
     */
    @SuppressWarnings("incomplete-switch")
    public static @NotNull BlockFace rotateXZPlaneBlockFaceOppositeAngle(@NotNull BlockFace original, int times) {
        //	N
        // W + E
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

    public static BlockFace @NotNull [] getRandomBlockfaceAxis(@NotNull Random rand) {
        if (rand.nextInt(2) == 0) {
            return new BlockFace[] {BlockFace.NORTH, BlockFace.SOUTH};
        }
        else {
            return new BlockFace[] {BlockFace.WEST, BlockFace.EAST};
        }
    }

    public static Material stoneBrick(@NotNull Random rand) {
        return GenUtils.randChoice(rand, stoneBricks);
    }

    public static Material stoneBrickSlab(@NotNull Random rand) {
        return GenUtils.randChoice(rand, stoneBrickSlabs);
    }

    public static BlockFace getXZPlaneBlockFace(@NotNull Random rand) {
        return xzPlaneBlockFaces[rand.nextInt(8)];
    }

    public static @Nullable BlockFace getBlockFaceFromAxis(@NotNull Axis ax) {
        return switch (ax) {
            case X -> BlockFace.EAST;
            case Z -> BlockFace.SOUTH;
            case Y -> BlockFace.UP;
        };
    }

    public static @NotNull Axis getAxisFromBlockFace(@NotNull BlockFace face) {
        return switch (face) {
            case NORTH, SOUTH -> Axis.Z;
            case EAST, WEST -> Axis.X;
            case UP, DOWN -> Axis.Y;
            default -> throw new IllegalArgumentException("Invalid block facing for axis: " + face);
        };
    }

    public static @NotNull Axis getPerpendicularHorizontalPlaneAxis(@NotNull Axis x) {
        return switch (x) {
            case X -> Axis.Z;
            case Z -> Axis.X;
            default -> x;
        };

    }

    public static BlockFace getDirectBlockFace(@NotNull Random rand) {
        return directBlockFaces[rand.nextInt(4)];
    }

    public static BlockFace getSixBlockFace(@NotNull Random rand) {
        return sixBlockFaces[rand.nextInt(6)];
    }

    public static Material pickCarpet() {
        return GenUtils.randChoice(CARPETS);
    }

    public static Material pickWool() {
        return GenUtils.randChoice(WOOLS);
    }

    public static Material pickBed() {
        return GenUtils.randChoice(BED);
    }

    public static PlantBuilder pickFlower() {
        return pickFlower(GenUtils.RANDOMIZER);
    }
    public static PlantBuilder pickFlower(Random rand) {
        return GenUtils.randChoice(rand, FLOWER);
    }

    public static PlantBuilder pickPottedPlant() {
        return GenUtils.randChoice(POTTED);
    }

    public static PlantBuilder pickTallFlower() {
        return GenUtils.randChoice(TALL_FLOWER);
    }

    public static void dropDownBlock(@NotNull SimpleBlock block) {
        dropDownBlock(block, Material.CAVE_AIR);
    }

    public static void dropDownBlock(@NotNull SimpleBlock block, @NotNull Material fluid) {
        if (block.isSolid()) {
            Material type = block.getType();
            block.setType(fluid);
            int depth = 0;
            while (!block.isSolid()) {
                block = block.getDown();
                depth++;
                if (depth > 50) {
                    return;
                }
            }
            block.getUp().setType(type);
        }
    }

    public static void horizontalGlazedTerracotta(@NotNull PopulatorDataAbstract data,
                                                  int x,
                                                  int y,
                                                  int z,
                                                  @NotNull Material glazedTerracotta)
    {
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

    public static void setVines(@NotNull PopulatorDataAbstract data, int x, int y, int z, int maxLength) {
        if (!TConfig.arePlantsEnabled()) {
            return;
        }

        SimpleBlock rel = new SimpleBlock(data, x, y, z);
        for (BlockFace face : directBlockFaces) {
            MultipleFacing dir = (MultipleFacing) Bukkit.createBlockData(Material.VINE);
            dir.setFace(face.getOppositeFace(), true);
            SimpleBlock vine = rel.getRelative(face);
            if (vine.isSolid()) {
                continue;
            }

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

    public static void setDownUntilSolid(int x, int y, int z, @NotNull PopulatorDataAbstract data, Material... type) {
        while (!data.getType(x, y, z).isSolid()) {
            data.setType(x, y, z, GenUtils.randChoice(type));
            y--;
        }
    }

    public static void downPillar(int x,
                                  int y,
                                  int z,
                                  int height,
                                  @NotNull PopulatorDataAbstract data,
                                  Material... type)
    {
        while (!data.getType(x, y, z).isSolid() && height > TerraformGeneratorPlugin.injector.getMinY()) {
            data.setType(x, y, z, GenUtils.randChoice(type));
            height--;
            y--;
        }
    }

    public static boolean isStoneLike(@NotNull Material mat) {
        return isDirtLike(mat) || stoneLike.contains(mat) || ores.contains(mat);
    }

    public static boolean isDirtLike(@NotNull Material mat) {
        return switch (mat) {
            case DIRT, GRASS_BLOCK, PODZOL, COARSE_DIRT, MYCELIUM -> true;
            default -> mat == Material.DIRT_PATH || mat == Material.ROOTED_DIRT;
        };
    }

    public static void setPersistentLeaves(@NotNull PopulatorDataAbstract data, int x, int y, int z) {
        if (!TConfig.arePlantsEnabled()) {
            return;
        }

        setPersistentLeaves(data, x, y, z, Material.OAK_LEAVES);
    }

    public static void setPersistentLeaves(@NotNull PopulatorDataAbstract data,
                                           int x,
                                           int y,
                                           int z,
                                           @NotNull Material type)
    {
        if (!TConfig.arePlantsEnabled()) {
            return;
        }

        data.setType(x, y, z, Material.OAK_LEAVES);
        Leaves bd = (Leaves) Bukkit.createBlockData(type);
        bd.setPersistent(true);
        data.setBlockData(x, y, z, bd);
    }

    public static void setDoublePlant(@NotNull PopulatorDataAbstract data,
                                      int x,
                                      int y,
                                      int z,
                                      @NotNull Material doublePlant)
    {
        if (!TConfig.arePlantsEnabled()) {
            return;
        }
        if(data.getType(x,y,z) != Material.AIR
            || data.getType(x,y+1,z) != Material.AIR) return;

        Bisected d = ((Bisected) Bukkit.createBlockData(doublePlant));
        d.setHalf(Half.BOTTOM);
        data.lsetBlockData(x, y, z, d);

        d = ((Bisected) Bukkit.createBlockData(doublePlant));
        d.setHalf(Half.TOP);
        data.lsetBlockData(x, y + 1, z, d);
    }

    public static boolean isSameChunk(@NotNull Block a, @NotNull Block b) {
        return SimpleChunkLocation.of(a).equals(SimpleChunkLocation.of(b));
    }

    public static boolean areAdjacentChunksLoaded(@NotNull Chunk middle) {
        for (int nx = -1; nx <= 1; nx++) {
            for (int nz = -1; nz <= 1; nz++) {
                int x = middle.getX() + nx;
                int z = middle.getZ() + nz;
                if (!middle.getWorld().isChunkLoaded(x, z)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static @NotNull Material getTerracotta(int height) {
        int mapped = (height + 10) % 17;

        if (mapped == 2 || mapped == 9 || mapped == 13 || mapped == 16) {
            return Material.TERRACOTTA;
        }
        if (mapped == 4 || mapped == 5 || mapped == 12 || mapped == 15) {
            return Material.RED_TERRACOTTA;
        }
        if (mapped == 6) {
            return Material.YELLOW_TERRACOTTA;
        }
        if (mapped == 8) {
            return Material.BROWN_TERRACOTTA;
        }

        return Material.ORANGE_TERRACOTTA;
    }

    public static int spawnPillar(@NotNull Random rand,
                                  @NotNull PopulatorDataAbstract data,
                                  int x,
                                  int y,
                                  int z,
                                  Material type,
                                  int minHeight,
                                  int maxHeight)
    {
        // TODO: PlantBuilder
        int height = GenUtils.randInt(rand, minHeight, maxHeight);
        for (int i = 0; i < height; i++) {
            data.setType(x, y + i, z, type);
        }
        return height;
    }

    public static void generateClayDeposit(int x,
                                           int y,
                                           int z,
                                           @NotNull PopulatorDataAbstract data,
                                           @NotNull Random random)
    {
        // CLAY DEPOSIT
        replaceCircularPatch(
                random.nextInt(9999),
                TConfig.c.BIOME_CLAY_DEPOSIT_SIZE,
                new SimpleBlock(data, x, y, z),
                Material.CLAY
        );

    }

    public static void vineUp(@NotNull SimpleBlock base, int maxLength) {
        if (!TConfig.arePlantsEnabled()) {
            return;
        }

        for (BlockFace face : directBlockFaces) {
            MultipleFacing dir = (MultipleFacing) Bukkit.createBlockData(Material.VINE);
            dir.setFace(face.getOppositeFace(), true);
            SimpleBlock vine = base.getRelative(face);
            if (vine.isSolid()) {
                continue;
            }

            vine.setType(Material.VINE);
            vine.setBlockData(dir);
            for (int i = 1; i < GenUtils.randInt(1, maxLength); i++) {
                SimpleBlock relative = vine.getRelative(0, -i, 0);
                if (relative.getType() != Material.AIR) {
                    break;
                }
                relative.setType(Material.VINE);
                relative.setBlockData(dir);
            }
        }
    }

    /**
     * Places a noise-fuzzed circle of the defined material
     */
    public static void replaceCircle(int seed, float radius, @NotNull SimpleBlock base, Material... type) {
        if (radius <= 0) {
            return;
        }
        if (radius <= 0.5) {
            // block.setReplaceType(ReplaceType.ALL);
            base.setType(GenUtils.randChoice(new Random(seed), type));
            return;
        }

        FastNoise noise = new FastNoise(seed);
        noise.SetNoiseType(NoiseType.Simplex);
        noise.SetFrequency(0.09f);

        for (float x = -radius; x <= radius; x++) {
            for (float z = -radius; z <= radius; z++) {
                SimpleBlock rel = base.getRelative(Math.round(x), 0, Math.round(z));

                // double radiusSquared = Math.pow(trueRadius+noise.GetNoise(rel.getX(), rel.getY(), rel.getZ())*2,2);
                double equationResult = Math.pow(x, 2) / Math.pow(radius, 2) + Math.pow(z, 2) / Math.pow(radius, 2);
                if (equationResult <= 1 + 0.7 * noise.GetNoise(rel.getX(), rel.getZ())) {
                    // if(rel.getLocation().distanceSquared(block.getLocation()) <= radiusSquared){
                    rel.lsetType(GenUtils.randChoice(type));
                }
            }
        }

    }

    public static void replaceCircularPatch(int seed, float radius, @NotNull SimpleBlock base, Material... type) {
        replaceCircularPatch(seed, radius, base, false, type);
    }

    /**
     * Replaces the highest ground with a noise-fuzzed circle of the defined material
     */
    public static void replaceCircularPatch(int seed,
                                            float radius,
                                            @NotNull SimpleBlock base,
                                            boolean snowy,
                                            Material... type)
    {
        if (radius <= 0) {
            return;
        }
        if (radius <= 0.5) {
            // block.setReplaceType(ReplaceType.ALL);
            base.setType(GenUtils.randChoice(new Random(seed), type));
            return;
        }

        FastNoise noise = new FastNoise(seed);
        noise.SetNoiseType(NoiseType.Simplex);
        noise.SetFrequency(0.09f);

        for (float x = -radius; x <= radius; x++) {
            for (float z = -radius; z <= radius; z++) {
                SimpleBlock rel = base.getRelative(Math.round(x), 0, Math.round(z));
                rel = rel.getGround();

                // double radiusSquared = Math.pow(trueRadius+noise.GetNoise(rel.getX(), rel.getY(), rel.getZ())*2,2);
                double equationResult = Math.pow(x, 2) / Math.pow(radius, 2) + Math.pow(z, 2) / Math.pow(radius, 2);
                if (equationResult <= 1 + 0.7 * noise.GetNoise(rel.getX(), rel.getZ())) {
                    // if(rel.getLocation().distanceSquared(block.getLocation()) <= radiusSquared){
                    rel.setType(GenUtils.randChoice(type));
                    if (snowy && rel.getUp().isAir()) {
                        rel.getUp().setType(Material.SNOW);
                    }
                }
            }
        }
    }

    /**
     * Passes the highest ground to a lambda function in a circle
     */
    public static void lambdaCircularPatch(int seed,
                                            float radius,
                                            @NotNull SimpleBlock base,
                                           Consumer<@NotNull SimpleBlock> lambda)
    {
        if (radius <= 0) {
            return;
        }
        if (radius <= 0.5) {
            lambda.accept(base);
            return;
        }

        FastNoise noise = new FastNoise(seed);
        noise.SetNoiseType(NoiseType.Simplex);
        noise.SetFrequency(0.09f);

        for (float x = -radius; x <= radius; x++) {
            for (float z = -radius; z <= radius; z++) {
                SimpleBlock rel = base.getRelative(Math.round(x), 0, Math.round(z));
                rel = rel.getGround();

                double equationResult = Math.pow(x, 2) / Math.pow(radius, 2) + Math.pow(z, 2) / Math.pow(radius, 2);
                if (equationResult <= 1 + 0.7 * noise.GetNoise(rel.getX(), rel.getZ())) {
                    lambda.accept(rel);
                }
            }
        }
    }

    public static void replaceSphere(int seed,
                                     float radius,
                                     @NotNull SimpleBlock base,
                                     boolean hardReplace,
                                     Material... type)
    {
        if (radius > 0) {
            replaceSphere(seed, radius, radius, radius, base, hardReplace, type);
        }
    }

    public static void replaceSphere(int seed,
                                     float rX,
                                     float rY,
                                     float rZ,
                                     @NotNull SimpleBlock block,
                                     boolean hardReplace,
                                     Material... type)
    {
        replaceSphere(seed, rX, rY, rZ, block, hardReplace, false, type);
    }

    public static void replaceWaterSphere(int seed, float radius, @NotNull SimpleBlock base) {
        if (radius <= 0) {
            return;
        }
        if (radius <= 0.5) {
            // block.setReplaceType(ReplaceType.ALL);
            if (base.getY() <= TerraformGenerator.seaLevel) {
                base.setType(Material.WATER);
            }
            else {
                base.setType(Material.AIR);
            }
            return;
        }

        FastNoise noise = new FastNoise(seed);
        noise.SetNoiseType(NoiseType.Simplex);
        noise.SetFrequency(0.09f);

        for (float x = -radius; x <= radius; x++) {
            for (float y = -radius; y <= radius; y++) {
                for (float z = -radius; z <= radius; z++) {

                    SimpleBlock rel = base.getRelative(Math.round(x), Math.round(y), Math.round(z));
                    // double radiusSquared = Math.pow(trueRadius+noise.GetNoise(rel.getX(), rel.getY(), rel.getZ())*2,2);
                    double equationResult = Math.pow(x, 2) / Math.pow(radius, 2)
                                            + Math.pow(y, 2) / Math.pow(radius, 2)
                                            + Math.pow(z, 2) / Math.pow(radius, 2);
                    if (equationResult <= 1 + 0.7 * noise.GetNoise(rel.getX(), rel.getY(), rel.getZ())) {
                        // if(rel.getLocation().distanceSquared(block.getLocation()) <= radiusSquared){
                        if (rel.getY() <= TerraformGenerator.seaLevel) {
                            rel.setType(Material.WATER);
                        }
                        else {
                            rel.setType(Material.AIR);
                        }
                    }
                }
            }
        }
    }

    public static void carveCaveAir(int seed,
                                    float rX,
                                    float rY,
                                    float rZ,
                                    @NotNull SimpleBlock block,
                                    boolean waterToAir,
                                    @NotNull EnumSet<Material> toReplace)
    {
        carveCaveAir(seed, rX, rY, rZ, 0.09f, block, waterToAir, toReplace);
    }

    public static void carveCaveAir(int seed,
                                    float rX,
                                    float rY,
                                    float rZ,
                                    float frequency,
                                    @NotNull SimpleBlock block,
                                    boolean waterToAir,
                                    @NotNull EnumSet<Material> toReplace)
    {
        carveCaveAir(seed, rX, rY, rZ, frequency, block, false, waterToAir, toReplace);
    }

    /**
     * Put barrier in toReplace to hard replace all solid blocks.
     */
    public static void carveCaveAir(int seed,
                                    float rX,
                                    float rY,
                                    float rZ,
                                    float frequency,
                                    @NotNull SimpleBlock block,
                                    boolean blockWaterHoles,
                                    boolean waterToAir,
                                    @NotNull EnumSet<Material> toReplace)
    {
        if (rX <= 0 && rY <= 0 && rZ <= 0) {
            return;
        }
        if (rX <= 0.5 && rY <= 0.5 && rZ <= 0.5) {
            if (waterToAir || block.getType() != Material.WATER) {
                block.setType(Material.CAVE_AIR);
            }
            return;
        }

        FastNoise noise = new FastNoise(seed);
        noise.SetNoiseType(NoiseType.Simplex);
        noise.SetFrequency(frequency);

        for (float x = -rX * 1.3f; x <= rX * 1.3f; x++) {
            for (float y = -rY; y <= rY; y++) {
                for (float z = -rZ * 1.3f; z <= rZ * 1.3f; z++) {
                    SimpleBlock rel = block.getRelative(Math.round(x), Math.round(y), Math.round(z));
                    double equationResult = Math.pow(x, 2) / Math.pow(rX, 2)
                                            + Math.pow(y, 2) / Math.pow(rY, 2)
                                            + Math.pow(z, 2) / Math.pow(rZ, 2);
                    double noiseVal = 1 + 0.7 * noise.GetNoise(rel.getX(), rel.getY(), rel.getZ());
                    if (equationResult <= noiseVal) {
                        if (toReplace.contains(Material.BARRIER)) { // Blacklist
                            if (!toReplace.contains(rel.getType())) {
                                if (!isWet(rel) || waterToAir) {
                                    rel.physicsSetType(Material.CAVE_AIR, false);
                                }
                            }

                        }
                        else if (toReplace.contains(rel.getType())) { // Whitelist
                            if (!isWet(rel) || waterToAir) {
                                rel.physicsSetType(Material.CAVE_AIR, false);
                            }

                        }
                        else if (!rel.isSolid()) {
                            if (!isWet(rel) || waterToAir) {
                                rel.physicsSetType(Material.CAVE_AIR, false);
                            }
                        }

                        // Patches found water holes
                        if (blockWaterHoles) {
                            for (BlockFace face : BlockUtils.sixBlockFaces) {
                                SimpleBlock relrel = rel.getRelative(face);
                                if (isWet(relrel) || relrel.getType() == Material.LAVA) {
                                    Material setMat = relrel.getY() < 0 ? Material.DEEPSLATE : Material.STONE;
                                    relrel.physicsSetType(setMat, false);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static void replaceSphere(int seed,
                                     float rX,
                                     float rY,
                                     float rZ,
                                     @NotNull SimpleBlock block,
                                     boolean hardReplace,
                                     boolean snowy,
                                     Material... type)
    {
        if (rX <= 0 && rY <= 0 && rZ <= 0) {
            return;
        }
        if (rX <= 0.5 && rY <= 0.5 && rZ <= 0.5) {
            // block.setReplaceType(ReplaceType.ALL);
            block.setType(GenUtils.randChoice(new Random(seed), type));
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
                    // double radiusSquared = Math.pow(trueRadius+noise.GetNoise(rel.getX(), rel.getY(), rel.getZ())*2,2);
                    double equationResult = Math.pow(x, 2) / Math.pow(rX, 2)
                                            + Math.pow(y, 2) / Math.pow(rY, 2)
                                            + Math.pow(z, 2) / Math.pow(rZ, 2);
                    if (equationResult <= 1 + 0.7 * noise.GetNoise(rel.getX(), rel.getY(), rel.getZ())) {
                        // if(rel.getLocation().distanceSquared(block.getLocation()) <= radiusSquared){
                        if (hardReplace || !rel.isSolid()) {
                            rel.setType(GenUtils.randChoice(rand, type));
                            if (snowy) {
                                rel.getUp().lsetType(Material.SNOW);
                            }
                        }
                    }
                }
            }
        }
    }

    public static void replaceUpperSphere(int seed,
                                          float rX,
                                          float rY,
                                          float rZ,
                                          @NotNull SimpleBlock block,
                                          boolean hardReplace,
                                          Material... type)
    {
        if (rX <= 0 && rY <= 0 && rZ <= 0) {
            return;
        }
        if (rX <= 0.5 && rY <= 0.5 && rZ <= 0.5) {
            // block.setReplaceType(ReplaceType.ALL);
            block.setType(GenUtils.randChoice(new Random(seed), type));
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
                    // double radiusSquared = Math.pow(trueRadius+noise.GetNoise(rel.getX(), rel.getY(), rel.getZ())*2,2);
                    double equationResult = Math.pow(x, 2) / Math.pow(rX, 2)
                                            + Math.pow(y, 2) / Math.pow(rY, 2)
                                            + Math.pow(z, 2) / Math.pow(rZ, 2);
                    if (equationResult <= 1 + 0.7 * noise.GetNoise(rel.getX(), rel.getY(), rel.getZ())) {
                        // if(rel.getLocation().distanceSquared(block.getLocation()) <= radiusSquared){
                        if (hardReplace || !rel.isSolid()) {
                            rel.setType(GenUtils.randChoice(rand, type));
                        }
                        // rel.setReplaceType(ReplaceType.ALL);
                    }
                }
            }
        }
    }

    public static void replaceLowerSphere(int seed,
                                          float rX,
                                          float rY,
                                          float rZ,
                                          @NotNull SimpleBlock block,
                                          boolean hardReplace,
                                          Material... type)
    {
        if (rX <= 0 && rY <= 0 && rZ <= 0) {
            return;
        }
        if (rX <= 0.5 && rY <= 0.5 && rZ <= 0.5) {
            // block.setReplaceType(ReplaceType.ALL);
            block.setType(GenUtils.randChoice(new Random(seed), type));
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
                    // double radiusSquared = Math.pow(trueRadius+noise.GetNoise(rel.getX(), rel.getY(), rel.getZ())*2,2);
                    double equationResult = Math.pow(x, 2) / Math.pow(rX, 2)
                                            + Math.pow(y, 2) / Math.pow(rY, 2)
                                            + Math.pow(z, 2) / Math.pow(rZ, 2);
                    if (equationResult <= 1 + 0.7 * noise.GetNoise(rel.getX(), rel.getY(), rel.getZ())) {
                        // if(rel.getLocation().distanceSquared(block.getLocation()) <= radiusSquared){
                        if (hardReplace || !rel.isSolid()) {
                            rel.setType(GenUtils.randChoice(rand, type));
                        }
                        // rel.setReplaceType(ReplaceType.ALL);
                    }
                }
            }
        }
    }

    public static BlockFace @NotNull [] getAdjacentFaces(@NotNull BlockFace original) {
        //   N
        // W    E
        //   S
        return switch (original) {
            case EAST -> new BlockFace[] {BlockFace.SOUTH, BlockFace.NORTH};
            case NORTH -> new BlockFace[] {BlockFace.EAST, BlockFace.WEST};
            case SOUTH -> new BlockFace[] {BlockFace.WEST, BlockFace.EAST};
            default -> new BlockFace[] {BlockFace.NORTH, BlockFace.SOUTH};
        };
    }

    public static BlockFace getTurnBlockFace(@NotNull Random rand, @NotNull BlockFace original) {
        return getAdjacentFaces(original)[GenUtils.randInt(rand, 0, 1)];
    }

    public static BlockFace getLeft(@NotNull BlockFace original) {
        return getAdjacentFaces(original)[0];
    }

    public static BlockFace getRight(@NotNull BlockFace original) {
        return getAdjacentFaces(original)[1];
    }

    public static void correctMultifacingData(@NotNull SimpleBlock target) {
        if (!(target.getBlockData() instanceof MultipleFacing data)) {
            if (Tag.WALLS.isTagged(target.getType())) {
                v1_16_R1_BlockDataFixer.correctSurroundingWallData(target);
            }
            return;
        }

        for (BlockFace face : data.getAllowedFaces()) {
            Material type = target.getRelative(face).getType();
            boolean facing = type.isSolid()
                             && !Tag.PRESSURE_PLATES.isTagged(type)
                             && !Tag.BANNERS.isTagged(type)
                             && !Tag.SLABS.isTagged(type)
                             && !Tag.TRAPDOORS.isTagged(type);
            if (glassPanes.contains(target.getType()) && (Tag.FENCE_GATES.isTagged(type)
                                                          || Tag.FENCES.isTagged(type)))
            {
                facing = false;
            }
            data.setFace(face, facing);
            if (Tag.STAIRS.isTagged(type)) {
                Stairs stairs = (Stairs) target.getRelative(face).getBlockData();
                data.setFace(face, stairs.getFacing() == face.getOppositeFace());
            }
        }
        target.setBlockData(data);
    }

    public static boolean isExposedToNonSolid(@NotNull SimpleBlock target) {
        for (BlockFace face : directBlockFaces) {
            if (!target.getRelative(face).isSolid()) {
                return true;
            }
        }
        return false;
    }

    public static boolean isExposedToMaterial(@NotNull SimpleBlock target, @NotNull Set<Material> mats) {
        for (BlockFace face : directBlockFaces) {
            if (mats.contains(target.getRelative(face).getType())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isExposedToMaterial(@NotNull SimpleBlock target, Material mat) {
        for (BlockFace face : directBlockFaces) {
            if (target.getRelative(face).getType() == mat) {
                return true;
            }
        }
        return false;
    }

    /**
     * Correct fencse for example
     */
    public static void correctSurroundingMultifacingData(@NotNull SimpleBlock target) {
        if (!(target.getBlockData() instanceof MultipleFacing)) {
            if (Version.isAtLeast(16.1) && Tag.WALLS.isTagged(target.getType())) {
                v1_16_R1_BlockDataFixer.correctSurroundingWallData(target);
            }
            return;
        }

        correctMultifacingData(target);
        if (!(target.getBlockData() instanceof MultipleFacing data)) {
            return;
        }

        for (BlockFace face : data.getAllowedFaces()) {
            if (target.getRelative(face).getBlockData() instanceof MultipleFacing) {
                correctMultifacingData(target.getRelative(face));
            }
        }
    }

    public static void correctStairData(@NotNull SimpleBlock target) {
        if (!(target.getBlockData() instanceof Stairs data)) {
            return;
        }

        BlockFace left = BlockUtils.getLeft(data.getFacing());
        BlockFace right = BlockUtils.getRight(data.getFacing());

        // Left is a stair and right isn't
        if (Tag.STAIRS.isTagged(target.getRelative(left).getType()) && !Tag.STAIRS.isTagged(target.getRelative(right)
                                                                                                  .getType()))
        {

            // Only adjust if the left side has the same facing.
            if (((Stairs) target.getRelative(left).getBlockData()).getFacing() == data.getFacing()) {

                // Back is a stair
                if (Tag.STAIRS.isTagged(target.getRelative(data.getFacing()).getType())) {

                    // Only set if the back stair is facing a valid location
                    if (((Stairs) target.getRelative(data.getFacing()).getBlockData()).getFacing()
                        == getLeft(data.getFacing()))
                    {
                        data.setShape(Stairs.Shape.OUTER_RIGHT);
                    }

                    // Front is a stair
                }
                else if (Tag.STAIRS.isTagged(target.getRelative(data.getFacing().getOppositeFace()).getType())) {

                    // Only set if the front stair is facing a valid location
                    if (((Stairs) target.getRelative(data.getFacing().getOppositeFace()).getBlockData()).getFacing()
                        == getRight(data.getFacing()))
                    {
                        data.setShape(Stairs.Shape.INNER_RIGHT);
                    }
                }
            }

            // Right is a stair and left isn't.
        }
        else if (!Tag.STAIRS.isTagged(target.getRelative(left).getType()) && Tag.STAIRS.isTagged(target.getRelative(
                right).getType()))
        {

            // Only adjust if the right side has the same facing.
            if (((Stairs) target.getRelative(right).getBlockData()).getFacing() == data.getFacing()) {

                // Back is a stair
                if (Tag.STAIRS.isTagged(target.getRelative(data.getFacing()).getType())) {

                    // Only set if the back stair is facing a valid location
                    if (((Stairs) target.getRelative(data.getFacing()).getBlockData()).getFacing()
                        == getRight(data.getFacing()))
                    {
                        data.setShape(Stairs.Shape.OUTER_LEFT);
                    }

                    // Front is a stair
                }
                else if (Tag.STAIRS.isTagged(target.getRelative(data.getFacing().getOppositeFace()).getType())) {

                    // Only set if the front stair is facing a valid location
                    if (((Stairs) target.getRelative(data.getFacing().getOppositeFace()).getBlockData()).getFacing()
                        == getLeft(data.getFacing()))
                    {
                        data.setShape(Stairs.Shape.INNER_LEFT);
                    }
                }
            }

            // Right is a stair and left isn't.
        }
        target.setBlockData(data);
    }

    public static void correctSurroundingStairData(@NotNull SimpleBlock target) {
        if (!(target.getBlockData() instanceof Stairs data)) {
            return;
        }

        correctStairData(target);
        for (BlockFace face : getAdjacentFaces(data.getFacing())) {
            if (target.getRelative(face).getBlockData() instanceof Stairs) {
                correctStairData(target.getRelative(face));
            }
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean isMushroom(@NotNull SimpleBlock target) {
        Material material = target.getType();
        return material == Material.BROWN_MUSHROOM_BLOCK || material == Material.RED_MUSHROOM_BLOCK;
    }

    public static void correctMushroomData(@NotNull SimpleBlock target) {
        if (!isMushroom(target)) {
            return;
        }
        MultipleFacing data = (MultipleFacing) target.getBlockData();
        for (BlockFace face : data.getAllowedFaces()) {
            data.setFace(face, !isMushroom(target.getRelative(face)));
        }

        target.setBlockData(data);
    }

    public static void correctSurroundingMushroomData(@NotNull SimpleBlock target) {
        correctMushroomData(target);
        for (BlockFace face : sixBlockFaces) {
            correctMushroomData(target.getRelative(face));
        }
    }

    public static void placeDoor(@NotNull PopulatorDataAbstract data, @NotNull Material mat, @NotNull Wall w) {
        placeDoor(data, mat, w.getX(), w.getY(), w.getZ(), w.getDirection());
    }

    public static void placeDoor(@NotNull PopulatorDataAbstract data,
                                 @NotNull Material mat,
                                 int x,
                                 int y,
                                 int z,
                                 @NotNull BlockFace dir)
    {
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

    public static void placeBed(@NotNull SimpleBlock block, @NotNull Material mat, @NotNull BlockFace dir) {
        if (BlockUtils.isAir(block.getType()) && BlockUtils.isAir(block.getRelative(dir).getType())) {
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

    public static BlockFace @NotNull [] getDirectFacesFromDiagonal(@NotNull BlockFace face) {
        return switch (face) {
            case NORTH_EAST -> new BlockFace[] {BlockFace.NORTH, BlockFace.EAST};
            case NORTH_WEST -> new BlockFace[] {BlockFace.NORTH, BlockFace.WEST};
            case SOUTH_EAST -> new BlockFace[] {BlockFace.SOUTH, BlockFace.EAST};
            case SOUTH_WEST -> new BlockFace[] {BlockFace.SOUTH, BlockFace.EAST};
            default -> throw new UnsupportedOperationException(
                    "getDirectFacesFromDiagonal can only be used for XZ-Plane diagonals");
        };
    }

    public static void placeRail(@NotNull SimpleBlock block, @NotNull Material mat) {
        Rail rail = (Rail) Bukkit.createBlockData(mat);
        Set<BlockFace> faces = EnumSet.noneOf(BlockFace.class);
        BlockFace upperFace = null;
        for (BlockFace face : BlockUtils.directBlockFaces) {
            SimpleBlock relative = block.getRelative(face);
            if (Tag.RAILS.isTagged(relative.getType())) {
                faces.add(face);
            }
            if (Tag.RAILS.isTagged(relative.getUp().getType())) {
                upperFace = face;
            }
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
        }
        else if (!faces.isEmpty()) {
            if (faces.contains(BlockFace.NORTH) && faces.contains(BlockFace.EAST)) {
                rail.setShape(Shape.NORTH_EAST);
            }
            else if (faces.contains(BlockFace.NORTH) && faces.contains(BlockFace.WEST)) {
                rail.setShape(Shape.NORTH_WEST);
            }
            else if (faces.contains(BlockFace.SOUTH) && faces.contains(BlockFace.EAST)) {
                rail.setShape(Shape.SOUTH_EAST);
            }
            else if (faces.contains(BlockFace.NORTH) || faces.contains(BlockFace.SOUTH)) {
                rail.setShape(Shape.NORTH_SOUTH);
            }
            else if (faces.contains(BlockFace.EAST) || faces.contains(BlockFace.WEST)) {
                rail.setShape(Shape.EAST_WEST);
            }
        }

        block.setBlockData(rail);
    }

    public static void correctSurroundingRails(@NotNull SimpleBlock target) {
        if (!(target.getBlockData() instanceof Rail)) {
            return;
        }

        placeRail(target, target.getType());
        for (BlockFace face : BlockUtils.directBlockFaces) {
            SimpleBlock relative = target.getRelative(face);
            if (relative.getBlockData() instanceof Rail) {
                placeRail(relative, relative.getType());
            }
            if (target.getRelative(face).getDown().getBlockData() instanceof Rail) {
                placeRail(relative.getDown(), target.getDown().getRelative(face).getType());
            }
        }
    }

    public static boolean emitsLight(@NotNull Material mat) {
        return switch (mat) {
            case TORCH, SEA_PICKLE, SEA_LANTERN, GLOWSTONE, LANTERN, LAVA, CAMPFIRE, REDSTONE_LAMP, FIRE -> true;
            default -> false;
        };
    }

    public static @NotNull BlockData infestStone(@NotNull BlockData mat) {
        return switch (mat.getMaterial()) {
            case STONE_BRICKS -> Bukkit.createBlockData(Material.INFESTED_STONE_BRICKS);
            case MOSSY_STONE_BRICKS -> Bukkit.createBlockData(Material.INFESTED_MOSSY_STONE_BRICKS);
            case CRACKED_STONE_BRICKS -> Bukkit.createBlockData(Material.INFESTED_CRACKED_STONE_BRICKS);
            case CHISELED_STONE_BRICKS -> Bukkit.createBlockData(Material.INFESTED_CHISELED_STONE_BRICKS);
            case COBBLESTONE -> Bukkit.createBlockData(Material.INFESTED_COBBLESTONE);
            case STONE -> Bukkit.createBlockData(Material.INFESTED_STONE);
            default -> mat;
        };
    }

    public static void stairwayUntilSolid(@NotNull SimpleBlock start,
                                          @NotNull BlockFace extensionDir,
                                          Material[] downTypes,
                                          Material... stairTypes)
    {
        while (!start.isSolid()) {
            new StairBuilder(stairTypes).setFacing(extensionDir.getOppositeFace()).apply(start);
            BlockUtils.setDownUntilSolid(start.getX(), start.getY() - 1, start.getZ(), start.getPopData(), downTypes);
            start = start.getRelative(extensionDir).getDown();
        }
    }

    public static boolean isAir(Material mat) {
        return airs.contains(mat);
    }

    public static @NotNull BlockData getRandomBarrel() {
        Directional barrel = (Directional) Bukkit.createBlockData(Material.BARREL);
        barrel.setFacing(BlockUtils.sixBlockFaces[GenUtils.randInt(0, BlockUtils.sixBlockFaces.length - 1)]);
        return barrel;
    }

    public static void angledStairwayUntilSolid(@NotNull SimpleBlock start,
                                                BlockFace extensionDir,
                                                Material[] downTypes,
                                                Material... stairTypes)
    {
        int threshold = 5;
        while (!start.isSolid()) {

            if (threshold == 0) {
                extensionDir = BlockUtils.getTurnBlockFace(new Random(), extensionDir);
            }

            new StairBuilder(stairTypes).setFacing(extensionDir.getOppositeFace()).apply(start);
            BlockUtils.setDownUntilSolid(start.getX(), start.getY() - 1, start.getZ(), start.getPopData(), downTypes);
            threshold--;
            start = start.getRelative(extensionDir).getDown();
        }
    }

    /**
     * Checks if the target is in a wet material, or if the material it is
     * in is waterlogged.
     */
    public static boolean isWet(@NotNull SimpleBlock target) {
        return BlockUtils.wetMaterials.contains(target.getType()) || (target.getBlockData() instanceof Waterlogged
                                                                      && ((Waterlogged) target.getBlockData()).isWaterlogged());
    }

    public static float yawFromBlockFace(@NotNull BlockFace face) {
        return switch (face) {
            case EAST -> -90f;
            case NORTH -> 180f;
            case SOUTH -> 0f;
            case WEST -> 90f;
            default -> 180f;
        };
    }

    public static void randRotateBlockData(@NotNull Random rand, BlockData data) {
        if (data instanceof Directional) {
            Set<BlockFace> faces = ((Directional) data).getFaces();
            ((Directional) data).setFacing(faces.stream()
                                                .skip((int) (faces.size() * rand.nextDouble()))
                                                .findAny()
                                                .get());
        }
        else if (data instanceof Rotatable) {
            ((Rotatable) data).setRotation(BlockUtils.getXZPlaneBlockFace(rand));
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isOre(Material mat) {
        for (Material ore : ores) {
            if (ore == mat) {
                return true;
            }
        }
        return false;
    }

    public static void placeCandle(@NotNull SimpleBlock block, int numCandles, boolean lit) {
        if (!TConfig.areDecorationsEnabled()) {
            return;
        }

        Candle candle = (Candle) Bukkit.createBlockData(Material.CANDLE);
        candle.setLit(lit);

        candle.setCandles(numCandles);
        block.setBlockData(candle);
    }

    public static void downLPointedDripstone(int height, @NotNull SimpleBlock base) {
        if (!TConfig.areDecorationsEnabled()) {
            return;
        }

        int realHeight = 0;
        while (!base.getRelative(0, -realHeight, 0).isSolid() && height > 0) {
            realHeight++;
            height--;
        }
        if (base.getRelative(0, -realHeight, 0).isSolid()) {
            realHeight--;
        }

        if (realHeight <= 0) {
            return;
        }

        for (int i = realHeight; i > 0; i--) {
            PointedDripstone.Thickness thickness = PointedDripstone.Thickness.MIDDLE;
            if (i == 1) {
                thickness = PointedDripstone.Thickness.TIP;
            }
            if (i == 2) {
                thickness = PointedDripstone.Thickness.FRUSTUM;
            }
            if (i == realHeight && realHeight > 2) {
                thickness = PointedDripstone.Thickness.BASE;
            }

            PointedDripstone dripstone = (PointedDripstone) Bukkit.createBlockData(Material.POINTED_DRIPSTONE);
            dripstone.setVerticalDirection(BlockFace.DOWN);
            dripstone.setThickness(thickness);
            base.getRelative(0, -(realHeight - i), 0).setBlockData(dripstone);
        }
    }

    public static Material stoneOrSlate(int y)
    {
        return y > 0
               ? Material.STONE
               : y < -3 ? Material.DEEPSLATE : GenUtils.randChoice(Material.STONE, Material.DEEPSLATE);
    }

    public static Material stoneOrSlateWall(int y)
    {
        return y > 0
               ? Material.COBBLESTONE_WALL
               : y < -3
                 ? Material.COBBLED_DEEPSLATE_WALL
                 : GenUtils.randChoice(Material.COBBLESTONE_WALL, Material.COBBLED_DEEPSLATE_WALL);
    }

    public static void upLPointedDripstone(int height, @NotNull SimpleBlock base) {
        if (!TConfig.areDecorationsEnabled()) {
            return;
        }

        int realHeight = 0;
        while (!base.getRelative(0, realHeight, 0).isSolid() && height > 0) {
            realHeight++;
            height--;
        }
        if (base.getRelative(0, realHeight, 0).isSolid()) {
            realHeight--;
        }

        if (realHeight <= 0) {
            return;
        }

        for (int i = 0; i < realHeight; i++) {
            PointedDripstone.Thickness thickness = PointedDripstone.Thickness.MIDDLE;

            if (realHeight >= 4) {
                if (i == realHeight - 1) {
                    thickness = PointedDripstone.Thickness.TIP;
                }
                if (i == realHeight - 2) {
                    thickness = PointedDripstone.Thickness.FRUSTUM;
                }
                if (i == 0) {
                    thickness = PointedDripstone.Thickness.BASE;
                }
            }
            else if (realHeight >= 3) {
                if (i == realHeight - 1) {
                    thickness = PointedDripstone.Thickness.TIP;
                }
                if (i == realHeight - 2) {
                    thickness = PointedDripstone.Thickness.FRUSTUM;
                }
                if (i == 0) {
                    thickness = PointedDripstone.Thickness.BASE;
                }
            }
            else if (realHeight >= 2) {
                thickness = PointedDripstone.Thickness.TIP;
                if (i == 0) {
                    thickness = PointedDripstone.Thickness.FRUSTUM;
                }
            }
            else {
                thickness = PointedDripstone.Thickness.TIP;
            }

            PointedDripstone dripstone = (PointedDripstone) Bukkit.createBlockData(Material.POINTED_DRIPSTONE);
            dripstone.setVerticalDirection(BlockFace.UP);
            dripstone.setThickness(thickness);
            base.getRelative(0, i, 0).setBlockData(dripstone);
        }
    }

    public static void downLCaveVines(int height, @NotNull SimpleBlock base) {
        if (!TConfig.arePlantsEnabled()) {
            return;
        }

        int realHeight = 0;
        while (!base.getRelative(0, -realHeight, 0).isSolid() && height > 0) {
            realHeight++;
            height--;
        }
        if (base.getRelative(0, -realHeight, 0).isSolid()) {
            realHeight--;
        }

        if (realHeight <= 0) {
            return;
        }

        for (int i = realHeight; i > 0; i--) {
            CaveVinesPlant vines = (CaveVinesPlant) Bukkit.createBlockData(i == 1
                                                                           ? Material.CAVE_VINES
                                                                           : Material.CAVE_VINES_PLANT);
            vines.setBerries(new Random().nextInt(3) == 0);
            base.getRelative(0, -(realHeight - i), 0).lsetBlockData(vines);
        }
    }

    public static @NotNull BlockData deepSlateVersion(@NotNull Material target) {
        BlockData data = deepslateMap.get("DEEPSLATE_" + target);
        if (data == null) {
            Material mat = Material.getMaterial("DEEPSLATE_" + target);
            if (mat == null)
                return Bukkit.createBlockData(target);
            else {
                data = Bukkit.createBlockData(mat);
                deepslateMap.put("DEEPSLATE_" + target, data);
            }
        }
        return data;
    }

}
