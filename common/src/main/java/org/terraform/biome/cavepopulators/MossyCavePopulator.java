package org.terraform.biome.cavepopulators;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.BlockFace;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.data.Wall;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.StalactiteBuilder;

import java.util.Random;

public class MossyCavePopulator extends AbstractCavePopulator {

    @Override
    public void populate(TerraformWorld tw, Random random, SimpleBlock ceil, SimpleBlock floor) {

        int caveHeight = ceil.getY() - floor.getY();

        //Don't touch slabbed floors or stalagmites
        if (Tag.SLABS.isTagged(floor.getType()) ||
        		Tag.WALLS.isTagged(floor.getType()))
        	return;

        //=========================
        //Upper decorations
        //=========================

        //Stalactites
        if (GenUtils.chance(random, 1, 35)) {
            Wall w = new Wall(ceil);
            if (w.getRelative(0, 1, 0).getType() == Material.SAND || w.getRelative(0, 1, 0).getType() == Material.SANDSTONE)
                new StalactiteBuilder(Material.SANDSTONE_WALL)
                .setSolidBlockType(Material.SANDSTONE)
                .setFacingUp(false)
                .setVerticalSpace(caveHeight)
                .build(random, w);
            	//w.downLPillar(random, h, Material.SANDSTONE_WALL);
            else 
                if(w.getUp().getType() == Material.DEEPSLATE) {
                    new StalactiteBuilder(Material.COBBLED_DEEPSLATE_WALL)
                    .setSolidBlockType(Material.DEEPSLATE)
                    .setFacingUp(false)
                    .setVerticalSpace(caveHeight)
                    .build(random, w);
                	//w.downLPillar(random, h, Material.COBBLED_DEEPSLATE_WALL);
                }
                else if(BlockUtils.isStoneLike(w.getUp().getType())){
                    new StalactiteBuilder(Material.COBBLESTONE_WALL,Material.MOSSY_COBBLESTONE_WALL)
                    .setSolidBlockType(Material.COBBLESTONE, Material.MOSSY_COBBLESTONE)
                    .setFacingUp(false)
                    .setVerticalSpace(caveHeight)
                    .build(random, w);
                    //w.downLPillar(random, h, Material.COBBLESTONE_WALL);
                }
        }

        //=========================
        //Lower decorations 
        //=========================

        //Stalagmites
        if (GenUtils.chance(random, 1, 35)) {
            int h = caveHeight / 4;
            if (h < 1) h = 1;
            if (h > 4) h = 4;
            Wall w = new Wall(floor.getRelative(0,1,0), BlockFace.NORTH);
            if (BlockUtils.isAir(w.getType())) {
                if (w.getRelative(0, -1, 0).getType() == Material.SAND || w.getRelative(0, 1, 0).getType() == Material.SANDSTONE)
                    new StalactiteBuilder(Material.SANDSTONE_WALL)
                    .setSolidBlockType(Material.SANDSTONE)
                    .setFacingUp(true)
                    .setVerticalSpace(caveHeight)
                    .build(random, w);
                	//w.downLPillar(random, h, Material.SANDSTONE_WALL);
                else 
                    if(w.getDown().getType() == Material.DEEPSLATE) {
                        new StalactiteBuilder(Material.COBBLED_DEEPSLATE_WALL)
                        .setSolidBlockType(Material.DEEPSLATE)
                        .setFacingUp(true)
                        .setVerticalSpace(caveHeight)
                        .build(random, w);
                    	//w.downLPillar(random, h, Material.COBBLED_DEEPSLATE_WALL);
                    }
                    else if(BlockUtils.isStoneLike(w.getDown().getType())){
                        new StalactiteBuilder(Material.COBBLESTONE_WALL,Material.MOSSY_COBBLESTONE_WALL)
                        .setSolidBlockType(Material.COBBLESTONE, Material.MOSSY_COBBLESTONE)
                        .setFacingUp(true)
                        .setVerticalSpace(caveHeight)
                        .build(random, w);
                        //w.downLPillar(random, h, Material.COBBLESTONE_WALL);
                    }
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
                    	if(base.getRelative(0,-1,0).getType() == Material.DEEPSLATE)
                    		base.setType(Material.COBBLED_DEEPSLATE_SLAB);
                    	else
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
