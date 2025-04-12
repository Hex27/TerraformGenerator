package org.terraform.structure.stronghold;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.CoordPair;
import org.terraform.data.MegaChunk;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.main.config.TConfig;
import org.terraform.structure.SingleMegaChunkStructurePopulator;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.RoomLayout;
import org.terraform.structure.room.RoomLayoutGenerator;
import org.terraform.utils.GenUtils;
import org.terraform.utils.MazeSpawner;

import java.util.HashMap;
import java.util.Random;

public class StrongholdPopulator extends SingleMegaChunkStructurePopulator {
    private static boolean debugSpawnMessage = false;
    private static final HashMap<TerraformWorld,CoordPair[]> POSITIONS = new HashMap<>();

    /**
     * @return x, z coords on the circumference of
     * a circle of the specified radius, center 0,0
     */
    private static CoordPair randomCircleCoords(@NotNull Random rand, int radius) {
        double angle = rand.nextDouble() * Math.PI * 2;
        int x = (int) (Math.cos(angle) * radius);
        int z = (int) (Math.sin(angle) * radius);
        return new CoordPair(x, z);
    }

    public CoordPair[] strongholdPositions(@NotNull TerraformWorld tw) {
        if (!POSITIONS.containsKey(tw)) {
            CoordPair[] positions = new CoordPair[3 + 6 + 10 + 15 + 21 + 28 + 36 + 9];
            int pos = 0;
            int radius = 1408;
            Random rand = tw.getHashedRand(1, 1, 1);
            for (int i = 0; i < 3; i++) {
                CoordPair coords = randomCircleCoords(rand, radius);
                if (!debugSpawnMessage) {
                    TerraformGeneratorPlugin.logger.info("Will spawn stronghold at: " + coords);
                    debugSpawnMessage = true;
                }
                positions[pos++] = coords;
            }
            radius += 3072;
            // TerraformGeneratorPlugin.logger.debug("sp-1");
            for (int i = 0; i < 6; i++) {
                positions[pos++] = randomCircleCoords(rand, radius);
            }
            radius += 3072;
            // TerraformGeneratorPlugin.logger.debug("sp-2");
            for (int i = 0; i < 10; i++) {
                positions[pos++] = randomCircleCoords(rand, radius);
            }
            radius += 3072;
            // TerraformGeneratorPlugin.logger.debug("sp-3");
            for (int i = 0; i < 15; i++) {
                positions[pos++] = randomCircleCoords(rand, radius);
            }
            radius += 3072;
            // TerraformGeneratorPlugin.logger.debug("s-pop-4");
            for (int i = 0; i < 21; i++) {
                positions[pos++] = randomCircleCoords(rand, radius);
            }
            radius += 3072;
            // TerraformGeneratorPlugin.logger.debug("s-pop-5");
            for (int i = 0; i < 28; i++) {
                positions[pos++] = randomCircleCoords(rand, radius);
            }
            radius += 3072;
            // TerraformGeneratorPlugin.logger.debug("s-pop-6");
            for (int i = 0; i < 36; i++) {
                positions[pos++] = randomCircleCoords(rand, radius);
            }
            radius += 3072;
            // TerraformGeneratorPlugin.logger.debug("s-pop-7");
            for (int i = 0; i < 9; i++) {
                positions[pos++] = randomCircleCoords(rand, radius);
            }
            // TerraformGeneratorPlugin.logger.debug("s-pop-8");
            POSITIONS.put(tw,positions);
        }
        return POSITIONS.get(tw);
    }


