package org.terraform.biome;

import org.drycell.command.InvalidArgumentException;
import org.terraform.coregen.HeightMap;
import org.terraform.data.TerraformWorld;

public class BiomeGrid {
    private static final BiomeBank[][] terrestrialGrid = {
            new BiomeBank[]{BiomeBank.SNOWY_WASTELAND, BiomeBank.SNOWY_WASTELAND, BiomeBank.TAIGA, BiomeBank.PLAINS, BiomeBank.PLAINS, BiomeBank.SAVANNA, BiomeBank.SAVANNA,
                    BiomeBank.DESERT, BiomeBank.DESERT, BiomeBank.BADLANDS, BiomeBank.BADLANDS},
            new BiomeBank[]{BiomeBank.SNOWY_WASTELAND, BiomeBank.SNOWY_WASTELAND, BiomeBank.TAIGA, BiomeBank.PLAINS, BiomeBank.PLAINS, BiomeBank.SAVANNA, BiomeBank.SAVANNA,
                    BiomeBank.DESERT, BiomeBank.DESERT, BiomeBank.BADLANDS, BiomeBank.BADLANDS},
            new BiomeBank[]{BiomeBank.SNOWY_WASTELAND, BiomeBank.SNOWY_WASTELAND, BiomeBank.TAIGA, BiomeBank.PLAINS, BiomeBank.PLAINS, BiomeBank.SAVANNA, BiomeBank.SAVANNA,
                    BiomeBank.DESERT, BiomeBank.DESERT, BiomeBank.BADLANDS, BiomeBank.BADLANDS},
            new BiomeBank[]{BiomeBank.SNOWY_WASTELAND, BiomeBank.SNOWY_WASTELAND, BiomeBank.TAIGA, BiomeBank.PLAINS, BiomeBank.PLAINS, BiomeBank.SAVANNA, BiomeBank.SAVANNA,
                    BiomeBank.DESERT, BiomeBank.DESERT, BiomeBank.DESERT, BiomeBank.DESERT},
            new BiomeBank[]{BiomeBank.SNOWY_WASTELAND, BiomeBank.SNOWY_WASTELAND, BiomeBank.TAIGA, BiomeBank.PLAINS, BiomeBank.PLAINS, BiomeBank.SAVANNA, BiomeBank.SAVANNA,
                    BiomeBank.DESERT, BiomeBank.DESERT, BiomeBank.DESERT, BiomeBank.DESERT},
            new BiomeBank[]{BiomeBank.SNOWY_TAIGA, BiomeBank.SNOWY_TAIGA, BiomeBank.TAIGA, BiomeBank.PLAINS, BiomeBank.PLAINS, BiomeBank.PLAINS, BiomeBank.PLAINS,
                    BiomeBank.SAVANNA, BiomeBank.SAVANNA, BiomeBank.SAVANNA, BiomeBank.SAVANNA},
            new BiomeBank[]{BiomeBank.SNOWY_TAIGA, BiomeBank.SNOWY_TAIGA, BiomeBank.TAIGA, BiomeBank.PLAINS, BiomeBank.PLAINS, BiomeBank.PLAINS, BiomeBank.PLAINS,
                    BiomeBank.PLAINS, BiomeBank.PLAINS, BiomeBank.PLAINS, BiomeBank.PLAINS},
            new BiomeBank[]{BiomeBank.SNOWY_TAIGA, BiomeBank.SNOWY_TAIGA, BiomeBank.TAIGA, BiomeBank.DARK_FOREST, BiomeBank.DARK_FOREST, BiomeBank.FOREST, BiomeBank.FOREST,
                    BiomeBank.JUNGLE, BiomeBank.JUNGLE, BiomeBank.JUNGLE, BiomeBank.JUNGLE},
            new BiomeBank[]{BiomeBank.SNOWY_TAIGA, BiomeBank.SNOWY_TAIGA, BiomeBank.TAIGA, BiomeBank.DARK_FOREST, BiomeBank.DARK_FOREST, BiomeBank.FOREST, BiomeBank.FOREST,
                    BiomeBank.FOREST, BiomeBank.JUNGLE, BiomeBank.JUNGLE, BiomeBank.JUNGLE},
            new BiomeBank[]{BiomeBank.ICE_SPIKES, BiomeBank.ICE_SPIKES, BiomeBank.TAIGA, BiomeBank.DARK_FOREST, BiomeBank.DARK_FOREST, BiomeBank.FOREST, BiomeBank.FOREST,
                    BiomeBank.BAMBOO_FOREST, BiomeBank.BAMBOO_FOREST, BiomeBank.JUNGLE, BiomeBank.JUNGLE},
            new BiomeBank[]{BiomeBank.ICE_SPIKES, BiomeBank.ICE_SPIKES, BiomeBank.TAIGA, BiomeBank.DARK_FOREST, BiomeBank.DARK_FOREST, BiomeBank.FOREST, BiomeBank.FOREST,
                    BiomeBank.BAMBOO_FOREST, BiomeBank.BAMBOO_FOREST, BiomeBank.JUNGLE, BiomeBank.JUNGLE}
    };

