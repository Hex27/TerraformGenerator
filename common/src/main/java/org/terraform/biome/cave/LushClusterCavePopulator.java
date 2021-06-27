package org.terraform.biome.cave;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.BlockFace;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.tree.TreeDB;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.blockdata.BisectedBuilder;
import org.terraform.utils.blockdata.DirectionalBuilder;
import org.terraform.utils.blockdata.MultipleFacingBuilder;
import org.terraform.utils.version.OneOneSevenBlockHandler;
import org.terraform.utils.version.Version;

import java.util.Random;

public class LushClusterCavePopulator extends AbstractCaveClusterPopulator {

	private boolean isForLargeCave;
	public LushClusterCavePopulator(boolean isForLargeCave) {
		super();
		this.isForLargeCave = isForLargeCave;
	}
	
    @Override
    protected void oneUnit(TerraformWorld tw, Random random, SimpleBlock ceil, SimpleBlock floor) {
    	
    	//=========================
        //Upper decorations
        //=========================

        int caveHeight = ceil.getY() - floor.getY();

        //Don't decorate wet areas
        if(BlockUtils.isWet(ceil.getRelative(0,-1,0))) {
        	return;
        }
        
        //Don't touch slabbed floors or stalagmites
        if (Tag.SLABS.isTagged(floor.getType()) ||
        		Tag.WALLS.isTagged(floor.getType()))
            return;
        
        //Ceiling is sometimes roots
        if(GenUtils.chance(random, 1, 8)) {
        	//This part doesn't spawn Azaleas
    		ceil.setType(OneOneSevenBlockHandler.ROOTED_DIRT);
        	if(random.nextBoolean())
        		ceil.getRelative(0,-1,0).setType(OneOneSevenBlockHandler.HANGING_ROOTS);
        	
        }
        else //If not, it's moss
        {
        	ceil.setType(OneOneSevenBlockHandler.MOSS_BLOCK);
        	for(BlockFace face:BlockUtils.sixBlockFaces)
        		if(ceil.getRelative(face).getType() == Material.LAVA)
        			ceil.getRelative(face).setType(Material.AIR);
        	
        	//Spore blossom
        	if(GenUtils.chance(random, 1, 15))
        		ceil.getRelative(0,-1,0).setType(OneOneSevenBlockHandler.SPORE_BLOSSOM);
        }
        
        //Spawn these on the surface, and let the roots go downwards.
        //Hopefully, there won't be random small caves in between the tree
        //and this cave hole.
        if(isForLargeCave && GenUtils.chance(random, 1, 300)) {
        	SimpleBlock base = ceil.getGround();
        	if(BlockUtils.isDirtLike(base.getType()) && !BlockUtils.isWet(base.getRelative(0,1,0)))
        		TreeDB.spawnAzalea(random, tw, base.getPopData(), base.getX(), base.getY()+1, base.getZ());
        }
        
        //Glow Berries
        int glowBerryChance = 5;
        if(isForLargeCave) glowBerryChance = 15;
        if (GenUtils.chance(random, 1, glowBerryChance)) {
            int h = caveHeight / 4;
            if (h < 1) h = 1;
            if (h > 6) h = 6;
            OneOneSevenBlockHandler.downLCaveVines(h, ceil.getRelative(0,-1,0));
        }

        //=========================
        //Lower decorations 
        //=========================
        
        //If floor is submerged, don't touch it.
        if(BlockUtils.isWet(floor.getRelative(0,1,0)))
        	return;
        
        //Ground is moss.
        floor.setType(OneOneSevenBlockHandler.MOSS_BLOCK);
        
       
        if (GenUtils.chance(random, 1, 15)) 
        { //Azaleas
        	if(random.nextBoolean())
        		floor.getRelative(0,1,0).setType(OneOneSevenBlockHandler.AZALEA);
        	else
        		floor.getRelative(0,1,0).setType(OneOneSevenBlockHandler.FLOWERING_AZALEA);
        }
        else if (Version.isAtLeast(17) && GenUtils.chance(random, 1, 7)) 
        { //Dripleaves
        	if(random.nextBoolean())
	        	new DirectionalBuilder(OneOneSevenBlockHandler.BIG_DRIPLEAF)
	        	.setFacing(BlockUtils.getDirectBlockFace(random))
	        	.apply(floor.getRelative(0,1,0));
        	else
        		new BisectedBuilder(OneOneSevenBlockHandler.SMALL_DRIPLEAF)
        		.placeBoth(floor.getRelative(0,1,0));
        }
        else if(GenUtils.chance(random, 1, 6))
        	//Grass
    		floor.getRelative(0,1,0).setType(Material.GRASS);
        else if(GenUtils.chance(random, 1, 7))
        	//Moss carpets
    		floor.getRelative(0,1,0).setType(OneOneSevenBlockHandler.MOSS_CARPET);
        


        //=========================
        //Attempt to replace close-by walls with moss. Also apply lichen.
        //=========================
        
        SimpleBlock target = floor;
        while(target.getY() != ceil.getY()) {
        	for(BlockFace face:BlockUtils.directBlockFaces) {
        		SimpleBlock rel = target.getRelative(face);
        		if(BlockUtils.isStoneLike(rel.getType())) {
        			rel.setType(OneOneSevenBlockHandler.MOSS_BLOCK);
        			if(BlockUtils.isAir(target.getType()) && GenUtils.chance(random, 1, 5)) {
        				new MultipleFacingBuilder(OneOneSevenBlockHandler.GLOW_LICHEN)
        				.setFace(face, true)
        				.apply(target);
        			}
        		}
        	}
        	target = target.getRelative(0,1,0);
        }
        
    }
    
    
}
