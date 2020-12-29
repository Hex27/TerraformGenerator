package org.terraform.coregen.bukkit;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.HeightMap;
import org.terraform.data.SimpleChunkLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TConfigOption;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.FastNoise;
import org.terraform.utils.GenUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class TerraformGenerator extends ChunkGenerator {
    public static final ArrayList<SimpleChunkLocation> preWorldInitGen = new ArrayList<>();
    public static int seaLevel = 62;
    public static int minMountainLevel = 85;

    public static void updateSeaLevelFromConfig() {
        seaLevel = TConfigOption.HEIGHT_MAP_SEA_LEVEL.getInt();
    }

    public static void updateMinMountainLevelFromConfig() {
        minMountainLevel = TConfigOption.BIOME_MOUNTAIN_HEIGHT.getInt();
    }

    @SuppressWarnings("deprecation")
    @Override
    public ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeGrid biome) {
        ChunkData chunk = createChunkData(world);
        TerraformWorld tw = TerraformWorld.get(world);
        //Bukkit.getLogger().info("Attempting gen: " + chunkX + "," + chunkZ);

        //Patch for WorldInitEvent issues.
        if (!TerraformGeneratorPlugin.INJECTED_WORLDS.contains(world.getName())) {
            preWorldInitGen.add(new SimpleChunkLocation(world.getName(), chunkX, chunkZ));
        }

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int rawX = chunkX * 16 + x;
                int rawZ = chunkZ * 16 + z;

                double preciseHeight = HeightMap.getPreciseHeight(tw, rawX, rawZ);
                int height = (int) preciseHeight;

                BiomeBank bank = tw.getBiomeBank(rawX, height, rawZ);//BiomeBank.calculateBiome(tw,tw.getTemperature(rawX, rawZ), height);
                Material[] crust = bank.getHandler().getSurfaceCrust(random);
                biome.setBiome(x, z, bank.getHandler().getBiome());
                int undergroundHeight = height;
                int index = 0;
                while (index < crust.length) {
                    //if(!attemptSimpleBlockUpdate(tw, chunk, chunkX, chunkZ, x,undergroundHeight,z))
                    chunk.setBlock(x, undergroundHeight, z, crust[index]);
                    index++;
                    undergroundHeight--;
                }

                for (int y = undergroundHeight; y > 0; y--) {
                    chunk.setBlock(x, y, z, Material.STONE);
                }

                //Any low elevation is sea
                for (int y = height + 1; y <= seaLevel; y++) {
                    //if(!attemptSimpleBlockUpdate(tw, chunk, chunkX, chunkZ, x,undergroundHeight,z))
                    chunk.setBlock(x, y, z, Material.WATER);
                }

                if (BiomeBank.calculateFlatBiome(tw, rawX, rawZ, height) == BiomeBank.BADLANDS && HeightMap.getRiverDepth(tw, rawX, rawZ) > 0) {
                    FastNoise wallNoise = new FastNoise((int) (world.getSeed() * 2));
                    wallNoise.SetNoiseType(FastNoise.NoiseType.SimplexFractal);
                    wallNoise.SetFrequency(0.07f);
                    wallNoise.SetFractalOctaves(2);

                    double riverlessHeight = HeightMap.getRiverlessHeight(tw, rawX, rawZ) - 2;

                    double maxDiff = riverlessHeight - seaLevel;
                    double f = (preciseHeight - 2 - seaLevel) / maxDiff; // 0 at river level

                    if (f > 0) {
                        int buildHeight = (int) Math.round(Math.min(1, 4 * Math.pow(f, 4)) * maxDiff
                                + wallNoise.GetNoise(rawX, rawZ) * 1.5
                        );

                        for (int i = buildHeight; i >= 0; i--) {
                            int lowerHeight = Math.min(seaLevel + i, (int) Math.round(riverlessHeight));

                            chunk.setBlock(x, lowerHeight, z, BlockUtils.getTerracotta(lowerHeight));
                        }

                        // Todo:
                        //  - no edges when riverless height is low

                        if (f - 0.4 > 0) {
                            int upperBuildHeight = (int) Math.round(Math.min(1, 50 * Math.pow(f - 0.4, 2.5)) * maxDiff + wallNoise.GetNoise(rawX, rawZ) * 1.5);

                            for (int i = 0; i <= upperBuildHeight; i++) {
                                int upperHeight = (int) riverlessHeight - i;

                                chunk.setBlock(x, upperHeight, z, BlockUtils.getTerracotta(upperHeight));
                            }
                        }

                        // Coat with sand
                        if (f > 0.52)
                            chunk.setBlock(x, (int) riverlessHeight + 1, z, Material.RED_SAND);
                    }

//                    if (riverlessHeight - riverDepth > seaLevel) {
//                        chunk.setBlock(x, riverlessHeight, z, Material.ORANGE_CONCRETE);
//                    }
                }

                //Bedrock Base
                chunk.setBlock(x, 2, z, GenUtils.randMaterial(random, Material.STONE, Material.BEDROCK));
                chunk.setBlock(x, 1, z, GenUtils.randMaterial(random, Material.STONE, Material.BEDROCK));
                chunk.setBlock(x, 0, z, Material.BEDROCK);

            }
        }

        //Bukkit.getLogger().info("Finished: " + chunkX + "," + chunkZ);

        return chunk;
    }

    @Override
    public Location getFixedSpawnLocation(World world, Random random) {
        return new Location(world, 0, HeightMap.getHeight(TerraformWorld.get(world), 0, 0), 0);
    }

    @Override
    public List<BlockPopulator> getDefaultPopulators(World world) {
        TerraformWorld tw = TerraformWorld.get(world);
        return Collections.singletonList(new TerraformBukkitBlockPopulator(tw));
    }
}
