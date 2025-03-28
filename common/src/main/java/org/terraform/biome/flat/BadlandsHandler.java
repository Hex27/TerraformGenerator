package org.terraform.biome.flat;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockFace;
import org.bukkit.generator.ChunkGenerator;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeBlender;
import org.terraform.biome.BiomeHandler;
import org.terraform.biome.beach.OasisBeach;
import org.terraform.biome.mountainous.BadlandsCanyonHandler;
import org.terraform.coregen.ChunkCache;
import org.terraform.coregen.HeightMap;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.main.config.TConfig;
import org.terraform.small_items.PlantBuilder;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.NoiseCacheHandler;
import org.terraform.utils.noise.NoiseCacheHandler.NoiseCacheEntry;
import org.terraform.utils.version.V_1_21_5;
import org.terraform.utils.version.Version;

import java.util.ArrayList;
import java.util.Random;

public class BadlandsHandler extends BiomeHandler {
    static final int sandRadius = TConfig.c.BIOME_BADLANDS_PLATEAU_SAND_RADIUS;
    static final int plateauHeight = TConfig.c.BIOME_BADLANDS_PLATEAU_HEIGHT;
    static final float plateauFrequency = TConfig.c.BIOME_BADLANDS_PLATEAU_FREQUENCY;
    static final double plateauThreshold = TConfig.c.BIOME_BADLANDS_PLATEAU_THRESHOLD;
    static final double plateauCommonness = TConfig.c.BIOME_BADLANDS_PLATEAU_COMMONNESS;
    static private BiomeBlender riversBlender;
    static private BiomeBlender plateauBlender;

    private static @NotNull BiomeBlender getRiversBlender(TerraformWorld tw) {
        // Only one blender needed!
        if (riversBlender == null) {
            riversBlender = new BiomeBlender(tw, true, false).setGridBlendingFactor(0.45);
        }
        return riversBlender;
    }

    private static @NotNull BiomeBlender getPlateauBlender(TerraformWorld tw) {
        if (plateauBlender == null) {
            plateauBlender = new BiomeBlender(tw, true, true).setRiverThreshold(10);
        }
        return plateauBlender;
    }

    public static @NotNull FastNoise getPlateauNoise(TerraformWorld tw) {
        return NoiseCacheHandler.getNoise(tw, NoiseCacheEntry.BIOME_BADLANDS_PLATEAUNOISE, world -> {
            FastNoise n = new FastNoise((int) (world.getSeed() * 7509));
            n.SetNoiseType(FastNoise.NoiseType.CubicFractal);
            n.SetFractalOctaves(2);
            n.SetFrequency(plateauFrequency);
            return n;
        });
    }

    // This is for optimizing sand, ew
    static int getPlateauHeight(TerraformWorld tw, int x, int z) {
        double rawValue = Math.max(0, getPlateauNoise(tw).GetNoise(x, z) + plateauCommonness);
        double noiseValue = rawValue * getPlateauBlender(tw).getEdgeFactor(BiomeBank.BADLANDS, x, z) * (1 - ((int) (
                rawValue
                / plateauThreshold) * 0.1));

        double graduated = noiseValue / plateauThreshold;
        double platformHeight = (int) graduated * plateauHeight + (10 * Math.pow(graduated
                                                                                 - (int) graduated
                                                                                 - 0.5
                                                                                 - 0.1, 7) * plateauHeight);

        return (int) Math.round(platformHeight);
    }

    @Override
    public @NotNull BiomeBank getRiverType() {
        return BiomeBank.BADLANDS_RIVER;
    }

    @Override
    public boolean isOcean() {
        return false;
    }

    @Override
    public @NotNull Biome getBiome() {
        return Biome.BADLANDS;
    }

    @Override
    public Material @NotNull [] getSurfaceCrust(@NotNull Random rand) {
        return new Material[] {
                Material.RED_SAND,
                Material.RED_SAND,
                GenUtils.randChoice(rand, Material.RED_SAND, Material.RED_SANDSTONE),
                GenUtils.randChoice(rand, Material.RED_SANDSTONE, Material.STONE),
                GenUtils.randChoice(rand, Material.RED_SANDSTONE, Material.STONE)
        };
    }

