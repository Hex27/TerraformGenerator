package org.terraform.biome.cavepopulators;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Biome;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataICABiomeWriterAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class DripstoneClusterCavePopulator extends AbstractCaveClusterPopulator {

    public DripstoneClusterCavePopulator(float radius) {
        super(radius);
    }

    @Override
    protected void oneUnit(TerraformWorld tw,
                           @NotNull Random random,
                           @NotNull SimpleBlock ceil,
                           @NotNull SimpleBlock floor,
                           boolean boundary)
    {

        // =========================
        // Upper decorations
        // =========================

        int caveHeight = ceil.getY() - floor.getY();

        // Don't touch slabbed floors or stalagmites
        if (Tag.SLABS.isTagged(floor.getType()) || Tag.WALLS.isTagged(floor.getType())) {
            return;
        }

        // All ceiling is dripstone
        ceil.setType(Material.DRIPSTONE_BLOCK);

        // Stalactites
        if (GenUtils.chance(random, 1, 4)) {
            int h = caveHeight / 4;
            if (h < 1) {
                h = 1;
            }
            if (h > 4) {
                h = 4;
            }
            BlockUtils.downLPointedDripstone(GenUtils.randInt(1, h), ceil.getDown());
        }

        // =========================
        // Lower decorations
        // =========================

        // Floor is dripstone
        floor.setType(Material.DRIPSTONE_BLOCK);

        // Stalagmites
        if (GenUtils.chance(random, 1, 4)) {
            int h = caveHeight / 4;
            if (h < 1) {
                h = 1;
            }
            if (h > 4) {
                h = 4;
            }
            BlockUtils.upLPointedDripstone(GenUtils.randInt(1, h), floor.getUp());
        }

        // =========================
        // Biome Setter
        // =========================
        if (TerraformGeneratorPlugin.injector.getICAData(ceil.getPopData()) instanceof PopulatorDataICABiomeWriterAbstract data) {
            while (floor.getY() < ceil.getY()) {
                data.setBiome(floor.getX(), floor.getY(), floor.getZ(), Biome.DRIPSTONE_CAVES);
                floor = floor.getUp();
            }
        }
    }


}
