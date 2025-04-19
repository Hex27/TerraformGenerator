package org.terraform.utils;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeSection;
import org.terraform.coregen.ChunkCache;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.coregen.populatordata.PopulatorDataSpigotAPI;
import org.terraform.data.CoordPair;
import org.terraform.data.SimpleBlock;
import org.terraform.data.SimpleLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.small_items.PlantBuilder;
import org.terraform.utils.datastructs.ConcurrentLRUCache;
import org.terraform.utils.noise.FastNoise;

import java.util.*;

public class GenUtils {
    public static final Random RANDOMIZER = new Random();
    private static final EnumSet<Material> BLACKLIST_HIGHEST_GROUND = EnumSet.noneOf(Material.class);
    public static ConcurrentLRUCache<ChunkCache, EnumSet<BiomeBank>> biomeQueryCache;

    public static void initGenUtils() {

        // Initialize highest ground blacklist
        for (Material mat : Material.values()) {
            if (mat.toString().contains("LEAVES")
                || mat.toString().contains("LOG")
                || mat.toString().contains("WOOD")
                || mat.toString().contains("MUSHROOM")
                || mat.toString().contains("FENCE")
                || mat.toString().contains("WALL")
                || mat.toString().contains("POTTED")
                || mat.toString().contains("BRICK")
                || mat.toString().contains("CHAIN")
                || mat.toString().contains("CORAL")
                || mat.toString().contains("POINTED_DRIPSTONE")
                || mat.toString().contains("NETHERRACK")
                || mat.toString().contains("MANGROVE")
                || mat == Material.HAY_BLOCK
                || mat == Material.ICE
                || mat == Material.CACTUS
                || mat == Material.BAMBOO
                || mat == Material.BAMBOO_SAPLING
                || mat == Material.IRON_BARS
                || mat == Material.LANTERN
                // || mat == Material.SNOW_BLOCK
                // || mat == Material.PACKED_ICE
                // || mat == Material.BLUE_ICE
            )
            {
                //    	    	{
                //              "LEAVES", "LOG",
                //              "WOOD", "MUSHROOM",
                //              "FENCE", "WALL",
                //              "POTTED", "BRICK",
                //              "CHAIN", "CORAL",
                //              "POINTED_DRIPSTONE",
                //              "NETHERRACK"
                //      };
                BLACKLIST_HIGHEST_GROUND.add(mat);
            }
        }
    }

    public static int getSign(@NotNull Random rand) {
        return rand.nextBoolean() ? 1 : -1;
    }

    /**
     * This will now use StoneLike, and not just any solid block.
     */
    public static @NotNull Collection<CoordPair> getCaveCeilFloors(PopulatorDataAbstract data,
                                                               int x,
                                                               int z,
                                                               int minimumHeight)
    {
        int y = getHighestGround(data, x, z); //The check for transformedGround is ALREADY done here.
        final int INVAL = TerraformGeneratorPlugin.injector.getMinY() - 1;
        int[] pair = {INVAL, INVAL};
        List<CoordPair> list = new ArrayList<>();
        // Subtract one as the first cave floor cannot be the surface
        for (int ny = y - 1; ny > TerraformGeneratorPlugin.injector.getMinY(); ny--) {
            Material type = data.getType(x, ny, z);
            if (BlockUtils.isStoneLike(type)) {
                pair[1] = ny;
                if (pair[0] - pair[1] >= minimumHeight) {
                    list.add(new CoordPair(pair[0],pair[1]));
                }
                pair[0] = INVAL;
                pair[1] = INVAL;
            }
            else if (pair[0] == INVAL) {
                pair[0] = ny;
            }
        }

        return list;
    }

    public static int[] randomCoords(@NotNull Random rand, int @NotNull [] lowBound, int @NotNull [] highBound) {
        return new int[] {
                randInt(rand, lowBound[0], highBound[0]),
                randInt(rand, lowBound[1], highBound[1]),
                randInt(rand, lowBound[2], highBound[2])
        };
    }

    public static boolean chance(@NotNull Random rand, int chance, int outOf) {
        return randInt(rand, 1, outOf) <= chance;
    }

