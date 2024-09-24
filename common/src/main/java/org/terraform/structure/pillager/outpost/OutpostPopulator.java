package org.terraform.structure.pillager.outpost;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Slab.Type;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.NaturalSpawnType;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.coregen.populatordata.PopulatorDataPostGen;
import org.terraform.data.MegaChunk;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.data.Wall;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.main.config.TConfig;
import org.terraform.schematic.TerraSchematic;
import org.terraform.structure.SingleMegaChunkStructurePopulator;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.RoomLayout;
import org.terraform.structure.room.RoomLayoutGenerator;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.WoodUtils;
import org.terraform.utils.WoodUtils.WoodType;
import org.terraform.utils.blockdata.SlabBuilder;
import org.terraform.utils.blockdata.StairBuilder;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.FastNoise.NoiseType;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

public class OutpostPopulator extends SingleMegaChunkStructurePopulator {
    private Material[] stakeGravel;

    private static void setupPillagerSpawns(@NotNull PopulatorDataAbstract data, int x, int y, int z) {
        int i = -5;
        ArrayList<Integer> done = new ArrayList<>();
        for (int nx = x - 80 / 2 - i; nx <= x + 80 / 2 + i; nx++) {
            for (int nz = z - 80 / 2 - i; nz <= z + 80 / 2 + i; nz++) {
                int chunkX = nx >> 4;
                int chunkZ = nz >> 4;
                int hash = Objects.hash(chunkX, chunkZ);

                if (done.contains(hash)) {
                    continue;
                }

                done.add(hash);

                TerraformGeneratorPlugin.injector.getICAData(((PopulatorDataPostGen) data).getWorld()
                                                                                          .getChunkAt(chunkX, chunkZ))
                                                 .registerNaturalSpawns(NaturalSpawnType.PILLAGER,
                                                         x - 80 / 2,
                                                         y,
                                                         z - 80 / 2,
                                                         x + 80 / 2,
                                                         255,
                                                         z + 80 / 2
                                                 );
            }
        }
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
        int height = new SimpleBlock(data, x, 0, z).getGroundOrSeaLevel().getY();
        spawnOutpost(tw, this.getHashedRandom(tw, data.getChunkX(), data.getChunkZ()), data, x, height + 1, z);
    }

    public void spawnOutpost(@NotNull TerraformWorld tw,
                             @NotNull Random random,
                             @NotNull PopulatorDataAbstract data,
                             int x,
                             int y,
                             int z)
    {
        try {
            TerraformGeneratorPlugin.logger.info("Spawning outpost at " + x + "," + y + "," + z);

            BiomeBank biome = tw.getBiomeBank(x, z);

            stakeGravel = new Material[] {Material.COBBLESTONE, Material.STONE, Material.ANDESITE, Material.GRAVEL};

            if (biome == BiomeBank.BADLANDS) {
                stakeGravel = new Material[] {
                        Material.TERRACOTTA,
                        Material.RED_TERRACOTTA,
                        Material.BROWN_TERRACOTTA,
                        Material.YELLOW_TERRACOTTA,
                        Material.ORANGE_TERRACOTTA
                };
            }


            spawnStakes(random, new SimpleBlock(data, x, y, z), biome);

            TerraSchematic outpostBase = TerraSchematic.load("outpost/outpostbase1", new SimpleBlock(data, x, y, z));
            outpostBase.parser = new OutpostSchematicParser(biome, random, data, y - 1);
            outpostBase.setFace(BlockUtils.getDirectBlockFace(random));
            outpostBase.apply();
            TerraSchematic outpostCore = TerraSchematic.load(
                    "outpost/outpostcore1",
                    new SimpleBlock(data, x, y + 5, z)
            );
            outpostCore.parser = new OutpostSchematicParser(biome, random, data, y - 1);
            outpostCore.setFace(BlockUtils.getDirectBlockFace(random));
            outpostCore.apply();
            TerraSchematic outpostTop = TerraSchematic.load("outpost/outposttop1", new SimpleBlock(data, x, y + 11, z));
            outpostTop.parser = new OutpostSchematicParser(biome, random, data, y - 1);
            outpostTop.setFace(BlockUtils.getDirectBlockFace(random));
            outpostTop.apply();

            spawnStairway(random, biome, new SimpleBlock(data, x, y, z), 11);

            RoomLayoutGenerator propGenerator = new RoomLayoutGenerator(
                    random,
                    RoomLayout.RANDOM_BRUTEFORCE,
                    100,
                    x,
                    y,
                    z,
                    35
            );
            propGenerator.setRoomMinX(12);
            propGenerator.setRoomMinZ(12);
            propGenerator.setRoomMaxX(12);
            propGenerator.setRoomMaxZ(12);
            // Placeholder room to ensure primary outpost structure does not
            // get overlapped.
            CubeRoom placeholder = new CubeRoom(20, 20, 15, x, y, z);
            propGenerator.getRooms().add(placeholder);

            propGenerator.registerRoomPopulator(new OutpostTent(random, false, false, biome));
            propGenerator.registerRoomPopulator(new OutpostCampfire(random, false, true));
            propGenerator.registerRoomPopulator(new OutpostLogpile(random, false, true, biome));
            propGenerator.registerRoomPopulator(new OutpostStakeCage(random, false, true, biome, stakeGravel));

            // No paths
            propGenerator.setGenPaths(false);
            propGenerator.calculateRoomPlacement();

            // Remove the placeholder room
            propGenerator.getRooms().remove(placeholder);

            // Only run room populators. We don't want any room space carving.
            propGenerator.runRoomPopulators(data, tw);
            setupPillagerSpawns(data, x, y, z);
        }
        catch (Throwable e) {
            TerraformGeneratorPlugin.logger.error("Something went wrong trying to place outpost at "
                                                  + x
                                                  + ","
                                                  + y
                                                  + ","
                                                  + z
                                                  + "!");
            TerraformGeneratorPlugin.logger.stackTrace(e);
        }
    }

