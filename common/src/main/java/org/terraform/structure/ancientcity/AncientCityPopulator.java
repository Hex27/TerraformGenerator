package org.terraform.structure.ancientcity;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.MultipleFacing;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeType;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.coregen.populatordata.PopulatorDataICABiomeWriterAbstract;
import org.terraform.data.MegaChunk;
import org.terraform.data.SimpleBlock;
import org.terraform.data.SimpleLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.main.config.TConfigOption;
import org.terraform.structure.SingleMegaChunkStructurePopulator;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.RoomLayout;
import org.terraform.structure.room.RoomLayoutGenerator;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.blockdata.MultipleFacingBuilder;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.NoiseCacheHandler;
import org.terraform.utils.noise.NoiseCacheHandler.NoiseCacheEntry;
import org.terraform.utils.version.OneOneNineBlockHandler;
import org.terraform.utils.version.Version;

import java.util.HashSet;
import java.util.Random;

public class AncientCityPopulator extends SingleMegaChunkStructurePopulator {

    @Override
    public boolean canSpawn(TerraformWorld tw, int chunkX, int chunkZ, BiomeBank biome) {
        if (!TConfigOption.STRUCTURES_ANCIENTCITY_ENABLED.getBoolean())
            return false;
        
        //MegaChunk mc = new MegaChunk(chunkX, chunkZ);
        //int[] coords = mc.getCenterBiomeSectionBlockCoords();      	
		//Do not spawn ancient cities in non-mountains, like vanilla
		if(biome.getType() != BiomeType.MOUNTAINOUS
				&& biome.getType() != BiomeType.HIGH_MOUNTAINOUS)
			return false;
		
    	if(!Version.isAtLeast(19)) return false;

        return rollSpawnRatio(tw,chunkX,chunkZ);
    }
    private boolean rollSpawnRatio(TerraformWorld tw, int chunkX, int chunkZ) {
        return GenUtils.chance(tw.getHashedRand(chunkX, chunkZ, 123122),
                (int) (TConfigOption.STRUCTURES_ANCIENTCITY_SPAWNRATIO
                        .getDouble() * 10000),
                10000);
    }

    @Override
    public void populate(TerraformWorld tw, PopulatorDataAbstract data) {
        if (!TConfigOption.STRUCTURES_ANCIENTCITY_ENABLED.getBoolean())
            return;

        MegaChunk mc = new MegaChunk(data.getChunkX(), data.getChunkZ());
        int[] coords = mc.getCenterBiomeSectionBlockCoords();
        int x = coords[0];
        int z = coords[1];
        //int height = HeightMap.getBlockHeight(tw, x, z);
        int minY = TConfigOption.STRUCTURES_ANCIENTCITY_MIN_Y.getInt();
        //if(!Version.isAtLeast(18) && minY < 0) minY = 8;
        int y = GenUtils.randInt(minY, TConfigOption.STRUCTURES_ANCIENTCITY_MAX_Y.getInt());

        
        spawnAncientCity(tw,
                tw.getHashedRand(x, y, z, 23412222),
                data, x, y + 1, z);
    }

