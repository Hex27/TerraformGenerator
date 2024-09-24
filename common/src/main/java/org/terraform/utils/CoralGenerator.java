package org.terraform.utils;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.CoralWallFan;
import org.bukkit.block.data.type.SeaPickle;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.main.config.TConfig;

import java.util.Random;

public class CoralGenerator {
    //    private static final Material[] VALUES = Material.values();
    public static final Material[] CORAL_BLOCKS = {
            Material.BRAIN_CORAL_BLOCK,
            Material.BUBBLE_CORAL_BLOCK,
            Material.FIRE_CORAL_BLOCK,
            Material.HORN_CORAL_BLOCK,
            Material.TUBE_CORAL_BLOCK,
            };
    public static final Material[] CORAL_FANS = {
            Material.BRAIN_CORAL_FAN,
            Material.BUBBLE_CORAL_FAN,
            Material.FIRE_CORAL_FAN,
            Material.HORN_CORAL_FAN,
            Material.TUBE_CORAL_FAN,
            };
    public static final Material[] CORAL_WALL_FANS = {
            Material.BRAIN_CORAL_WALL_FAN,
            Material.BUBBLE_CORAL_WALL_FAN,
            Material.FIRE_CORAL_WALL_FAN,
            Material.HORN_CORAL_WALL_FAN,
            Material.TUBE_CORAL_WALL_FAN,
            };
    public static final BlockFace[] FACES = {
            BlockFace.EAST,
            BlockFace.WEST,
            BlockFace.NORTH,
            BlockFace.SOUTH,
            BlockFace.EAST,
            BlockFace.WEST,
            BlockFace.NORTH,
            BlockFace.SOUTH,
            BlockFace.EAST,
            BlockFace.WEST,
            BlockFace.NORTH,
            BlockFace.SOUTH,
            BlockFace.UP,
            BlockFace.DOWN
    };

