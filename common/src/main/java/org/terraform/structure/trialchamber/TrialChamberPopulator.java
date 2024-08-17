package org.terraform.structure.trialchamber;

import org.terraform.biome.BiomeBank;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.TerraformWorld;
import org.terraform.main.config.TConfigOption;
import org.terraform.structure.VanillaStructurePopulator;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class TrialChamberPopulator extends VanillaStructurePopulator {
    public TrialChamberPopulator() {
        super("trial_chambers");
    }

    @Override
    public boolean canSpawn(TerraformWorld tw, int chunkX, int chunkZ, BiomeBank biome) {
        return rollSpawnRatio(tw,chunkX,chunkZ);
    }
    public void spawnTrialChamber(TerraformWorld tw, Random random, PopulatorDataAbstract data, int x, int y, int z){
//        int numRooms = 10;
//        int range = 40;
//
//        //Level One
//        Random hashedRand = tw.getHashedRand(x, y, z);
//        RoomLayoutGenerator gen = new RoomLayoutGenerator(hashedRand, RoomLayout.RANDOM_BRUTEFORCE, numRooms, x, y, z, range);
//        gen.setPathPopulator(new TrailRuinsPathPopulator(hashedRand));
//        gen.setRoomMaxX(10);
//        gen.setRoomMaxZ(10);
//        gen.setRoomMinX(6);
//        gen.setRoomMinZ(6);
//        gen.setRoomMaxHeight(15);
//        gen.setCarveRooms(true);
//        gen.setCarveRoomsMultiplier(0,0,0); //No carving
//
//        CubeRoom towerRoom = new CubeRoom(7,7,7, x,y,z);
//        towerRoom.setRoomPopulator(new TrailRuinsTowerRoom(random, false, false));
//        gen.getRooms().add(towerRoom);
//
//        gen.registerRoomPopulator(new TrailRuinsTowerRoom(random, false, false));
//        gen.registerRoomPopulator(new TrailRuinsHutRoom(random, false, false));
//
//        gen.calculateRoomPlacement();
//        gen.carvePathsOnly(data, tw, Material.BARRIER);
//        gen.populatePathsOnly();
//        gen.fillRoomsOnly(data, tw, Material.STONE_BRICKS);

    }

    @Override
    public void populate(TerraformWorld tw, PopulatorDataAbstract data) {
//        MegaChunk mc = new MegaChunk(data.getChunkX(), data.getChunkZ());
//        int[] coords = mc.getCenterBiomeSectionBlockCoords(); //getCoordsFromMegaChunk(tw, mc);
//        int x = coords[0];//data.getChunkX()*16 + random.nextInt(16);
//        int z = coords[1];//data.getChunkZ()*16 + random.nextInt(16);
//
//        int y = GenUtils.getHighestGround(data, x, z) - GenUtils.randInt(
//                this.getHashedRandom(tw, data.getChunkX(), data.getChunkZ()),
//                10, 15);
//
//
//        spawnTrialChamber(tw,this.getHashedRandom(tw, data.getChunkX(), data.getChunkZ()),
//                data,x,y,z);
    }


    @Override
    public Random getHashedRandom(TerraformWorld tw, int chunkX, int chunkZ) {
        return tw.getHashedRand(670191632, chunkX, chunkZ);
    }

    @Override
    public boolean isEnabled() {
        return TConfigOption.STRUCTURES_TRIALCHAMBER_ENABLED.getBoolean();
    }

    private boolean rollSpawnRatio(TerraformWorld tw, int chunkX, int chunkZ) {
        return GenUtils.chance(tw.getHashedRand(chunkX, chunkZ, 19650),
                (int) (TConfigOption.STRUCTURES_TRIALCHAMBER_SPAWNRATIO
                        .getDouble() * 10000),
                10000);
    }
}
