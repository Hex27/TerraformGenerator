package org.terraform.structure.trailruins;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.MegaChunk;
import org.terraform.data.TerraformWorld;
import org.terraform.main.config.TConfig;
import org.terraform.structure.SingleMegaChunkStructurePopulator;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.RoomLayout;
import org.terraform.structure.room.RoomLayoutGenerator;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class TrailRuinsPopulator extends SingleMegaChunkStructurePopulator {
    @Override
    public boolean canSpawn(@NotNull TerraformWorld tw, int chunkX, int chunkZ, BiomeBank biome) {
        if (!isEnabled()) {
            return false;
        }

        if (biome == (BiomeBank.TAIGA) || biome == (BiomeBank.SNOWY_TAIGA) || biome == (BiomeBank.JUNGLE)) {
            return rollSpawnRatio(tw, chunkX, chunkZ);
        }
        return false;
    }

    public void spawnTrailRuins(@NotNull TerraformWorld tw,
                                Random random,
                                @NotNull PopulatorDataAbstract data,
                                int x,
                                int y,
                                int z)
    {
        int numRooms = 10;
        int range = 40;

        // Level One
        Random hashedRand = tw.getHashedRand(x, y, z);
        RoomLayoutGenerator gen = new RoomLayoutGenerator(
                hashedRand,
                RoomLayout.RANDOM_BRUTEFORCE,
                numRooms,
                x,
                y,
                z,
                range
        );
        gen.setPathPopulator(new TrailRuinsPathPopulator(hashedRand));
        gen.setRoomMaxX(10);
        gen.setRoomMaxZ(10);
        gen.setRoomMinX(6);
        gen.setRoomMinZ(6);
        gen.setRoomMaxHeight(15);
        gen.setCarveRooms(true);
        gen.setCarveRoomsMultiplier(0, 0, 0); // No carving

        CubeRoom towerRoom = new CubeRoom(7, 7, 7, x, y, z);
        towerRoom.setRoomPopulator(new TrailRuinsTowerRoom(random, false, false));
        gen.getRooms().add(towerRoom);

        gen.registerRoomPopulator(new TrailRuinsTowerRoom(random, false, false));
        gen.registerRoomPopulator(new TrailRuinsHutRoom(random, false, false));

        gen.calculateRoomPlacement();
        gen.carvePathsOnly(data, tw, Material.BARRIER);
        gen.populatePathsOnly();
        gen.fillRoomsOnly(data, tw, Material.STONE_BRICKS);

    }

    @Override
    public void populate(@NotNull TerraformWorld tw, @NotNull PopulatorDataAbstract data) {
        if (!isEnabled()) {
            return;
        }

        MegaChunk mc = new MegaChunk(data.getChunkX(), data.getChunkZ());
        int[] coords = mc.getCenterBiomeSectionBlockCoords(); // getCoordsFromMegaChunk(tw, mc);
        int x = coords[0];// data.getChunkX()*16 + random.nextInt(16);
        int z = coords[1];// data.getChunkZ()*16 + random.nextInt(16);

        int y = GenUtils.getHighestGround(data, x, z) - GenUtils.randInt(this.getHashedRandom(
                tw,
                data.getChunkX(),
                data.getChunkZ()
        ), 10, 15);


        spawnTrailRuins(tw, this.getHashedRandom(tw, data.getChunkX(), data.getChunkZ()), data, x, y, z);
    }


    @Override
    public @NotNull Random getHashedRandom(@NotNull TerraformWorld tw, int chunkX, int chunkZ) {
        return tw.getHashedRand(217842323, chunkX, chunkZ);
    }

    @Override
    public boolean isEnabled() {
        return TConfig.areStructuresEnabled()
               && (BiomeBank.isBiomeEnabled(BiomeBank.TAIGA)
                   || BiomeBank.isBiomeEnabled(BiomeBank.SNOWY_TAIGA)
                   || BiomeBank.isBiomeEnabled(BiomeBank.JUNGLE))
               && TConfig.c.STRUCTURES_TRAILRUINS_ENABLED;
    }

    private boolean rollSpawnRatio(@NotNull TerraformWorld tw, int chunkX, int chunkZ) {
        return GenUtils.chance(tw.getHashedRand(chunkX, chunkZ, 34122),
                (int) (TConfig.c.STRUCTURES_TRAILRUINS_SPAWNRATIO * 10000),
                10000
        );
    }
}
