package org.terraform.structure.ancientcity;

import org.bukkit.Material;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.MegaChunk;
import org.terraform.data.SimpleLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.main.config.TConfigOption;
import org.terraform.structure.SingleMegaChunkStructurePopulator;
import org.terraform.structure.room.RoomLayout;
import org.terraform.structure.room.RoomLayoutGenerator;
import org.terraform.utils.GenUtils;
import org.terraform.utils.version.Version;

import java.util.ArrayList;
import java.util.Random;

public class AncientCityPopulator extends SingleMegaChunkStructurePopulator {

    @Override
    public boolean canSpawn(TerraformWorld tw, int chunkX, int chunkZ, BiomeBank biome) {
//        MegaChunk mc = new MegaChunk(chunkX, chunkZ);
//        int[] coords = mc.getCenterBiomeSectionBlockCoords();      	
		//Do not spawn catacombs under deep oceans, there's no space.
//		if(biome.getType() == BiomeType.DEEP_OCEANIC)
//			return false;
//
//		//Don't compete with badlandsmine for space
//		if(biome == BiomeBank.BADLANDS_CANYON)
//			return false;
//		
//		//Don't compete with villages for space. In future, this may be changed
//		//to allow multiple structures per megachunk
//		if(biome == (BiomeBank.PLAINS)
//            		|| biome == (BiomeBank.FOREST)
//            		|| biome == (BiomeBank.SAVANNA)
//            		|| biome == (BiomeBank.TAIGA)
//               		|| biome == (BiomeBank.SCARLET_FOREST)
//               		|| biome == (BiomeBank.CHERRY_GROVE))
//			return false;
    	if(!Version.isAtLeast(1.18)) return false;

        return rollSpawnRatio(tw,chunkX,chunkZ);
    }
    private boolean rollSpawnRatio(TerraformWorld tw, int chunkX, int chunkZ) {
        return GenUtils.chance(tw.getHashedRand(chunkX, chunkZ, 17261),
                (int) (TConfigOption.STRUCTURES_CATACOMBS_SPAWNRATIO
                        .getDouble() * 10000),
                10000);
    }

    @Override
    public void populate(TerraformWorld tw, PopulatorDataAbstract data) {
        if (!TConfigOption.STRUCTURES_CATACOMBS_ENABLED.getBoolean())
            return;

        MegaChunk mc = new MegaChunk(data.getChunkX(), data.getChunkZ());
        int[] coords = mc.getCenterBiomeSectionBlockCoords();
        int x = coords[0];
        int z = coords[1];
        //int height = HeightMap.getBlockHeight(tw, x, z);
        int minY = TConfigOption.STRUCTURES_CATACOMBS_MIN_Y.getInt();
        if(!Version.isAtLeast(18) && minY < 0) minY = 8;
        int y = GenUtils.randInt(minY, TConfigOption.STRUCTURES_CATACOMBS_MAX_Y.getInt());

        
        spawnAncientCity(tw,
                tw.getHashedRand(x, y, z, 1928374),
                data, x, y + 1, z);
    }

    public void spawnAncientCity(TerraformWorld tw, Random random, PopulatorDataAbstract data, int x, int y, int z) {
    	TerraformGeneratorPlugin.logger.info("Spawning ancient city at: " + x + "," + z);
    	
        //Level One
    	ArrayList<SimpleLocation> occupied = new ArrayList<>();
        Random hashedRand = tw.getHashedRand(x, y, z);
        RoomLayoutGenerator gen = new RoomLayoutGenerator(hashedRand, RoomLayout.RANDOM_BRUTEFORCE, 10, x, y, z, 150);
        gen.setPathPopulator(new AncientCityPathPopulator(tw.getHashedRand(x, y, z, 2), gen, occupied));
        gen.setRoomMaxX(17);
        gen.setRoomMaxZ(17);
        gen.setRoomMinX(13);
        gen.setRoomMinZ(13);
        
        //gen.registerRoomPopulator(new SmeltingHallPopulator(random, false, false));
        //gen.registerRoomPopulator(new CaveSpiderDenPopulator(random, false, false));

        gen.setCarveRooms(true);
        
        gen.generate();
        gen.fill(data, tw, Material.CAVE_AIR);
    
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
