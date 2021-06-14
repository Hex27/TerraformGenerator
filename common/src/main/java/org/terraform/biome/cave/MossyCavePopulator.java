package org.terraform.biome.cave;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.BlockFace;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.data.Wall;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class MossyCavePopulator extends AbstractCavePopulator {
    private static boolean genned = false;

    @Override
    public void populate(TerraformWorld tw, Random random, SimpleBlock ceil, SimpleBlock floor) {

        if (!genned) {
        	
            genned = true;
            TerraformGeneratorPlugin.logger.info("Spawning mossy cave at " + floor.toString() + ", floortype=" + floor.getType()  + ", aboveFloorType=" + floor.getRelative(0,1,0).getType());
        }
        int caveHeight = ceil.getY() - floor.getY();

        //Don't touch slabbed floors or stalagmites
        if (Tag.SLABS.isTagged(floor.getType()) ||
        		Tag.WALLS.isTagged(floor.getType()))
        	return;

        //=========================
        //Upper decorations
        //=========================

        //Stalactites
        if (GenUtils.chance(random, 1, 25)) {
            int h = caveHeight / 4;
            if (h < 1) h = 1;
            if (h > 4) h = 4;
            Wall w = new Wall(ceil);
            if (w.getRelative(0, 1, 0).getType() == Material.SAND || w.getRelative(0, 1, 0).getType() == Material.SANDSTONE)
                w.downLPillar(random, h, Material.SANDSTONE_WALL);
            else if (BlockUtils.isStoneLike(w.getRelative(0, 1, 0).getType()))
                w.downLPillar(random, h, Material.COBBLESTONE_WALL, Material.MOSSY_COBBLESTONE_WALL);
        }

        //=========================
        //Lower decorations 
        //=========================

        //Stalagmites
        if (GenUtils.chance(random, 1, 25)) {
            int h = caveHeight / 4;
            if (h < 1) h = 1;
            if (h > 4) h = 4;
            Wall w = new Wall(floor.getRelative(0,1,0), BlockFace.NORTH);
            if (BlockUtils.isAir(w.getType())) {
                if (w.getRelative(0, 1, 0).getType() == Material.SAND || w.getRelative(0, 1, 0).getType() == Material.SANDSTONE)
                    w.LPillar(h, random, Material.SANDSTONE_WALL);
                else if (BlockUtils.isStoneLike(w.getRelative(0, 1, 0).getType()))
                    w.LPillar(h, random, Material.COBBLESTONE_WALL, Material.MOSSY_COBBLESTONE_WALL);
            }

        } 
        else if (GenUtils.chance(random, 1, 25) 
        		&& BlockUtils.isStoneLike(floor.getRelative(0,1,0).getType())) 
        { //Slabbing
            SimpleBlock base = floor.getRelative(0,1,0);
            //Only next to spots where there's some kind of solid block.
            if (BlockUtils.isAir(base.getType()))
                for (BlockFace face : BlockUtils.directBlockFaces) {
                    if (base.getRelative(face).getType().isSolid()) {
                        base.setType(Material.STONE_SLAB);
                        break;
                    }
                }
        } 
        else if (GenUtils.chance(random, 1, 35) && BlockUtils.isStoneLike(floor.getRelative(0,1,0).getType())) 
        { //Shrooms
            if (BlockUtils.isAir(floor.getRelative(0,1,0).getType()))
                floor.getRelative(0,1,0).setType(
                		GenUtils.randMaterial(
                				Material.RED_MUSHROOM,
                				Material.BROWN_MUSHROOM)
                		);
        }
        
    }
}
