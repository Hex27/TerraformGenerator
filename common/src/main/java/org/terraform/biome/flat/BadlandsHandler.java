package org.terraform.biome.flat;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockFace;
import org.bukkit.generator.ChunkGenerator;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeBlender;
import org.terraform.biome.BiomeHandler;
import org.terraform.biome.mountainous.BadlandsMountainHandler;
import org.terraform.coregen.HeightMap;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TConfigOption;
import org.terraform.structure.small.DesertWellPopulator;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.FastNoise;
import org.terraform.utils.GenUtils;

import java.util.ArrayList;
import java.util.Random;

public class BadlandsHandler extends BiomeHandler {
    static private BiomeBlender riversBlender;
    static private BiomeBlender plateauBlender;
    static private FastNoise plateauNoise;

    static int sandRadius = TConfigOption.BIOME_BADLANDS_PLATEAU_SAND_RADIUS.getInt();
    static int plateauHeight = TConfigOption.BIOME_BADLANDS_PLATEAU_HEIGHT.getInt();
    static float plateauFrequency = TConfigOption.BIOME_BADLANDS_PLATEAU_FREQUENCY.getFloat();
    static double plateauThreshold = TConfigOption.BIOME_BADLANDS_PLATEAU_THRESHOLD.getDouble();
    static double plateauCommonness = TConfigOption.BIOME_BADLANDS_PLATEAU_COMMONNESS.getDouble();

    private static BiomeBlender getRiversBlender(TerraformWorld tw) {
        // Only one blender needed!
        if (riversBlender == null) riversBlender = new BiomeBlender(tw, true, false, false)
                .setBiomeThreshold(0.45);
        return riversBlender;
    }

    private static BiomeBlender getPlateauBlender(TerraformWorld tw) {
        if (plateauBlender == null) plateauBlender = new BiomeBlender(tw, true, true, true)
                .setBiomeThreshold(0.35).setMountainThreshold(8).setRiverThreshold(10);
        return plateauBlender;
    }

    public static FastNoise getPlateauNoise(TerraformWorld tw) {
        if (plateauNoise == null) {
            plateauNoise = new FastNoise((int) (tw.getSeed() * 7509));
            plateauNoise.SetNoiseType(FastNoise.NoiseType.CubicFractal);
            plateauNoise.SetFractalOctaves(2);
            plateauNoise.SetFrequency(plateauFrequency);
        }
        return plateauNoise;
    }

    @Override
    public boolean isOcean() {
        return false;
    }

    @Override
    public Biome getBiome() {
        return Biome.BADLANDS;
    }

    @Override
    public Material[] getSurfaceCrust(Random rand) {
        return new Material[]{
                Material.RED_SAND,
                Material.RED_SAND,
                GenUtils.randMaterial(rand, Material.RED_SAND, Material.RED_SANDSTONE),
                GenUtils.randMaterial(rand, Material.RED_SANDSTONE, Material.STONE),
                GenUtils.randMaterial(rand, Material.RED_SANDSTONE, Material.STONE)};
    }

    @Override
    public void populateSmallItems(TerraformWorld world, Random random, PopulatorDataAbstract data) {
        
    	//While not a small item, generatePlateaus is left in, as it
    	//transforms the terrain itself. Structures placed must account for
    	//these terrain changes.
    	generatePlateaus(world, data);

        for (int x = data.getChunkX() * 16; x < data.getChunkX() * 16 + 16; x++) {
            for (int z = data.getChunkZ() * 16; z < data.getChunkZ() * 16 + 16; z++) {
                int highest = GenUtils.getTrueHighestBlock(data, x, z);

                BiomeBank currentBiome = BiomeBank.calculateBiome(world, x, z);
                if (currentBiome != BiomeBank.BADLANDS &&
                        currentBiome != BiomeBank.BADLANDS_BEACH &&
                        currentBiome != BiomeBank.BADLANDS_MOUNTAINS) continue;

                if (HeightMap.getNoiseGradient(world, x, z, 3) >= 1.5 && GenUtils.chance(random, 49, 50)) {
                    BadlandsMountainHandler.oneUnit(world, random, data, x, z, true);
                    continue;
                }

                Material base = data.getType(x, highest, z);
                if (base == Material.SAND ||
                        base == Material.RED_SAND) {
                    if (GenUtils.chance(random, 1, 200)) {

                        boolean canSpawn = true;
                        for (BlockFace face : BlockUtils.directBlockFaces) {
                            if (data.getType(x + face.getModX(), highest + 1, z + face.getModZ()) != Material.AIR)
                                canSpawn = false;
                        }
                        // Prevent cactus from spawning on plateaus:
                        if (HeightMap.getBlockHeight(world, x, z) + 5 < highest) canSpawn = false;
                        if (canSpawn && GenUtils.chance(1, 50))
                            spawnDeadTree(data, x, highest, z);
                        else if (canSpawn)
                            BlockUtils.spawnPillar(random, data, x, highest + 1, z, Material.CACTUS, 2, 5);
                    } else if (GenUtils.chance(random, 1, 80) && highest > TerraformGenerator.seaLevel) {
                        data.setType(x, highest + 1, z, Material.DEAD_BUSH);
                    }
                }

            }
        }
    }

