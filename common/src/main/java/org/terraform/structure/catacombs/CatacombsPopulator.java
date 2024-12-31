package org.terraform.structure.catacombs;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeType;
import org.terraform.coregen.HeightMap;
import org.terraform.data.MegaChunk;
import org.terraform.data.TerraformWorld;
import org.terraform.main.config.TConfig;
import org.terraform.structure.JigsawState;
import org.terraform.structure.JigsawStructurePopulator;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.RoomLayout;
import org.terraform.structure.room.RoomLayoutGenerator;
import org.terraform.structure.room.carver.StandardRoomCarver;
import org.terraform.structure.room.path.CavePathWriter;
import org.terraform.structure.room.path.PathState;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class CatacombsPopulator extends JigsawStructurePopulator {

    @Override
    public boolean canSpawn(@NotNull TerraformWorld tw, int chunkX, int chunkZ, @NotNull BiomeBank biome) {
        if (!isEnabled()) {
            return false;
        }

        MegaChunk mc = new MegaChunk(chunkX, chunkZ);
        int[] coords = mc.getCenterBiomeSectionBlockCoords();

        // Do not spawn catacombs under deep oceans, there's no space.
        if (biome.getType() == BiomeType.DEEP_OCEANIC) {
            return false;
        }

        // Don't compete with badlandsmine for space
        if (biome == BiomeBank.BADLANDS_CANYON) {
            return false;
        }

        // Don't compete with villages for space. In future, this may be changed
        // to allow multiple structures per megachunk
        if (biome == (BiomeBank.PLAINS)
            || biome == (BiomeBank.FOREST)
            || biome == (BiomeBank.SAVANNA)
            || biome == (BiomeBank.TAIGA)
            || biome == (BiomeBank.SCARLET_FOREST)
            || biome == (BiomeBank.CHERRY_GROVE))
        {
            return false;
        }

        // Do height and space checks
        int height = HeightMap.getBlockHeight(tw, coords[0], coords[1]);
        if (height < TConfig.c.STRUCTURES_CATACOMBS_MAX_Y + 15) {
            // Way too little space. Abort generation.
            return false;
        }

        return rollSpawnRatio(tw, chunkX, chunkZ);
    }

    private boolean rollSpawnRatio(@NotNull TerraformWorld tw, int chunkX, int chunkZ) {
        return GenUtils.chance(tw.getHashedRand(chunkX, chunkZ, 17261),
                (int) (TConfig.c.STRUCTURES_CATACOMBS_SPAWNRATIO * 10000),
                10000
        );
    }

    @Override
    public @NotNull JigsawState calculateRoomPopulators(@NotNull TerraformWorld tw, @NotNull MegaChunk mc) {

        JigsawState state = new JigsawState();

        int[] coords = mc.getCenterBiomeSectionBlockCoords();
        int x = coords[0];
        int z = coords[1];
        int minY = TConfig.c.STRUCTURES_CATACOMBS_MIN_Y;
        int y = GenUtils.randInt(minY, TConfig.c.STRUCTURES_CATACOMBS_MAX_Y);
        int numRooms = 10;
        int range = 50;
        Random random = tw.getHashedRand(x, y, z, 1928374);

        // Level One
        Random hashedRand = tw.getHashedRand(x, y, z);
        boolean canGoDeeper = canGoDeeper(tw, y, hashedRand);
        RoomLayoutGenerator gen = new RoomLayoutGenerator(
                hashedRand,
                RoomLayout.RANDOM_BRUTEFORCE,
                numRooms,
                x,
                y,
                z,
                range
        );
        gen.setPathPopulator(new CatacombsPathPopulator(tw.getHashedRand(x, y, z, 2)));
        gen.setRoomMaxX(10);
        gen.setRoomMaxZ(10);
        gen.setRoomMinX(7);
        gen.setRoomMinZ(7);
        gen.setRoomMinHeight(7);
        gen.setRoomMaxHeight(10);

        gen.registerRoomPopulator(new CatacombsStandardPopulator(random, false, false));
        gen.registerRoomPopulator(new CatacombsSkeletonDungeonPopulator(random, false, false));
        gen.registerRoomPopulator(new CatacombsPillarRoomPopulator(random, false, false));
        gen.registerRoomPopulator(new CatacombsCasketRoomPopulator(random, false, false));

        if (canGoDeeper) {
            gen.registerRoomPopulator(new CatacombsStairwayPopulator(random, true, false));
            gen.registerRoomPopulator(new CatacombsDripstoneCavern(random, true, false));
        }
        gen.roomCarver = new StandardRoomCarver(-1, Material.CAVE_AIR);
        gen.calculateRoomPlacement();
        PathState ps = gen.getOrCalculatePathState(tw);
        ps.writer = new CavePathWriter(0f, 0f, 0f, 0, 2, 0);
        gen.calculateRoomPopulators(tw);
        state.roomPopulatorStates.add(gen);
        //gen.fill(data, tw, Material.CAVE_AIR);

        int catacombLevels = 1;
        RoomLayoutGenerator previousGen;
        while (canGoDeeper) {
            if (catacombLevels >= TConfig.c.STRUCTURES_CATACOMBS_MAX_LEVELS) {
                break;
            }
            y -= 15;
            // Level Two
            hashedRand = tw.getHashedRand(x, y, z);
            canGoDeeper = canGoDeeper(tw, y, hashedRand);
            previousGen = gen;
            gen = new RoomLayoutGenerator(hashedRand, RoomLayout.RANDOM_BRUTEFORCE, numRooms, x, y, z, range);
            gen.setPathPopulator(new CatacombsPathPopulator(tw.getHashedRand(x, y, z, 2)));
            int stairways = 0;

            for (CubeRoom room : previousGen.getRooms()) {

                if (room.getPop() instanceof CatacombsStairwayPopulator) {
                    CubeRoom stairwayBase = new CubeRoom(room.getWidthX(),
                            room.getHeight(),
                            room.getWidthZ(),
                            room.getX(),
                            room.getY() - 15,
                            room.getZ()
                    );
                    stairwayBase.setRoomPopulator(new CatacombsStairwayBasePopulator(hashedRand, true, false));
                    gen.getRooms().add(stairwayBase);
                    stairways++;
                }
                else if (room.getPop() instanceof CatacombsDripstoneCavern) {
                    CubeRoom stairwayBase = new CubeRoom(room.getWidthX(),
                            room.getHeight(),
                            room.getWidthZ(),
                            room.getX(),
                            room.getY() - 15,
                            room.getZ()
                    );
                    stairwayBase.setRoomPopulator(new CatacombsDripstoneBasinPopulator(hashedRand, true, false));
                    gen.getRooms().add(stairwayBase);
                    stairways++;
                }
            }

            gen.setRoomMaxX(10);
            gen.setRoomMaxZ(10);
            gen.setRoomMinX(7);
            gen.setRoomMinZ(7);
            gen.setRoomMinHeight(7);
            gen.setRoomMaxHeight(10);
            gen.registerRoomPopulator(new CatacombsStandardPopulator(random, false, false));
            gen.registerRoomPopulator(new CatacombsSkeletonDungeonPopulator(random, false, false));
            gen.registerRoomPopulator(new CatacombsPillarRoomPopulator(random, false, false));
            gen.registerRoomPopulator(new CatacombsCasketRoomPopulator(random, false, false));

            if (canGoDeeper) {
                gen.registerRoomPopulator(new CatacombsStairwayPopulator(random, true, false));
                gen.registerRoomPopulator(new CatacombsDripstoneCavern(random, true, false));
            }
            if (stairways <= 0) {
                break; // no more stairways. Don't generate.
            }

            gen.roomCarver = new StandardRoomCarver(-1, Material.CAVE_AIR);//new CaveRoomCarver(1.5f,1.7f,1.5f,0.09f,0.03f);
            gen.calculateRoomPlacement();
            ps = gen.getOrCalculatePathState(tw);
            ps.writer = new CavePathWriter(0f, 0f, 0f, 0, 2, 0);
            gen.calculateRoomPopulators(tw);
            state.roomPopulatorStates.add(gen);
            //gen.fill(data, tw, Material.CAVE_AIR);
        }
        return state;
    }

    private boolean canGoDeeper(@NotNull TerraformWorld tw, int y, @NotNull Random random) {
        return y > tw.minY + 10 && GenUtils.chance(random,
                (int) (TConfig.c.STRUCTURES_CATACOMBS_SIZEROLLCHANCE * 10000d),
                10000
        );
    }

    @Override
    public @NotNull Random getHashedRandom(@NotNull TerraformWorld world, int chunkX, int chunkZ) {
        return world.getHashedRand(91829209, chunkX, chunkZ);
    }

    @Override
    public boolean isEnabled() {
        return TConfig.areStructuresEnabled() && TConfig.c.STRUCTURES_CATACOMBS_ENABLED;
    }

    // Underground structures don't need a decorative buffer
    @Override
    public int getChunkBufferDistance() {
        return 0;
    }
}