    @Override
    public void populateSmallItems(@NotNull TerraformWorld world,
                                   @NotNull Random random,
                                   int rawX,
                                   int surfaceY,
                                   int rawZ,
                                   @NotNull PopulatorDataAbstract data)
    {

        // While not a small item, generatePlateaus is left in, as it
        // transforms the terrain itself. Structures placed must account for
        // these terrain changes.
        // TODO: Past me wrote this to kick the bucket to future me. I am future me. Fuck you.

        generatePlateaus(world, rawX, surfaceY, rawZ, data);

        OasisBeach.generateOasisBeach(world, random, data, rawX, rawZ, BiomeBank.BADLANDS);

        if (HeightMap.getNoiseGradient(world, rawX, rawZ, 3) >= 1.5 && GenUtils.chance(random, 49, 50)) {
            BadlandsCanyonHandler.oneUnit(random, data, rawX, rawZ, true);
            return;
        }

        Material base = data.getType(rawX, surfaceY, rawZ);
        if (base == Material.SAND || base == Material.RED_SAND) {
            if (GenUtils.chance(random, 1, 200)) {

                for (BlockFace face : BlockUtils.directBlockFaces) {
                    if (data.getType(rawX + face.getModX(), surfaceY + 1, rawZ + face.getModZ()) != Material.AIR) {
                        return;
                    }
                }
                // Prevent cactus from spawning on plateaus:
                if (HeightMap.getBlockHeight(world, rawX, rawZ) + 5 < surfaceY) {
                    return;
                }
                if (GenUtils.chance(1, 50)) {
                    spawnDeadTree(data, rawX, surfaceY, rawZ);
                }
                else if(GenUtils.chance(1, 30))
                    PlantBuilder.FIREFLY_BUSH.build(data, rawX, surfaceY+1, rawZ);
                else {
                    int cactusHeight = PlantBuilder.CACTUS.build(random, data, rawX, surfaceY + 1, rawZ, 2, 5);
                    if(Version.isAtLeast(21.5)
                        && GenUtils.chance(random, 1, 10))
                        data.setType(rawX, surfaceY+1+cactusHeight, rawZ, V_1_21_5.CACTUS_FLOWER);
                }
            }
            else if (GenUtils.chance(random, 1, 80) && surfaceY > TerraformGenerator.seaLevel) {
                PlantBuilder.build(new SimpleBlock(data,rawX, surfaceY + 1,rawZ),
                        PlantBuilder.DEAD_BUSH, PlantBuilder.SHORT_DRY_GRASS, PlantBuilder.TALL_DRY_GRASS);            }
        }
    }

    @Override
    public BiomeHandler getTransformHandler() {
        return this;
    }

    @Override
    public void transformTerrain(ChunkCache cache,
                                 @NotNull TerraformWorld tw,
                                 Random random,
                                 ChunkGenerator.@NotNull ChunkData chunk,
                                 int x,
                                 int z,
                                 int chunkX,
                                 int chunkZ)
    {
        // Badlands doesn't actually mutate height in here (WHY??).
        // Because of that, don't edit heightChanges
        // This is perpetuating the cycle of abuse and falsehood
        // Let's leave it till it explodes for some reason

        BiomeBlender blender = getRiversBlender(tw);

        FastNoise wallNoise = NoiseCacheHandler.getNoise(tw, NoiseCacheEntry.BIOME_BADLANDS_WALLNOISE, world -> {
            FastNoise n = new FastNoise((int) (tw.getWorld().getSeed() * 2));
            n.SetNoiseType(FastNoise.NoiseType.SimplexFractal);
            n.SetFrequency(0.07f);
            n.SetFractalOctaves(2);
            return n;
        });

        int rawX = chunkX * 16 + x;
        int rawZ = chunkZ * 16 + z;

        double preciseHeight = HeightMap.getPreciseHeight(tw, rawX, rawZ);

        if (HeightMap.getRawRiverDepth(tw, rawX, rawZ) > 0) {
            double riverlessHeight = HeightMap.getRiverlessHeight(tw, rawX, rawZ) - 2;

            // These are for blending river effect with other biomes
            double edgeFactor = blender.getEdgeFactor(BiomeBank.BADLANDS, rawX, rawZ);
            double bottomEdgeFactor = Math.min(2 * edgeFactor, 1);
            double topEdgeFactor = Math.max(2 * edgeFactor - 1, 0);

            // Max height difference between sea level and riverlessHeight
            double maxDiff = riverlessHeight - TerraformGenerator.seaLevel;
            double heightAboveSea = preciseHeight - 2 - TerraformGenerator.seaLevel;
            double riverFactor = heightAboveSea / maxDiff; // 0 at river level, 1 at riverlessHeight

            if (riverFactor > 0 && heightAboveSea > 0) {
                int buildHeight = (int) Math.round(bottomEdgeFactor * (Math.min(1, 4 * Math.pow(riverFactor, 4))
                                                                       * maxDiff
                                                                       + wallNoise.GetNoise(rawX, rawZ) * 1.5));

                for (int i = buildHeight; i >= 0; i--) {
                    int lowerHeight = Math.min(TerraformGenerator.seaLevel + i, (int) Math.round(riverlessHeight));

                    chunk.setBlock(x, lowerHeight, z, BlockUtils.getTerracotta(lowerHeight));
                }

                double threshold = 0.4 + (1 - topEdgeFactor) * 0.6;

                // Curved top edges
                if (riverFactor > threshold) {
                    int upperBuildHeight = (int) Math.round(1 *// topEdgeFactor *
                                                            (Math.min(1, 50 * Math.pow(riverFactor - threshold, 2.5))
                                                             * maxDiff + wallNoise.GetNoise(rawX, rawZ) * 1.5));

                    if (topEdgeFactor == 0) {
                        return;
                    }

                    for (int i = 0; i <= upperBuildHeight; i++) {
                        int upperHeight = (int) riverlessHeight - i;

                        chunk.setBlock(x, upperHeight, z, BlockUtils.getTerracotta(upperHeight));
                    }
                }

                // Coat with red sand
                if (riverFactor > threshold + 0.12) {
                    chunk.setBlock(x, (int) riverlessHeight + 1, z, Material.RED_SAND);
                }
            }
        }
    }

