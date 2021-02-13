package org.terraform.biome.flat;

import org.bukkit.Axis;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockFace;
import org.terraform.biome.BiomeHandler;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.SimpleLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.data.Wall;
import org.terraform.main.TConfigOption;
import org.terraform.structure.small.DesertWellPopulator;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.blockdata.OrientableBuilder;

import java.util.Random;

public class DesertHandler extends BiomeHandler {

    @Override
    public boolean isOcean() {
        return false;
    }

    @Override
    public Biome getBiome() {
        return Biome.DESERT;
    }

    @Override
    public Material[] getSurfaceCrust(Random rand) {
        return new Material[]{Material.SAND,
                Material.SAND,
                GenUtils.randMaterial(rand, Material.SANDSTONE, Material.SAND),
                GenUtils.randMaterial(rand, Material.SANDSTONE, Material.STONE),
                GenUtils.randMaterial(rand, Material.SANDSTONE, Material.STONE)};
    }

    @Override
    public void populateSmallItems(TerraformWorld world, Random random, PopulatorDataAbstract data) {

        boolean cactusGathering = GenUtils.chance(random, 1, 100);
        for (int x = data.getChunkX() * 16; x < data.getChunkX() * 16 + 16; x++) {
            for (int z = data.getChunkZ() * 16; z < data.getChunkZ() * 16 + 16; z++) {
                int y = GenUtils.getTrueHighestBlock(data, x, z);
                if (data.getBiome(x, z) != getBiome()) continue;
                Material base = data.getType(x, y, z);

                if (cactusGathering) {
                    if (GenUtils.chance(random, 5, 100))
                        data.setType(x, y, z, Material.GRASS_PATH);
                }

                if (base == Material.SAND) {
                    if (GenUtils.chance(random, 1, 100) ||
                            (GenUtils.chance(random, 1, 20) && cactusGathering)) {
                        boolean canSpawn = true;
                        for (BlockFace face : BlockUtils.directBlockFaces) {
                            if (data.getType(x + face.getModX(), y + 1, z + face.getModZ()) != Material.AIR)
                                canSpawn = false;
                        }
                        if (canSpawn)
                            BlockUtils.spawnPillar(random, data, x, y + 1, z, Material.CACTUS, 3, 5);
                    } else if (GenUtils.chance(random, 1, 80)) {
                        data.setType(x, y + 1, z, Material.DEAD_BUSH);
                    }
                }


            }
        }
    }

	@Override
	public void populateLargeItems(TerraformWorld tw, Random random, PopulatorDataAbstract data) {
		
		//Rib cages
        SimpleLocation[] ribCages = GenUtils.randomObjectPositions(tw, data.getChunkX(), data.getChunkZ(), 
        		256, 0.6f);

        for (SimpleLocation sLoc : ribCages) {
            int ribY = GenUtils.getHighestGround(data, sLoc.getX(),sLoc.getZ());
            sLoc.setY(ribY - GenUtils.randInt(random, 0, 6));
            if(data.getBiome(sLoc.getX(),sLoc.getZ()) == getBiome() &&
            		data.getType(sLoc.getX(),ribY,sLoc.getZ()) == Material.SAND) {
            	spawnRibCage(random, new SimpleBlock(data, sLoc.getX(), sLoc.getY(), sLoc.getZ()));
            }
        }

        if (GenUtils.chance(random, TConfigOption.STRUCTURES_DESERTWELL_CHANCE_OUT_OF_TEN_THOUSAND.getInt(), 10000)) {
            new DesertWellPopulator().populate(tw, random, data, false);
        }
	}
	
	public void spawnRibCage(Random random, SimpleBlock target) {
		BlockFace direction = BlockUtils.getDirectBlockFace(random);
		int spineLength = GenUtils.randInt(random, 10, 14);
		float ribWidthRadius = GenUtils.randInt(random, 1, 2) + spineLength/2;
		float ribHeightRadius = 0.7f*ribWidthRadius; //GenUtils.randInt(random, 6, 8);
		//eqn -> ((y-ribHeight)/(ribHeight))^2 + ((x)/(ribWidth))^2 = 1
		int interval = 2;
		if(random.nextBoolean()) 
			interval += 1;

		float ribSizeMultiplier = 1.0f;
		
		for(int segmentIndex = 0; segmentIndex < spineLength; segmentIndex++) {
			Wall seg = new Wall(target.getRelative(direction, segmentIndex),direction);
			new OrientableBuilder(Material.BONE_BLOCK)
			.setAxis(BlockUtils.getAxisFromBlockFace(direction))
			.apply(seg);
			
			if(segmentIndex < (int) (spineLength/2f)) {
				ribSizeMultiplier += 0.05f;
			}else if(segmentIndex > (int) (spineLength/2f))
				ribSizeMultiplier -= 0.05f;
			
			if(segmentIndex % interval == 0 && segmentIndex > spineLength/6) {
				for(float nHor = 1; nHor <= ribWidthRadius*ribSizeMultiplier; nHor+=0.01) {
					
					int[] multipliers = {-1};
					if(nHor > ribWidthRadius*ribSizeMultiplier/3)
						multipliers = new int[]{-1,1};
					
					for(int multiplier:multipliers) {
						int ny = (int) Math.round(ribHeightRadius*ribSizeMultiplier + (multiplier*ribHeightRadius*ribSizeMultiplier
								*Math.sqrt(1-Math.pow(
										(nHor)
										/(ribWidthRadius*ribSizeMultiplier),
										2)
								)));
						
						int horRel = Math.round(nHor);
						Axis axis = BlockUtils.getAxisFromBlockFace(BlockUtils.getLeft(direction));
						if(ny > ribSizeMultiplier*ribHeightRadius/3 && ny < 5*ribSizeMultiplier*ribHeightRadius/3) {
							axis = Axis.Y;
						}
						
						new OrientableBuilder(Material.BONE_BLOCK)
						.setAxis(axis)
						.apply(seg.getRelative(0,ny,0).getRight(horRel))
						.apply(seg.getRelative(0,ny,0).getLeft(horRel));
					}
				}
				
			}
		}
		
	}
}
