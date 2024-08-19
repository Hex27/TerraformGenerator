package org.terraform.biome.cavepopulators;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.small_items.PlantBuilder;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.CoralGenerator;
import org.terraform.utils.CylinderBuilder;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class ForestedMountainsCavePopulator extends AbstractCavePopulator {
    private final @NotNull MossyCavePopulator mossyCavePop;

    public ForestedMountainsCavePopulator() {
        mossyCavePop = new MossyCavePopulator();
    }

    @Override
    public void populate(TerraformWorld tw,
                         @NotNull Random random,
                         @NotNull SimpleBlock ceil,
                         @NotNull SimpleBlock floor)
    {

        // Likely to be a river cave
        if (ceil.getY() > TerraformGenerator.seaLevel && floor.getY() < TerraformGenerator.seaLevel) {
            // Definitely a river cave
            if (ceil.getAtY(TerraformGenerator.seaLevel).getType() == Material.WATER) {

                int caveHeight = ceil.getY() - TerraformGenerator.seaLevel - 1;

                if (caveHeight <= 2) {
                    return;
                }


                // Pillars
                if (GenUtils.chance(random, 1, 100)) {
                    new CylinderBuilder(random,
                            floor.getRelative(0, (ceil.getY() - floor.getY()) / 2, 0),
                            Material.STONE
                    ).setRadius(1.5f).setRY((ceil.getY() - floor.getY()) / 2f + 3).setHardReplace(false).build();
                    return;
                }

                // CEILING DECORATIONS

                // Glow berries
                int glowBerryChance = 15;
                if (GenUtils.chance(random, 1, glowBerryChance)) {
                    int h = caveHeight / 2;
                    if (h > 6) {
                        h = 6;
                    }
                    BlockUtils.downLCaveVines(h, ceil);
                }

                // Spore blossom
                if (GenUtils.chance(random, 1, 30)) {
                    PlantBuilder.SPORE_BLOSSOM.build(ceil);
                }

                // WATER DECORATIONS
                // Lily pads
                if (GenUtils.chance(random, 1, 50)) {
                    var at = ceil.getAtY(TerraformGenerator.seaLevel + 1);
                    if (!at.isSolid()) {
                        PlantBuilder.LILY_PAD.build(at);
                    }
                }

                // Don't touch slabbed floors or stalagmites
                if (Tag.SLABS.isTagged(floor.getType()) || Tag.WALLS.isTagged(floor.getType())) {
                    return;
                }

                // BOTTOM DECORATIONS (underwater)

                // sea pickles
                if (GenUtils.chance(random, 1, 20)) {
                    CoralGenerator.generateSeaPickles(floor.getPopData(), floor.getX(), floor.getY() + 1, floor.getZ());
                }

                return;
            }
        }

        mossyCavePop.populate(tw, random, ceil, floor);

    }
}