    /*TODO: This is currently not in transformTerrain because of some impurity regarding
     * how the surrounding base sand is placed. transformTerrain is only supposed to access
     * one pair of x and z coordinates at a time.
     */
    void generatePlateaus(@NotNull TerraformWorld tw,
                          int rawX,
                          int surfaceY,
                          int rawZ,
                          @NotNull PopulatorDataAbstract data)
    {
        FastNoise detailsNoise = NoiseCacheHandler.getNoise(tw, NoiseCacheEntry.BIOME_BADLANDS_WALLNOISE, world -> {
            FastNoise n = new FastNoise((int) (tw.getSeed() * 7509));
            n.SetNoiseType(FastNoise.NoiseType.SimplexFractal);
            n.SetFrequency(0.08f);
            return n;
        });

        // Calculate plateau height
        double rawValue = Math.max(0, getPlateauNoise(tw).GetNoise(rawX, rawZ) + plateauCommonness);
        double noiseValue = rawValue * getPlateauBlender(tw).getEdgeFactor(BiomeBank.BADLANDS, rawX, rawZ) * (1
                                                                                                              - ((int) (
                rawValue
                / plateauThreshold) * 0.05));

        double graduated = noiseValue / plateauThreshold;
        double platformHeight = (int) graduated * plateauHeight + (10 * Math.pow(graduated
                                                                                 - (int) graduated
                                                                                 - 0.5
                                                                                 - 0.1, 7) * plateauHeight);

        boolean placeSand = false;
        for (int y = 1; y <= (int) Math.round(platformHeight); y++) {
            placeSand = true;
            Material material; // Coat plateaus with sand
            if ((int) graduated * plateauHeight == y) {
                material = Material.RED_SAND;
            }
            else if ((int) graduated * plateauHeight == y + 1) {
                material = GenUtils.randChoice(Material.RED_SAND,
                        Material.RED_SAND,
                        BlockUtils.getTerracotta(surfaceY + y)
                );
            }
            else if ((int) graduated * plateauHeight == y + 2) {
                material = GenUtils.randChoice(Material.RED_SAND,
                        BlockUtils.getTerracotta(surfaceY + y),
                        BlockUtils.getTerracotta(surfaceY + y)
                );
            }
            else {
                material = BlockUtils.getTerracotta(surfaceY + y);
            }

            data.setType(rawX, surfaceY + y, rawZ, material);
        }

        // Prevent inner parts of plateau from generating sand in vain
        if (!placeSand || graduated - (int) graduated > 0.2) {
            return;
        }

        // Surround plateaus with sand
        int level = (((int) graduated) - 1) * plateauHeight; // handle second and third levels of plateau
        for (int sx = rawX - sandRadius; sx <= rawX + sandRadius; sx++) {
            for (int sz = rawZ - sandRadius; sz <= rawZ + sandRadius; sz++) {
                double distance = Math.sqrt(Math.pow(sx - rawX, 2) + Math.pow(sz - rawZ, 2));

                if (distance < sandRadius) {
                    // Skip if sand would levitate
                    if ((int) graduated != 1 && getPlateauHeight(tw, sx, sz) != plateauHeight) {
                        continue;
                    }

                    int sandHeight = (int) Math.round(plateauHeight * 0.55 * Math.pow(1 - distance / sandRadius, 1.7)
                                                      + detailsNoise.GetNoise(sx, sz));
                    for (int y = 1 + level; y <= sandHeight + level; y++) {
                        if (data.getType(sx, HeightMap.getBlockHeight(tw, sx, sz) + y, sz) == Material.AIR) {
                            data.setType(sx, HeightMap.getBlockHeight(tw, sx, sz) + y, sz, Material.RED_SAND);
                        }
                    }
                }
            }
        }
    }

