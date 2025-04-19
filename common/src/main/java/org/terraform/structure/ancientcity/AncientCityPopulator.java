package org.terraform.structure.ancientcity;

import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeType;
import org.terraform.data.MegaChunk;
import org.terraform.data.SimpleChunkLocation;
import org.terraform.data.SimpleLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.main.config.TConfig;
import org.terraform.structure.EmptyPathWriter;
import org.terraform.structure.JigsawState;
import org.terraform.structure.JigsawStructurePopulator;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.RoomLayout;
import org.terraform.structure.room.RoomLayoutGenerator;
import org.terraform.structure.room.path.CavePathWriter;
import org.terraform.structure.room.path.PathState;
import org.terraform.utils.GenUtils;
import org.terraform.utils.version.Version;

import java.util.HashSet;
import java.util.Random;

public class AncientCityPopulator extends JigsawStructurePopulator {
    public static final int RADIUS = 50;

    @Override
    public boolean canSpawn(@NotNull TerraformWorld tw, int chunkX, int chunkZ, @NotNull BiomeBank biome) {
        if (!isEnabled()) {
            return false;
        }

        // MegaChunk mc = new MegaChunk(chunkX, chunkZ);
        // int[] coords = mc.getCenterBiomeSectionBlockCoords();      	
        // Do not spawn ancient cities in non-mountains, like vanilla
        if (biome.getType() != BiomeType.MOUNTAINOUS && biome.getType() != BiomeType.HIGH_MOUNTAINOUS) {
            return false;
        }

        if (!Version.isAtLeast(19)) {
            return false;
        }

        return rollSpawnRatio(tw, chunkX, chunkZ);
    }

    private boolean rollSpawnRatio(@NotNull TerraformWorld tw, int chunkX, int chunkZ) {
        return GenUtils.chance(tw.getHashedRand(chunkX, chunkZ, 123122),
                (int) (TConfig.c.STRUCTURES_ANCIENTCITY_SPAWNRATIO * 10000),
                10000
        );
    }

    @Override
    public @NotNull JigsawState calculateRoomPopulators(@NotNull TerraformWorld tw, @NotNull MegaChunk mc)
    {
        JigsawState state = new JigsawState();
        int[] coords = mc.getCenterBiomeSectionBlockCoords();
        int x = coords[0];
        int z = coords[1];
        int minY = TConfig.c.STRUCTURES_ANCIENTCITY_MIN_Y;
        int y = GenUtils.randInt(minY, TConfig.c.STRUCTURES_ANCIENTCITY_MAX_Y);
        Random random = tw.getHashedRand(x, y, z, 23412222);

        TerraformGeneratorPlugin.logger.info("Spawning ancient city at: " + x + "," + y + "," + z);

        //Cave carver
        RoomLayoutGenerator carverGen = new RoomLayoutGenerator(GenUtils.RANDOMIZER, RoomLayout.RANDOM_BRUTEFORCE, 0, x,y,z, RADIUS);
        for(int nx = ((((x-RADIUS)>>4)-1)<<4) + 7; nx <= ((((x+RADIUS)>>4)+1)<<4) + 7; nx+=16)
            for(int nz = ((((z-RADIUS)>>4)-1)<<4) + 7; nz <= ((((z+RADIUS)>>4)+1)<<4) + 7; nz+=16)
            {
                carverGen.getRooms().add(new CubeRoom(1,1,5,nx, y, nz));
            }
        carverGen.roomCarver = new AncientCityBFSCarver(new SimpleLocation(x,y,z));
        PathState ps = carverGen.getOrCalculatePathState(tw);
        ps.writer = new EmptyPathWriter();
        state.roomPopulatorStates.add(carverGen);


        // Level One
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

        // Forcefully place the center platform in the middle.
        SimpleChunkLocation centChunk = new SimpleChunkLocation(tw.getName(), x,y,z);
        int centX = (centChunk.getX() << 4) + 8;
        int centZ = (centChunk.getZ() << 4) + 8;
        CubeRoom room = new CubeRoom(46, 46, 40, centX, y, centZ);
        room.setRoomPopulator(new AncientCityCenterPlatformPopulator(tw, occupied, gen, random, true, true));
        gen.getRooms().add(room);

        gen.calculateRoomPlacement();
        ps = gen.getOrCalculatePathState(tw);
        ps.writer = new CavePathWriter(2f,2f,2f,0,0,0);
        gen.calculateRoomPopulators(tw);
        state.roomPopulatorStates.add(gen);

        return state;
    }

    @Override
    public @NotNull Random getHashedRandom(@NotNull TerraformWorld world, int chunkX, int chunkZ) {
        return world.getHashedRand(318377, chunkX, chunkZ);
    }

    @Override
    public boolean isEnabled() {
        return TConfig.areStructuresEnabled() && TConfig.c.STRUCTURES_ANCIENTCITY_ENABLED;
    }

    // Underground structures don't need a decorative buffer on the surface
    @Override
    public int getChunkBufferDistance() {
        return 0;
    }

    //But no cave decorations.
    @Override
    public int getCaveClusterBufferDistance() {
        return 3;
    }
}
