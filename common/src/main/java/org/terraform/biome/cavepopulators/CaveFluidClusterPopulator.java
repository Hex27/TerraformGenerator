package org.terraform.biome.cavepopulators;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class CaveFluidClusterPopulator extends AbstractCaveClusterPopulator {

    Random rand;
    @Nullable
    Material fluid;
    int rY;

    public CaveFluidClusterPopulator(float radius) {
        super(radius);
    }

    @Override
    public void oneUnit(@NotNull TerraformWorld tw,
                        Random doNotUse,
                        @Nullable SimpleBlock ceil,
                        @Nullable SimpleBlock floor,
                        boolean boundary)
    {
        if (ceil == null || floor == null) {
            return;
        }
        if (rand == null) {
            rand = tw.getHashedRand(center.getX(), center.getY(), center.getZ());

            fluid = GenUtils.choice(rand, new Material[] {Material.WATER, Material.LAVA});
            if (center.getY() < TerraformGeneratorPlugin.injector.getMinY() + 32) {
                fluid = Material.LAVA;
            }
            rY = 3 + rand.nextInt(3);
        }
        Material original = floor.getType();
        for (int i = 0; i < rY; i++) {
            // If the floor is above the pinned water level, set it to cave air
            // If not, set it to the solid boundary block, or the fluid
            // The exposedToMaterial check is REQUIRED as adjacent fluid sources
            // may spawn, and cave air may forcefully remove boundaries.
            if (boundary) {
                floor.setType(original);
            }
            else if (floor.getY() <= lowestYCenter.getY()) {
                floor.setType(fluid);
            }
            else if (!BlockUtils.isExposedToMaterial(floor, BlockUtils.fluids)
                     && !BlockUtils.fluids.contains(floor.getUp().getType())) // isExposed only checks NSEW
            {
                floor.setType(Material.CAVE_AIR);
            }

            floor = floor.getDown();

            // Fix floating fluids
            if (!floor.isSolid()) {
                floor.setType(original);
                break;
            }
        }

    }


}
