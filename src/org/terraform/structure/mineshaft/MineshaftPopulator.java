package org.terraform.structure.mineshaft;

import org.bukkit.Material;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.HeightMap;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.MegaChunk;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TConfigOption;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.structure.SingleMegaChunkStructurePopulator;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.RoomLayout;
import org.terraform.structure.room.RoomLayoutGenerator;
import org.terraform.utils.GenUtils;

import java.util.ArrayList;
import java.util.Random;

public class MineshaftPopulator extends SingleMegaChunkStructurePopulator {

    @Override
    public boolean canSpawn(TerraformWorld tw, int chunkX, int chunkZ, ArrayList<BiomeBank> biomes) {
        MegaChunk mc = new MegaChunk(chunkX, chunkZ);
        int[] coords = getCoordsFromMegaChunk(tw, mc);
        if (coords[0] >> 4 == chunkX && coords[1] >> 4 == chunkZ) {
            int height = HeightMap.getHeight(tw, coords[0], coords[1]);
            if (height < TConfigOption.STRUCTURES_MINESHAFT_MAX_Y.getInt() + 15) {
                //Way too little space. Abort generation.
                TerraformGeneratorPlugin.logger.info("Aborting Mineshaft generation: Not enough space (Y=" + height + ")");
                return false;
            }

            return GenUtils
                    .chance(this.getHashedRandom(tw, chunkX, chunkZ),
                            TConfigOption.STRUCTURES_MINESHAFT_CHANCE.getInt(),
                            100);
        }
        return false;
    }

    @Override
    public int[] getCoordsFromMegaChunk(TerraformWorld tw, MegaChunk mc) {
        return mc.getRandomCoords(tw.getHashedRand(mc.getX(), mc.getZ(), 1232412222));
    }

    @Override
    public int[] getNearestFeature(TerraformWorld tw, int rawX, int rawZ) {
        MegaChunk mc = new MegaChunk(rawX, 0, rawZ);

        double minDistanceSquared = Integer.MAX_VALUE;
        int[] min = null;
        for (int nx = -1; nx <= 1; nx++) {
            for (int nz = -1; nz <= 1; nz++) {
                int[] loc = getCoordsFromMegaChunk(tw, mc.getRelative(nx, nz));
                double distSqr = Math.pow(loc[0] - rawX, 2) + Math.pow(loc[1] - rawZ, 2);
                if (distSqr < minDistanceSquared) {
                    minDistanceSquared = distSqr;
                    min = loc;
                }
            }
        }
        return min;
    }

    @Override
    public void populate(TerraformWorld tw, PopulatorDataAbstract data) {

        if (!TConfigOption.STRUCTURES_MINESHAFT_ENABLED.getBoolean())
            return;
        MegaChunk mc = new MegaChunk(data.getChunkX(), data.getChunkZ());
        int[] coords = getCoordsFromMegaChunk(tw, mc);
        int x = coords[0];//data.getChunkX()*16 + random.nextInt(16);
        int z = coords[1];//data.getChunkZ()*16 + random.nextInt(16);
        int height = HeightMap.getHeight(tw, x, z);//GenUtils.getHighestGround(data, x, z);

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
        TerraformGeneratorPlugin.logger.info("Spawning mineshaft at: " + x + "," + z);

        int numRooms = 10;
        int range = 150;

        //Level One
        Random hashedRand = tw.getHashedRand(x, y, z);
        RoomLayoutGenerator gen = new RoomLayoutGenerator(hashedRand, RoomLayout.RANDOM_BRUTEFORCE, numRooms, x, y, z, range);
        gen.setPathPopulator(new MineshaftPathPopulator(tw.getHashedRand(x, y, z, 2)));
        gen.setRoomMaxX(17);
        gen.setRoomMaxZ(17);
        gen.setRoomMinX(13);
        gen.setRoomMinZ(13);

        gen.registerRoomPopulator(new SmeltingHallPopulator(random, false, false));
        gen.registerRoomPopulator(new CaveSpiderDenPopulator(random, false, false));

        if (doubleLevel)
            gen.registerRoomPopulator(new ShaftRoomPopulator(random, true, false));

        gen.setCarveRooms(true);
        gen.generate();
        gen.fill(data, tw, Material.CAVE_AIR);

        if (doubleLevel) {
            //Level Two
            hashedRand = tw.getHashedRand(x, y + 15, z);
            RoomLayoutGenerator secondGen = new RoomLayoutGenerator(hashedRand, RoomLayout.RANDOM_BRUTEFORCE, numRooms, x, y + 15, z, range);
            secondGen.setPathPopulator(new MineshaftPathPopulator(tw.getHashedRand(x, y + 15, z, 2)));
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
}
