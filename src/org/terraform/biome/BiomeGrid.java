package org.terraform.biome;

import org.drycell.command.InvalidArgumentException;

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
        if (type == BiomeType.FLAT)
            return terrestrialGrid[normalise(moisture)][normalise(temperature)];
        else if (type == BiomeType.OCEANIC)
            return oceanicGrid[normalise(moisture)][normalise(temperature)];
        else if (type == BiomeType.MOUNTAINOUS)
            return mountainousGrid[normalise(moisture)][normalise(temperature)];
        else if (type == BiomeType.BEACH)
            return beachGrid[normalise(moisture)][normalise(temperature)];

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

    public static int normalise(double i) {
        if (i > 2.5) i = 2.5;
        else if (i < -2.5) i = -2.5;

        i += 2.5;//Range 0 to 5
        i *= 2; //Range 0 to 10

        return (int) Math.round(i);
    }
}
