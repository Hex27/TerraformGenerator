package org.terraform.structure;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.terraform.data.MegaChunk;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.main.config.TConfig;
import org.terraform.structure.ancientcity.AncientCityPopulator;
import org.terraform.structure.catacombs.CatacombsPopulator;
import org.terraform.structure.caves.LargeCavePopulator;
import org.terraform.structure.mineshaft.BadlandsMinePopulator;
import org.terraform.structure.mineshaft.MineshaftPopulator;
import org.terraform.structure.monument.MonumentPopulator;
import org.terraform.structure.pillager.mansion.MansionPopulator;
import org.terraform.structure.pillager.outpost.OutpostPopulator;
import org.terraform.structure.pyramid.PyramidPopulator;
import org.terraform.structure.small.DesertWellPopulator;
import org.terraform.structure.small.WitchHutPopulator;
import org.terraform.structure.small.buriedtreasure.BuriedTreasurePopulator;
import org.terraform.structure.small.dungeon.SmallDungeonPopulator;
import org.terraform.structure.small.igloo.IglooPopulator;
import org.terraform.structure.small.ruinedportal.RuinedPortalPopulator;
import org.terraform.structure.small.shipwreck.ShipwreckPopulator;
import org.terraform.structure.stronghold.StrongholdPopulator;
import org.terraform.structure.trailruins.TrailRuinsPopulator;
import org.terraform.structure.trialchamber.TrialChamberPopulator;
import org.terraform.structure.village.VillagePopulator;
import org.terraform.structure.villagehouse.VillageHousePopulator;
import org.terraform.structure.warmoceanruins.WarmOceanRuinsPopulator;
import org.terraform.utils.version.Version;

import java.util.*;
import java.util.Map.Entry;

public class StructureRegistry {

    /**
     * The difference small and large is that Large structure populators only run when
     * getLargeStructureForMegaChunk contains the populator. Small structure populators
     * will always attempt to spawn.
     */
    public static final Map<StructureType, SingleMegaChunkStructurePopulator[]> largeStructureRegistry = new EnumMap<>(
            StructureType.class);
    public static final Collection<MultiMegaChunkStructurePopulator> smallStructureRegistry = new ArrayList<>();
    private static final HashMap<MegaChunkKey, SingleMegaChunkStructurePopulator[]> queryCache = new HashMap<>();


    public static void init() {
        registerStructure(StructureType.VILLAGE, new VillageHousePopulator());
        registerStructure(StructureType.VILLAGE, new VillagePopulator());
        registerStructure(StructureType.VILLAGE, new OutpostPopulator());

        registerStructure(StructureType.MEGA_DUNGEON, new PyramidPopulator());
        registerStructure(StructureType.MEGA_DUNGEON, new MonumentPopulator());
        registerStructure(StructureType.MEGA_DUNGEON, new StrongholdPopulator());
        registerStructure(StructureType.MEGA_DUNGEON, new MansionPopulator());
        registerStructure(StructureType.MEGA_DUNGEON, new AncientCityPopulator());
        if (Version.isAtLeast(21)) {
            registerStructure(StructureType.MEGA_DUNGEON, new TrialChamberPopulator());
        }

        registerStructure(StructureType.LARGE_CAVE, new LargeCavePopulator());

        registerStructure(StructureType.LARGE_MISC, new MineshaftPopulator());
        registerStructure(StructureType.LARGE_MISC, new CatacombsPopulator());
        registerStructure(StructureType.LARGE_MISC, new BadlandsMinePopulator());
        registerStructure(StructureType.LARGE_MISC, new WarmOceanRuinsPopulator());
        registerStructure(StructureType.LARGE_MISC, new TrailRuinsPopulator());

        registerStructure(StructureType.SMALL, new SmallDungeonPopulator());
        registerStructure(StructureType.SMALL, new ShipwreckPopulator());
        registerStructure(StructureType.SMALL, new BuriedTreasurePopulator());
        registerStructure(StructureType.SMALL, new RuinedPortalPopulator());
        registerStructure(StructureType.SMALL, new IglooPopulator());
        registerStructure(StructureType.SMALL, new DesertWellPopulator());
        registerStructure(StructureType.SMALL, new WitchHutPopulator());
    }

