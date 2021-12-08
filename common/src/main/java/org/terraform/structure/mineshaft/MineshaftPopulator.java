package org.terraform.structure.mineshaft;

import org.bukkit.Material;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeType;
import org.terraform.coregen.HeightMap;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.MegaChunk;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.main.config.TConfigOption;
import org.terraform.structure.SingleMegaChunkStructurePopulator;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.RoomLayout;
import org.terraform.structure.room.RoomLayoutGenerator;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class MineshaftPopulator extends SingleMegaChunkStructurePopulator {

    @Override
    public boolean canSpawn(TerraformWorld tw, int chunkX, int chunkZ, BiomeBank biome) {
        MegaChunk mc = new MegaChunk(chunkX, chunkZ);
        int[] coords = mc.getCenterBiomeSectionBlockCoords();
        	
		//Do not spawn mineshafts under deep oceans, there's no space.
		if(biome.getType() == BiomeType.DEEP_OCEANIC)
			return false;

		//Don't compete with badlandsmine for space
		if(biome == BiomeBank.BADLANDS_CANYON)
			return false;
		
		//Do height and space checks
        int height = HeightMap.getBlockHeight(tw, coords[0], coords[1]);
        if (height < TConfigOption.STRUCTURES_MINESHAFT_MAX_Y.getInt() + 15) {
            //Way too little space. Abort generation.
            //TerraformGeneratorPlugin.logger.info("Aborting Mineshaft generation: Not enough space (Y=" + height + ")");
            return false;
        }

        return rollSpawnRatio(tw,chunkX,chunkZ);
    }
    private boolean rollSpawnRatio(TerraformWorld tw, int chunkX, int chunkZ) {
        return GenUtils.chance(tw.getHashedRand(chunkX, chunkZ, 12222),
                (int) (TConfigOption.STRUCTURES_MINESHAFT_SPAWNRATIO
                        .getDouble() * 10000),
                10000);
    }

    @Override
    public void populate(TerraformWorld tw, PopulatorDataAbstract data) {
        if (!TConfigOption.STRUCTURES_MINESHAFT_ENABLED.getBoolean())
            return;

        MegaChunk mc = new MegaChunk(data.getChunkX(), data.getChunkZ());
        int[] coords = mc.getCenterBiomeSectionBlockCoords();
        int x = coords[0];
        int z = coords[1];
        int height = HeightMap.getBlockHeight(tw, x, z);

        int y = GenUtils.randInt(TConfigOption.STRUCTURES_MINESHAFT_MIN_Y.getInt(), TConfigOption.STRUCTURES_MINESHAFT_MAX_Y.getInt());

        spawnMineshaft(tw,
                tw.getHashedRand(x, y, z, 82392812),
                data, x, y + 1, z,
                height - y > 25);
    }

    public void spawnMineshaft(TerraformWorld tw, Random random, PopulatorDataAbstract data, int x, int y, int z) {
        spawnMineshaft(tw, random, data, x, y, z, true);
    }

    public void spawnMineshaft(TerraformWorld tw, Random random, PopulatorDataAbstract data, int x, int y, int z, boolean doubleLevel) {
        spawnMineshaft(tw, random, data, x, y, z, doubleLevel, 10, 150, false);
    }

    public void spawnMineshaft(TerraformWorld tw, Random random, PopulatorDataAbstract data, int x, int y, int z, boolean doubleLevel, int numRooms, int range, boolean badlandsMine) {
        TerraformGeneratorPlugin.logger.info("Spawning mineshaft at: " + x + "," + z);

        //Level One
        Random hashedRand = tw.getHashedRand(x, y, z);
        RoomLayoutGenerator gen = new RoomLayoutGenerator(hashedRand, RoomLayout.RANDOM_BRUTEFORCE, numRooms, x, y, z, range);
        if (!badlandsMine)
            gen.setPathPopulator(new MineshaftPathPopulator(tw.getHashedRand(x, y, z, 2)));
        else
            gen.setPathPopulator(new BadlandsMineshaftPathPopulator(tw.getHashedRand(x, y, z, 2)));
        gen.setRoomMaxX(17);
        gen.setRoomMaxZ(17);
        gen.setRoomMinX(13);
        gen.setRoomMinZ(13);

        gen.registerRoomPopulator(new SmeltingHallPopulator(random, false, false));
        gen.registerRoomPopulator(new CaveSpiderDenPopulator(random, false, false));

        if (doubleLevel)
            gen.registerRoomPopulator(new ShaftRoomPopulator(random, true, false));

        gen.setCarveRooms(true);
        if (badlandsMine) {
            CubeRoom brokenShaft = new CubeRoom(
                    15,
                    15,
                    7,
                    gen.getCentX(), gen.getCentY(), gen.getCentZ());
                    brokenShaft.setRoomPopulator(new BrokenShaftPopulator(hashedRand, true, false));
                    gen.getRooms().add(brokenShaft);
        }
        gen.generate();
        gen.fill(data, tw, Material.CAVE_AIR);

        if (doubleLevel) {
            //Level Two
            hashedRand = tw.getHashedRand(x, y + 15, z);
            RoomLayoutGenerator secondGen = new RoomLayoutGenerator(hashedRand, RoomLayout.RANDOM_BRUTEFORCE, numRooms, x, y + 15, z, range);
            if (!badlandsMine)
                secondGen.setPathPopulator(new MineshaftPathPopulator(tw.getHashedRand(x, y + 15, z, 2)));
            else
                secondGen.setPathPopulator(new BadlandsMineshaftPathPopulator(tw.getHashedRand(x, y + 15, z, 2)));
            secondGen.setRoomMaxX(17);
            secondGen.setRoomMaxZ(17);
            secondGen.setRoomMinX(13);
            secondGen.setRoomMinZ(13);

            for (CubeRoom room : gen.getRooms()) {

                if (room.getPop() instanceof ShaftRoomPopulator) {
                    CubeRoom topShaft = new CubeRoom(
                            room.getWidthX(),
                            room.getHeight(),
                            room.getWidthZ(),
                            room.getX(), room.getY() + 15, room.getZ());
                    topShaft.setRoomPopulator(new ShaftTopPopulator(hashedRand, true, false));
                    secondGen.getRooms().add(topShaft);
                }
            }

            secondGen.registerRoomPopulator(new SmeltingHallPopulator(random, false, false));
            secondGen.registerRoomPopulator(new CaveSpiderDenPopulator(random, false, false));
            secondGen.setCarveRooms(true);
            secondGen.generate();
            secondGen.fill(data, tw, Material.CAVE_AIR);
        }
    }

    @Override
    public Random getHashedRandom(TerraformWorld world, int chunkX, int chunkZ) {
        return world.getHashedRand(3929202, chunkX, chunkZ);
    }

    @Override
    public boolean isEnabled() {
        return TConfigOption.STRUCTURES_MINESHAFT_ENABLED.getBoolean();
    }
    
    @Override
    public int getChunkBufferDistance() {
    	return 0;
    }
}