    private static final BiomeBank[][] mountainousGrid = {
            new BiomeBank[]{BiomeBank.SNOWY_MOUNTAINS, BiomeBank.SNOWY_MOUNTAINS, BiomeBank.BIRCH_MOUNTAINS, BiomeBank.ROCKY_MOUNTAINS, BiomeBank.ROCKY_MOUNTAINS,
                    BiomeBank.ROCKY_MOUNTAINS, BiomeBank.ROCKY_MOUNTAINS, BiomeBank.DESERT_MOUNTAINS, BiomeBank.DESERT_MOUNTAINS, BiomeBank.BADLANDS_MOUNTAINS,
                    BiomeBank.BADLANDS_MOUNTAINS},
            new BiomeBank[]{BiomeBank.SNOWY_MOUNTAINS, BiomeBank.SNOWY_MOUNTAINS, BiomeBank.BIRCH_MOUNTAINS, BiomeBank.ROCKY_MOUNTAINS, BiomeBank.ROCKY_MOUNTAINS,
                    BiomeBank.ROCKY_MOUNTAINS, BiomeBank.ROCKY_MOUNTAINS, BiomeBank.DESERT_MOUNTAINS, BiomeBank.DESERT_MOUNTAINS, BiomeBank.BADLANDS_MOUNTAINS,
                    BiomeBank.BADLANDS_MOUNTAINS},
            new BiomeBank[]{BiomeBank.SNOWY_MOUNTAINS, BiomeBank.SNOWY_MOUNTAINS, BiomeBank.BIRCH_MOUNTAINS, BiomeBank.ROCKY_MOUNTAINS, BiomeBank.ROCKY_MOUNTAINS,
                    BiomeBank.ROCKY_MOUNTAINS, BiomeBank.ROCKY_MOUNTAINS, BiomeBank.DESERT_MOUNTAINS, BiomeBank.DESERT_MOUNTAINS, BiomeBank.BADLANDS_MOUNTAINS,
                    BiomeBank.BADLANDS_MOUNTAINS},
            new BiomeBank[]{BiomeBank.SNOWY_MOUNTAINS, BiomeBank.SNOWY_MOUNTAINS, BiomeBank.BIRCH_MOUNTAINS, BiomeBank.ROCKY_MOUNTAINS, BiomeBank.ROCKY_MOUNTAINS,
                    BiomeBank.ROCKY_MOUNTAINS, BiomeBank.ROCKY_MOUNTAINS, BiomeBank.DESERT_MOUNTAINS, BiomeBank.DESERT_MOUNTAINS, BiomeBank.DESERT_MOUNTAINS,
                    BiomeBank.DESERT_MOUNTAINS},
            new BiomeBank[]{BiomeBank.SNOWY_MOUNTAINS, BiomeBank.SNOWY_MOUNTAINS, BiomeBank.BIRCH_MOUNTAINS, BiomeBank.ROCKY_MOUNTAINS, BiomeBank.ROCKY_MOUNTAINS,
                    BiomeBank.ROCKY_MOUNTAINS, BiomeBank.ROCKY_MOUNTAINS, BiomeBank.DESERT_MOUNTAINS, BiomeBank.DESERT_MOUNTAINS, BiomeBank.DESERT_MOUNTAINS,
                    BiomeBank.DESERT_MOUNTAINS},
            new BiomeBank[]{BiomeBank.SNOWY_MOUNTAINS, BiomeBank.SNOWY_MOUNTAINS, BiomeBank.BIRCH_MOUNTAINS, BiomeBank.ROCKY_MOUNTAINS, BiomeBank.ROCKY_MOUNTAINS,
                    BiomeBank.ROCKY_MOUNTAINS, BiomeBank.ROCKY_MOUNTAINS, BiomeBank.ROCKY_MOUNTAINS, BiomeBank.ROCKY_MOUNTAINS, BiomeBank.ROCKY_MOUNTAINS,
                    BiomeBank.ROCKY_MOUNTAINS},
            new BiomeBank[]{BiomeBank.SNOWY_MOUNTAINS, BiomeBank.SNOWY_MOUNTAINS, BiomeBank.BIRCH_MOUNTAINS, BiomeBank.ROCKY_MOUNTAINS, BiomeBank.ROCKY_MOUNTAINS,
                    BiomeBank.ROCKY_MOUNTAINS, BiomeBank.ROCKY_MOUNTAINS, BiomeBank.ROCKY_MOUNTAINS, BiomeBank.ROCKY_MOUNTAINS, BiomeBank.ROCKY_MOUNTAINS,
                    BiomeBank.ROCKY_MOUNTAINS},
            new BiomeBank[]{BiomeBank.SNOWY_MOUNTAINS, BiomeBank.SNOWY_MOUNTAINS, BiomeBank.BIRCH_MOUNTAINS, BiomeBank.ROCKY_MOUNTAINS, BiomeBank.ROCKY_MOUNTAINS,
                    BiomeBank.ROCKY_MOUNTAINS, BiomeBank.ROCKY_MOUNTAINS, BiomeBank.ROCKY_MOUNTAINS, BiomeBank.ROCKY_MOUNTAINS, BiomeBank.ROCKY_MOUNTAINS,
                    BiomeBank.ROCKY_MOUNTAINS},
            new BiomeBank[]{BiomeBank.SNOWY_MOUNTAINS, BiomeBank.SNOWY_MOUNTAINS, BiomeBank.BIRCH_MOUNTAINS, BiomeBank.ROCKY_MOUNTAINS, BiomeBank.ROCKY_MOUNTAINS,
                    BiomeBank.ROCKY_MOUNTAINS, BiomeBank.ROCKY_MOUNTAINS, BiomeBank.ROCKY_MOUNTAINS, BiomeBank.ROCKY_MOUNTAINS, BiomeBank.ROCKY_MOUNTAINS,
                    BiomeBank.ROCKY_MOUNTAINS},
            new BiomeBank[]{BiomeBank.SNOWY_MOUNTAINS, BiomeBank.SNOWY_MOUNTAINS, BiomeBank.BIRCH_MOUNTAINS, BiomeBank.ROCKY_MOUNTAINS, BiomeBank.ROCKY_MOUNTAINS,
                    BiomeBank.ROCKY_MOUNTAINS, BiomeBank.ROCKY_MOUNTAINS, BiomeBank.ROCKY_MOUNTAINS, BiomeBank.ROCKY_MOUNTAINS, BiomeBank.ROCKY_MOUNTAINS,
                    BiomeBank.ROCKY_MOUNTAINS},
            new BiomeBank[]{BiomeBank.SNOWY_MOUNTAINS, BiomeBank.SNOWY_MOUNTAINS, BiomeBank.BIRCH_MOUNTAINS, BiomeBank.ROCKY_MOUNTAINS, BiomeBank.ROCKY_MOUNTAINS,
                    BiomeBank.ROCKY_MOUNTAINS, BiomeBank.ROCKY_MOUNTAINS, BiomeBank.ROCKY_MOUNTAINS, BiomeBank.ROCKY_MOUNTAINS, BiomeBank.ROCKY_MOUNTAINS,
                    BiomeBank.ROCKY_MOUNTAINS}
    };