    /**
     * Assumes that the supplied type is a singlemegachunkstructurepopulator.
     */
    public static @Nullable StructureType getStructureType(@NotNull Class<? extends SingleMegaChunkStructurePopulator> populatorType) {
        for (Entry<StructureType, SingleMegaChunkStructurePopulator[]> entry : largeStructureRegistry.entrySet()) {
            for (SingleMegaChunkStructurePopulator pops : entry.getValue()) {
                if (populatorType.isInstance(pops)) {
                    return entry.getKey();
                }
            }
        }
        return null; // Invalid populator Type.
    }

    /**
     * @return the structure types that can spawn in this mega chunk
     * Only one is meant to be picked.
     */
    public static SingleMegaChunkStructurePopulator[] getLargeStructureForMegaChunk(@NotNull TerraformWorld tw,
                                                                                    @NotNull MegaChunk mc)
    {
        // TerraformGeneratorPlugin.logger.info("getLargeStructureForMegaChunkQuery: " + mc.getX() + "," + mc.getZ());
        // Clear the cache if it gets big.
        if (queryCache.size() > 50) {
            queryCache.clear();
        }
        MegaChunkKey key = new MegaChunkKey(tw, mc);
        // Don't re-calculate
        if (queryCache.containsKey(key)) {
            return queryCache.get(key);
        }

        Random structRand = tw.getHashedRand(9, mc.getX(), mc.getZ());
        int maxStructures = 3; // GenUtils.randInt(structRand, 1, TConfigOption.STRUCTURES_MEGACHUNK_MAXSTRUCTURES);
        SingleMegaChunkStructurePopulator[] pops = new SingleMegaChunkStructurePopulator[maxStructures];
        int size = 0;

        // Check if there are any mega dungeons enabled
        if (largeStructureRegistry.containsKey(StructureType.MEGA_DUNGEON)
            && largeStructureRegistry.get(StructureType.MEGA_DUNGEON).length > 0)
        {
            // First check if the megadungeons can spawn. Shuffle the array first.
            SingleMegaChunkStructurePopulator[] available = (SingleMegaChunkStructurePopulator[]) shuffleArray(
                    structRand,
                    largeStructureRegistry.get(StructureType.MEGA_DUNGEON)
            );
            for (SingleMegaChunkStructurePopulator pop : available) {
                int[] coords = mc.getCenterBiomeSectionBlockCoords(); // pop.getCoordsFromMegaChunk(tw, mc);
                if (coords == null) {
                    continue;
                }

                if (TConfig.areStructuresEnabled() && pop.canSpawn(
                        tw,
                        coords[0] >> 4,
                        coords[1] >> 4,
                        mc.getCenterBiomeSection(tw).getBiomeBank()
                ))
                {
                    pops[size] = pop;
                    size++;
                    break; // ONLY ONE MEGA DUNGEON.
                }
            }
        }
        // Mega Dungeon will be in slot 0 (highest priority). The others are backups.
        // if (size == 0) {
        // TerraformGeneratorPlugin.logger.info(ChatColor.YELLOW + "MC: " + mc.getX() + "," + mc.getZ() + " - No Mega Dungeon");
        StructureType[] types = {StructureType.LARGE_CAVE, StructureType.VILLAGE, StructureType.LARGE_MISC};
        types = (StructureType[]) shuffleArray(structRand, types);
        for (StructureType type : types) {
            if (largeStructureRegistry.containsKey(type)) {
                for (SingleMegaChunkStructurePopulator pop : largeStructureRegistry.get(type)) {
                    int[] coords = mc.getCenterBiomeSectionBlockCoords();
                    if (TConfig.areStructuresEnabled() && pop.canSpawn(tw,
                            coords[0] >> 4,
                            coords[1] >> 4,
                            mc.getCenterBiomeSection(tw).getBiomeBank()
                    ))
                    {
                        pops[size] = pop;
                        size++;
                        break; // ONLY ONE OF EACH TYPE. Do not try to spawn multiple.
                    }
                }
            }

            // Stop trying if max structures is hit
            if (size >= maxStructures) {
                break;
            }
        }
        // }

        SingleMegaChunkStructurePopulator[] returnVal = new SingleMegaChunkStructurePopulator[size];
        System.arraycopy(pops, 0, returnVal, 0, size);

        // cache
        queryCache.put(key, returnVal);
        return returnVal;
    }


