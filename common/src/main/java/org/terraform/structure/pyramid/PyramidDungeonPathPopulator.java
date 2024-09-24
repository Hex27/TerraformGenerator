package org.terraform.structure.pyramid;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.jetbrains.annotations.NotNull;
import org.terraform.structure.room.PathPopulatorAbstract;
import org.terraform.structure.room.PathPopulatorData;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class PyramidDungeonPathPopulator extends PathPopulatorAbstract {
    private final Random rand;
    private final int height;

    public PyramidDungeonPathPopulator(Random rand) {
        this.rand = rand;
        this.height = 3;
    }

    public PyramidDungeonPathPopulator(Random rand, int height) {
        this.rand = rand;
        this.height = height;
    }

    @Override
    public void populate(@NotNull PathPopulatorData ppd) {

        // Gravel tnt trap
        if (GenUtils.chance(this.rand, 1, 300)) {
            // TerraformGeneratorPlugin.logger.info("Pyramid trap at " + ppd.base.getX()+","+ppd.base.getY()+","+ppd.base.getZ());
            // To make the
            ppd.base.setType(Material.GRAVEL);
            ppd.base.getDown().setType(Material.TNT);
            ppd.base.getUp().setType(Material.STONE_PRESSURE_PLATE);

            // Generate cross-shaped hole.
            for (int i = -2; i > -8; i--) {
                ppd.base.getRelative(0, i, 0).setType(Material.AIR);
                for (BlockFace face : BlockUtils.directBlockFaces) {
                    ppd.base.getRelative(face).getRelative(0, i, 0).setType(Material.AIR);
                }
            }

            // Place torches against the tnt. This leads to a larger pit when the trap is triggered.
            for (BlockFace face : BlockUtils.directBlockFaces) {
                Directional torch = (Directional) Bukkit.createBlockData(Material.WALL_TORCH);
                torch.setFacing(face);
                ppd.base.getDown().getRelative(face).setBlockData(torch);
                ppd.base.getRelative(face).setType(Material.GRAVEL);
            }
        }

        // Cobwebs
        if (GenUtils.chance(this.rand, 1, 100)) {
            if (ppd.base.getRelative(0, height + 1, 0).isSolid()) {
                ppd.base.getRelative(0, height, 0).setType(Material.COBWEB);
            }
        }
    }

    @Override
    public int getPathWidth() {
        return 1;
    }

    @Override
    public int getPathHeight() {
        return height;
    }

}