    private static final BiomeBank[][] oceanicGrid = {
            new BiomeBank[]{BiomeBank.FROZEN_OCEAN, BiomeBank.FROZEN_OCEAN, BiomeBank.COLD_OCEAN, BiomeBank.COLD_OCEAN, BiomeBank.OCEAN, BiomeBank.OCEAN, BiomeBank.OCEAN,
                    BiomeBank.LUKEWARM_OCEAN, BiomeBank.WARM_OCEAN, BiomeBank.WARM_OCEAN, BiomeBank.WARM_OCEAN},
            new BiomeBank[]{BiomeBank.FROZEN_OCEAN, BiomeBank.FROZEN_OCEAN, BiomeBank.COLD_OCEAN, BiomeBank.COLD_OCEAN, BiomeBank.OCEAN, BiomeBank.OCEAN, BiomeBank.OCEAN,
                    BiomeBank.LUKEWARM_OCEAN, BiomeBank.WARM_OCEAN, BiomeBank.WARM_OCEAN, BiomeBank.WARM_OCEAN},
            new BiomeBank[]{BiomeBank.FROZEN_OCEAN, BiomeBank.FROZEN_OCEAN, BiomeBank.COLD_OCEAN, BiomeBank.COLD_OCEAN, BiomeBank.OCEAN, BiomeBank.OCEAN, BiomeBank.OCEAN,
                    BiomeBank.LUKEWARM_OCEAN, BiomeBank.WARM_OCEAN, BiomeBank.WARM_OCEAN, BiomeBank.WARM_OCEAN},
            new BiomeBank[]{BiomeBank.FROZEN_OCEAN, BiomeBank.FROZEN_OCEAN, BiomeBank.COLD_OCEAN, BiomeBank.COLD_OCEAN, BiomeBank.OCEAN, BiomeBank.OCEAN, BiomeBank.OCEAN,
                    BiomeBank.LUKEWARM_OCEAN, BiomeBank.WARM_OCEAN, BiomeBank.WARM_OCEAN, BiomeBank.WARM_OCEAN},
            new BiomeBank[]{BiomeBank.FROZEN_OCEAN, BiomeBank.FROZEN_OCEAN, BiomeBank.COLD_OCEAN, BiomeBank.COLD_OCEAN, BiomeBank.OCEAN, BiomeBank.OCEAN, BiomeBank.OCEAN,
                    BiomeBank.LUKEWARM_OCEAN, BiomeBank.WARM_OCEAN, BiomeBank.WARM_OCEAN, BiomeBank.WARM_OCEAN},
            new BiomeBank[]{BiomeBank.FROZEN_OCEAN, BiomeBank.FROZEN_OCEAN, BiomeBank.COLD_OCEAN, BiomeBank.COLD_OCEAN, BiomeBank.OCEAN, BiomeBank.OCEAN, BiomeBank.OCEAN,
                    BiomeBank.LUKEWARM_OCEAN, BiomeBank.WARM_OCEAN, BiomeBank.WARM_OCEAN, BiomeBank.WARM_OCEAN},
            new BiomeBank[]{BiomeBank.FROZEN_OCEAN, BiomeBank.FROZEN_OCEAN, BiomeBank.COLD_OCEAN, BiomeBank.COLD_OCEAN, BiomeBank.OCEAN, BiomeBank.OCEAN, BiomeBank.OCEAN,
                    BiomeBank.LUKEWARM_OCEAN, BiomeBank.WARM_OCEAN, BiomeBank.WARM_OCEAN, BiomeBank.WARM_OCEAN},
            new BiomeBank[]{BiomeBank.FROZEN_OCEAN, BiomeBank.FROZEN_OCEAN, BiomeBank.COLD_OCEAN, BiomeBank.BLACK_OCEAN, BiomeBank.OCEAN, BiomeBank.SWAMP, BiomeBank.OCEAN,
                    BiomeBank.LUKEWARM_OCEAN, BiomeBank.WARM_OCEAN, BiomeBank.WARM_OCEAN, BiomeBank.WARM_OCEAN},
            new BiomeBank[]{BiomeBank.FROZEN_OCEAN, BiomeBank.FROZEN_OCEAN, BiomeBank.COLD_OCEAN, BiomeBank.COLD_OCEAN, BiomeBank.OCEAN, BiomeBank.SWAMP, BiomeBank.SWAMP,
                    BiomeBank.SWAMP, BiomeBank.LUKEWARM_OCEAN, BiomeBank.WARM_OCEAN, BiomeBank.WARM_OCEAN},
            new BiomeBank[]{BiomeBank.FROZEN_OCEAN, BiomeBank.FROZEN_OCEAN, BiomeBank.COLD_OCEAN, BiomeBank.COLD_OCEAN, BiomeBank.OCEAN, BiomeBank.SWAMP, BiomeBank.SWAMP,
                    BiomeBank.SWAMP, BiomeBank.SWAMP, BiomeBank.LUKEWARM_OCEAN, BiomeBank.WARM_OCEAN},
            new BiomeBank[]{BiomeBank.FROZEN_OCEAN, BiomeBank.FROZEN_OCEAN, BiomeBank.COLD_OCEAN, BiomeBank.COLD_OCEAN, BiomeBank.OCEAN, BiomeBank.SWAMP, BiomeBank.SWAMP,
                    BiomeBank.SWAMP, BiomeBank.SWAMP, BiomeBank.LUKEWARM_OCEAN, BiomeBank.WARM_OCEAN}
    };

