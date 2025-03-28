package org.terraform.biome.flat;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.generator.ChunkGenerator;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeHandler;
import org.terraform.coregen.ChunkCache;
import org.terraform.coregen.HeightMap;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.main.config.TConfig;
import org.terraform.small_items.PlantBuilder;
import org.terraform.tree.FractalTypes;
import org.terraform.tree.TreeDB;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.CoralGenerator;
import org.terraform.utils.GenUtils;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.FastNoise.NoiseType;
import org.terraform.utils.noise.NoiseCacheHandler;
import org.terraform.utils.noise.NoiseCacheHandler.NoiseCacheEntry;
import org.terraform.utils.version.V_1_19;

import java.util.Random;

public class MangroveHandler extends BiomeHandler {

    @Override
    public @NotNull BiomeBank getRiverType() {
        return BiomeBank.MANGROVE;
    }

    @Override
    public boolean isOcean() {
        return true;
    }

    @Override
    public @NotNull Biome getBiome() {
        return V_1_19.MANGROVE_SWAMP;
    }

    @Override
    public Material @NotNull [] getSurfaceCrust(@NotNull Random rand) {
        return new Material[] {
                GenUtils.randChoice(rand, Material.GRASS_BLOCK, Material.PODZOL, Material.PODZOL),
                GenUtils.randChoice(rand, Material.DIRT),
                GenUtils.randChoice(rand, Material.DIRT, Material.DIRT, Material.STONE),
                GenUtils.randChoice(rand, Material.DIRT, Material.STONE),
                GenUtils.randChoice(rand, Material.DIRT, Material.STONE)
        };
    }

    @Override
    public @NotNull BiomeHandler getTransformHandler() {
        return this;
    }

    @Override
    public void transformTerrain(@NotNull ChunkCache cache,
                                 TerraformWorld tw,
                                 @NotNull Random random,
                                 ChunkGenerator.@NotNull ChunkData chunk,
                                 int x,
                                 int z,
                                 int chunkX,
                                 int chunkZ)
    {
        int surfaceY = cache.getTransformedHeight(x, z);
        if (surfaceY < TerraformGenerator.seaLevel) {
            int rawX = chunkX * 16 + x;
            int rawZ = chunkZ * 16 + z;
            FastNoise mudNoise = NoiseCacheHandler.getNoise(tw, NoiseCacheEntry.BIOME_SWAMP_MUDNOISE, world -> {
                FastNoise n = new FastNoise((int) (world.getSeed() * 4));
                n.SetNoiseType(NoiseType.SimplexFractal);
                n.SetFrequency(0.05f);
                n.SetFractalOctaves(4);

                return n;
            });

            double noise = mudNoise.GetNoise(rawX, rawZ);

            if (noise < 0) {
                noise = 0;
            }
            int att = (int) Math.round(noise * 10);
            if (att + surfaceY > TerraformGenerator.seaLevel) {
                att = TerraformGenerator.seaLevel - surfaceY;
            }
            for (int i = 1; i <= att; i++) {
                if (i < att) {
                    chunk.setBlock(x, surfaceY + i, z, getSurfaceCrust(random)[1]);
                }
                else {
                    chunk.setBlock(x, surfaceY + i, z, getSurfaceCrust(random)[0]);
                }
            }
            // No guard needed, att < 1 will write surfaceY
            cache.writeTransformedHeight(x, z, (short) (surfaceY + att));
        }
    }

    @Override
    public void populateSmallItems(TerraformWorld tw,
                                   @NotNull Random random,
                                   int rawX,
                                   int surfaceY,
                                   int rawZ,
                                   @NotNull PopulatorDataAbstract data)
    {
        int seaLevel = TerraformGenerator.seaLevel;

        if (!BlockUtils.isStoneLike(data.getType(rawX, surfaceY, rawZ))) {
            return;
        }
        if (surfaceY < seaLevel) {

            if (data.getType(rawX, TerraformGenerator.seaLevel, rawZ) == Material.WATER) {
                if (GenUtils.chance(random, 1, 30)) {
                    PlantBuilder.LILY_PAD.build(data, rawX, TerraformGenerator.seaLevel + 1, rawZ);
                }
            }
        }
        else if(GenUtils.chance(random, 1, 30)){
            PlantBuilder.FIREFLY_BUSH.build(data, rawX, surfaceY + 1, rawZ);
        }

        if (BlockUtils.isWet(new SimpleBlock(data, rawX, surfaceY + 1, rawZ))
            && GenUtils.chance(random, 10, 100)
            && surfaceY < TerraformGenerator.seaLevel - 3)
        { // SEA GRASS/KELP
            CoralGenerator.generateKelpGrowth(data, rawX, surfaceY + 1, rawZ);

        }
        if (GenUtils.chance(random, TConfig.c.BIOME_CLAY_DEPOSIT_CHANCE_OUT_OF_THOUSAND, 1000)) {
            BlockUtils.generateClayDeposit(rawX, surfaceY, rawZ, data, random);
        }
        if (GenUtils.chance(random, 5, 1000)) {
            BlockUtils.replaceCircularPatch(random.nextInt(9999),
                    3.5f,
                    new SimpleBlock(data, rawX, surfaceY, rawZ),
                    V_1_19.MUD
            );
        }
    }

    @Override
    public void populateLargeItems(@NotNull TerraformWorld tw,
                                   @NotNull Random random,
                                   @NotNull PopulatorDataAbstract data)
    {

        int treeX, treeY, treeZ;
        if (GenUtils.chance(random, 8, 10)) {
            treeX = GenUtils.randInt(random, 2, 12) + data.getChunkX() * 16;
            treeZ = GenUtils.randInt(random, 2, 12) + data.getChunkZ() * 16;

            if (data.getBiome(treeX, treeZ) == getBiome()) {
                treeY = GenUtils.getHighestGround(data, treeX, treeZ);

                if (treeY > TerraformGenerator.seaLevel - 6) {
                    // Don't do gradient checks for swamp trees, the mud is uneven.
                    // just make sure it's submerged
                    TreeDB.spawnBreathingRoots(
                            tw,
                            new SimpleBlock(data, treeX, treeY, treeZ),
                            V_1_19.MANGROVE_ROOTS
                    );
                    FractalTypes.Tree.SWAMP_TOP.build(
                            tw,
                            new SimpleBlock(data, treeX, treeY, treeZ),
                            (t) -> t.setCheckGradient(false)
                    );
                }
            }
        }
    }

    @Override
    public @NotNull BiomeBank getBeachType() {
        return BiomeBank.MUDFLATS;
    }

    @Override
    public double calculateHeight(TerraformWorld tw, int x, int z) {

        double height = HeightMap.CORE.getHeight(tw, x, z) - 10;

        // If the height is too low, force it back to 3.
        // 30/11/2023: what the fuck is this guard clause for
        if (height <= 0) {
            height = 3;
        }

        return height;
    }

}
