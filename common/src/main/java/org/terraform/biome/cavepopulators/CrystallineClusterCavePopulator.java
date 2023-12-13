package org.terraform.biome.cavepopulators;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockFace;
import org.terraform.biome.custombiomes.CustomBiomeType;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.coregen.populatordata.PopulatorDataICABiomeWriterAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.blockdata.DirectionalBuilder;
import org.terraform.utils.blockdata.MultipleFacingBuilder;
import org.terraform.utils.version.Version;

import java.util.Random;

public class CrystallineClusterCavePopulator extends AbstractCaveClusterPopulator {
    //private static boolean genned = false;
    
    public CrystallineClusterCavePopulator(float radius) {
		super(radius);
	}

	@Override
    protected void oneUnit(TerraformWorld tw, Random random, SimpleBlock ceil, SimpleBlock floor) {
    	//Forget it on 1.16. This cave uses nothing but 1.17+ blocks
		if(!Version.isAtLeast(17)) return;
		//if (!genned) {
        //    genned = true;
        //}
    	
    	//=========================
        //Upper decorations
        //=========================

        int caveHeight = ceil.getY() - floor.getY();

        //Don't touch slabbed floors or stalagmites
        if (Tag.SLABS.isTagged(floor.getType()) ||
        		Tag.WALLS.isTagged(floor.getType()))
            return;
        
        //Amethyst crust. Don't cover ores. Don't mess with moss.
        if(Material.MOSS_BLOCK != ceil.getType()
        		&& !BlockUtils.isOre(ceil.getType())) {
        	ceil.setType(Material.AMETHYST_BLOCK);
        	
            //Amethysts
            if (GenUtils.chance(random, 1, 5)) {
                new DirectionalBuilder(Material.AMETHYST_CLUSTER)
                .setFacing(BlockFace.DOWN)
                .apply(ceil.getRelative(0,-1,0));
            }
        }

        //=========================
        //Lower decorations 
        //=========================        

        //Amethyst crust. Don't cover ores. Don't mess with moss.
        if(Material.MOSS_BLOCK != floor.getType()
        		&& !BlockUtils.isOre(floor.getType())) {
        	floor.setType(Material.AMETHYST_BLOCK);
        	
            //Amethysts
            if (GenUtils.chance(random, 1, 5)) {
                new DirectionalBuilder(Material.AMETHYST_CLUSTER)
                .setFacing(BlockFace.UP)
                .apply(floor.getRelative(0,1,0));
            }else if(GenUtils.chance(random, 1, 20)) { //Calcite Pillars
            	floor.setType(Material.CALCITE);
            	floor.getRelative(0,1,0).LPillar(2*caveHeight, new Random(), Material.CALCITE);
            }
        }
        
        //=========================
        //Attempt to replace close-by walls with Amethyst. Also apply lichen.
        //=========================
        
        SimpleBlock target = floor;
        while(target.getY() != ceil.getY()) {
        	for(BlockFace face:BlockUtils.directBlockFaces) {
        		SimpleBlock rel = target.getRelative(face);
        		if(rel.getType() != Material.CALCITE
                		&& !BlockUtils.isOre(ceil.getType()) 
        				&& BlockUtils.isStoneLike(rel.getType())) {
        			rel.setType(Material.AMETHYST_BLOCK);
        			if(BlockUtils.isAir(target.getType()) && GenUtils.chance(random, 1, 3)) {
        				new MultipleFacingBuilder(Material.GLOW_LICHEN)
        				.setFace(face, true)
        				.apply(target);
        			}
        		}
        	}
        	target = target.getRelative(0,1,0);
        }

        //=========================
        //Biome Setter 
        //=========================
        PopulatorDataAbstract d = TerraformGeneratorPlugin.injector.getICAData(ceil.getPopData());
        if(d instanceof PopulatorDataICABiomeWriterAbstract data) {
            while(floor.getY() < ceil.getY()) {
        		data.setBiome(floor.getX(), floor.getY(), floor.getZ(), CustomBiomeType.CRYSTALLINE_CLUSTER, Biome.DRIPSTONE_CAVES);
        		floor = floor.getRelative(0,1,0);
        	}
        }
    }
    
    
}