    private static final BiomeBank[][] riverGrid = {
            new BiomeBank[]{BiomeBank.FROZEN_RIVER, BiomeBank.FROZEN_RIVER, BiomeBank.RIVER, BiomeBank.RIVER, BiomeBank.RIVER, BiomeBank.RIVER, BiomeBank.RIVER, BiomeBank.RIVER,
                    BiomeBank.RIVER, BiomeBank.RIVER, BiomeBank.RIVER},
            new BiomeBank[]{BiomeBank.FROZEN_RIVER, BiomeBank.FROZEN_RIVER, BiomeBank.RIVER, BiomeBank.RIVER, BiomeBank.RIVER, BiomeBank.RIVER, BiomeBank.RIVER, BiomeBank.RIVER,
                    BiomeBank.RIVER, BiomeBank.RIVER, BiomeBank.RIVER},
            new BiomeBank[]{BiomeBank.FROZEN_RIVER, BiomeBank.FROZEN_RIVER, BiomeBank.RIVER, BiomeBank.RIVER, BiomeBank.RIVER, BiomeBank.RIVER, BiomeBank.RIVER, BiomeBank.RIVER,
                    BiomeBank.RIVER, BiomeBank.RIVER, BiomeBank.RIVER},
            new BiomeBank[]{BiomeBank.FROZEN_RIVER, BiomeBank.FROZEN_RIVER, BiomeBank.RIVER, BiomeBank.RIVER, BiomeBank.RIVER, BiomeBank.RIVER, BiomeBank.RIVER, BiomeBank.RIVER,
                    BiomeBank.RIVER, BiomeBank.RIVER, BiomeBank.RIVER},
            new BiomeBank[]{BiomeBank.FROZEN_RIVER, BiomeBank.FROZEN_RIVER, BiomeBank.RIVER, BiomeBank.RIVER, BiomeBank.RIVER, BiomeBank.RIVER, BiomeBank.RIVER, BiomeBank.RIVER,
                    BiomeBank.RIVER, BiomeBank.RIVER, BiomeBank.RIVER},
            new BiomeBank[]{BiomeBank.FROZEN_RIVER, BiomeBank.FROZEN_RIVER, BiomeBank.RIVER, BiomeBank.RIVER, BiomeBank.RIVER, BiomeBank.RIVER, BiomeBank.RIVER, BiomeBank.RIVER,
                    BiomeBank.RIVER, BiomeBank.RIVER, BiomeBank.RIVER},
            new BiomeBank[]{BiomeBank.FROZEN_RIVER, BiomeBank.FROZEN_RIVER, BiomeBank.RIVER, BiomeBank.RIVER, BiomeBank.RIVER, BiomeBank.RIVER, BiomeBank.RIVER, BiomeBank.RIVER,
                    BiomeBank.RIVER, BiomeBank.RIVER, BiomeBank.RIVER},
            new BiomeBank[]{BiomeBank.FROZEN_RIVER, BiomeBank.FROZEN_RIVER, BiomeBank.RIVER, BiomeBank.RIVER, BiomeBank.RIVER, BiomeBank.RIVER, BiomeBank.RIVER,
                    BiomeBank.JUNGLE_RIVER, BiomeBank.JUNGLE_RIVER, BiomeBank.JUNGLE_RIVER, BiomeBank.JUNGLE_RIVER},
            new BiomeBank[]{BiomeBank.FROZEN_RIVER, BiomeBank.FROZEN_RIVER, BiomeBank.RIVER, BiomeBank.RIVER, BiomeBank.RIVER, BiomeBank.RIVER, BiomeBank.RIVER, BiomeBank.RIVER,
                    BiomeBank.JUNGLE_RIVER, BiomeBank.JUNGLE_RIVER, BiomeBank.JUNGLE_RIVER},
            new BiomeBank[]{BiomeBank.FROZEN_RIVER, BiomeBank.FROZEN_RIVER, BiomeBank.RIVER, BiomeBank.RIVER, BiomeBank.RIVER, BiomeBank.RIVER, BiomeBank.RIVER, BiomeBank.RIVER,
                    BiomeBank.RIVER, BiomeBank.JUNGLE_RIVER, BiomeBank.JUNGLE_RIVER},
            new BiomeBank[]{BiomeBank.FROZEN_RIVER, BiomeBank.FROZEN_RIVER, BiomeBank.RIVER, BiomeBank.RIVER, BiomeBank.RIVER, BiomeBank.RIVER, BiomeBank.RIVER, BiomeBank.RIVER,
                    BiomeBank.RIVER, BiomeBank.JUNGLE_RIVER, BiomeBank.JUNGLE_RIVER}
    };

