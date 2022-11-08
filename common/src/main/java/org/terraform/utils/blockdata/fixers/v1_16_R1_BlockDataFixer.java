package org.terraform.utils.blockdata.fixers;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Wall;
import org.bukkit.block.data.type.Wall.Height;
import org.bukkit.util.Vector;
import org.terraform.coregen.BlockDataFixerAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.utils.BlockUtils;

public class v1_16_R1_BlockDataFixer extends BlockDataFixerAbstract {

    //TODO: Investigate what this class is for. Seems quite random to have this around.
    public static void correctWallData(SimpleBlock target) {
        if (!(target.getBlockData() instanceof Wall data)) return;
        for (BlockFace face : BlockUtils.directBlockFaces) {
        	Material relType = target.getRelative(face).getType();
            if (relType.isSolid() 
            		&& !Tag.BANNERS.isTagged(relType)
            		&& !Tag.PRESSURE_PLATES.isTagged(relType)
                    && !Tag.TRAPDOORS.isTagged(relType)
                    && !Tag.SLABS.isTagged(relType)) {
                data.setHeight(face, Height.LOW);
                if (target.getRelative(BlockFace.UP).getType().isSolid()) {
                    data.setHeight(face, Height.TALL);
                }

                //Ensure that target panes do not join with relType fences and vice versa
                if(BlockUtils.glassPanes.contains(target.getType())
                		&& (Tag.FENCE_GATES.isTagged(relType)||Tag.FENCES.isTagged(relType))) {
                	data.setHeight(face, Height.NONE);
                }else if((Tag.FENCES.isTagged(target.getType())||Tag.FENCE_GATES.isTagged(target.getType()))
                		&& (BlockUtils.glassPanes.contains(relType))) {
                	data.setHeight(face, Height.NONE);
                }

            } else 
            	data.setHeight(face, Height.NONE);
        }

//		if(target.getRelative(BlockFace.UP).getBlockData() instanceof Wall&&
//				((Wall) target.getRelative(BlockFace.UP).getBlockData()).isUp()) {
//			data.setUp(true);
//		}
        //TerraformGeneratorPlugin.logger.info("Changed wall at " + target.toVector().toString());
        target.setBlockData(data);
    }

    public static void correctSurroundingWallData(SimpleBlock target) {
        if (!(target.getBlockData() instanceof Wall)) return;

        correctWallData(target);
        for (BlockFace face : BlockUtils.directBlockFaces) {
            if (Tag.WALLS.isTagged(target.getRelative(face).getType()))
                correctWallData(target.getRelative(face));
        }
    }

    @Override
    public String updateSchematic(double schematicVersion, String schematic) {
//        if(schematicVersion < 16)
//            if (schematic.contains("_wall[")) {
//                schematic = StringUtils.replace(schematic, "north=false", "north=none");
//                schematic = StringUtils.replace(schematic, "south=false", "south=none");
//                schematic = StringUtils.replace(schematic, "east=false", "east=none");
//                schematic = StringUtils.replace(schematic, "west=false", "west=none");
//                schematic = StringUtils.replace(schematic, "north=true", "north=low");
//                schematic = StringUtils.replace(schematic, "south=true", "south=low");
//                schematic = StringUtils.replace(schematic, "east=true", "east=low");
//                schematic = StringUtils.replace(schematic, "west=true", "west=low");
//            }
        return schematic;
    }

    @Override
    public void correctFacing(Vector v, SimpleBlock b, BlockData data, BlockFace face) {
        if (data == null && b != null) data = b.getBlockData();

        if (!hasFlushed && data instanceof Wall) {
            this.pushChanges(v);
            return;
        }

        if (data instanceof Wall && b != null) {
            //TerraformGeneratorPlugin.logger.info("corrected");
            correctSurroundingWallData(b);
        }
    }
}
