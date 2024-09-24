package org.terraform.structure.mineshaft;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeType;
import org.terraform.coregen.HeightMap;
import org.terraform.data.MegaChunk;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.main.config.TConfig;
import org.terraform.structure.JigsawState;
import org.terraform.structure.JigsawStructurePopulator;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.RoomLayout;
import org.terraform.structure.room.RoomLayoutGenerator;
import org.terraform.structure.room.carver.CaveRoomCarver;
import org.terraform.structure.room.path.CavePathWriter;
import org.terraform.structure.room.path.PathState;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class MineshaftPopulator extends JigsawStructurePopulator {

    @Override
    public boolean canSpawn(@NotNull TerraformWorld tw, int chunkX, int chunkZ, @NotNull BiomeBank biome) {
        if (!isEnabled()) {
            return false;
        }

        // MegaChunk mc = new MegaChunk(chunkX, chunkZ);
        // int[] coords = mc.getCenterBiomeSectionBlockCoords();

        // Do not spawn mineshafts under deep oceans, there's no space.
        if (biome.getType() == BiomeType.DEEP_OCEANIC) {
            return false;
        }

        // Don't compete with badlandsmine for space
        if (biome == BiomeBank.BADLANDS_CANYON) {
            return false;
        }

        // Do height and space checks
        // In the interest of optimisation, this check will not be performed.

        return rollSpawnRatio(tw, chunkX, chunkZ);
    }

    private boolean rollSpawnRatio(@NotNull TerraformWorld tw, int chunkX, int chunkZ) {
        return GenUtils.chance(tw.getHashedRand(chunkX, chunkZ, 12222),
                (int) (TConfig.c.STRUCTURES_MINESHAFT_SPAWNRATIO * 10000),
                10000
        );
    }

    @Override
    public @NotNull JigsawState calculateRoomPopulators(@NotNull TerraformWorld tw, @NotNull MegaChunk mc) {
        return calculateRoomPopulators(tw, mc, false);
    }

    public @NotNull JigsawState calculateRoomPopulators(@NotNull TerraformWorld tw,
                                                        @NotNull MegaChunk mc,
                                                        boolean badlandsMineshaft)
    {
        JigsawState state = new JigsawState();

        int[] coords = mc.getCenterBiomeSectionBlockCoords();
        int x = coords[0];
        int z = coords[1];

        int y;
        if (!badlandsMineshaft) {
            y = GenUtils.randInt(
                    TConfig.c.STRUCTURES_MINESHAFT_MIN_Y,
                    TConfig.c.STRUCTURES_MINESHAFT_MAX_Y
            );
            if (y < TerraformGeneratorPlugin.injector.getMinY()) {
                y = TerraformGeneratorPlugin.injector.getMinY() + 15;
            }
        }
        else {
            // Badlands mines want to spawn an entrance shaft. Because of this,
            // they will spawn closer to the surface.
            y = (int) (HeightMap.CORE.getHeight(tw, x, z) - BadlandsMinePopulator.shaftDepth);
        }

        // Level One
        Random hashedRand = tw.getHashedRand(mc.getX(), mc.getZ(), 179821643);
        boolean doubleLevel = hashedRand.nextBoolean();

        RoomLayoutGenerator gen = new RoomLayoutGenerator(hashedRand, RoomLayout.RANDOM_BRUTEFORCE, 10, x, y, z, 150);
        Random pathRand = tw.getHashedRand(x, y, z, 2);
        gen.setPathPopulator(badlandsMineshaft
                             ? new BadlandsMineshaftPathPopulator(pathRand)
                             : new MineshaftPathPopulator(pathRand));
        gen.setRoomMaxX(15);
        gen.setRoomMaxZ(15);
        gen.setRoomMinX(13);
        gen.setRoomMinZ(13);

        gen.registerRoomPopulator(new SmeltingHallPopulator(
                tw.getHashedRand(mc.getX(), mc.getZ(), 198034143),
                false,
                false
        ));
        gen.registerRoomPopulator(new CaveSpiderDenPopulator(
                tw.getHashedRand(mc.getX(), mc.getZ(), 1829731),
                false,
                false
        ));

        if (doubleLevel) {
            gen.registerRoomPopulator(new ShaftRoomPopulator(tw.getHashedRand(mc.getX(), mc.getZ(), 213098),
                    true,
                    false
            ));
        }

        // To connect the mineshaft to the surface entrance
        if (badlandsMineshaft) {
            CubeRoom brokenShaft = new CubeRoom(15, 15, 7, gen.getCentX(), gen.getCentY(), gen.getCentZ());
            brokenShaft.setRoomPopulator(new BrokenShaftPopulator(hashedRand, true, false));
            gen.getRooms().add(brokenShaft);
        }

        gen.wallMaterials = new Material[] {Material.CAVE_AIR};
        gen.roomCarver = new CaveRoomCarver();
        gen.calculateRoomPlacement();
        PathState ps = gen.getOrCalculatePathState(tw);
        ps.writer = new CavePathWriter(0f, 1f, 0f, 0, 1, 0);
        gen.calculateRoomPopulators(tw);
        state.roomPopulatorStates.add(gen);
        // gen.fill(data, tw, Material.CAVE_AIR);

        if (doubleLevel) {
            // Level Two
            RoomLayoutGenerator secondGen = new RoomLayoutGenerator(
                    hashedRand,
                    RoomLayout.RANDOM_BRUTEFORCE,
                    10,
                    x,
                    y + 15,
                    z,
                    150
            );
            pathRand = tw.getHashedRand(x, y + 15, z, 2);
            secondGen.setPathPopulator(badlandsMineshaft
                                       ? new BadlandsMineshaftPathPopulator(pathRand)
                                       : new MineshaftPathPopulator(pathRand));
            secondGen.setRoomMaxX(15);
            secondGen.setRoomMaxZ(15);
            secondGen.setRoomMinX(13);
            secondGen.setRoomMinZ(13);

            for (CubeRoom room : gen.getRooms()) {

                if (room.getPop() instanceof ShaftRoomPopulator) {
                    CubeRoom topShaft = new CubeRoom(room.getWidthX(),
                            room.getHeight(),
                            room.getWidthZ(),
                            room.getX(),
                            room.getY() + 15,
                            room.getZ()
                    );
                    topShaft.setRoomPopulator(new ShaftTopPopulator(hashedRand, true, false));
                    secondGen.getRooms().add(topShaft);
                }
            }

            secondGen.registerRoomPopulator(new SmeltingHallPopulator(
                    tw.getHashedRand(mc.getX(), mc.getZ(), 9870312),
                    false,
                    false
            ));
            secondGen.registerRoomPopulator(new CaveSpiderDenPopulator(
                    tw.getHashedRand(mc.getX(), mc.getZ(), 46783129),
                    false,
                    false
            ));

            secondGen.wallMaterials = new Material[] {Material.CAVE_AIR};
            secondGen.roomCarver = new CaveRoomCarver();
            secondGen.calculateRoomPlacement();
            ps = secondGen.getOrCalculatePathState(tw);
            ps.writer = new CavePathWriter(0f, 1f, 0f, 0, 1, 0);
            secondGen.calculateRoomPopulators(tw);
            state.roomPopulatorStates.add(secondGen);
            // secondGen.fill(data, tw, Material.CAVE_AIR);
        }

        return state;
    }

    @Override
    public @NotNull Random getHashedRandom(@NotNull TerraformWorld world, int chunkX, int chunkZ) {
        return world.getHashedRand(3929202, chunkX, chunkZ);
    }

    @Override
    public boolean isEnabled() {
        return TConfig.areStructuresEnabled() && TConfig.c.STRUCTURES_MINESHAFT_ENABLED;
    }

    @Override
    public int getChunkBufferDistance() {
        return 0;
    }
}