    private static final BiomeBank[][] beachGrid = {
            new BiomeBank[]{BiomeBank.ICY_BEACH, BiomeBank.ICY_BEACH, BiomeBank.ROCKY_BEACH, BiomeBank.ROCKY_BEACH, BiomeBank.SANDY_BEACH, BiomeBank.SANDY_BEACH,
                    BiomeBank.SANDY_BEACH, BiomeBank.SANDY_BEACH, BiomeBank.SANDY_BEACH, BiomeBank.SANDY_BEACH, BiomeBank.SANDY_BEACH},
            new BiomeBank[]{BiomeBank.ICY_BEACH, BiomeBank.ICY_BEACH, BiomeBank.ROCKY_BEACH, BiomeBank.ROCKY_BEACH, BiomeBank.SANDY_BEACH, BiomeBank.SANDY_BEACH,
                    BiomeBank.SANDY_BEACH, BiomeBank.SANDY_BEACH, BiomeBank.SANDY_BEACH, BiomeBank.SANDY_BEACH, BiomeBank.SANDY_BEACH},
            new BiomeBank[]{BiomeBank.ICY_BEACH, BiomeBank.ICY_BEACH, BiomeBank.ROCKY_BEACH, BiomeBank.ROCKY_BEACH, BiomeBank.SANDY_BEACH, BiomeBank.SANDY_BEACH,
                    BiomeBank.SANDY_BEACH, BiomeBank.SANDY_BEACH, BiomeBank.SANDY_BEACH, BiomeBank.SANDY_BEACH, BiomeBank.SANDY_BEACH},
            new BiomeBank[]{BiomeBank.ICY_BEACH, BiomeBank.ICY_BEACH, BiomeBank.ROCKY_BEACH, BiomeBank.ROCKY_BEACH, BiomeBank.SANDY_BEACH, BiomeBank.SANDY_BEACH,
                    BiomeBank.SANDY_BEACH, BiomeBank.SANDY_BEACH, BiomeBank.SANDY_BEACH, BiomeBank.SANDY_BEACH, BiomeBank.SANDY_BEACH},
            new BiomeBank[]{BiomeBank.ICY_BEACH, BiomeBank.ICY_BEACH, BiomeBank.ROCKY_BEACH, BiomeBank.ROCKY_BEACH, BiomeBank.SANDY_BEACH, BiomeBank.SANDY_BEACH,
                    BiomeBank.SANDY_BEACH, BiomeBank.SANDY_BEACH, BiomeBank.SANDY_BEACH, BiomeBank.SANDY_BEACH, BiomeBank.SANDY_BEACH},
            new BiomeBank[]{BiomeBank.ICY_BEACH, BiomeBank.ICY_BEACH, BiomeBank.ROCKY_BEACH, BiomeBank.ROCKY_BEACH, BiomeBank.SANDY_BEACH, BiomeBank.SANDY_BEACH,
                    BiomeBank.SANDY_BEACH, BiomeBank.SANDY_BEACH, BiomeBank.SANDY_BEACH, BiomeBank.SANDY_BEACH, BiomeBank.SANDY_BEACH},
            new BiomeBank[]{BiomeBank.ICY_BEACH, BiomeBank.ICY_BEACH, BiomeBank.ROCKY_BEACH, BiomeBank.ROCKY_BEACH, BiomeBank.SANDY_BEACH, BiomeBank.SANDY_BEACH,
                    BiomeBank.SANDY_BEACH, BiomeBank.SANDY_BEACH, BiomeBank.SANDY_BEACH, BiomeBank.SANDY_BEACH, BiomeBank.SANDY_BEACH},
            new BiomeBank[]{BiomeBank.ICY_BEACH, BiomeBank.ICY_BEACH, BiomeBank.ROCKY_BEACH, BiomeBank.ROCKY_BEACH, BiomeBank.SANDY_BEACH, BiomeBank.MUDFLATS,
                    BiomeBank.SANDY_BEACH, BiomeBank.SANDY_BEACH, BiomeBank.SANDY_BEACH, BiomeBank.SANDY_BEACH, BiomeBank.SANDY_BEACH},
            new BiomeBank[]{BiomeBank.ICY_BEACH, BiomeBank.ICY_BEACH, BiomeBank.ROCKY_BEACH, BiomeBank.ROCKY_BEACH, BiomeBank.SANDY_BEACH, BiomeBank.MUDFLATS, BiomeBank.MUDFLATS
                    , BiomeBank.MUDFLATS, BiomeBank.SANDY_BEACH, BiomeBank.SANDY_BEACH, BiomeBank.SANDY_BEACH},
            new BiomeBank[]{BiomeBank.ICY_BEACH, BiomeBank.ICY_BEACH, BiomeBank.ROCKY_BEACH, BiomeBank.ROCKY_BEACH, BiomeBank.SANDY_BEACH, BiomeBank.MUDFLATS, BiomeBank.MUDFLATS
                    , BiomeBank.MUDFLATS, BiomeBank.MUDFLATS, BiomeBank.SANDY_BEACH, BiomeBank.SANDY_BEACH},
            new BiomeBank[]{BiomeBank.ICY_BEACH, BiomeBank.ICY_BEACH, BiomeBank.ROCKY_BEACH, BiomeBank.ROCKY_BEACH, BiomeBank.SANDY_BEACH, BiomeBank.MUDFLATS, BiomeBank.MUDFLATS
                    , BiomeBank.MUDFLATS, BiomeBank.MUDFLATS, BiomeBank.SANDY_BEACH, BiomeBank.SANDY_BEACH}
    };

