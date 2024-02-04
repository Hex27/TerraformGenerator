package org.terraform.structure.caves;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeType;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.MegaChunk;
import org.terraform.data.SimpleChunkLocation;
import org.terraform.data.SimpleLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.main.config.TConfigOption;
import org.terraform.structure.JigsawState;
import org.terraform.structure.JigsawStructurePopulator;
import org.terraform.structure.room.RoomLayout;
import org.terraform.structure.room.RoomLayoutGenerator;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.NoiseCacheHandler;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

public class LargeCavePopulator extends JigsawStructurePopulator {
    /**
     * Overall idea: One room layout generator.
     * Apply BFS from the center spawning coord against
     * SimpleLocations, with node connections denoted by the deformed
     * sphere equation. When processing each SimpleLocation, dump it
     * into its corresponding room inside the room generator. Each room
     * shall be 16x16, allowing it to carve exactly within one chunk.
     * <br>
     * Replace roomCarver with this logic (including placing water and
     * containing water spillage)
     * <br>
     * Use room populators for stalactites and the like.
     */
    @Override
    public JigsawState calculateRoomPopulators(TerraformWorld tw, MegaChunk mc) {
        JigsawState state = new JigsawState();

        int[] spawnCoords = mc.getCenterBiomeSectionBlockCoords();

        int x = spawnCoords[0];
        int z = spawnCoords[1];
        Random rand = tw.getHashedRand(x, z, 79810139);

        FastNoise noise = NoiseCacheHandler.getNoise(
                tw,
                NoiseCacheHandler.NoiseCacheEntry.STRUCTURE_LARGECAVE_CARVER,
                world -> {
                    FastNoise n = new FastNoise((int) (world.getSeed()*8726));
                    n.SetNoiseType(FastNoise.NoiseType.Simplex);
                    n.SetFrequency(0.04f);

                    return n;
                });

        //Max Y: TransformedHeight
        //Min Y: -32
        int minY = GenUtils.randInt(rand, -5, -32);
        int highest = GenUtils.getTransformedHeight(tw, x, z) - minY;
        int rX = GenUtils.randInt(rand, 30, 50);
        int rY = (highest - 20) / 2; //5 block padding bottom, 15 padding top.
        int rZ = GenUtils.randInt(rand, 30, 50);
        int y = rY + minY + 6; //6 is derived from the padding rule above

        //Random will be unused
        //All the arguments in gen are going to be unused too, as it won't be generating rooms
        GenericLargeCavePopulator cavePopulator = GenUtils.choice(rand,
            new GenericLargeCavePopulator[] {
                    new GenericLargeCavePopulator(tw.getHashedRand(x, 13729804, z), false, false),
                    new LargeLushCavePopulator(tw.getHashedRand(x, 13729804, z), false, false)
            });

        RoomLayoutGenerator gen = new RoomLayoutGenerator(new Random(), RoomLayout.RANDOM_BRUTEFORCE, 10, x, y, z, 150);
        gen.setGenPaths(false);
        gen.roomCarver = new LargeCaveRoomCarver(GenUtils.randMaterial(rand, Material.LAVA, Material.WATER));
        SimpleLocation center = new SimpleLocation(x, y, z);
        TerraformGeneratorPlugin.logger.info("Large Cave at " + center + " has water level > " + minY);
        HashMap<SimpleChunkLocation, LargeCaveRoomPiece> chunkToRoom = new HashMap<>();

        //BFS against center with "edges" as the noise equation
        ArrayDeque<SimpleLocation> queue = new ArrayDeque<>();
        HashSet<SimpleLocation> seen = new HashSet<>();
        int actualMinY = y;
        seen.add(center);
        queue.add(center);

        while(queue.size() > 0)
        {
            SimpleLocation v = queue.remove();

            //Process v
            LargeCaveRoomPiece caveRoom = chunkToRoom.computeIfAbsent(
                new SimpleChunkLocation(tw.getName(), v.getX()>>4, v.getZ()>>4),
                (loc)->{
                    LargeCaveRoomPiece newRoom = new LargeCaveRoomPiece(15, 15, 15, (v.getX()>>4)*16+8, y, (v.getZ()>>4)*16+8);
                    newRoom.setRoomPopulator(cavePopulator);
                    gen.getRooms().add(newRoom);
                    return newRoom;
                }
            );
            actualMinY = Math.min(actualMinY, v.getY());
            boolean nextToBoundary = false;

            for(BlockFace face: BlockUtils.sixBlockFaces)
            {
                SimpleLocation neighbour = v.getRelative(face);

                if(seen.contains(neighbour)) continue;

                //"Edges" are whether the neighbour satisfies the equation
                double equationResult = Math.pow(neighbour.getX()-center.getX(), 2) / Math.pow(rX, 2)
                        + Math.pow(neighbour.getY()-center.getY(), 2) / Math.pow(rY, 2)
                        + Math.pow(neighbour.getZ()-center.getZ(), 2) / Math.pow(rZ, 2);
                double n = 0.7 * noise.GetNoise(neighbour.getX(), neighbour.getY(), neighbour.getZ());
                if (equationResult > 1 + Math.max(0,n)){
                    nextToBoundary = true;
                    continue;
                }

                seen.add(neighbour);
                queue.add(neighbour);
            }

            //Continue v processing. This is done here to detect boundaries.
            caveRoom.toCarve.put(v, nextToBoundary);
        }

        //Set water levels
        ((LargeCaveRoomCarver) gen.roomCarver).waterLevel = actualMinY + GenUtils.randInt(rand, 4,7);
        gen.getRooms().forEach((room)->((LargeCaveRoomPiece)room).waterLevel = ((LargeCaveRoomCarver) gen.roomCarver).waterLevel);
        state.roomPopulatorStates.add(gen);

        return state;
    }