    public void spawnAncientCity(TerraformWorld tw, Random random, PopulatorDataAbstract data, int x, int y, int z) {
    	TerraformGeneratorPlugin.logger.info("Spawning ancient city at: " + x + "," + y + "," + z);
    	
        //Level One
    	HashSet<SimpleLocation> occupied = new HashSet<>();
        Random hashedRand = tw.getHashedRand(x, y, z);
        RoomLayoutGenerator gen = new RoomLayoutGenerator(hashedRand, RoomLayout.RANDOM_BRUTEFORCE, 40, x, y, z, 120);
        gen.setPathPopulator(new AncientCityPathPopulator(tw.getHashedRand(x, y, z, 2), gen, occupied));
        gen.setRoomMaxX(26);
        gen.setRoomMaxZ(26);
        gen.setRoomMinHeight(14);
        gen.setRoomMinHeight(20);
        gen.setRoomMinX(16);
        gen.setRoomMinZ(16);
        
        gen.registerRoomPopulator(new AncientCityRuinsPlatform(tw, occupied, gen, random, false, false));
        gen.registerRoomPopulator(new AncientCitySchematicPlatform(tw, occupied, gen, random, false, false));
        gen.registerRoomPopulator(new AncientCityAltarPopulator(tw, occupied, gen, random, false, false));
        gen.registerRoomPopulator(new AncientCityLargePillarRoomPopulator(tw, occupied, gen, random, false, false));
        
        //Forcefully place the center platform in the middle
        CubeRoom room = new CubeRoom(50, 50, 40, x,y,z);
        room.setRoomPopulator(new AncientCityCenterPlatformPopulator(tw, occupied, gen, random, true, true));
        gen.getRooms().add(room);
        
        gen.setCarveRooms(true);
        gen.setCarveRoomsMultiplier(1.5f, 2f, 1.5f);
        gen.generate();
        //gen.fill(data, tw, Material.CAVE_AIR);
        gen.carvePathsOnly(data, tw, Material.CAVE_AIR);
        gen.carveRoomsOnly(data, tw, Material.CAVE_AIR);
        
        //Creep up the whole place.
        float radius = 80;

        FastNoise circleNoise = NoiseCacheHandler.getNoise(
                tw,
                NoiseCacheEntry.BIOME_CAVECLUSTER_CIRCLENOISE,
                world -> {
                    FastNoise n = new FastNoise((int) (world.getSeed() * 11));
                    n.SetNoiseType(FastNoise.NoiseType.Simplex);
                    n.SetFrequency(0.09f);

                    return n;
                });
        
        SimpleBlock center = new SimpleBlock(data, x, y, z);
        TerraformGeneratorPlugin.logger.info("Spawning sculk...");
        
        PopulatorDataICABiomeWriterAbstract ica = null;
        if(center.getPopData() instanceof PopulatorDataICABiomeWriterAbstract) {
        	ica = (PopulatorDataICABiomeWriterAbstract) center.getPopData();
        }
        
        for (float nx = -radius; nx <= radius; nx++) {
            for (float nz = -radius; nz <= radius; nz++) {
                for (float ny = -50; ny <= 50; ny++) {
	                SimpleBlock rel = center.getRelative(Math.round(nx), Math.round(ny), Math.round(nz));
	                
	                if(ica != null)
	                	ica.setBiome(rel.getX(), rel.getY(), rel.getZ(), OneOneNineBlockHandler.DEEP_DARK);
	        		
	                
	                if(!BlockUtils.isStoneLike(rel.getType())) continue;
	                //double radiusSquared = Math.pow(trueRadius+noise.GetNoise(rel.getX(), rel.getY(), rel.getZ())*2,2);
	                double equationResult = Math.pow(nx, 2) / Math.pow(radius, 2)
	                        + Math.pow(nz, 2) / Math.pow(radius, 2) + Math.pow(ny, 2) / Math.pow(50, 2);
	                float noiseVal = circleNoise.GetNoise(rel.getX(), rel.getY(), rel.getZ());
	                if (equationResult <= 1 + 0.7 * noiseVal) {
	                	if(BlockUtils.isExposedToNonSolid(rel) || !rel.getDown().isSolid() || !rel.getUp().isSolid())
	                	{
	                		//Inner area of the circle is sculk
	                		if(equationResult <= 0.7*(1 + 0.7 * noiseVal)) 
	                		{
	                			rel.setType(OneOneNineBlockHandler.SCULK);
		                		
		                		//If the above is not solid, place some decorations
		                		if(!rel.getUp().isSolid())
			                		if(GenUtils.chance(random, 1, 230))
			                			rel.getUp().setType(OneOneNineBlockHandler.SCULK_CATALYST);
			                		else if(GenUtils.chance(random, 1, 150))
			                			rel.getUp().setType(OneOneNineBlockHandler.SCULK_SENSOR);
			                		else if(GenUtils.chance(random, 1, 600))
			                			rel.getUp().setBlockData(OneOneNineBlockHandler.getActiveSculkShrieker());
	                		}
	                		else //Outer area are sculk veins
	                		{
	                			for(BlockFace face:BlockUtils.sixBlockFaces) {
	                				SimpleBlock adj = rel.getRelative(face);
	                				if(adj.isAir())
	                				{
	                					new MultipleFacingBuilder(OneOneNineBlockHandler.SCULK_VEIN)
	                					.setFace(face.getOppositeFace(), true)
	                					.apply(adj);
	                				}
	                				else if(adj.getBlockData() instanceof MultipleFacing)
	                				{
	                					MultipleFacing mf = (MultipleFacing) adj.getBlockData();
	                					mf.setFace(face.getOppositeFace(), true);
	                					adj.setBlockData(mf);
	                				}
	                			}
	                		}
	                	}
	                	else if(rel.getType() == Material.WATER)
	                		rel.setType(Material.CAVE_AIR);
                	}
                }
            }
        }
        
        gen.populatePathsOnly();
        gen.runRoomPopulators(data, tw);
    }

    @Override
    public Random getHashedRandom(TerraformWorld world, int chunkX, int chunkZ) {
        return world.getHashedRand(318377, chunkX, chunkZ);
    }

    @Override
    public boolean isEnabled() {
        return TConfigOption.STRUCTURES_ANCIENTCITY_ENABLED.getBoolean();
    }
    
    //Underground structures don't need a decorative buffer
    @Override
    public int getChunkBufferDistance() {
    	return 0;
    }
}