    // Implementing FisherYates shuffle
    private static Object @NotNull [] shuffleArray(@NotNull Random rand, Object[] ar) {
        ar = ar.clone();
        if (ar.length == 0) {
            return ar;
        }
        for (int i = ar.length - 1; i > 0; i--) {
            int index = rand.nextInt(i + 1);
            // Simple swap
            Object a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
        return ar;
    }

    /**
     * Registers small or large structures. Must implement either SingleMegaChunkStructurePopulator or MultiMegaChunkStructurePopulator.
     */
    public static void registerStructure(StructureType type, @NotNull StructurePopulator pop) {
        if (!pop.isEnabled()) {
            return;// Don't register disabled features
        }

        if (pop instanceof SingleMegaChunkStructurePopulator) {
            SingleMegaChunkStructurePopulator[] pops = {(SingleMegaChunkStructurePopulator) pop};
            if (largeStructureRegistry.containsKey(type)) {
                SingleMegaChunkStructurePopulator[] existing = largeStructureRegistry.get(type);
                SingleMegaChunkStructurePopulator[] old = pops;
                pops = new SingleMegaChunkStructurePopulator[existing.length + 1];
                System.arraycopy(existing, 0, pops, 0, existing.length);
                System.arraycopy(old, 0, pops, existing.length, 1);
            }
            TerraformGeneratorPlugin.logger.info("[Structure Registry] Registered Large Structure: " + pop.getClass()
                                                                                                          .getSimpleName());
            largeStructureRegistry.put(type, pops);
        }
        else if (pop instanceof MultiMegaChunkStructurePopulator) {
            TerraformGeneratorPlugin.logger.info("[Structure Registry] Registered Small Structure: " + pop.getClass()
                                                                                                          .getSimpleName());
            smallStructureRegistry.add((MultiMegaChunkStructurePopulator) pop);
        }

    }

    public static StructurePopulator @NotNull [] getAllPopulators() {
        int size = smallStructureRegistry.size();
        for (StructurePopulator[] types : largeStructureRegistry.values()) {
            size += types.length;
        }
        StructurePopulator[] pops = new StructurePopulator[size];
        int index = 0;

        // Account for all small structures
        for (StructurePopulator pop : smallStructureRegistry) {
            pops[index] = pop;
            index++;
        }

        // Account for all large structures
        for (StructurePopulator[] types : largeStructureRegistry.values()) {
            for (StructurePopulator pop : types) {
                pops[index] = pop;
                index++;
            }
        }
        return pops;
    }

    private static class MegaChunkKey {
        private final TerraformWorld tw;
        private final MegaChunk mc;

        public MegaChunkKey(TerraformWorld tw, MegaChunk mc) {
            super();
            this.tw = tw;
            this.mc = mc;
        }

        @Override
        public int hashCode() {
            return Objects.hash(tw.hashCode(), mc.getX(), mc.getZ());
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof MegaChunkKey other)) {
                return false;
            }
            return this.tw.equals(other.tw) && mc.getX() == other.mc.getX() && mc.getZ() == other.mc.getZ();
        }
    }
}
