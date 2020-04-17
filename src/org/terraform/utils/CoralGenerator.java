package org.terraform.utils;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.CoralWallFan;
import org.bukkit.block.data.type.SeaPickle;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.coregen.TerraformGenerator;

public class CoralGenerator {

	private static List<Material> coralBlocks = new ArrayList<>();
    private static List<Material> coralFans = new ArrayList<>();
    private static List<Material> coralWallFans = new ArrayList<>();
    
    /**
     * @return a list of LIVE coral BLOCKS
     */
    public static List<Material> coralBlocks() {
        if (coralBlocks.size() == 0)
            for (Material coralMat : Material.values())
                if (coralMat.name().contains("CORAL_BLOCK") &&
                        !coralMat.name().contains("DEAD")) coralBlocks.add(coralMat);

        return coralBlocks;
    }

    /**
     * @return a list of LIVE corals (non-blocks)
     */
    public static List<Material> coralFans() {
        if (coralFans.size() == 0)
            for (Material coralMat : Material.values())
                if (coralMat.name().contains("CORAL") &&
                        !coralMat.name().contains("DEAD") &&
                        !coralMat.name().contains("WALL")) coralFans.add(coralMat);

        return coralFans;
    }

    /**
     * @return a list of LIVE coral wall fans (non-blocks)
     */
    public static List<Material> coralWallFans() {
        if (coralWallFans.size() == 0)
            for (Material coralMat : Material.values())
                if (coralMat.name().contains("CORAL_WALL_FAN") &&
                        !coralMat.name().contains("DEAD")) coralWallFans.add(coralMat);

        return coralWallFans;
    }

    /**
     * Creates a random coral
     *
     * @param base refers to the block the coral will grow ON.
     */
    public static void generateSingleCoral(PopulatorDataAbstract data, int x, int y, int z) {
        BlockFace face = getRandomBlockFace();
        
        if (face == BlockFace.DOWN) face = BlockFace.UP;
        Material coral = coralFans().get(GenUtils.randInt(0, coralFans().size() - 1));
        if (face != BlockFace.UP) coral = coralWallFans().get(GenUtils.randInt(0, coralWallFans().size() - 1));

        attemptReplace(data, x+face.getModX(),y+face.getModY(),z+face.getModZ(), coral);
        if (face != BlockFace.UP) {
            if (data.getBlockData(x+face.getModX(),y+face.getModY(),z+face.getModZ()) instanceof CoralWallFan) {
                CoralWallFan bdata = (CoralWallFan) data.getBlockData(x+face.getModX(),y+face.getModY(),z+face.getModZ());
                bdata.setFacing(face);
                data.setBlockData(x+face.getModX(),y+face.getModY(),z+face.getModZ(),bdata);
            }
        }
    }

    /**
     * Creates a cluster of Sea Pickles.
     *
     * @param base refers to the block to replace with sea pickles
     */
    public static void generateSeaPickles(PopulatorDataAbstract data, int x, int y, int z) {
        int fullSize = GenUtils.randInt(1, 4);
        if (attemptReplace(data,x,y,z, Material.SEA_PICKLE)) {
            if (data.getBlockData(x,y,z) instanceof SeaPickle) {
                SeaPickle state = (SeaPickle) data.getBlockData(x,y,z);
                state.setPickles(fullSize);
                data.setBlockData(x,y,z,state);
            }
        }
    }

    /**
     * Generates a Kelp plant 2-4 blocks tall.
     *
     * @param base refers to the block ABOVE the floor (lowest block of the kelp plant)
     */
    public static void generateKelpGrowth(PopulatorDataAbstract data, int x, int y, int z) {
        int fullSize = GenUtils.randInt(1, 5);
        if (fullSize == 1) {
            attemptReplace(data,x,y,z, Material.SEAGRASS);
        } else if (fullSize == 2) {
        	BlockUtils.setDoublePlant(data, x, y, z, Material.TALL_SEAGRASS);
        } else {
            for (int size = 0; size < fullSize; size++) {
                attemptReplace(data,x,y,z, Material.KELP_PLANT);
                y++;
            }
        }
    }

    /**
     * Will replace the block if it was previously water or air.
     *
     * @param target  block to be replaced
     * @param newType type to replace with
     */
    public static boolean attemptReplace(PopulatorDataAbstract data, int x, int y, int z, Material newType) {
    	if(y >= TerraformGenerator.seaLevel) return false;
        if (data.getType(x,y,z) != Material.WATER &&
        		data.getType(x,y,z) != Material.SEAGRASS &&
        		data.getType(x,y,z) != Material.TALL_SEAGRASS &&
        		data.getType(x,y,z) != Material.KELP_PLANT) return false;
        data.setType(x,y,z,newType);
        return true;
    }

    /**
     * Generates a coral-ish structure at the location, of a random
     * length.
     *
     * @param middle it is
     */
    public static void generateCoral(PopulatorDataAbstract data, int x, int y, int z) {
        Material coral = coralBlocks().get(GenUtils.randInt(0, coralBlocks().size() - 1));
        int fullSize = GenUtils.randInt(15, 35);
        int[] middle = new int[]{x,y,z};
        for (int size = 0; size < fullSize; size++) {
            if (attemptReplace(data,middle[0],middle[1],middle[2], coral)) {
                if (GenUtils.randInt(0, 100) < 20) generateSeaPickles(data,middle[0],middle[1]+1,middle[2]);
                if (GenUtils.randInt(0, 100) < 40) generateSingleCoral(data,middle[0],middle[1],middle[2]);
            }
            
            middle = getRandomRelative(middle);
        }
    }

    /**
     * Gets a random connecting side of the block.
     */
    public static int[] getRandomRelative(int[] middle) {
    	BlockFace face = getRandomBlockFace();
    	middle[0] += face.getModX();
    	middle[1] += face.getModY();
    	middle[2] += face.getModZ();
        return middle;
    }

    public static BlockFace getRandomBlockFace() {
        BlockFace[] faces = new BlockFace[]{
                BlockFace.EAST, BlockFace.WEST,
                BlockFace.NORTH, BlockFace.SOUTH,
                BlockFace.UP, BlockFace.DOWN};
        return faces[GenUtils.randInt(0, 5)];
    }
}