    //Every index represents 0.5.
    public static BiomeBank calculateBiome(BiomeType type, double temperature, double moisture) {
        return getBiome(type, (int) Math.round(normalise(temperature)), (int) Math.round(normalise(moisture)));
    }

    private static BiomeBank getBiome(BiomeType type, int temperatureIndex, int moistureIndex)  {
        if (type == BiomeType.FLAT)
            return terrestrialGrid[moistureIndex][temperatureIndex];
        else if (type == BiomeType.OCEANIC)
            return oceanicGrid[moistureIndex][temperatureIndex];
        else if (type == BiomeType.MOUNTAINOUS)
            return mountainousGrid[moistureIndex][temperatureIndex];
        else if (type == BiomeType.BEACH)
            return beachGrid[moistureIndex][temperatureIndex];
        else if (type == BiomeType.RIVER)
            return riverGrid[moistureIndex][temperatureIndex];

        return null;
    }

    //11x11 grids
    public static BiomeBank[][] parseBiomeGrid(String gridString) throws InvalidArgumentException {
        BiomeBank[][] grid = new BiomeBank[11][11];
        String[] items = gridString.split(",");
        if (items.length != 11 * 11)
            throw new InvalidArgumentException("Invalid grid length! Must be " + 11 * 11 + " units, but instead was " + items.length);
        for (int i = 0; i < 11 * 11; i++) {
            grid[i / 11][i % 11] = BiomeBank.valueOf(items[i].toUpperCase());
        }
        return grid;
    }

