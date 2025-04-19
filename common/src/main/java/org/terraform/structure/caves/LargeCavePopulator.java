package org.terraform.structure.caves;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeType;
import org.terraform.data.MegaChunk;
import org.terraform.data.SimpleChunkLocation;
import org.terraform.data.SimpleLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.main.config.TConfig;
import org.terraform.structure.JigsawState;
import org.terraform.structure.JigsawStructurePopulator;
import org.terraform.structure.room.RoomLayout;
import org.terraform.structure.room.RoomLayoutGenerator;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.NoiseCacheHandler;

import java.util.*;

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
    public @NotNull JigsawState calculateRoomPopulators(@NotNull TerraformWorld tw, @NotNull MegaChunk mc) {
        JigsawState state = new JigsawState();

        int[] spawnCoords = mc.getCenterBiomeSectionBlockCoords();

        int x = spawnCoords[0];
        int z = spawnCoords[1];
        Random rand = tw.getHashedRand(x, z, 79810139);

        FastNoise noise = NoiseCacheHandler.getNoise(tw,
                NoiseCacheHandler.NoiseCacheEntry.STRUCTURE_LARGECAVE_CARVER,
                world -> {
                    FastNoise n = new FastNoise((int) (world.getSeed() * 8726));
                    n.SetNoiseType(FastNoise.NoiseType.Simplex);
                    n.SetFrequency(0.04f);

                    return n;
                }
        );

        // Max Y: TransformedHeight
        // Min Y: -32
        int minY = GenUtils.randInt(rand, -5, -32);
        int highest = GenUtils.getTransformedHeight(tw, x, z) - minY;
        int rX = GenUtils.randInt(rand, 30, 50);
        int rY = (highest - 20) / 2; // 5 block padding bottom, 15 padding top.
        int rZ = GenUtils.randInt(rand, 30, 50);
        int y = rY + minY + 6; // 6 is derived from the padding rule above

        // Random will be unused
        // All the arguments in gen are going to be unused too, as it won't be generating rooms
        GenericLargeCavePopulator cavePopulator = Objects.requireNonNull(GenUtils.choice(rand,
                new GenericLargeCavePopulator[] {
                        new MushroomCavePopulator(tw.getHashedRand(x, 13729804, z), false, false),
                        new GenericLargeCavePopulator(tw.getHashedRand(x, 13729804, z), false, false),
                        new LargeLushCavePopulator(tw.getHashedRand(x, 13729804, z), false, false)
                }
        ));

        RoomLayoutGenerator gen = new RoomLayoutGenerator(new Random(), RoomLayout.RANDOM_BRUTEFORCE, 10, x, y, z, 150);
        gen.setGenPaths(false);
        gen.roomCarver = new LargeCaveRoomCarver(GenUtils.randChoice(rand, Material.LAVA, Material.WATER));
        SimpleLocation center = new SimpleLocation(x, y, z);
        TerraformGeneratorPlugin.logger.info("Large Cave at "
                                             + center
                                             + " has water level > "
                                             + minY
                                             + " with populator "
                                             + cavePopulator.getClass().getSimpleName());
        HashMap<SimpleChunkLocation, LargeCaveRoomPiece> chunkToRoom = new HashMap<>();

        // BFS against center with "edges" as the noise equation
        ArrayDeque<SimpleLocation> queue = new ArrayDeque<>();
        HashSet<SimpleLocation> seen = new HashSet<>();
        int actualMinY = y;
        seen.add(center);
        queue.add(center);

        while (!queue.isEmpty()) {
            SimpleLocation v = queue.remove();

            // Process v
            LargeCaveRoomPiece caveRoom = chunkToRoom.computeIfAbsent(new SimpleChunkLocation(tw.getName(),
                    GenUtils.getTripleChunk(v.getX() >> 4),
                    GenUtils.getTripleChunk(v.getZ() >> 4)
            ), (loc) -> {
                // Each room is 48x48 blocks wide to ensure that rooms do not carve over
                // one another when writing outside their bounds.
                LargeCaveRoomPiece newRoom = new LargeCaveRoomPiece(41,
                        41,
                        15,
                        GenUtils.getTripleChunk(v.getX() >> 4) * 16 + 7,
                        y,
                        GenUtils.getTripleChunk(v.getZ() >> 4) * 16 + 7
                );
                newRoom.setRoomPopulator(cavePopulator);
                gen.getRooms().add(newRoom);
                return newRoom;
            });
            actualMinY = Math.min(actualMinY, v.getY());
            boolean nextToBoundary = false;

            for (BlockFace face : BlockUtils.sixBlockFaces) {
                SimpleLocation neighbour = v.getRelative(face);

                if (seen.contains(neighbour)) {
                    continue;
                }

                //Don't carve 3 blocks under the surface
                if(neighbour.getY() + 3 >= GenUtils.getTransformedHeight(tw, neighbour.getX(),neighbour.getZ())){
                    nextToBoundary = true;
                    continue;
                }

                // "Edges" are whether the neighbour satisfies the equation
                double equationResult = Math.pow(neighbour.getX() - center.getX(), 2) / Math.pow(rX, 2)
                                        + Math.pow(neighbour.getY() - center.getY(), 2) / Math.pow(rY, 2)
                                        + Math.pow(neighbour.getZ() - center.getZ(), 2) / Math.pow(rZ, 2);
                double n = 0.7 * noise.GetNoise(neighbour.getX(), neighbour.getY(), neighbour.getZ());
                if (equationResult > 1 + Math.max(0, n)) {
                    nextToBoundary = true;
                    continue;
                }

                seen.add(neighbour);
                queue.add(neighbour);
            }

            // Continue v processing. This is done here to detect boundaries.
            caveRoom.toCarve.set(v);
            caveRoom.boundaries.set(v, nextToBoundary);
            if(!nextToBoundary && caveRoom.startingLoc == null)
                caveRoom.startingLoc = v;
        }

        // Set water levels
        ((LargeCaveRoomCarver) gen.roomCarver).waterLevel = actualMinY + GenUtils.randInt(rand, 4, 7);
        gen.getRooms()
           .forEach((room) -> ((LargeCaveRoomPiece) room).waterLevel = ((LargeCaveRoomCarver) gen.roomCarver).waterLevel);
        state.roomPopulatorStates.add(gen);

        return state;
    }

    private boolean rollSpawnRatio(@NotNull TerraformWorld tw, int chunkX, int chunkZ) {
        return GenUtils.chance(tw.getHashedRand(chunkX, chunkZ, 12345),
                (int) (TConfig.c.STRUCTURES_LARGECAVE_SPAWNRATIO * 10000),
                10000
        );
    }

    @Override
    public boolean canSpawn(@NotNull TerraformWorld tw, int chunkX, int chunkZ, @NotNull BiomeBank biome) {
        if (!isEnabled()) {
            return false;
        }

        if (biome.getType() == BiomeType.DEEP_OCEANIC) {
            return false;
        }
        return rollSpawnRatio(tw, chunkX, chunkZ);
    }

    @Override
    public @NotNull Random getHashedRandom(@NotNull TerraformWorld tw, int chunkX, int chunkZ) {
        return tw.getHashedRand(123912, chunkX, chunkZ);
    }

    @Override
    public boolean isEnabled() {
        return TConfig.areCavesEnabled() && TConfig.c.STRUCTURES_LARGECAVE_ENABLED;
    }

    @Override
    public int getChunkBufferDistance() {
        return 0;
    }
    @Override
    public int getCaveClusterBufferDistance() {
        return 3;
    }
}