    public static boolean chance(int chance, int outOf) {
        return randInt(new Random(), 1, outOf) <= chance;
    }

    public static @NotNull EnumSet<BiomeBank> getBiomesInChunk(TerraformWorld tw, int chunkX, int chunkZ) {
        return biomeQueryCache.get(new ChunkCache(tw, chunkX, chunkZ));
    }

    /**
     * Locates the target biome in given area using brute force.
     * Note that the function is blocking.
     * <br><br>
     * This should not be used for first phase biomes (i.e. height-independent biomes like oceans, mountains etc)
     * This should be used for stuff like beaches and rivers, which cannot be broadly searched for with biome sections.
     * <br><br>
     * If for whatever reason you still use this for height-independent biomes, it will still work. It's just slower.
     *
     * @return Position for the biome or null if no biomes found.
     */
    public static @Nullable Vector2f locateHeightDependentBiome(@NotNull TerraformWorld tw,
                                                                @NotNull BiomeBank biome,
                                                                @NotNull Vector2f center,
                                                                int radius,
                                                                int blockSkip)
    {
        if (!BiomeBank.isBiomeEnabled(biome)) {
            return null;
        }
        if (tw.getBiomeBank(Math.round(center.x), Math.round(center.y)) == biome) {
            return new Vector2f(center.x, center.y);
        }
        int iter = 2;

        int x = (int) center.x;
        int z = (int) center.y;

        while (Math.abs(center.x - x) < radius || Math.abs(center.y - z) < radius) {
            for (int i = 0; i < iter / 2; i++) {
                switch (iter % 4) {
                    case 0 -> x += blockSkip;
                    case 1 -> z -= blockSkip;
                    case 2 -> x -= blockSkip;
                    case 3 -> z += blockSkip;
                }
            }

            if (tw.getBiomeBank(x, z) == biome) {
                return new Vector2f(x, z);
            }
            iter++;
        }

        return null;
    }

    /**
     * Locates a biome with BiomeSection.
     * WILL NOT FIND RIVERS AND BEACHES.
     * Does not stop until biome is found, much like structure locate, because it should work.
     */
    public static @Nullable Vector2f locateHeightIndependentBiome(TerraformWorld tw,
                                                                  @NotNull BiomeBank biome,
                                                                  @NotNull Vector2f centerBlockLocation)
    {
        if (!BiomeBank.isBiomeEnabled(biome)) {
            return null;
        }

        BiomeSection center = BiomeBank.getBiomeSectionFromBlockCoords(
                tw,
                (int) centerBlockLocation.x,
                (int) centerBlockLocation.y
        );
        int radius = 0;

        while (true) {
            for (BiomeSection sect : center.getRelativeSurroundingSections(radius)) {
                if (sect.getBiomeBank() == biome) {
                    SimpleLocation sectionCenter = sect.getCenter();
                    return new Vector2f(sectionCenter.getX(), sectionCenter.getZ());
                }
                radius++;
            }
        }
    }

    public static Object weightedChoice(@NotNull Random rand, Object @NotNull ... candidates) {
        if (candidates.length % 2 != 0) {
            throw new IllegalArgumentException();
        }
        ArrayList<Object> types = new ArrayList<>(50);
        for (int i = 0; i < candidates.length; i += 2) {
            Object type = candidates[i];
            int freq = (int) candidates[i + 1];
            for (int z = 0; z < freq; z++) {
                types.add(type);
            }
        }

        return types.get(randInt(rand, 0, types.size() - 1));
    }

    public static PlantBuilder weightedRandomSmallItem(@NotNull Random rand, Object @NotNull ... candidates) {
        return (PlantBuilder) weightedChoice(rand, candidates);
    }

    public static Material weightedRandomMaterial(@NotNull Random rand, Object @NotNull ... candidates) {
        return (Material) weightedChoice(rand, candidates);
    }

    @SafeVarargs
    public static <T> T randChoice(@NotNull Random rand, T @NotNull ... candidates) {
        if (candidates.length == 1) {
            return candidates[0]; // avoid invocation to randInt
        }
        return candidates[randInt(rand, 0, candidates.length - 1)];
    }

    @SafeVarargs
    public static <T> T randChoice(T... candidates) {
        return randChoice(RANDOMIZER, candidates);
    }