    void spawnDeadTree(@NotNull PopulatorDataAbstract data, int x, int y, int z) {
        if (!TConfig.areTreesEnabled()) {
            return;
        }

        int height = GenUtils.randInt(5, 7);
        int branches = GenUtils.randInt(1, height == 5 ? 2 : 3);

        for (int i = 1; i <= height; i++) {
            data.setType(x, y + i, z, Material.DARK_OAK_WOOD);
        }

        ArrayList<Integer> usedBranchHorizontals = new ArrayList<>();
        ArrayList<Integer> usedBranchVerticals = new ArrayList<>();
        for (int i = 0; i < branches; i++) {
            int bHeight = GenUtils.randInt(2, height - 1);
            int bDirection = GenUtils.randInt(1, 4);

            if (usedBranchHorizontals.contains(bDirection) || usedBranchVerticals.contains(bHeight)) {
                i--;
                continue;
            }

            int bx = x;
            int bz = z;

            switch (bDirection) {
                case 1:
                    bz++;
                    break;
                case 2:
                    bx++;
                    break;
                case 3:
                    bz--;
                    break;
                default:
                    bx--;
                    break;
            }

            data.setType(bx, y + bHeight, bz, Material.DARK_OAK_WOOD);

            usedBranchHorizontals.add(bDirection);
            usedBranchVerticals.add(bHeight);
        }
    }

    // TODO: Seems like a mass of excessive calculation just to spawn dead trees
    // Look into optimisation here in future.
    @Override
    public void populateLargeItems(@NotNull TerraformWorld tw,
                                   @NotNull Random random,
                                   @NotNull PopulatorDataAbstract data)
    {

        for (int x = data.getChunkX() * 16; x < data.getChunkX() * 16 + 16; x++) {
            for (int z = data.getChunkZ() * 16; z < data.getChunkZ() * 16 + 16; z++) {
                int highest = GenUtils.getTrueHighestBlock(data, x, z);

                BiomeBank currentBiome = tw.getBiomeBank(x, z);
                if (currentBiome != BiomeBank.BADLANDS
                    && currentBiome != BiomeBank.BADLANDS_BEACH
                    && currentBiome != BiomeBank.BADLANDS_CANYON)
                {
                    continue;
                }

                if (HeightMap.getNoiseGradient(tw, x, z, 3) >= 1.5 && GenUtils.chance(random, 49, 50)) {
                    BadlandsCanyonHandler.oneUnit(random, data, x, z, true);
                    continue;
                }

                Material base = data.getType(x, highest, z);
                if (base == Material.SAND || base == Material.RED_SAND) {
                    if (GenUtils.chance(random, 1, 200)) {

                        boolean canSpawn = true;
                        for (BlockFace face : BlockUtils.directBlockFaces) {
                            if (data.getType(x + face.getModX(), highest + 1, z + face.getModZ()) != Material.AIR) {
                                canSpawn = false;
                            }
                        }

                        if (GenUtils.getHighestGround(data, x, z) + 5 < highest) {
                            canSpawn = false;
                        }
                        if (canSpawn && GenUtils.chance(1, 50)) {
                            spawnDeadTree(data, x, highest, z);
                        }

                    }
                }

            }
        }
    }

    @Override
    public @NotNull BiomeBank getBeachType() {
        return BiomeBank.BADLANDS_BEACH;
    }
}
