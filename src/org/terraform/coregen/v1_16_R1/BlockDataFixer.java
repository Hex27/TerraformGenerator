package org.terraform.coregen.v1_16_R1;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Tag;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Wall;
import org.bukkit.block.data.type.Wall.Height;
import org.bukkit.util.Vector;
import org.terraform.coregen.BlockDataFixerAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.utils.BlockUtils;

public class BlockDataFixer extends BlockDataFixerAbstract {
    public static void correctWallData(SimpleBlock target) {
        if (!(target.getBlockData() instanceof Wall)) return;
        Wall data = (Wall) target.getBlockData();
        for (BlockFace face : BlockUtils.directBlockFaces) {
            if (target.getRelative(face).getType().isSolid() &&
                    !target.getRelative(face).getType().toString().contains("PRESSURE_PLATE")) {
                data.setHeight(face, Height.LOW);
                if (target.getRelative(BlockFace.UP).getType().isSolid()) {
                    data.setHeight(face, Height.TALL);
                }
            } else data.setHeight(face, Height.NONE);
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
    public String updateSchematic(String schematic) {
        if (schematic.contains("_wall[")) {
            schematic = StringUtils.replace(schematic, "north=false", "north=none");
            schematic = StringUtils.replace(schematic, "south=false", "south=none");
            schematic = StringUtils.replace(schematic, "east=false", "east=none");
            schematic = StringUtils.replace(schematic, "west=false", "west=none");
            schematic = StringUtils.replace(schematic, "north=true", "north=low");
            schematic = StringUtils.replace(schematic, "south=true", "south=low");
            schematic = StringUtils.replace(schematic, "east=true", "east=low");
            schematic = StringUtils.replace(schematic, "west=true", "west=low");
        }
        return schematic;
    }

    @Override
    public void correctFacing(Vector v, SimpleBlock b, BlockData data, BlockFace face) {
        if (data == null && b != null) data = b.getBlockData();

        if (data.getMaterial().toString().endsWith("_WALL")) {
//			TerraformGeneratorPlugin.logger.info("====================");
//			TerraformGeneratorPlugin.logger.info("hasflushed: " + hasFlushed);
//			TerraformGeneratorPlugin.logger.info("Has simpleblock: " + (b != null));
//			TerraformGeneratorPlugin.logger.info("data: " + data.getAsString());
//			TerraformGeneratorPlugin.logger.info("has vector: " + (v != null));
//			TerraformGeneratorPlugin.logger.info("Instanceof wall: " + (data instanceof Wall));
//
        }
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