    // Map input values from -2.5 to 2.5 to range from 0 to 10
    public static double normalise(double i) {
        if (i > 2.5) i = 2.5;
        else if (i < -2.5) i = -2.5;

        i += 2.5;//Range 0 to 5
        i *= 2; //Range 0 to 10

        return i;
    }

    /**
     * A value between 1 and 0 that gets closer to 0
     * when moving closer to the biome edge or water,
     * and closer to 1 when moving to center of a biome.
     * 
     * @param biomeThreshold Value between > 0 and 1, defines how quickly
     *                       output value approaches 0 when near biome edge.
     *                       Default of 0.25.
     * @param riverThreshold How quickly output value approaches 0 when near rivers.
     *                       Default of -5. Smaller values mean less quick changes.
     * @param temp Normalised temperature value, not rounded
     * @param moist Normalised moisture value, not rounded
     * @param riverDepth Current river depth, has to have also negative values
     */
    public static double getEdgeFactor(double biomeThreshold, int riverThreshold, BiomeBank currentBiome, double temp, double moist, double riverDepth) {
        double tempDecimals = Math.abs(temp - (int) temp);
        double moistDecimals = Math.abs(moist - (int) moist);

        // These tell if current point is near biome edge in biome grid
        boolean tempIncrease = tempDecimals < 0.5 && tempDecimals > 0.5 - biomeThreshold;
        boolean tempDecrease = tempDecimals > 0.5 && tempDecimals < 0.5 + biomeThreshold;
        boolean moistIncrease = moistDecimals < 0.5 && moistDecimals > 0.5 - biomeThreshold;
        boolean moistDecrease = moistDecimals > 0.5 && moistDecimals < 0.5 + biomeThreshold;

        // Calculate biome that will be changed to
        double nextTemp = temp;
        if (tempIncrease) nextTemp = Math.min(10, temp + 1);
        else if (tempDecrease) nextTemp = Math.max(0, temp - 1);

        double nextMoist = moist;
        if (moistIncrease) nextMoist = Math.min(10, moist + 1);
        else if (moistDecrease) nextMoist = Math.max(0, moist - 1);

        BiomeBank nextTempBiome = getBiome(currentBiome.getType(), (int) Math.round(nextTemp), (int) Math.round(moist));
        BiomeBank nextMoistBiome = getBiome(currentBiome.getType(), (int) Math.round(temp), (int) Math.round(nextMoist));
        BiomeBank nextCornerBiome = getBiome(currentBiome.getType(), (int) Math.round(nextTemp), (int) Math.round(nextMoist));

        // Calculate how near to the edge the point is
        double tempFactor = Math.abs((0.5 - tempDecimals) / biomeThreshold);
        double moistFactor = Math.abs((0.5 - moistDecimals) / biomeThreshold);

        double factor = 1;

        boolean cornerSituation = tempFactor < 1 && moistFactor < 1 && (
                nextCornerBiome != currentBiome && nextTempBiome == currentBiome && nextMoistBiome == currentBiome
        );
        boolean tempSituation = tempFactor < 1 && nextTempBiome != currentBiome;
        boolean moistSituation = moistFactor < 1 && nextMoistBiome != currentBiome;

        // If in L shaped corner in BiomeGrid
        if (cornerSituation) factor = Math.max(tempFactor, moistFactor);
        else if (tempSituation) factor = tempFactor;
        else if (moistSituation) factor = moistFactor;

        // Rivers
        double riverFactor = riverDepth / riverThreshold;

        if (riverFactor < factor) {
            factor = Math.max(0, riverFactor);
        }

        return factor;
    }

    public static double getEdgeFactor(TerraformWorld tw, double biomeThreshold, int riverThreshold, BiomeBank currentBiome, int x, int z) {
        return getEdgeFactor(biomeThreshold, riverThreshold, currentBiome, normalise(tw.getTemperature(x, z)), normalise(tw.getMoisture(x, z)), HeightMap.getRiverDepth(tw, x, z));
    }

    public static double getEdgeFactor(TerraformWorld tw, BiomeBank currentBiome, int x, int z) {
            return getEdgeFactor(0.25, -5, currentBiome, normalise(tw.getTemperature(x, z)), normalise(tw.getMoisture(x, z)), HeightMap.getRiverDepth(tw, x, z));
    }
}