    @Override
    public void populate(TerraformWorld tw, PopulatorDataAbstract data) {
/*        MegaChunk mc = new MegaChunk(data.getChunkX(), data.getChunkZ());

        int[] spawnCoords = mc.getCenterBiomeSectionBlockCoords();

        int x = spawnCoords[0];//data.getChunkX()*16 + random.nextInt(16);
        int z = spawnCoords[1];//data.getChunkZ()*16 + random.nextInt(16);
        Random rand = tw.getHashedRand(x, z, 999323);

        int highest = HeightMap.getBlockHeight(tw, x, z);//GenUtils.getHighestGround(data, x, z);
        int rY = (highest - 20) / 2; //5 block padding bottom, 15 padding top.
        int y = rY+6; //Center near 0,

        switch(rand.nextInt(3)) {
            case 0 -> new GenericLargeCavePopulator().createLargeCave(tw, rand, data, rY, x, rY + 6, z);
            case 1 -> new MushroomCavePopulator().createLargeCave(tw, rand, data, rY, x, rY + 6, z);
            default -> new LargeLushCavePopulator().createLargeCave(tw, rand, data, rY, x, rY + 6, z);
        }*/
    }

    private boolean rollSpawnRatio(TerraformWorld tw, int chunkX, int chunkZ) {
        return GenUtils.chance(tw.getHashedRand(chunkX, chunkZ, 12345),
                (int) (TConfigOption.STRUCTURES_LARGECAVE_SPAWNRATIO
                        .getDouble() * 10000),
                10000);
    }

    @Override
    public boolean canSpawn(TerraformWorld tw, int chunkX, int chunkZ, BiomeBank biome) {
		if(biome.getType() == BiomeType.DEEP_OCEANIC)
			return false;
        return rollSpawnRatio(tw,chunkX,chunkZ);
    }

    @Override
    public Random getHashedRandom(TerraformWorld tw, int chunkX, int chunkZ) {
        return tw.getHashedRand(123912, chunkX, chunkZ);
    }

    @Override
    public boolean isEnabled() {
        return TConfigOption.STRUCTURES_LARGECAVE_ENABLED.getBoolean();
    }

    @Override
    public int getChunkBufferDistance() {
    	return 0;
    }
}
