package org.terraform.structure.stronghold;

import org.bukkit.Material;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.MegaChunk;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.main.config.TConfigOption;
import org.terraform.structure.SingleMegaChunkStructurePopulator;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.RoomLayout;
import org.terraform.structure.room.RoomLayoutGenerator;
import org.terraform.utils.GenUtils;
import org.terraform.utils.MazeSpawner;

import java.util.Random;

public class StrongholdPopulator extends SingleMegaChunkStructurePopulator {
    private static boolean debugSpawnMessage = false;
    private int[][] POSITIONS;

    /**
     * @return x, z coords on the circumference of
     * a circle of the specified radius, center 0,0
     */
    private static int[] randomCircleCoords(Random rand, int radius) {
        double angle = rand.nextDouble() * Math.PI * 2;
        int x = (int) (Math.cos(angle) * radius);
        int z = (int) (Math.sin(angle) * radius);
        return new int[]{x, z};
    }

    private static boolean areCoordsEqual(int[] a, int x, int z) {
        return a[0] == x && a[1] == z;
    }

    public int[][] strongholdPositions(TerraformWorld tw) {
        if (POSITIONS == null) {
            POSITIONS = new int[3 + 6 + 10 + 15 + 21 + 28 + 36 + 9][2];
            int pos = 0;
            int radius = 1408;
            Random rand = tw.getHashedRand(1, 1, 1);
            for (int i = 0; i < 3; i++) {
                int[] coords = randomCircleCoords(rand, radius);
                if (!debugSpawnMessage) {
                    TerraformGeneratorPlugin.logger.info("Will spawn stronghold at: " + coords[0] + ", " + coords[1]);
                    debugSpawnMessage = true;
                }
                POSITIONS[pos++] = coords;
            }
            radius += 3072;
            //TerraformGeneratorPlugin.logger.debug("sp-1");
            for (int i = 0; i < 6; i++) POSITIONS[pos++] = randomCircleCoords(rand, radius);
            radius += 3072;
            //TerraformGeneratorPlugin.logger.debug("sp-2");
            for (int i = 0; i < 10; i++) POSITIONS[pos++] = randomCircleCoords(rand, radius);
            radius += 3072;
            //TerraformGeneratorPlugin.logger.debug("sp-3");
            for (int i = 0; i < 15; i++) POSITIONS[pos++] = randomCircleCoords(rand, radius);
            radius += 3072;
            //TerraformGeneratorPlugin.logger.debug("s-pop-4");
            for (int i = 0; i < 21; i++) POSITIONS[pos++] = randomCircleCoords(rand, radius);
            radius += 3072;
            //TerraformGeneratorPlugin.logger.debug("s-pop-5");
            for (int i = 0; i < 28; i++) POSITIONS[pos++] = randomCircleCoords(rand, radius);
            radius += 3072;
            //TerraformGeneratorPlugin.logger.debug("s-pop-6");
            for (int i = 0; i < 36; i++) POSITIONS[pos++] = randomCircleCoords(rand, radius);
            radius += 3072;
            //TerraformGeneratorPlugin.logger.debug("s-pop-7");
            for (int i = 0; i < 9; i++) POSITIONS[pos++] = randomCircleCoords(rand, radius);
            //TerraformGeneratorPlugin.logger.debug("s-pop-8");

        }
        return POSITIONS;
    }

    @Override
    public boolean canSpawn(TerraformWorld tw, int chunkX, int chunkZ, BiomeBank biome) {
        int[][] positions = strongholdPositions(tw);
        for (int x = chunkX * 16; x < chunkX * 16 + 16; x++) {
            for (int z = chunkZ * 16; z < chunkZ * 16 + 16; z++) {
                for (int[] pos : positions) {
                    if (areCoordsEqual(pos, x, z)) return true;
                }
            }
        }

        return false;
    }

    @Override
    public void populate(TerraformWorld tw, PopulatorDataAbstract data) {
        //TerraformGeneratorPlugin.logger.debug("s-populate");
        if (!TConfigOption.STRUCTURES_STRONGHOLD_ENABLED.getBoolean()) return;
        int[][] positions = strongholdPositions(tw);
        for (int x = data.getChunkX() * 16; x < data.getChunkX() * 16 + 16; x++) {
            for (int z = data.getChunkZ() * 16; z < data.getChunkZ() * 16 + 16; z++) {
                for (int[] pos : positions) {
                    if (areCoordsEqual(pos, x, z)) {
                        
                    	//Strongholds no longer calculate from the surface.
                    	//Just pick a directly underground location.
                        int y = GenUtils.randInt(TConfigOption.STRUCTURES_STRONGHOLD_MIN_Y.getInt(), TConfigOption.STRUCTURES_STRONGHOLD_MAX_Y.getInt());
                        
                        //Attempt to force strongholds further underground if
                        //they're above the surface.
                        if(y + 18 > GenUtils.getHighestGround(data, x, z)) {
                        	if(y > TConfigOption.STRUCTURES_STRONGHOLD_FAILSAFE_Y.getInt()) y = TConfigOption.STRUCTURES_STRONGHOLD_FAILSAFE_Y.getInt();
                        }
                        
                        spawnStronghold(tw, this.getHashedRandom(tw, data.getChunkX(), data.getChunkZ()), data, x, y, z);
                        break;
                    }
                }
            }
        }


    }