    // This is possibly a performance bottleneck.
    // Consider getting a better data structure to store this.
    @Override
    public boolean canSpawn(@NotNull TerraformWorld tw, int chunkX, int chunkZ, BiomeBank biome) {
        if (!isEnabled()) {
            return false;
        }

        CoordPair[] positions = strongholdPositions(tw);
        for (CoordPair pos : positions) {
            if ((pos.x() >> 4) == chunkX && (pos.z() >> 4) == chunkZ) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void populate(@NotNull TerraformWorld tw, @NotNull PopulatorDataAbstract data) {
        if (!isEnabled()) {
            return;
        }

        CoordPair[] positions = strongholdPositions(tw);
        for (int x = data.getChunkX() * 16; x < data.getChunkX() * 16 + 16; x++) {
            for (int z = data.getChunkZ() * 16; z < data.getChunkZ() * 16 + 16; z++) {
                for (CoordPair pos : positions) {
                    if (pos.x() == x && pos.z() == z) {

                        // Strongholds no longer calculate from the surface.
                        // Just pick a directly underground location.
                        int y = GenUtils.randInt(
                                TConfig.c.STRUCTURES_STRONGHOLD_MIN_Y,
                                TConfig.c.STRUCTURES_STRONGHOLD_MAX_Y
                        );

                        // Attempt to force strongholds further underground if
                        // they're above the surface.
                        if (y + 18 > GenUtils.getHighestGround(data, x, z)) {
                            if (y > TConfig.c.STRUCTURES_STRONGHOLD_FAILSAFE_Y) {
                                y = TConfig.c.STRUCTURES_STRONGHOLD_FAILSAFE_Y;
                            }
                        }

                        spawnStronghold(
                                tw,
                                this.getHashedRandom(tw, data.getChunkX(), data.getChunkZ()),
                                data,
                                x,
                                y,
                                z
                        );
                        break;
                    }
                }
            }
        }


    }

    public void spawnStronghold(@NotNull TerraformWorld tw,
                                Random random,
                                @NotNull PopulatorDataAbstract data,
                                int x,
                                int y,
                                int z)
    {
        // TerraformGeneratorPlugin.logger.info("Spawning stronghold at: " + x + "," + z);

        int numRooms = 70;
        int range = 100;

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

        MazeSpawner mazeSpawner = new MazeSpawner();
        mazeSpawner.setMazePeriod(10);
        mazeSpawner.setMazePathWidth(3);
        mazeSpawner.setWidth(range + 20);
        mazeSpawner.setMazeHeight(4);
        mazeSpawner.setCovered(true);
        gen.setMazePathGenerator(mazeSpawner);
        gen.setPathPopulator(new StrongholdPathPopulator(tw.getHashedRand(x, y, z, 2)));
        gen.setRoomMaxX(30);
        gen.setRoomMaxZ(30);
        gen.setRoomMaxHeight(15);
        gen.forceAddRoom(25, 25, 15); // At least one room that can be the Portal room.

        CubeRoom stairwayOne = gen.forceAddRoom(5, 5, 18);
        assert stairwayOne != null;
        stairwayOne.setRoomPopulator(new StairwayRoomPopulator(random, false, false));
        gen.registerRoomPopulator(new PortalRoomPopulator(random, true, true));
        gen.registerRoomPopulator(new LibraryRoomPopulator(random, false, false));
        gen.registerRoomPopulator(new NetherPortalRoomPopulator(random, false, true));
        gen.registerRoomPopulator(new PrisonRoomPopulator(random, false, false));
        gen.registerRoomPopulator(new SilverfishDenPopulator(random, false, false));
        gen.registerRoomPopulator(new SupplyRoomPopulator(random, false, false));
        gen.registerRoomPopulator(new TrapChestRoomPopulator(random, false, false));
        gen.registerRoomPopulator(new HallwayPopulator(random, false, false));
        gen.calculateRoomPlacement();
        gen.fill(
                data,
                tw,
                Material.STONE_BRICKS,
                Material.STONE_BRICKS,
                Material.MOSSY_STONE_BRICKS,
                Material.CRACKED_STONE_BRICKS
        );

        gen.reset();
        mazeSpawner = new MazeSpawner();
        mazeSpawner.setMazePeriod(10);
        mazeSpawner.setMazePathWidth(3);
        mazeSpawner.setCovered(true);
        mazeSpawner.setMazeHeight(4);
        mazeSpawner.setWidth(range + 20);
        gen.setMazePathGenerator(mazeSpawner);

        // Level Two
        y += 18;
        gen.setCentY(y);
        gen.setRand(tw.getHashedRand(x, y, z));


        gen.setPathPopulator(new StrongholdPathPopulator(tw.getHashedRand(x, y, z, 2)));
        CubeRoom stairwayTwo = new CubeRoom(5, 5, 5, stairwayOne.getX(), y, stairwayOne.getZ());
        stairwayTwo.setRoomPopulator(new StairwayTopPopulator(random, false, false));
        gen.getRooms().add(stairwayTwo);
        gen.calculateRoomPlacement();
        gen.fill(
                data,
                tw,
                Material.STONE_BRICKS,
                Material.STONE_BRICKS,
                Material.MOSSY_STONE_BRICKS,
                Material.CRACKED_STONE_BRICKS
        );

    }

    // This has to be kept. It is used to locate strongholds
    public int[] getNearestFeature(@NotNull TerraformWorld tw, int rawX, int rawZ) {
        double minDistanceSquared = Double.MAX_VALUE;
        CoordPair min = null;
        for (CoordPair pos : strongholdPositions(tw)) {
            double distSqr = Math.pow(pos.x() - rawX, 2) + Math.pow(pos.z() - rawZ, 2);

            if (min == null) {
                minDistanceSquared = distSqr;
                min = pos;
                continue;
            }

            if (distSqr < minDistanceSquared) {
                minDistanceSquared = distSqr;
                min = pos;
            }
        }
        assert min != null;
        return new int[] {min.x(),min.z()};
    }


    public CoordPair getCoordsFromMegaChunk(@NotNull TerraformWorld tw, @NotNull MegaChunk mc) {
        CoordPair[] positions = strongholdPositions(tw);
        for (CoordPair pos : positions) {
            if (mc.containsXZBlockCoords(pos.x(),pos.z())) {
                return pos;
            }
        }
        return null;
    }

    @Override
    public @NotNull Random getHashedRandom(@NotNull TerraformWorld world, int chunkX, int chunkZ) {
        return world.getHashedRand(1999222, chunkX, chunkZ);
    }


    @Override
    public boolean isEnabled() {
        return TConfig.areStructuresEnabled() && TConfig.c.STRUCTURES_STRONGHOLD_ENABLED;
    }

    @Override
    public int getChunkBufferDistance() {
        return 0;
    }
}