    @Override
    public BiomeHandler getTransformHandler() {
        return this;
    }

    @Override
    public void transformTerrain(TerraformWorld tw, Random random, ChunkGenerator.ChunkData chunk, int chunkX, int chunkZ) {
        BiomeBlender blender = getRiversBlender(tw);

        FastNoise wallNoise = new FastNoise((int) (tw.getWorld().getSeed() * 2));
        wallNoise.SetNoiseType(FastNoise.NoiseType.SimplexFractal);
        wallNoise.SetFrequency(0.07f);
        wallNoise.SetFractalOctaves(2);

        // Rivers
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int rawX = chunkX * 16 + x;
                int rawZ = chunkZ * 16 + z;

                double preciseHeight = HeightMap.getPreciseHeight(tw, rawX, rawZ);
                //int height = (int) preciseHeight;
                BiomeBank currentBiome = BiomeBank.calculateBiome(tw, rawX, rawZ);

                if (currentBiome == BiomeBank.BADLANDS
                        || currentBiome == BiomeBank.BADLANDS_MOUNTAINS
                        || currentBiome == BiomeBank.BADLANDS_BEACH
//                        && HeightMap.getRiverDepth(tw, rawX, rawZ) > 0
                ) {
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
                        int buildHeight = (int) Math.round(bottomEdgeFactor *
                                (Math.min(1, 4 * Math.pow(riverFactor, 4)) * maxDiff + wallNoise.GetNoise(rawX, rawZ) * 1.5));

                        for (int i = buildHeight; i >= 0; i--) {
                            int lowerHeight = Math.min(TerraformGenerator.seaLevel + i, (int) Math.round(riverlessHeight));

                            chunk.setBlock(x, lowerHeight, z, BlockUtils.getTerracotta(lowerHeight));
                        }

                        double threshold = 0.4 + (1 - topEdgeFactor) * 0.6;

                        // Curved top edges
                        if (riverFactor > threshold) {
                            int upperBuildHeight = (int) Math.round(
                                    1 *//topEdgeFactor *
                                            (Math.min(1, 50 * Math.pow(riverFactor - threshold, 2.5)) * maxDiff + wallNoise.GetNoise(rawX, rawZ) * 1.5));

                            if (topEdgeFactor == 0) continue;

                            for (int i = 0; i <= upperBuildHeight; i++) {
                                int upperHeight = (int) riverlessHeight - i;

                                chunk.setBlock(x, upperHeight, z, BlockUtils.getTerracotta(upperHeight));
                            }
                        }

                        // Coat with red sand
                        if (riverFactor > threshold + 0.12)
                            chunk.setBlock(x, (int) riverlessHeight + 1, z, Material.RED_SAND);
                    }
                }
            }
        }
    }

    void generatePlateaus(TerraformWorld tw, PopulatorDataAbstract data) {
        FastNoise detailsNoise = new FastNoise((int) (tw.getSeed() * 7509));
        detailsNoise.SetNoiseType(FastNoise.NoiseType.SimplexFractal);
        detailsNoise.SetFrequency(0.08f);

        for (int x = data.getChunkX() * 16; x < data.getChunkX() * 16 + 16; x++) {
            for (int z = data.getChunkZ() * 16; z < data.getChunkZ() * 16 + 16; z++) {
                int height = HeightMap.getBlockHeight(tw, x, z);

                // Calculate plateau height
                double rawValue = Math.max(0, getPlateauNoise(tw).GetNoise(x, z) + plateauCommonness);
                double noiseValue = rawValue * getPlateauBlender(tw).getEdgeFactor(BiomeBank.BADLANDS, x, z) * (1 - ((int) (rawValue / plateauThreshold) * 0.05));

                double graduated = noiseValue / plateauThreshold;
                double platformHeight = (int) graduated * plateauHeight
                        + (10 * Math.pow(graduated - (int) graduated - 0.5 - 0.1, 7) * plateauHeight);

                boolean placeSand = false;
                for (int y = 1; y <= (int) Math.round(platformHeight); y++) {
                    placeSand = true;
                    Material material; // Coat plateaus with sand
                    if ((int) graduated * plateauHeight == y)
                        material = Material.RED_SAND;
                    else if ((int) graduated * plateauHeight == y + 1)
                        material = GenUtils.randMaterial(Material.RED_SAND, Material.RED_SAND, BlockUtils.getTerracotta(height + y));
                    else if ((int) graduated * plateauHeight == y + 2)
                        material = GenUtils.randMaterial(Material.RED_SAND, BlockUtils.getTerracotta(height + y),
                                BlockUtils.getTerracotta(height + y));
                    else
                        material = BlockUtils.getTerracotta(height + y);

                    data.setType(x, height + y, z, material);

                }

                // Prevent inner parts of plateau from generating sand in vain
                if (!placeSand || graduated - (int) graduated > 0.2) continue;

                // Surround plateaus with sand
                int level = (((int) graduated) - 1) * plateauHeight; // handle second and third levels of plateau
                for (int sx = x - sandRadius; sx <= x + sandRadius; sx++) {
                    for (int sz = z - sandRadius; sz <= z + sandRadius; sz++) {
                        double distance = Math.sqrt(Math.pow(sx - x, 2) + Math.pow(sz - z, 2));

                        if (distance < sandRadius) {
                            // Skip if sand would levitate
                            if ((int) graduated != 1 && getPlateauHeight(tw, sx, sz) != plateauHeight) continue;

                            int sandHeight = (int) Math.round(plateauHeight * 0.55 * Math.pow(1 - distance / sandRadius, 1.7) + detailsNoise.GetNoise(sx, sz));
                            for (int y = 1 + level; y <= sandHeight + level; y++)
                                if (data.getType(sx, HeightMap.getBlockHeight(tw, sx, sz) + y, sz) == Material.AIR)
                                    data.setType(sx, HeightMap.getBlockHeight(tw, sx, sz) + y, sz, Material.RED_SAND);
                        }
                    }
                }
            }
        }
    }

    public static boolean containsPlateau(TerraformWorld tw, int x, int z) {
        return getPlateauHeight(tw, x, z) > 0;
    }

    public static boolean mineCanSpawn(TerraformWorld tw, int x, int z) {
        int h = getPlateauHeight(tw, x, z);
        return (h < plateauHeight - 1 && h > plateauHeight / 3);
    }

    // This is for optimizing sand, ew
    static int getPlateauHeight(TerraformWorld tw, int x, int z) {
        double rawValue = Math.max(0, getPlateauNoise(tw).GetNoise(x, z) + plateauCommonness);
        double noiseValue = rawValue * getPlateauBlender(tw).getEdgeFactor(BiomeBank.BADLANDS, x, z) * (1 - ((int) (rawValue / plateauThreshold) * 0.1));

        double graduated = noiseValue / plateauThreshold;
        double platformHeight = (int) graduated * plateauHeight
                + (10 * Math.pow(graduated - (int) graduated - 0.5 - 0.1, 7) * plateauHeight);

        return (int) Math.round(platformHeight);
    }

    void spawnDeadTree(PopulatorDataAbstract data, int x, int y, int z) {
        int height = GenUtils.randInt(5, 7);
        int branches = GenUtils.randInt(1, height == 5 ? 2 : 3);

        for (int i = 1; i <= height; i++) data.setType(x, y + i, z, Material.DARK_OAK_WOOD);

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
                case 1: bz++; break;
                case 2: bx++; break;
                case 3: bz--; break;
                default: bx--; break;
            }

            data.setType(bx, y + bHeight, bz, Material.DARK_OAK_WOOD);

            usedBranchHorizontals.add(bDirection);
            usedBranchVerticals.add(bHeight);
        }
    }

    //TODO: Seems like a mass of excessive calculation just to spawn dead trees
    //Look into optimisation here in future.
	@Override
	public void populateLargeItems(TerraformWorld tw, Random random, PopulatorDataAbstract data) {

        for (int x = data.getChunkX() * 16; x < data.getChunkX() * 16 + 16; x++) {
            for (int z = data.getChunkZ() * 16; z < data.getChunkZ() * 16 + 16; z++) {
                int highest = GenUtils.getTrueHighestBlock(data, x, z);

                BiomeBank currentBiome = tw.getBiomeBank(x, z);
                if (currentBiome != BiomeBank.BADLANDS &&
                        currentBiome != BiomeBank.BADLANDS_BEACH &&
                        currentBiome != BiomeBank.BADLANDS_MOUNTAINS) continue;

                if (HeightMap.getNoiseGradient(tw, x, z, 3) >= 1.5 && GenUtils.chance(random, 49, 50)) {
                    BadlandsMountainHandler.oneUnit(tw, random, data, x, z, true);
                    continue;
                }

                Material base = data.getType(x, highest, z);
                if (base == Material.SAND ||
                        base == Material.RED_SAND) {
                    if (GenUtils.chance(random, 1, 200)) {

                        boolean canSpawn = true;
                        for (BlockFace face : BlockUtils.directBlockFaces) {
                            if (data.getType(x + face.getModX(), highest + 1, z + face.getModZ()) != Material.AIR)
                                canSpawn = false;
                        }
                        
                        if (HeightMap.getBlockHeight(tw, x, z) + 5 < highest) canSpawn = false;
                        if (canSpawn && GenUtils.chance(1, 50))
                            spawnDeadTree(data, x, highest, z);
                        
                    }
                }

            }
        }
		
        if (GenUtils.chance(random, TConfigOption.STRUCTURES_DESERTWELL_CHANCE_OUT_OF_TEN_THOUSAND.getInt(), 10000)) {
            new DesertWellPopulator().populate(tw, random, data, true);
        }
	}
}
