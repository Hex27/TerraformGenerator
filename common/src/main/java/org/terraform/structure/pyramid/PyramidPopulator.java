package org.terraform.structure.pyramid;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Stairs;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.HeightMap;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.data.MegaChunk;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.data.Wall;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.main.config.TConfigOption;
import org.terraform.structure.SingleMegaChunkStructurePopulator;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.RoomLayout;
import org.terraform.structure.room.RoomLayoutGenerator;
import org.terraform.utils.GenUtils;
import org.terraform.utils.MazeSpawner;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.NoiseCacheHandler;
import org.terraform.utils.noise.FastNoise.NoiseType;
import org.terraform.utils.noise.NoiseCacheHandler.NoiseCacheEntry;

import java.util.ArrayList;
import java.util.Random;

public class PyramidPopulator extends SingleMegaChunkStructurePopulator {

    @Override
    public boolean canSpawn(TerraformWorld tw, int chunkX, int chunkZ, BiomeBank biome) {

        //Check biome
        if (biome != BiomeBank.DESERT)
            return false;

        return rollSpawnRatio(tw, chunkX, chunkZ);
    }

    private boolean rollSpawnRatio(TerraformWorld tw, int chunkX, int chunkZ) {
        return GenUtils.chance(tw.getHashedRand(chunkX, chunkZ, 163456),
                (int) (TConfigOption.STRUCTURES_PYRAMID_SPAWNRATIO
                        .getDouble() * 10000),
                10000);
    }

