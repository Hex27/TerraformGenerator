package org.terraform.biome.mountainous;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeSection;
import org.terraform.biome.BiomeType;
import org.terraform.coregen.HeightMap;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.data.SimpleBlock;
import org.terraform.data.SimpleLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.main.config.TConfigOption;
import org.terraform.tree.FractalTreeBuilder;
import org.terraform.tree.FractalTypes;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class BirchMountainsHandler extends AbstractMountainHandler {
	
	//Birch Mountains must be shorter to allow trees to populate.
	@Override
	protected double getPeakMultiplier(BiomeSection section, Random sectionRandom) {
		return GenUtils.randDouble(sectionRandom, 1.1, 1.3);
	}
	
    @Override
    public boolean isOcean() {
        return false;
    }

    @Override
    public Biome getBiome() {
        return Biome.MOUNTAINS;
    }

    @Override
    public Material[] getSurfaceCrust(Random rand) {
        return new Material[]{
        		Material.GRASS_BLOCK,
                Material.DIRT,
                Material.DIRT,
                GenUtils.randMaterial(rand, Material.DIRT, Material.STONE),
                GenUtils.randMaterial(rand, Material.DIRT, Material.STONE)};
    }

    @Override
    public void populateSmallItems(TerraformWorld tw, Random random, PopulatorDataAbstract data) {

        for (int x = data.getChunkX() * 16; x < data.getChunkX() * 16 + 16; x++) {
            for (int z = data.getChunkZ() * 16; z < data.getChunkZ() * 16 + 16; z++) {
                int y = GenUtils.getHighestGround(data, x, z);
                if (data.getBiome(x, z) != getBiome()) continue;
                setRock(new SimpleBlock(data,x,0,z).getGround());
                
                if (data.getType(x, y, z) == Material.GRASS_BLOCK) {
                	
                    if (GenUtils.chance(random, 1, 10)) {
                        data.setType(x, y + 1, z, Material.GRASS);
                        if (random.nextBoolean()) {
                            BlockUtils.setDoublePlant(data, x, y + 1, z, Material.TALL_GRASS);
                        } else {
                            data.setType(x, y + 1, z, BlockUtils.pickFlower());
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Replace steep areas with various rocks.
     * @param target
     */
    private void setRock(SimpleBlock target) {
    	if(HeightMap.getTrueHeightGradient(target.getPopData(), target.getX(), target.getZ(), 3) 
    			> TConfigOption.MISC_TREES_GRADIENT_LIMIT.getDouble()) {
    		Material rock = Material.ANDESITE;
    		if(HeightMap.getTrueHeightGradient(target.getPopData(), target.getX(), target.getZ(), 3) 
        			> TConfigOption.MISC_TREES_GRADIENT_LIMIT.getDouble()*2) 
    			rock = Material.DIORITE;
    		while(BlockUtils.isExposedToNonSolid(target)) {
    			target.setType(rock);
    			target = target.getRelative(0,-1,0);
    		}
    	}
    }

	@Override
	public void populateLargeItems(TerraformWorld tw, Random random, PopulatorDataAbstract data) {
		SimpleLocation[] trees = GenUtils.randomObjectPositions(tw, data.getChunkX(), data.getChunkZ(), 7);

        for (SimpleLocation sLoc : trees) {
            if (data.getBiome(sLoc.getX(),sLoc.getZ()) == getBiome()) {
                int treeY = GenUtils.getHighestGround(data, sLoc.getX(),sLoc.getZ());
                sLoc.setY(treeY);
                // Rarely spawn huge taiga trees
                if (TConfigOption.TREES_BIRCH_BIG_ENABLED.getBoolean() && GenUtils.chance(random, 1, 20)) {
                    new FractalTreeBuilder(FractalTypes.Tree.BIRCH_BIG).build(tw, data, sLoc.getX(),sLoc.getY(),sLoc.getZ());
                    
                }else { // Normal trees
                    new FractalTreeBuilder(FractalTypes.Tree.BIRCH_SMALL).build(tw, data, sLoc.getX(),sLoc.getY(),sLoc.getZ());
                    
                }
            }
        }
	}
	@Override
	public BiomeBank getBeachType() {
		return BiomeBank.ROCKY_BEACH;
	}
	
	/**
	 * Birch Mountains will allow rivers to carve through them.
	 */
	@Override
    public double calculateHeight(TerraformWorld tw, int x, int z) {
    	double coreRawHeight;
        double height = HeightMap.CORE.getHeight(tw, x, z);//HeightMap.MOUNTAINOUS.getHeight(tw, x, z); //Added here
        
        //Let mountains cut into adjacent sections.
        double maxMountainRadius = ((double) BiomeSection.sectionWidth);
        //Double attrition height
        height += HeightMap.ATTRITION.getHeight(tw, x, z);
        coreRawHeight = height;
        
        BiomeSection sect = BiomeBank.getBiomeSectionFromBlockCoords(tw, x, z);
        if(sect.getBiomeBank().getType() != BiomeType.MOUNTAINOUS) {
        	sect = BiomeSection.getMostDominantSection(tw, x, z);
        }
        
        Random sectionRand = sect.getSectionRandom();
        double maxPeak = getPeakMultiplier(sect, sectionRand);
        
        //Let's just not offset the peak. This seems to give a better result.
        SimpleLocation mountainPeak = sect.getCenter();
        
        double distFromPeak = (1.42*maxMountainRadius)-Math.sqrt(
        		Math.pow(x-mountainPeak.getX(), 2)+Math.pow(z-mountainPeak.getZ(), 2)
        		);
        
        double heightMultiplier = maxPeak*(distFromPeak/maxMountainRadius);
        
        if(heightMultiplier < 1) heightMultiplier = 1;
        
        height = height*heightMultiplier;
        
        //If the height is too high, just force it to smooth out
        if (height > 200) height = 200 + (height - 200) * 0.5;
        if (height > 230) height = 230 + (height - 230) * 0.3;
        if (height > 240) height = 240 + (height - 240) * 0.1;
        if (height > 250) height = 250 + (height - 250) * 0.05;
        
        //Let rivers forcefully carve through birch mountains if they're deep enough.
        double riverDepth = HeightMap.getRawRiverDepth(tw, x, z); //HeightMap.RIVER.getHeight(tw, x, z);
        
        if(coreRawHeight - riverDepth <= TerraformGenerator.seaLevel - 4) {
        	double makeup = 0;
        	//Ensure depth
        	if(coreRawHeight - riverDepth > TerraformGenerator.seaLevel - 10) {
        		makeup = (coreRawHeight - riverDepth) - (TerraformGenerator.seaLevel - 10);
        	}
        	height = coreRawHeight - makeup;// - riverDepth;
        }
        
        return height;
    }
}
