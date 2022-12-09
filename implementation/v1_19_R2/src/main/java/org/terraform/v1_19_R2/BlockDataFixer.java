package org.terraform.v1_19_R2;

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

    @Override
    public String updateSchematic(double schematicVersion, String schematic) {
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
            //1.16 stuff.
            correctSurroundingWallData(b);
        }
    }
    
    //--------[1.16 stuff]
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

//		target.setBlockData(data);
    }

    public static void correctSurroundingWallData(SimpleBlock target) {
        if (!(target.getBlockData() instanceof Wall)) return;

        correctWallData(target);
        for (BlockFace face : BlockUtils.directBlockFaces) {
            if (Tag.WALLS.isTagged(target.getRelative(face).getType()))
                correctWallData(target.getRelative(face));
        }
    }

}
