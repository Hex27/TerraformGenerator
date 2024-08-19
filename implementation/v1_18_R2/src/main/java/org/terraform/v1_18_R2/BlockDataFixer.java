package org.terraform.v1_18_R2;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Tag;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Wall;
import org.bukkit.block.data.type.Wall.Height;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.terraform.coregen.BlockDataFixerAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.utils.BlockUtils;

public class BlockDataFixer extends BlockDataFixerAbstract {

    // --------[1.16 stuff]
    public static void correctWallData(@NotNull SimpleBlock target) {
        if (!(target.getBlockData() instanceof Wall data)) {
            return;
        }
        for (BlockFace face : BlockUtils.directBlockFaces) {
            if (target.getRelative(face).isSolid() && !target.getRelative(face)
                                                             .getType()
                                                             .toString()
                                                             .contains("PRESSURE_PLATE"))
            {
                data.setHeight(face, Height.LOW);
                if (target.getRelative(BlockFace.UP).isSolid()) {
                    data.setHeight(face, Height.TALL);
                }
            }
            else {
                data.setHeight(face, Height.NONE);
            }
        }

        //		target.setBlockData(data);
    }

    public static void correctSurroundingWallData(@NotNull SimpleBlock target) {
        if (!(target.getBlockData() instanceof Wall)) {
            return;
        }

        correctWallData(target);
        for (BlockFace face : BlockUtils.directBlockFaces) {
            if (Tag.WALLS.isTagged(target.getRelative(face).getType())) {
                correctWallData(target.getRelative(face));
            }
        }
    }

    @Override
    public @Nullable String updateSchematic(double schematicVersion, String schematic) {

        if (schematicVersion > 18) {
            // No waterlogged leaves in 1.18
            schematic = StringUtils.replace(schematic, "persistent=true,waterlogged=false]", "persistent=true]");

            // No mud bricks
            schematic = StringUtils.replace(schematic, "mud_brick_", "stone_brick_");
            schematic = StringUtils.replace(schematic, "mud_bricks", "stone_bricks");
        }
        return schematic;
    }

    @Override
    public void correctFacing(Vector v, @Nullable SimpleBlock b, @Nullable BlockData data, BlockFace face) {
        if (data == null && b != null) {
            data = b.getBlockData();
        }

        if (!hasFlushed && data instanceof Wall) {
            this.pushChanges(v);
            return;
        }

        if (data instanceof Wall && b != null) {
            // 1.16 stuff.
            correctSurroundingWallData(b);
        }
    }

}
