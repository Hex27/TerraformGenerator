package org.terraform.biome.cavepopulators;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.data.Wall;
import org.terraform.utils.GenUtils;
import org.terraform.utils.StalactiteBuilder;

import java.util.Random;

public class FrozenCavePopulator extends AbstractCavePopulator {

    @Override
    public void populate(TerraformWorld tw, @NotNull Random random, @NotNull SimpleBlock ceil, @NotNull SimpleBlock floor) {

        int caveHeight = ceil.getY() - floor.getY();

        //Don't touch slabbed floors or stalagmites
        if (Tag.SLABS.isTagged(floor.getType()) ||
        		Tag.WALLS.isTagged(floor.getType()))
            return;

        //=========================
        //Upper decorations
        //=========================

        //Upper Ice
        ceil.setType(Material.ICE);

        //Stalactites
        if (GenUtils.chance(random, 1, 24)) {
            int h = caveHeight / 4;
            Wall w = new Wall(ceil.getRelative(0,-1,0), BlockFace.NORTH);
            //w.downLPillar(random, h, Material.ICE);

            new StalactiteBuilder(Material.ICE)
            .setSolidBlockType(Material.ICE)
            .setFacingUp(false)
            .setVerticalSpace(caveHeight)
            .build(random, w);
        }

        //=========================
        //Lower decorations 
        //=========================

        //Lower Ice
        floor.getRelative(0,1,0).setType(Material.ICE);

        //Stalagmites
        if (GenUtils.chance(random, 1, 25)) {
            int h = caveHeight / 4;
            Wall w = new Wall(floor.getRelative(0,2,0));
            if (w.getType() == Material.CAVE_AIR)
                //w.LPillar(h, random, Material.ICE);
                new StalactiteBuilder(Material.ICE)
                .setSolidBlockType(Material.ICE)
                .setFacingUp(true)
                .setVerticalSpace(caveHeight)
                .build(random, w);
        }
    }
}
