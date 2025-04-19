package org.terraform.populators;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.main.config.TConfig;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.FastNoise.NoiseType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class OrePopulator {

    private final BlockData type;
    private final int baseChance; // Chance to spawn per attempt to spawn
    private final int maxOreSize; // Maximum size of one vein
    private final int minOreSize;
    private final int maxNumberOfVeins; // Maximum number of veins per chunk
    private final int peakSpawnChanceHeight; // Optimal height for ore to spawn
    private final int maxSpawnHeight; // max y height where ore can be rarely found
    private final Set<BiomeBank> requiredBiomes;
    private final int maxDistance;
    private final boolean ignorePeakSpawnChance;
    private int minRange; // min spawn height

    public OrePopulator(Material type,
                        int baseChance,
                        int maxOreSize,
                        int maxNumberOfVeins,
                        int peakSpawnChanceHeight,
                        int maxSpawnHeight,
                        boolean ignorePeakSpawnChance,
                        BiomeBank... requiredBiomes)
    {
        this.type = Bukkit.createBlockData(type);
        this.baseChance = baseChance;
        this.maxOreSize = maxOreSize;
        this.minOreSize = maxOreSize / 2;
        this.maxNumberOfVeins = maxNumberOfVeins;
        this.peakSpawnChanceHeight = peakSpawnChanceHeight;
        this.maxSpawnHeight = maxSpawnHeight;
        this.requiredBiomes = Set.of(requiredBiomes);
        this.ignorePeakSpawnChance = ignorePeakSpawnChance;
        this.minRange = TerraformGeneratorPlugin.injector.getMinY() + 1;
        this.maxDistance = Math.max(
                Math.abs(minRange - peakSpawnChanceHeight),
                Math.abs(maxSpawnHeight - peakSpawnChanceHeight)
        );
    }

    public OrePopulator(Material type,
                        int baseChance,
                        int maxOreSize,
                        int maxNumberOfVeins,
                        int minRange,
                        int peakSpawnChanceHeight,
                        int maxSpawnHeight,
                        boolean ignorePeakSpawnChance,
                        BiomeBank... requiredBiomes)
    {
        this.type = Bukkit.createBlockData(type);
        this.baseChance = baseChance;
        this.maxOreSize = maxOreSize;
        this.minOreSize = maxOreSize / 2;
        this.maxNumberOfVeins = TConfig.c.FEATURE_ORES_ENABLED ? maxNumberOfVeins : 0;
        this.minRange = minRange;
        this.peakSpawnChanceHeight = peakSpawnChanceHeight;
        this.maxSpawnHeight = maxSpawnHeight;
        this.requiredBiomes = Set.of(requiredBiomes);
        this.ignorePeakSpawnChance = ignorePeakSpawnChance;
        // this.minRange = TerraformGeneratorPlugin.injector.getMinY()+1;
        this.maxDistance = Math.max(
                Math.abs(minRange - peakSpawnChanceHeight),
                Math.abs(maxSpawnHeight - peakSpawnChanceHeight)
        );
    }

    public void populate(@NotNull TerraformWorld world, @NotNull Random random, @NotNull PopulatorDataAbstract data) {
        if (requiredBiomes.size() > 0) {
            BiomeBank b = BiomeBank.getBiomeSectionFromChunk(world, data.getChunkX(), data.getChunkZ()).getBiomeBank();
            if(!requiredBiomes.contains(b))
                return;
        }

        // Attempt maxNumberOfVeins number of times
        for (int i = 0; i < this.maxNumberOfVeins; i++) {

            // Roll chance to spawn one ore
            if (GenUtils.chance(random, this.baseChance, 100)) {
                // RNG determined X Y and Z within chunk
                int x = GenUtils.randInt(random, 0, 15) + data.getChunkX() * 16;
                int z = GenUtils.randInt(random, 0, 15) + data.getChunkZ() * 16;
                // int groundHeight = GenUtils.getHighestGround(data, x, z);
                int range = maxSpawnHeight;
                // Low chance for ores to spawn above max range
                // if (GenUtils.chance(random, 1, 5)) range = maxSpawnHeight;

                // Ignore ground height and just spawn
                // Range cannot be above ground
                // if(range > groundHeight) range = groundHeight;

                // Spawn failed.
                if (minRange > range) {
                    continue;
                }
                if (minRange < world.minY) {
                    minRange = world.minY;
                }

                int y = GenUtils.randInt(random, minRange + 64, range + 64)
                        - 64; // The 64 is to make sure no negative numbers are fed in.

                if (!ignorePeakSpawnChance) {
                    // Calculate chance based on spawnHeight and peakSpawnChanceHeight height. Max chance at peakSpawnChanceHeight.
                    int distance = Math.abs(y - peakSpawnChanceHeight);

                    if (!GenUtils.chance(
                            (int) Math.round(100.0 * (1.0 - ((float) distance) / ((float) maxDistance))),
                            100
                    ))
                    {

                        continue;
                    }
                }

                // Generate ore with rough sphere size.
                //Seed cannot vary with x,y,z, it gets cached per world.
                placeOre(Objects.hash(world.getSeed(),7118794), data, x, y, z);

            }
        }
    }

    private static final ConcurrentHashMap<TerraformWorld, FastNoise> privateNoiseCache = new ConcurrentHashMap<>();
    /**
     * The profiler thinks that this method is EXTREMELY hot, so some cursed tactics
     * were employed to try and make this method faster
     */
    public void placeOre(int seed, @NotNull PopulatorDataAbstract data, int coreX, int coreY, int coreZ) {
        double size = GenUtils.randDouble(new Random(seed), minOreSize, maxOreSize);
        // Size is the volume of the sphere, so radius is:
        double radius = Math.pow(((3.0 / 4.0) * size * (1.0 / Math.PI)), 1.0 / 3.0);

        if (radius <= 0) {
            return;
        }
        if (radius <= 0.5) {
            // block.setReplaceType(ReplaceType.ALL);
            data.setBlockData(coreX, coreY, coreZ, GenUtils.randChoice(new Random(seed), type));
            return;
        }

        //We don't use the noisecachehandler for this, as it is too fast
        // it started to create memory pressure on the created record keys.
        //Profiler may be lying there.
        FastNoise noise = privateNoiseCache.get(data.getTerraformWorld());
        if(noise == null) {
            noise = new FastNoise(seed);
            noise.SetNoiseType(NoiseType.Simplex);
            noise.SetFrequency(0.09f);
            privateNoiseCache.put(data.getTerraformWorld(),noise);
        }

        //We know that this circle will NEVER cross short bounds.
        // As such, we can encode each relative coordinate as a short.

        //BFS to fill this ore
        ArrayDeque<Long> bfsQueue = new ArrayDeque<>();
        HashSet<Integer> visited = new HashSet<>();

        visited.add(Objects.hash(0,0,0));
        bfsQueue.add(0L); //Encoded as x || y || z

        while(bfsQueue.size() > 0){
            long v = bfsQueue.remove();
            short rZ = (short)(v & 0xffff);
            short rY = (short)((v>>16) & 0xffff);
            short rX = (short)((v>>32) & 0xffff); //decode by shifting and anding
            for(BlockFace face:BlockUtils.sixBlockFaces){
                long nX = rX + face.getModX();
                long nY = rY + face.getModY();
                long nZ = rZ + face.getModZ();
                int hash = Objects.hash(nX,nY,nZ);
                //If you fail to add to the set, it was already in it
                if(!visited.add(hash)) continue;

                // do not touch bedrock layer
                if (coreY+nY <= TerraformGeneratorPlugin.injector.getMinY()
                    || coreY+nY >= TerraformGeneratorPlugin.injector.getMaxY())
                    continue;
                double equationResult = Math.pow(nX, 2) / Math.pow(radius, 2)
                                        + Math.pow(nY, 2) / Math.pow(radius, 2)
                                        + Math.pow(nZ, 2) / Math.pow(radius, 2);
                if (equationResult <= 1 + 0.7 * noise.GetNoise(nX+coreX,nY+coreY,nZ+coreZ))
                    bfsQueue.add(nZ | (nY << 16) | nX << 32); //encode

            }

            //Process v
            int x = rX+coreX;
            int y = rY+coreY;
            int z = rZ+coreZ;
            Material replaced = data.getType(x,y,z);
            if (replaced == Material.STONE) {
                data.setBlockData(x,y,z, type);
            }
            // Deepslate replacing other ores
            else if (type.getMaterial() == Material.DEEPSLATE && BlockUtils.ores.contains(replaced)) {
                data.setBlockData(x,y,z, BlockUtils.deepSlateVersion(replaced));
            }
            // Normal ores replacing deepslate
            else if (replaced == Material.DEEPSLATE) {
                data.setBlockData(x,y,z, BlockUtils.deepSlateVersion(type.getMaterial()));
            }
        }
    }

    public Material getType() {
        return type.getMaterial();
    }

    public int getBaseChance() {
        return baseChance;
    }

    public int getMaxOreSize() {
        return maxOreSize;
    }

    public int getMinOreSize() {
        return minOreSize;
    }

    public int getMaxNumberOfVeins() {
        return maxNumberOfVeins;
    }

    public int getPeakSpawnChanceHeight() {
        return peakSpawnChanceHeight;
    }

    public int getMaxSpawnHeight() {
        return maxSpawnHeight;
    }

    public int getMinRange() {
        return minRange;
    }

    public Set<BiomeBank> getRequiredBiomes() {
        return requiredBiomes;
    }

    public int getMaxDistance() {
        return maxDistance;
    }

    public boolean isIgnorePeakSpawnChance() {
        return ignorePeakSpawnChance;
    }
}