    public static <T extends Enum<T>> T randChoice(@NotNull EnumSet<T> candidates) {
        int index = randInt(RANDOMIZER, 0, candidates.size() - 1);
        int i = 0;
        for (T candidate : candidates) {
            if (i == index) {
                return candidate;
            }
            i++;
        }

        // This should never happen due to EnumSet constraints
        throw new IllegalArgumentException("EnumSet is empty");
    }

    public static int[] randomSurfaceCoordinates(@NotNull Random rand, @NotNull PopulatorDataAbstract data) {
        int chunkX = data.getChunkX();
        int chunkZ = data.getChunkZ();
        int x = randInt(rand, chunkX * 16, chunkX * 16 + 15);
        int z = randInt(rand, chunkZ * 16, chunkZ * 16 + 15);
        int y = getTrueHighestBlock(data, x, z);

        return new int[] {x, y, z};
    }

    public static int randInt(int min, int max) {
        if (min == max) {
            return min;
        }
        return randInt(RANDOMIZER, min, max);
    }

    public static int randInt(@NotNull Random rand, int d, int max) {
        if (d == max) {
            return d;
        }
        boolean negative = false;
        if (d < 0 && max < 0) {
            negative = true;
            d = -d;
            max = -max;
        }

        if (max < d) {
            int temp = d;
            d = max;
            max = temp;
        }

        int randomNum = rand.nextInt((max - d) + 1) + d;
        return negative ? -randomNum : randomNum;
    }

    /**
     * Try to have a max-min of more than 2.
     */
    public static int randOddInt(@NotNull Random rand, int min, int max) {
        int randomNum = rand.nextInt((max - min) + 1) + min;
        if (randomNum % 2 == 0) {
            if (randomNum++ > max) {
                randomNum -= 2;
            }
        }
        return randomNum;
    }

    public static double randDouble(@NotNull Random rand, double min, double max) {
        return rand.nextDouble() * (max - min) + min;
    }

    public static int getHighestX(@NotNull PopulatorDataAbstract data, int x, int z, Material X) {
        int y = TerraformGeneratorPlugin.injector.getMaxY() - 1;
        while (y > TerraformGeneratorPlugin.injector.getMinY() && data.getType(x, y, z) != X) {
            y--;
        }
        return y;
    }


    /**
     * @return the highest solid block
     */
    public static int getTrueHighestBlock(@NotNull PopulatorDataAbstract data, int x, int z) {
        int y = TerraformGeneratorPlugin.injector.getMaxY() - 1;
        while (y > TerraformGeneratorPlugin.injector.getMinY() && !data.getType(x, y, z).isSolid()) {
            y--;
        }
        return y;
    }


    /**
     * @return the highest dry ground, or the sea level
     */
    public static int getHighestGroundOrSeaLevel(PopulatorDataAbstract data, int x, int z) {
        int y = getHighestGround(data, x, z);
        return Math.max(y, TerraformGenerator.seaLevel);
    }

    /**
     * @return the highest solid block below y
     */
    public static int getTrueHighestBlockBelow(@NotNull PopulatorDataAbstract data, int x, int y, int z) {
        while (y > TerraformGeneratorPlugin.injector.getMinY() && !data.getType(x, y, z).isSolid()) {
            y--;
        }
        return y;
    }

    public static @NotNull SimpleBlock getTrueHighestBlockBelow(@NotNull SimpleBlock block) {
        int y = block.getY();
        while (y > TerraformGeneratorPlugin.injector.getMinY() && !block.getPopData()
                                                                        .getType(block.getX(), y, block.getZ())
                                                                        .isSolid()) {
            y--;
        }
        return new SimpleBlock(block.getPopData(), block.getX(), y, block.getZ());
    }

    public static boolean isGroundLike(@NotNull Material mat) {
        // Ice is considered stone-like, but not in a million years is it ground.
        if (BlockUtils.isStoneLike(mat) && mat != Material.PACKED_ICE && mat != Material.BLUE_ICE) {
            return true;
        }
        if (mat == Material.SAND || mat == Material.RED_SAND || mat == Material.GRAVEL) {
            return true;
        }

        if (mat.isSolid()) {
            if (mat.isInteractable()) {
                return false;
            }
            if (Tag.SLABS.isTagged(mat)) {
                return false;
            }
            return !BLACKLIST_HIGHEST_GROUND.contains(mat);
        }
        else {
            return false;
        }
    }

