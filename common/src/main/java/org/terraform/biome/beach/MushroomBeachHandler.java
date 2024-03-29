package org.terraform.biome.beach;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.terraform.biome.BiomeHandler;
import org.terraform.coregen.HeightMap;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.SimpleLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.main.config.TConfigOption;
import org.terraform.tree.FractalTypes;
import org.terraform.tree.MushroomBuilder;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import java.util.Random;

public class MushroomBeachHandler extends BiomeHandler {

    @Override
    public boolean isOcean() {
        return false;
    }

    @Override
    public Biome getBiome() {
        return Biome.MUSHROOM_FIELDS;
    }

    @Override
    public Material[] getSurfaceCrust(Random rand) {
        return new Material[]{
        		Material.MYCELIUM,
                Material.DIRT,
                GenUtils.randMaterial(rand, Material.STONE, Material.DIRT, Material.DIRT),
                GenUtils.randMaterial(rand, Material.STONE, Material.DIRT)
            	};
    }

    @Override
    public void populateSmallItems(TerraformWorld tw, Random random, int rawX, int surfaceY, int rawZ, PopulatorDataAbstract data) {
        if(surfaceY < TerraformGenerator.seaLevel) return;
                
        // Generate small shrooms
        if (BlockUtils.isDirtLike(data.getType(rawX, surfaceY, rawZ))) {
            if (BlockUtils.isAir(data.getType(rawX, surfaceY + 1, rawZ))
                    && GenUtils.chance(1, 60)) {
                data.setType(rawX, surfaceY + 1, rawZ, GenUtils.randMaterial(Material.RED_MUSHROOM, Material.BROWN_MUSHROOM));
            }
        }
    }

	@Override
	public void populateLargeItems(TerraformWorld tw, Random random, PopulatorDataAbstract data) {
		SimpleLocation[] bigTrees = GenUtils.randomObjectPositions(tw, data.getChunkX(), data.getChunkZ(), 33, 0.15f);
		SimpleLocation[] smallDecorations = GenUtils.randomObjectPositions(tw, data.getChunkX(), data.getChunkZ(), 15, 0.30f);
		
		// Giant mushrooms
        for (SimpleLocation sLoc : bigTrees) {
            int treeY = GenUtils.getHighestGround(data, sLoc.getX(),sLoc.getZ());
            sLoc.setY(treeY);
            if (data.getBiome(sLoc.getX(),sLoc.getZ()) == getBiome() &&
                BlockUtils.isDirtLike(data.getType(sLoc.getX(),sLoc.getY(),sLoc.getZ()))) {
            	int choice = random.nextInt(3);
            	FractalTypes.Mushroom type;
            	switch(choice) {
            	case 0:
            		type = FractalTypes.Mushroom.GIANT_RED_MUSHROOM;
            		break;
            	case 1:
            		type = FractalTypes.Mushroom.GIANT_BROWN_MUSHROOM;
            		break;
            	default:
            		type = FractalTypes.Mushroom.GIANT_BROWN_FUNNEL_MUSHROOM;
            		break;
            	}
            	
            	if(HeightMap.getTrueHeightGradient(data, sLoc.getX(), sLoc.getZ(), 3) <=
            			TConfigOption.MISC_TREES_GRADIENT_LIMIT.getDouble())
            		new MushroomBuilder(type).build(tw, data, sLoc.getX(),sLoc.getY(),sLoc.getZ());
            }
        }

        // Small mushrooms and rocks
        for (SimpleLocation sLoc : smallDecorations) {
            int treeY = GenUtils.getHighestGround(data, sLoc.getX(), sLoc.getZ());
            sLoc.setY(treeY);
            if (data.getBiome(sLoc.getX(), sLoc.getZ()) == getBiome() &&
                    BlockUtils.isDirtLike(data.getType(sLoc.getX(),sLoc.getY(),sLoc.getZ()))) {
                int choice = random.nextInt(4);
                switch(choice) {
                case 0:
                	new MushroomBuilder(FractalTypes.Mushroom.SMALL_POINTY_RED_MUSHROOM).build(tw, data, sLoc.getX(),sLoc.getY()+1,sLoc.getZ());
                	break;
                case 1:
                	new MushroomBuilder(FractalTypes.Mushroom.SMALL_BROWN_MUSHROOM).build(tw, data, sLoc.getX(),sLoc.getY()+1,sLoc.getZ());
                	break;
                case 2:
                	new MushroomBuilder(FractalTypes.Mushroom.SMALL_RED_MUSHROOM).build(tw, data, sLoc.getX(),sLoc.getY()+1,sLoc.getZ());
                	break;
                default:
                	new MushroomBuilder(FractalTypes.Mushroom.TINY_RED_MUSHROOM).build(tw, data, sLoc.getX(),sLoc.getY()+1,sLoc.getZ());
                	break;
                }
            }
        }
        
        //Bracket fungus
        for (int x = data.getChunkX() * 16; x < data.getChunkX() * 16 + 16; x+=2) {
            for (int z = data.getChunkZ() * 16; z < data.getChunkZ() * 16 + 16; z+=2) {
            	int y = GenUtils.getHighestGround(data, x, z);
                if(data.getBiome(x, z) != getBiome()) continue;
                if(y < TerraformGenerator.seaLevel+4) continue;
                if(HeightMap.getTrueHeightGradient(data, x, z, 3) > 2 
                		&& GenUtils.chance(random, 1, 20)) {
                	BlockUtils.replaceCircle(random.nextInt(919292), 
                			3, new SimpleBlock(data,x,y-2,z),
                			GenUtils.randMaterial(random, Material.BROWN_MUSHROOM_BLOCK, Material.RED_MUSHROOM_BLOCK));
                }
            }
        }
	}
}