    public void spawnStronghold(TerraformWorld tw, Random random, PopulatorDataAbstract data, int x, int y, int z) {
        //TerraformGeneratorPlugin.logger.info("Spawning stronghold at: " + x + "," + z);

        int numRooms = 70;
        int range = 100;

        //Level One
        Random hashedRand = tw.getHashedRand(x, y, z);
        RoomLayoutGenerator gen = new RoomLayoutGenerator(hashedRand, RoomLayout.RANDOM_BRUTEFORCE, numRooms, x, y, z, range);

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
        gen.forceAddRoom(25, 25, 15); //At least one room that can be the Portal room.

        CubeRoom stairwayOne = gen.forceAddRoom(5, 5, 18);
        stairwayOne.setRoomPopulator(new StairwayRoomPopulator(random, false, false));
        gen.registerRoomPopulator(new PortalRoomPopulator(random, true, true));
        gen.registerRoomPopulator(new LibraryRoomPopulator(random, false, false));
        gen.registerRoomPopulator(new NetherPortalRoomPopulator(random, false, true));
        gen.registerRoomPopulator(new PrisonRoomPopulator(random, false, false));
        gen.registerRoomPopulator(new SilverfishDenPopulator(random, false, false));
        gen.registerRoomPopulator(new SupplyRoomPopulator(random, false, false));
        gen.registerRoomPopulator(new TrapChestRoomPopulator(random, false, false));
        gen.registerRoomPopulator(new HallwayPopulator(random, false, false));
        gen.generate();
        gen.fill(data, tw, Material.STONE_BRICKS, Material.STONE_BRICKS, Material.MOSSY_STONE_BRICKS, Material.CRACKED_STONE_BRICKS);

        gen.reset();
        mazeSpawner = new MazeSpawner();
        mazeSpawner.setMazePeriod(10);
        mazeSpawner.setMazePathWidth(3);
        mazeSpawner.setCovered(true);
        mazeSpawner.setMazeHeight(4);
        mazeSpawner.setWidth(range + 20);
        gen.setMazePathGenerator(mazeSpawner);

        //Level Two
        y += 18;
        gen.setCentY(y);
        gen.setRand(tw.getHashedRand(x, y, z));


        gen.setPathPopulator(new StrongholdPathPopulator(tw.getHashedRand(x, y, z, 2)));
        CubeRoom stairwayTwo = new CubeRoom(5, 5, 5, stairwayOne.getX(), y, stairwayOne.getZ());
        stairwayTwo.setRoomPopulator(new StairwayTopPopulator(random, false, false));
        gen.getRooms().add(stairwayTwo);
        gen.generate();
        gen.fill(data, tw, Material.STONE_BRICKS, Material.STONE_BRICKS, Material.MOSSY_STONE_BRICKS, Material.CRACKED_STONE_BRICKS);

    }

    //This has to be kept. It is used to locate strongholds
    public int[] getNearestFeature(TerraformWorld tw, int rawX, int rawZ) {
        double minDistanceSquared = Integer.MAX_VALUE;
        int[] min = null;
        for (int[] loc : strongholdPositions(tw)) {
            double distSqr = Math.pow(loc[0] - rawX, 2) + Math.pow(loc[1] - rawZ, 2);
            if (distSqr < minDistanceSquared) {
                minDistanceSquared = distSqr;
                min = loc;
            }
        }
        return new int[]{min[0], min[1]};
    }


    public int[] getCoordsFromMegaChunk(TerraformWorld tw, MegaChunk mc) {
        int[][] positions = strongholdPositions(tw);
        for (int[] pos : positions) {
            if (mc.containsXZBlockCoords(pos[0], pos[1]))
                return pos;
        }

        return null;
    }

    @Override
    public Random getHashedRandom(TerraformWorld world, int chunkX, int chunkZ) {
        return world.getHashedRand(1999222, chunkX, chunkZ);
    }


    @Override
    public boolean isEnabled() {
        return TConfigOption.STRUCTURES_STRONGHOLD_ENABLED.getBoolean();
    }
    
    @Override
    public int getChunkBufferDistance() {
    	return 0;
    }
}