    /**
     * Meant as a new way to calculate noise-cached heights.
     * <br>
     * Stop fucking iterating from the sky, you look like an idiot.
     * Please for the love of god, use this where you can
     */
    public static int getTransformedHeight(@NotNull TerraformWorld tw, int rawX, int rawZ)
    {
        ChunkCache cache = TerraformGenerator.getCache(tw, rawX>>4, rawZ>>4);
        int cachedY = cache.getTransformedHeight(rawX & 0xF, rawZ & 0xF);
        if (cachedY == TerraformGeneratorPlugin.injector.getMinY() - 1) {
            TerraformGenerator.buildFilledCache(tw, rawX >> 4, rawZ >> 4, cache);
            cachedY = cache.getTransformedHeight(rawX & 0xF, rawZ & 0xF);
        }
        return cachedY;
    }

    /**
     * @return the highest solid ground. Is dirt-like or stone-like, and is
     * not leaves or logs
     * <br>
     * The damn issue with this stupid, stupid method is that caching it is
     * inherently unsafe. If I call this, then place stone on the floor, it
     * technically changes.
     * <br>
     * I hate this method so much.
     * <br>
     * But you know what? I'm going to cache it anyway.
     */
    public static int getHighestGround(PopulatorDataAbstract data, int x, int z) {
        // If you're too lazy to bother then just do this
        if (data instanceof PopulatorDataSpigotAPI) {
            return getTransformedHeight(data.getTerraformWorld(), x, z);
        }

        int y = TerraformGeneratorPlugin.injector.getMaxY() - 1;
        ChunkCache cache = TerraformGenerator.getCache(data.getTerraformWorld(), x>>4, z>>4);
        int cachedY = cache.getHighestGround(x, z);
        if (cachedY != TerraformGeneratorPlugin.injector.getMinY() - 1) {
            // Basic check to ensure block above is not ground
            // and current block is ground.
            // Will fail if the new ground is an overhang of some kind.
            if (isGroundLike(data.getType(x, cachedY, z)) && !isGroundLike(data.getType(x, cachedY + 1, z))) {
                return cache.getHighestGround(x, z);
            }
        }

        while (y > TerraformGeneratorPlugin.injector.getMinY()) {
            Material block = data.getType(x, y, z);
            if (!isGroundLike(block)) {
                y--;
                continue;
            }
            break;
        }
        if (y <= TerraformGeneratorPlugin.injector.getMinY()) {
            TerraformGeneratorPlugin.logger.error("GetHighestGround returned less than "
                                                  + TerraformGeneratorPlugin.injector.getMinY()
                                                  + "! ("
                                                  + y
                                                  + ")");
            try {
                throw new Exception("GetHighestGround returned less than "
                                    + TerraformGeneratorPlugin.injector.getMinY()
                                    + "! ("
                                    + y
                                    + ")");
            }
            catch (Exception e) {
                TerraformGeneratorPlugin.logger.stackTrace(e);
            }
        }

        // Y can be stored as a short, as there's no way world height will be 32k.
        cache.cacheHighestGround(x, z, Integer.valueOf(y).shortValue());
        return y;
    }

    public static Material @NotNull [] mergeArr(@NotNull Material[]... arrs) {
        int totalLength = 0, index = 0;
        for (Material[] arr : arrs) {
            totalLength += arr.length;
        }
        Material[] res = new Material[totalLength];

        for (Material[] arr : arrs) {
            for (Material mat : arr) {
                res[index++] = mat;
            }
        }

        return res;
    }

