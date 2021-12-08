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
import org.terraform.utils.StalactiteBuilder;
import org.terraform.utils.version.OneOneSevenBlockHandler;

import java.util.Random;

public class DeepCavePopulator extends AbstractCavePopulator {
    private static boolean genned = false;
    
    @Override
    public void populate(TerraformWorld tw, Random random, SimpleBlock ceil, SimpleBlock floor) {
        
        if (!genned) {
            genned = true;
            TerraformGeneratorPlugin.logger.info("Spawning deep cave at " + floor);
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
        if (GenUtils.chance(random, 1, 8*Math.max(3, caveHeight/4))) {
//            int h = caveHeight / 4;
//            if (h < 1) h = 1;
//            if (h > 4) h = 4;
            Wall w = new Wall(ceil, BlockFace.NORTH);
            if(w.getUp().getType() == OneOneSevenBlockHandler.DEEPSLATE) {
                new StalactiteBuilder(OneOneSevenBlockHandler.COBBLED_DEEPSLATE_WALL)
                .setSolidBlockType(OneOneSevenBlockHandler.DEEPSLATE)
                .setFacingUp(false)
                .setVerticalSpace(caveHeight)
                .build(random, w);
            	//w.downLPillar(random, h, OneOneSevenBlockHandler.COBBLED_DEEPSLATE_WALL);
            }
            else {
                new StalactiteBuilder(Material.COBBLESTONE_WALL)
                .setSolidBlockType(Material.COBBLESTONE)
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
        if (GenUtils.chance(random, 1, 8*Math.max(3, caveHeight/4))) {
            int h = caveHeight / 4;
            if (h < 1) h = 1;
            if (h > 4) h = 4;
            Wall w = new Wall(floor.getRelative(0,1,0));
            if (BlockUtils.isAir(w.getType()))
            	if(w.getDown().getType() == OneOneSevenBlockHandler.DEEPSLATE) 
            		//w.LPillar(h, random, OneOneSevenBlockHandler.COBBLED_DEEPSLATE_WALL);

                    new StalactiteBuilder(OneOneSevenBlockHandler.COBBLED_DEEPSLATE_WALL)
                    .setSolidBlockType(OneOneSevenBlockHandler.DEEPSLATE)
                    .setFacingUp(true)
                    .setVerticalSpace(caveHeight)
                    .build(random, w);
            	else
                   // w.LPillar(h, random, Material.COBBLESTONE_WALL);

                    new StalactiteBuilder(Material.COBBLESTONE_WALL)
                    .setSolidBlockType(Material.COBBLESTONE)
                    .setFacingUp(true)
                    .setVerticalSpace(caveHeight)
                    .build(random, w);

        } else if (GenUtils.chance(random, 1, 25)) { //Slabbing
            SimpleBlock base = floor.getRelative(0,1,0);
            //Only next to spots where there's some kind of solid block.
            if (BlockUtils.isAir(base.getType()))
                for (BlockFace face : BlockUtils.directBlockFaces) {
                    if (base.getRelative(face).getType().isSolid()) {
                    	if(base.getRelative(0,-1,0).getType() == OneOneSevenBlockHandler.DEEPSLATE)
                    		base.setType(OneOneSevenBlockHandler.COBBLED_DEEPSLATE_SLAB);
                    	else
                    		base.setType(Material.STONE_SLAB);
                        break;
                    }
                }
        } else if (GenUtils.chance(random, 1, 35)) { //Shrooms :3
            if (BlockUtils.isAir(floor.getRelative(0,1,0).getType()))
                floor.getRelative(0,1,0).setType(GenUtils.randMaterial(
                        Material.RED_MUSHROOM,
                        Material.BROWN_MUSHROOM));
        }
        
    }
}