    /**
     * Creates a random coral
     *
     * @param data refers to the block the coral will grow ON.
     */
    public static void generateSingleCoral(@NotNull PopulatorDataAbstract data, int x, int y, int z) {
        BlockFace face = getRandomBlockFace();

        if (face == BlockFace.DOWN) {
            face = BlockFace.UP;
        }
        Material coral = CORAL_FANS[GenUtils.randInt(0, CORAL_FANS.length - 1)];
        if (face != BlockFace.UP) {
            coral = CORAL_WALL_FANS[GenUtils.randInt(0, CORAL_WALL_FANS.length - 1)];
        }

        attemptReplace(data, x + face.getModX(), y + face.getModY(), z + face.getModZ(), coral);
        if (face != BlockFace.UP) {
            if (data.getBlockData(
                    x + face.getModX(),
                    y + face.getModY(),
                    z + face.getModZ()
            ) instanceof CoralWallFan bdata)
            {
                bdata.setFacing(face);
                data.setBlockData(x + face.getModX(), y + face.getModY(), z + face.getModZ(), bdata);
            }
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isSaturatedCoral(@NotNull SimpleBlock block) {
        for (BlockFace face : BlockUtils.directBlockFaces) {
            if (block.getRelative(face).getType() == Material.WATER) {
                return true;
            }
        }
        return false;
    }

    /**
     * Generates a coral on a surface.
     *
     * @param data refers to the block the coral will grow ON.
     */
    public static void generateSingleCoral(@NotNull PopulatorDataAbstract data, int x, int y, int z, String coralType) {
        BlockFace face = getRandomBlockFace();
        coralType = StringUtils.remove(coralType, "_BLOCK");
        if (face == BlockFace.DOWN) {
            face = BlockFace.UP;
        }
        Material coral = Material.getMaterial(coralType
                                              + "_FAN");// coralFans().get(GenUtils.randInt(0, coralFans().size() - 1));
        if (new Random().nextBoolean()) {
            coral = Material.getMaterial(coralType);
        }

        if (face != BlockFace.UP) {
            coral = Material.getMaterial(coralType + "_WALL_FAN");
        }
        else if (GenUtils.chance(1, 5)) {
            generateSeaPickles(data, x, y + 1, z);
            return;
        }
        attemptReplace(data, x + face.getModX(), y + face.getModY(), z + face.getModZ(), coral);
        if (face != BlockFace.UP) {
            if (data.getBlockData(
                    x + face.getModX(),
                    y + face.getModY(),
                    z + face.getModZ()
            ) instanceof CoralWallFan bdata)
            {
                bdata.setFacing(face);
                data.setBlockData(x + face.getModX(), y + face.getModY(), z + face.getModZ(), bdata);
            }
        }
    }

    /**
     * Creates a cluster of Sea Pickles.
     *
     * @param data refers to the block to replace with sea pickles
     */
    public static void generateSeaPickles(@NotNull PopulatorDataAbstract data, int x, int y, int z) {
        if (!TConfig.arePlantsEnabled()) {
            return;
        }

        int fullSize = GenUtils.randInt(1, 4);
        if (attemptReplace(data, x, y, z, Material.SEA_PICKLE)) {
            if (data.getBlockData(x, y, z) instanceof SeaPickle state) {
                state.setPickles(fullSize);
                data.setBlockData(x, y, z, state);
            }
        }
    }

    /**
     * Generates a Kelp plant 3-10 blocks tall. Or sea grass.
     *
     * @param data refers to the block ABOVE the floor (lowest block of the kelp plant)
     */
    public static void generateKelpGrowth(@NotNull PopulatorDataAbstract data, int x, int y, int z) {
        if (!TConfig.arePlantsEnabled()) {
            return;
        }

        int fullSize = GenUtils.randInt(1, 2);
        if (new Random().nextBoolean()) {
            fullSize += GenUtils.randInt(1, 20);
        }
        if (fullSize == 1) {
            attemptReplace(data, x, y, z, Material.SEAGRASS);
        }
        else if (fullSize == 2 && y < TerraformGenerator.seaLevel - 3) {
            BlockUtils.setDoublePlant(data, x, y, z, Material.TALL_SEAGRASS);
        }
        else {
            for (int size = 0; size < fullSize; size++) {
                if (!attemptReplace(data, x, y, z, Material.KELP_PLANT)) {
                    break;
                }
                y++;
            }
        }
    }

    /**
     * Will replace the block if it was previously water or air.
     *
     * @param data    block to be replaced
     * @param newType type to replace with
     */
    public static boolean attemptReplace(@NotNull PopulatorDataAbstract data, int x, int y, int z, Material newType) {
        if (y >= TerraformGenerator.seaLevel) {
            return false;
        }
        Material type = data.getType(x, y, z);
        if (type != Material.WATER
            && type != Material.SEAGRASS
            && type != Material.TALL_SEAGRASS
            && type != Material.KELP_PLANT)
        {
            return false;
        }
        data.setType(x, y, z, newType);
        return true;
    }

    /**
     * Generates a coral-ish structure at the location, of a random
     * length.
     */
    public static void generateCoral(@NotNull PopulatorDataAbstract data, int x, int y, int z) {
        Material coral = CORAL_BLOCKS[GenUtils.randInt(0, CORAL_BLOCKS.length - 1)];
        int fullSize = GenUtils.randInt(15, 35);
        int[] middle = {x, y, z};

        for (int size = 0; size < fullSize; size++) {
            if (attemptReplace(data, middle[0], middle[1], middle[2], coral)) {
                if (GenUtils.randInt(0, 100) < 20) {
                    generateSeaPickles(data, middle[0], middle[1] + 1, middle[2]);
                }
                if (GenUtils.randInt(0, 100) < 40) {
                    generateSingleCoral(data, middle[0], middle[1], middle[2]);
                }
            }
            getRandomRelative(middle);
        }
    }

    /**
     * Generates a sea sponge at the location, of a random
     * length.
     */
    public static void generateSponge(@NotNull PopulatorDataAbstract data, int x, int y, int z) {
        int fullSize = GenUtils.randInt(15, 35);
        int[] middle = {x, y, z};

        for (int size = 0; size < fullSize; size++) {
            if (attemptReplace(data, middle[0], middle[1], middle[2], Material.WET_SPONGE)) {
                if (GenUtils.randInt(0, 100) < 20) {
                    generateSeaPickles(data, middle[0], middle[1] + 1, middle[2]);
                }
            }
            getRandomRelative(middle);
        }
    }

    /**
     * Gets a random connecting side of the block.
     */
    public static void getRandomRelative(int @NotNull [] middle) {
        BlockFace face = getRandomBlockFace();
        middle[0] += face.getModX();
        middle[1] += face.getModY();
        middle[2] += face.getModZ();
    }

    public static BlockFace getRandomBlockFace() {
        return FACES[GenUtils.randInt(0, 13)];
    }
}