    @Override
    public void populate(TerraformWorld tw, PopulatorDataAbstract data) {

        int[] coords = new MegaChunk(data.getChunkX(), data.getChunkZ()).getCenterBlockCoords();
        int x = coords[0];
        int z = coords[1];

        int y = HeightMap.getBlockHeight(tw, x, z);//GenUtils.getHighestGround(data, x, z);
        try {
            spawnPyramid(tw, tw.getHashedRand(x, y, z, 1211222), data, x, y, z);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void spawnPyramid(TerraformWorld tw, Random random, PopulatorDataAbstract data, int x, int y, int z) {
        y -= 10;
    	TerraformGeneratorPlugin.logger.info("Spawning Pyramid at: " + x + "," + z);
        int numRooms = 1000;
        int range = 70;
        if(y >= TerraformGenerator.seaLevel+3) {
            spawnSandBase(tw, data, x, y, z);
        } else {
            spawnSandBase(tw, data, x, TerraformGenerator.seaLevel+3, z);
            y = TerraformGenerator.seaLevel-7;
        }
        
        spawnPyramidBase(data, x, y, z);

        Random hashedRand = tw.getHashedRand(x, y, z);

        //Level 0 - Labyrinth Layer (Dark, full of traps). Also the entry level.
        RoomLayoutGenerator level0 = new RoomLayoutGenerator(hashedRand, RoomLayout.RANDOM_BRUTEFORCE, numRooms, x, y - 8, z, range);
//		private int roomMaxHeight = 7;
//		private int roomMinHeight = 5;
//		private int roomMaxX = 15;
//		private int roomMinX = 10;
//		private int roomMaxZ = 15;
//		private int roomMinZ = 10;
        //Add pyramid entrance at the pyramid corner
        int entranceRoomHeight = 4 + GenUtils.getHighestGround(data, x, z + 5 + range / 2) - (y-8);
        CubeRoom entranceRoom = new CubeRoom(9, 9, entranceRoomHeight, x, y - 8, z + 5 + range / 2);
        MainEntrancePopulator entrancePopulator = new MainEntrancePopulator(hashedRand, false, false, BlockFace.NORTH);
        entranceRoom.setRoomPopulator(entrancePopulator);
        level0.getRooms().add(entranceRoom);
        level0.registerRoomPopulator(new HuskTombPopulator(random, false, true));
        level0.registerRoomPopulator(new SilverfishNestPopulator(random, false, false));
        level0.registerRoomPopulator(new CursedChamber(random, false, false));
        level0.registerRoomPopulator(new CryptRoom(random, false, false));
        level0.registerRoomPopulator(new GuardianChamberPopulator(random, false, false));
        level0.registerRoomPopulator(new TrapChestChamberPopulator(random, false, false));
        level0.setPathPopulator(new PyramidDungeonPathPopulator(tw.getHashedRand(x, y - 8, z, 2233)));

        range -= 20;

        //Level 1 - Maze level (?)

        RoomLayoutGenerator level1 = new RoomLayoutGenerator(hashedRand, RoomLayout.RANDOM_BRUTEFORCE, numRooms, x, y, z, range + 10);
        level1.setMazePathGenerator(new MazeSpawner());
        level1.setRoomMinX(5);
        level1.setRoomMaxX(6);
        level1.setRoomMinZ(5);
        level1.setRoomMaxZ(6);
        level1.setNumRooms(15);
        level1.setPathPopulator(new PyramidPathPopulator(tw.getHashedRand(x, y, z, 2233)));
        level1.registerRoomPopulator(new MazeLevelMonsterRoom(random, false, false));

        //Manually add a room for HuskTomb to work.
        CubeRoom room = level1.forceAddRoom(
                GenUtils.randInt(6, 12),
                GenUtils.randInt(6, 12), //6 and 12 because these are the bounds for husk tombs.
                GenUtils.randInt(level1.getRoomMinHeight(), level1.getRoomMaxHeight())
        );
        room.setRoomPopulator(new HuskTombPopulator(random, true, true));

        //Placeholder room to prevent stairways spawning in the middle.
        CubeRoom placeholder = new CubeRoom(20, 20, 15, x, y, z);
        level1.getRooms().add(placeholder);
        //Stairways (Level 0 to 1)
        for (int i = 0; i < 4; i++) {
            CubeRoom stairway = level0.forceAddRoom(5, 5, 10);

            //Don't generate stairways too far from the center.
            while (stairway.centralDistanceSquared(level1.getCenter()) > Math.pow(level1.getRange() / 2, 2)) {
                level0.getRooms().remove(stairway);
                stairway = level0.forceAddRoom(5, 5, 10);
            }
            stairway.setRoomPopulator(new PyramidStairwayRoomPopulator(random, false, false));
            CubeRoom stairwayTop = new CubeRoom(5, 5, 5, stairway.getX(), y, stairway.getZ());
            stairwayTop.setRoomPopulator(new PyramidStairwayTopPopulator(random, false, false));
            level1.getRooms().add(stairwayTop);
        }

        range -= 10;

        //Level 2
        RoomLayoutGenerator level2 = new RoomLayoutGenerator(hashedRand, RoomLayout.RANDOM_BRUTEFORCE, numRooms, x, y + 8, z, range);
        level2.setRoomMaxX(10);
        level2.setRoomMinX(7);
        level2.setRoomMaxZ(10);
        level2.setRoomMinZ(7);
        level2.setRoomMaxHeight(6);
        level2.registerRoomPopulator(new TerracottaRoom(random, false, false));
        level2.registerRoomPopulator(new GenericAntechamber(random, false, false));
        level2.registerRoomPopulator(new WarAntechamber(random, false, false));
        level2.registerRoomPopulator(new TreasureAntechamber(random, false, false));
        level2.registerRoomPopulator(new EnchantmentAntechamber(random, false, true));
        level2.setPathPopulator(new PyramidPathPopulator(tw.getHashedRand(x, y + 8, z, 2253)));
        MazeSpawner mazeSpawner = new MazeSpawner();
        mazeSpawner.setMazePeriod(5);
        level2.setMazePathGenerator(mazeSpawner);

        //Tomb room. 
        CubeRoom tomb = new CubeRoom(20, 20, 20, x, y + 8, z);
        tomb.setRoomPopulator(new ElderGuardianChamber(tw.getHashedRand(x, y + 8, z, 1121), true, true));
        level2.getRooms().add(tomb);

        //Stairways (Level 1 to 2)
        for (int i = 0; i < 3; i++) {
            CubeRoom stairway = level1.forceAddRoom(5, 5, 10);//Don't generate stairways too far from the center.
            while (stairway.centralDistanceSquared(level2.getCenter()) > Math.pow(level2.getRange() / 2, 2)) {
                level1.getRooms().remove(stairway);
                stairway = level1.forceAddRoom(5, 5, 10);
            }
            stairway.setRoomPopulator(new PyramidStairwayRoomPopulator(random, false, false));
            CubeRoom stairwayTop = new CubeRoom(5, 5, 5, stairway.getX(), y + 8, stairway.getZ());
            stairwayTop.setRoomPopulator(new PyramidStairwayTopPopulator(random, false, false));
            level2.getRooms().add(stairwayTop);
        }

        //Remove tombroom placeholder to allow other rooms to spawn there.
        level1.getRooms().remove(placeholder);

        range -= 15;

        //Fill Rooms
        level0.generate(false);
        level0.fill(data, tw, Material.SANDSTONE, Material.CUT_SANDSTONE);

        //ALL LEVEL 0 FLOORING IS STONE/COBBLE/ANDESITE. 
        //Facilitates traps as there is no sandstone pressure plate
        ArrayList<Material> toReplace = new ArrayList<Material>() {{
            add(Material.SANDSTONE);
            add(Material.CUT_SANDSTONE);
        }};
        for (int nx = -50; nx <= 50; nx++) {
            for (int nz = -50; nz <= 50; nz++) {
                if (toReplace.contains(data.getType(x + nx, y - 8, z + nz)))
                    data.setType(x + nx, y - 8, z + nz, GenUtils.randMaterial(Material.STONE, Material.STONE, Material.STONE, Material.COBBLESTONE, Material.ANDESITE));

                //Dither. Include infested stone here.
                if (random.nextBoolean()) {
                    if (toReplace.contains(data.getType(x + nx, y - 7, z + nz)))
                        data.setType(x + nx, y - 7, z + nz, GenUtils.weightedRandomMaterial(random, Material.STONE, 9, Material.INFESTED_STONE, 5, Material.COBBLESTONE, 3,
                                Material.ANDESITE, 3));
                    if (random.nextBoolean()) {
                        if (toReplace.contains(data.getType(x + nx, y - 6, z + nz)))
                            data.setType(x + nx, y - 6, z + nz, GenUtils.weightedRandomMaterial(random, Material.STONE, 9, Material.INFESTED_STONE, 5, Material.COBBLESTONE, 3,
                                    Material.ANDESITE, 3));

                    }
                }

            }
        }

        level1.generate(false);
        //level1.setGenPaths(false);
        level1.fill(data, tw, Material.SANDSTONE, Material.CUT_SANDSTONE);

        level2.generate(false);
        level2.fill(data, tw, Material.SANDSTONE, Material.CUT_SANDSTONE);
    }

    /**
     * Used to ensure that the dungeon level never gets revealed above the surface by a river or something stupid.
     * This will ensure that all ground levels around those coords are at least roughly at y blocks
     * @param data
     * @param x
     * @param y
     * @param z
     */
    public void spawnSandBase(TerraformWorld tw, PopulatorDataAbstract data, int x, int y, int z) {
        int squareRadius = 45;

        FastNoise noiseGenerator = NoiseCacheHandler.getNoise(
        		tw, 
        		NoiseCacheEntry.STRUCTURE_PYRAMID_BASEELEVATOR, 
        		world -> {
                    FastNoise n = new FastNoise((int)world.getSeed());
                    n.SetNoiseType(NoiseType.PerlinFractal);
                    n.SetFrequency(0.007f);
                    n.SetFractalOctaves(6);
                
        	        return n;
        		});

        FastNoise vertNoise = NoiseCacheHandler.getNoise(
        		tw, 
        		NoiseCacheEntry.STRUCTURE_PYRAMID_BASEFUZZER, 
        		world -> {
                    FastNoise n = new FastNoise((int)world.getSeed());
                    n.SetNoiseType(NoiseType.PerlinFractal);
                    n.SetFrequency(0.01f);
                    n.SetFractalOctaves(8);
                
        	        return n;
        		});

        for (int nx = x - squareRadius; nx <= x + squareRadius; nx++) {
            for (int nz = z - squareRadius; nz <= z + squareRadius; nz++) {
                int height = GenUtils.getHighestGround(data, nx, nz);
                Material[] crust = new Material[4];
                crust[0] = data.getType(nx, height, nz);
                Material mat = data.getType(nx, height, nz);
                int original = height;
                //int height = HeightMap.getBlockHeight(tw, nx, nz);


                //Raise ground according to noise levels.
                int raiseDone = 0;
                int noise = Math.round(noiseGenerator.GetNoise(nx, nz) * 5);
                int newHeight = y + noise - 1;
                if (newHeight < y - 1) newHeight = y - 1;

                while (height < newHeight) {
                    raiseDone++;
                    if (!data.getType(nx, height + 1, nz).isSolid()
                            || data.getType(nx, height + 1, nz) == Material.CACTUS)
                        data.setType(nx, height + 1, nz, mat);
                    height++;
                }

                if (raiseDone > 0) {
                    //Make the sides sink down naturally (lol. Or try to, anyway)
                    int XdistanceFromCenter = (int) (Math.abs(nx - x) + Math.abs(vertNoise.GetNoise(nx - 80, nz - 80) * 25));
                    int ZdistanceFromCenter = (int) (Math.abs(nz - z) + Math.abs(vertNoise.GetNoise(nx - 80, nz - 80) * 25));
//        			
                    if (XdistanceFromCenter > squareRadius-10 || ZdistanceFromCenter > squareRadius-10) {
                        //Depress downwards

                        int dist = XdistanceFromCenter > ZdistanceFromCenter ? XdistanceFromCenter : ZdistanceFromCenter;
                        //Bukkit.getLogger().info(height + ":" + (height-raiseDone+((raiseDone)*((50.0f-dist)/5.0f))));
                        float comp = original + ((raiseDone) * ((((float) squareRadius-5) - dist) / 5.0f)) + Math.abs(vertNoise.GetNoise(nx, nz) * 30);
                        if (comp < original) comp = original;
                        while (height > comp) {
                            if (data.getType(nx, height, nz) == mat)
                                if (height > TerraformGenerator.seaLevel)
                                    data.setType(nx, height, nz, Material.AIR);
                                else
                                    data.setType(nx, height, nz, Material.WATER);
                            height--;
                        }
                    }
                }
            }
        }
    }

    public void spawnPyramidBase(PopulatorDataAbstract data, int x, int y, int z) {
        for (int height = 0; height < 40; height++) {
            int radius = 40 - height;
            for (int nx = -radius; nx <= +radius; nx++) {
                for (int nz = -radius; nz <= +radius; nz++) {
                    data.setType(x + nx, y + height, z + nz, GenUtils.randMaterial(Material.SANDSTONE, Material.SMOOTH_SANDSTONE));
                    //data.setType(x + nx, y + height, z + nz, Material.GLASS); //dEBUG.

                    //Corners have special decorations
                    if (Math.abs(nx) == radius && Math.abs(nz) == radius) {
                        if (height < 40 && !data.getType(x + nx, y + height + 1, z + nz).isSolid())
                            data.setType(x + nx, y + height + 1, z + nz, Material.SANDSTONE_WALL);

                        if (height == 38)
                            data.setType(x + nx, y + 38 + 2, z + nz, Material.CAMPFIRE);
                    } else if (GenUtils.chance(1, 20)) {
                        //Side Decorations (Stairs)
                        BlockFace dir = null;

                        if (nx == -radius) {
                            dir = BlockFace.EAST;
                        } else if (nx == radius) {
                            dir = BlockFace.WEST;
                        } else if (nz == -radius) {
                            dir = BlockFace.SOUTH;
                        } else if (nz == radius) {
                            dir = BlockFace.NORTH;
                        }
                        if (dir != null) {
                            Stairs s = (Stairs) Bukkit.createBlockData(GenUtils.randMaterial(Material.SANDSTONE_STAIRS, Material.SMOOTH_SANDSTONE_STAIRS));
                            s.setFacing(dir);
                            data.setBlockData(x + nx, y + height, z + nz, s);
                        }
                    }
                }
            }
        }

        //Tip
        data.setType(x, y + 40, z, Material.GOLD_BLOCK);
        data.setType(x, y + 41, z, Material.SANDSTONE_WALL);

        //Pyramid Surface Decal
        int elevation = 14;
        for (int height = elevation; height <= elevation + 16; height++) {
            int radius = 40 - height;
            for (int nx : new int[]{-radius, 0, radius}) {
                for (int nz : new int[]{-radius, 0, radius}) {
                    //Pyramid Surface Decal
                    int carveLength = (height - elevation);
                    if (carveLength > 8)
                        carveLength = 16 - carveLength;
                    if (nx != 0 && nz != 0) continue;
                    Wall w = null;
                    if (nx == -radius) {
                        w = new Wall(new SimpleBlock(data, x + nx, y + height, z + nz), BlockFace.WEST);
                    } else if (nx == radius) {
                        w = new Wall(new SimpleBlock(data, x + nx, y + height, z + nz), BlockFace.EAST);
                    } else if (nz == -radius) {
                        w = new Wall(new SimpleBlock(data, x + nx, y + height, z + nz), BlockFace.NORTH);
                    } else if (nz == radius) {
                        w = new Wall(new SimpleBlock(data, x + nx, y + height, z + nz), BlockFace.SOUTH);
                    }
                    if (w != null)
                        for (int i = 0; i <= carveLength; i++) {
                            if (carveLength == 0) {
                                if (height == elevation) {
                                    w.getFront().setType(Material.SANDSTONE_WALL);
                                } else if (height == elevation + 16) {
                                    w.getRear().getRelative(0, 2, 0).setType(Material.SANDSTONE_WALL);
                                }
                            }
                            w.getLeft(i).setType(Material.AIR);
                            w.getLeft(i).getRear().setType(Material.CUT_RED_SANDSTONE);

                            w.getRight(i).setType(Material.AIR);
                            w.getRight(i).getRear().setType(Material.CUT_RED_SANDSTONE);

                            if (i == carveLength) {
                                w.getRight(i + 1).getRelative(0, 1, 0).setType(Material.SANDSTONE_WALL);
                                w.getLeft(i + 1).getRelative(0, 1, 0).setType(Material.SANDSTONE_WALL);
                            }
                        }
                }
            }
        }
    }

    @Override
    public Random getHashedRandom(TerraformWorld world, int chunkX, int chunkZ) {
        return world.getHashedRand(72917299, chunkX, chunkZ);
    }

    @Override
    public boolean isEnabled() {
        // TODO Auto-generated method stub
        return TConfigOption.STRUCTURES_MONUMENT_ENABLED.getBoolean();
    }


}