    /**
     * Spawns the surrounding stakes. Additionally, will raise ground levels up
     * with wood planks to make structures less floaty.
     */
    public void spawnStakes(@NotNull Random rand, @NotNull SimpleBlock center, @NotNull BiomeBank bank) {
        int radius = 40;
        Material planksMat = WoodUtils.getWoodForBiome(bank, WoodType.PLANKS);

        FastNoise noise = new FastNoise(rand.nextInt(5000));
        noise.SetNoiseType(NoiseType.Simplex);
        noise.SetFrequency(0.09f);

        for (float x = -radius; x <= radius; x++) {
            for (float z = -radius; z <= radius; z++) {
                SimpleBlock rel = center.getRelative(Math.round(x), 0, Math.round(z)).getGroundOrSeaLevel();

                // double radiusSquared = Math.pow(trueRadius+noise.GetNoise(rel.getX(), rel.getY(), rel.getZ())*2,2);
                double mainRadiusResult = Math.pow(x, 2) / Math.pow(radius, 2) + Math.pow(z, 2) / Math.pow(radius, 2);
                double secondaryRadiusResult = mainRadiusResult * 1.3;
                double noiseVal = noise.GetNoise(rel.getX(), rel.getZ());
                if (mainRadiusResult <= 1 + 0.7 * noiseVal) {

                    // Raise water ground
                    rel.lsetType(planksMat);
                    if (secondaryRadiusResult > 1 + 0.7 * noiseVal) {
                        // Rocky ground
                        BlockUtils.replaceCircularPatch(rand.nextInt(4211), 1.5f, rel, stakeGravel);

                        spawnOneStake(rand, rel.getUp(), bank, stakeGravel);
                    }

                }
            }
        }
    }

    public void spawnOneStake(@NotNull Random rand,
                              @NotNull SimpleBlock base,
                              @NotNull BiomeBank bank,
                              Material... stakeGravel)
    {
        WoodType type = new WoodType[] {WoodType.LOG, WoodType.STRIPPED_LOG}[rand.nextInt(2)];
        Wall w = new Wall(base);
        // Don't spawn stake next to another one
        for (BlockFace face : BlockUtils.xzPlaneBlockFaces) {
            if (Tag.LOGS.isTagged(base.getRelative(face).getType())) {
                return;
            }
        }
        int h = GenUtils.randInt(3, 5);

        for (BlockFace face : BlockUtils.directBlockFaces) {
            if (rand.nextBoolean()) {
                w.getRelative(face).downUntilSolid(rand, stakeGravel);
            }
        }
        w.Pillar(h, rand, WoodUtils.getWoodForBiome(bank, type));
        w.getRelative(0, h, 0).Pillar(GenUtils.randInt(1, 2), rand, WoodUtils.getWoodForBiome(bank, WoodType.FENCE));
        w.getDown().downUntilSolid(rand, stakeGravel);
        w.getDown(2).downUntilSolid(rand, stakeGravel);
    }

    public void spawnStairway(Random rand, @NotNull BiomeBank biome, @NotNull SimpleBlock core, int height) {
        Material pillarMat = GenUtils.randChoice(WoodUtils.getWoodForBiome(biome, WoodType.LOG), Material.COBBLESTONE);
        Material stair = WoodUtils.getWoodForBiome(biome, WoodType.STAIRS);
        Material slab = WoodUtils.getWoodForBiome(biome, WoodType.SLAB);

        BlockFace face = BlockFace.NORTH;

        for (int i = 0; i < height; i++) {
            core.setType(pillarMat);

            for (BlockFace adj : BlockUtils.xzPlaneBlockFaces) {
                core.getRelative(adj).setType(Material.AIR);
            }

            new StairBuilder(stair).setFacing(BlockUtils.getLeft(face)).apply(core.getRelative(face));

            new SlabBuilder(slab).setType(Type.TOP).apply(core.getRelative(BlockUtils.rotateXZPlaneBlockFace(face, 1)));

            core = core.getUp();
            face = BlockUtils.rotateFace(face, 1);
        }
    }

    @Override
    public boolean canSpawn(@NotNull TerraformWorld tw, int chunkX, int chunkZ, @NotNull BiomeBank biome) {
        if (!isEnabled()) {
            return false;
        }

        if (!biome.isDry()) {
            return false;
        }
        if (biome == BiomeBank.DESERT
            || biome == BiomeBank.SNOWY_WASTELAND
            || biome == BiomeBank.BADLANDS
            || biome == BiomeBank.BAMBOO_FOREST)
        {
            return rollSpawnRatio(tw, chunkX, chunkZ);
        }
        else {
            return false;
        }
    }

    private boolean rollSpawnRatio(@NotNull TerraformWorld tw, int chunkX, int chunkZ) {
        return GenUtils.chance(tw.getHashedRand(chunkX, chunkZ, 92992),
                (int) (TConfig.c.STRUCTURES_OUTPOST_SPAWNRATIO * 10000),
                10000
        );
    }

    @Override
    public boolean isEnabled() {
        return TConfig.areStructuresEnabled() && TConfig.c.STRUCTURES_OUTPOST_ENABLED;
    }

    @Override
    public @NotNull Random getHashedRandom(@NotNull TerraformWorld world, int chunkX, int chunkZ) {
        return world.getHashedRand(81903212, chunkX, chunkZ);
    }
}
