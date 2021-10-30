package org.terraform.biome.flat;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeHandler;
import org.terraform.biome.custombiomes.CustomBiomeType;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.SimpleLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.data.Wall;
import org.terraform.tree.FractalTreeBuilder;
import org.terraform.tree.FractalTypes;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.SphereBuilder;
import org.terraform.utils.version.OneOneSevenBlockHandler;
import org.terraform.utils.version.Version;

import java.util.Random;

public class CherryGroveHandler extends BiomeHandler {

    @Override
    public boolean isOcean() {
        return false;
    }

    @Override
    public Biome getBiome() {
        return Biome.PLAINS;
    }
    
    @Override
    public CustomBiomeType getCustomBiome() {
        return CustomBiomeType.CHERRY_GROVE;
    }

    @Override
    public Material[] getSurfaceCrust(Random rand) {
        return new Material[]{Material.GRASS_BLOCK,
                Material.DIRT,
                Material.DIRT,
                GenUtils.randMaterial(rand, Material.DIRT, Material.STONE),
                GenUtils.randMaterial(rand, Material.DIRT, Material.STONE)};
    }

    @Override
    public void populateSmallItems(TerraformWorld world, Random random, PopulatorDataAbstract data) {
        for (int x = data.getChunkX() * 16; x < data.getChunkX() * 16 + 16; x++) {
            for (int z = data.getChunkZ() * 16; z < data.getChunkZ() * 16 + 16; z++) {
                int y = GenUtils.getHighestGround(data, x, z);
                if (data.getBiome(x, z) != getBiome()) continue;                
                if (data.getType(x, y, z) == Material.GRASS_BLOCK) {
                	
                    if (GenUtils.chance(random, 1, 10)) { //Grass
                        if (GenUtils.chance(random, 6, 10)) {
                            data.setType(x, y + 1, z, Material.GRASS);
                            if (random.nextBoolean()) {
                                BlockUtils.setDoublePlant(data, x, y + 1, z, Material.TALL_GRASS);
                            }
                        } else {
                            if (GenUtils.chance(random, 7, 10))
                                data.setType(x, y + 1, z, Material.ALLIUM);
                            else
                                BlockUtils.setDoublePlant(data, x, y + 1, z, Material.PEONY);
                        }
                    }
                }
            }
        }
    }

	@Override
	public void populateLargeItems(TerraformWorld tw, Random random, PopulatorDataAbstract data) {
		
		//Small trees or grass poffs
        SimpleLocation[] trees = GenUtils.randomObjectPositions(tw, data.getChunkX(), data.getChunkZ(), 20);
        
        for (SimpleLocation sLoc : trees) {
        	
    		int treeY = GenUtils.getHighestGround(data, sLoc.getX(),sLoc.getZ());
            sLoc.setY(treeY);
            
            if(data.getBiome(sLoc.getX(),sLoc.getZ()) == getBiome() &&
                    BlockUtils.isDirtLike(data.getType(sLoc.getX(),sLoc.getY(),sLoc.getZ())))
            switch(random.nextInt(20)) //0 to 19 inclusive
            {
            case 19: 
            case 18:
            case 17:
            case 16:
            case 15: //Rock (5/20)
            	new SphereBuilder(random, new SimpleBlock(data,sLoc), Material.COBBLESTONE, Material.STONE, Material.STONE, Material.STONE, Material.MOSSY_COBBLESTONE)
            	.setRadius(GenUtils.randInt(random, 3,5))
            	.setRY(GenUtils.randInt(random, 6, 10))
            	.build();
        		break;
        	default: //Tree (15/20)
        		if(random.nextBoolean())  //small trees
            		new FractalTreeBuilder(FractalTypes.Tree.CHERRY_SMALL).build(tw, data, sLoc.getX(),sLoc.getY(),sLoc.getZ());
            	else
            		new FractalTreeBuilder(FractalTypes.Tree.CHERRY_THICK).build(tw, data, sLoc.getX(),sLoc.getY(),sLoc.getZ());		
            	if(Version.isAtLeast(17)) {
                	for(int rX = sLoc.getX()-6; rX <= sLoc.getX() + 6; rX++) {
                		for(int rZ = sLoc.getZ()-6; rZ <= sLoc.getZ() + 6; rZ++) {
                        	Wall ceil = new Wall(new SimpleBlock(data,rX,sLoc.getY(),rZ)).findCeiling(15);
                        	if(ceil != null && GenUtils.chance(random, 1, 30)) {
                        		if(ceil.getType() == Material.DARK_OAK_LEAVES)
                        		{
                        			ceil.getRelative(0,-1,0).setType(OneOneSevenBlockHandler.SPORE_BLOSSOM);
                        		}
                        	}
                        }
            		}
            	}
        		break;
            }
        }
	}
	
    public BiomeBank getBeachType() {
    	return BiomeBank.CHERRY_GROVE_BEACH;
    }
    
    public BiomeBank getRiverType() {
    	return BiomeBank.CHERRY_GROVE_RIVER;
    }

}