    /**
     * Function that returns random positions inside chunk.
     * An algorithm makes sure that objects are at least user-defined
     * distance away from each others. Object density can also be
     * configured precisely.
     *
     * @param distanceBetween Initial distance between objects in a grid.
     *                        (aka. density)
     * @param maxPerturbation Max amount a point can move in each axis
     * @return Array of points
     */
    public static CoordPair @NotNull [] vectorRandomObjectPositions(int seed,
                                                                    int chunkX,
                                                                    int chunkZ,
                                                                    int distanceBetween,
                                                                    float maxPerturbation)
    {

        FastNoise noise = new FastNoise(seed);
        noise.SetFrequency(1);
        noise.SetGradientPerturbAmp(maxPerturbation);

        ArrayList<CoordPair> positions = new ArrayList<>();

        // Calculate first grid element position in chunk
        // Fixme come up with better algorithm for calculating next grid item inside chunk
        int i;
        int startX = (chunkX << 4) - 5;
        i = (distanceBetween - (startX % distanceBetween));
        startX += i != distanceBetween ? i : 0;
        int startZ = (chunkZ << 4) - 5;
        i = (distanceBetween - (startZ % distanceBetween));
        startZ += i != distanceBetween ? i : 0;

        // Also checks if points from chunks close by are perturbed to this chunk
        for (int x = startX - distanceBetween; x < startX + 16 + distanceBetween; x += distanceBetween) {
            for (int z = startZ - distanceBetween; z < startZ + 16 + distanceBetween; z += distanceBetween) {
                Vector2f v = new Vector2f(x, z);
                noise.GradientPerturb(v);
                v.x = Math.round(v.x);
                v.y = Math.round(v.y);

                // If perturbed vector is inside chunk
                if (v.x >= (chunkX << 4)
                    && v.x < (chunkX << 4) + 16
                    && v.y >= (chunkZ << 4)
                    && v.y < (chunkZ << 4) + 16)
                {
                    positions.add(new CoordPair((int) v.x, (int) v.y));
                }
            }
        }

        return positions.toArray(new CoordPair[0]);
    }

    public static SimpleLocation @NotNull [] randomObjectPositions(@NotNull TerraformWorld world,
                                                                   int chunkX,
                                                                   int chunkZ,
                                                                   int distanceBetween)
    {
        CoordPair[] vecs = vectorRandomObjectPositions(
                (int) world.getSeed(),
                chunkX,
                chunkZ,
                distanceBetween,
                0.35f * distanceBetween
        );
        SimpleLocation[] locs = new SimpleLocation[vecs.length];

        for (int i = 0; i < vecs.length; i++) {
            locs[i] = new SimpleLocation((int) vecs[i].x(), 0, (int) vecs[i].z());
        }

        return locs;
    }

    /**
     * @param pertubMultiplier is normally 0.35.
     */
    public static SimpleLocation @NotNull [] randomObjectPositions(@NotNull TerraformWorld world,
                                                                   int chunkX,
                                                                   int chunkZ,
                                                                   int distanceBetween,
                                                                   float pertubMultiplier)
    {
        CoordPair[] vecs = vectorRandomObjectPositions(
                (int) world.getSeed(),
                chunkX,
                chunkZ,
                distanceBetween,
                pertubMultiplier * distanceBetween
        );
        SimpleLocation[] locs = new SimpleLocation[vecs.length];

        for (int i = 0; i < vecs.length; i++) {
            locs[i] = new SimpleLocation((int) vecs[i].x(), 0, (int) vecs[i].z());
        }

        return locs;
    }

    /**
     * Random-angle
     *
     * @return An angle between lowerBound*base to upperBound*base degrees in radians
     */
    public static double randAngle(double base, double lowerBound, double upperBound) {
        return GenUtils.randDouble(new Random(), lowerBound * base, upperBound * base);
    }

    public static <T> @NotNull T choice(@NotNull Random rand, T @NotNull [] array)
    {
        if (array.length == 0) {
            throw new IllegalArgumentException("Provided array was length 0");
        }
        if (array.length == 1) {
            return array[0];
        }
        return array[rand.nextInt(array.length)];
    }

    /**
     * Gets the center chunk of a hypothetically split limited region of 3x3 chunks
     *
     * @return a chunk coordinate
     */
    public static int getTripleChunk(int chunkCoord) {
        if (chunkCoord >= 0) {
            return 1 + 3 * (chunkCoord / 3);
        }

        return 1 + 3 * (-1 + (chunkCoord + 1) / 3);
    }
}
