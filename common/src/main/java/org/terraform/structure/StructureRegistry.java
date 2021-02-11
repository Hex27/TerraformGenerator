package org.terraform.structure;

import org.terraform.data.MegaChunk;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TConfigOption;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.structure.caves.LargeCavePopulator;
import org.terraform.structure.dungeon.SmallDungeonPopulator;
import org.terraform.structure.mineshaft.BadlandsMinePopulator;
import org.terraform.structure.mineshaft.MineshaftPopulator;
import org.terraform.structure.monument.MonumentPopulator;
import org.terraform.structure.pyramid.PyramidPopulator;
import org.terraform.structure.shipwreck.ShipwreckPopulator;
import org.terraform.structure.stronghold.StrongholdPopulator;
import org.terraform.structure.village.VillagePopulator;
import org.terraform.structure.villagehouse.VillageHousePopulator;
import org.terraform.utils.GenUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

public class StructureRegistry {

    /**
     * The difference small and large is that Large structure populators only run when
     * getLargeStructureForMegaChunk contains the populator. Small structure populators
     * will always attempt to spawn.
     */
    public static final Map<StructureType, SingleMegaChunkStructurePopulator[]> largeStructureRegistry = new EnumMap<>(StructureType.class);
    public static final Collection<MultiMegaChunkStructurePopulator> smallStructureRegistry = new ArrayList<>();
    private static final HashMap<MegaChunkKey, SingleMegaChunkStructurePopulator[]> queryCache = new HashMap<>();


    public static void init() {
        registerStructure(StructureType.VILLAGE, new VillageHousePopulator());
        registerStructure(StructureType.VILLAGE, new VillagePopulator());

        registerStructure(StructureType.MEGA_DUNGEON, new PyramidPopulator());
        registerStructure(StructureType.MEGA_DUNGEON, new MonumentPopulator());
        registerStructure(StructureType.MEGA_DUNGEON, new StrongholdPopulator());

        registerStructure(StructureType.LARGE_CAVE, new LargeCavePopulator());

        registerStructure(StructureType.LARGE_MISC, new MineshaftPopulator());

        registerStructure(StructureType.SMALL, new SmallDungeonPopulator());
        registerStructure(StructureType.SMALL, new ShipwreckPopulator());

        registerStructure(StructureType.SMALL, new BadlandsMinePopulator());

        //registerStructure(StructureType.SMALL, new WitchHutPopulator());
        //registerStructure(StructureType.SMALL, new DesertWellPopulator());
    }

    /**
     * Assumes that the supplied type is a singlemegachunkstructurepopulator.
     * @param populatorType
     * @return
     */
    public static StructureType getStructureType(Class<? extends SingleMegaChunkStructurePopulator> populatorType) {
        for (Entry<StructureType, SingleMegaChunkStructurePopulator[]> entry : largeStructureRegistry.entrySet()) {
            for (SingleMegaChunkStructurePopulator pops : entry.getValue()) {
                if (populatorType.isInstance(pops))
                    return entry.getKey();
            }
        }
        return null; //Invalid populator Type.
    }

    /**
     * @param tw
     * @param mc
     * @return the structure types that will spawn in this mega chunk
     */
    public static SingleMegaChunkStructurePopulator[] getLargeStructureForMegaChunk(TerraformWorld tw, MegaChunk mc) {

        //Clear the cache if it gets big.
        if (queryCache.size() > 50) queryCache.clear();
        MegaChunkKey key = new MegaChunkKey(tw,mc);
        //Don't re-calculate
        if (queryCache.containsKey(key))
            return queryCache.get(key);

        Random structRand = tw.getRand(9);
        int maxStructures = GenUtils.randInt(structRand, 1, TConfigOption.STRUCTURES_MEGACHUNK_MAXSTRUCTURES.getInt());
        SingleMegaChunkStructurePopulator[] pops = new SingleMegaChunkStructurePopulator[maxStructures];
        int size = 0;

        //Check if there are any mega dungeons enabled
        if (largeStructureRegistry.containsKey(StructureType.MEGA_DUNGEON)
                && largeStructureRegistry.get(StructureType.MEGA_DUNGEON).length > 0) {
            //First check if the megadungeons can spawn. Shuffle the array first.
            SingleMegaChunkStructurePopulator[] available = (SingleMegaChunkStructurePopulator[]) shuffleArray(structRand, largeStructureRegistry.get(StructureType.MEGA_DUNGEON));
            for (SingleMegaChunkStructurePopulator pop : available) {
                int[] coords = pop.getCoordsFromMegaChunk(tw, mc);
                if (coords == null) continue;

                if (pop.canSpawn(tw, coords[0] >> 4, coords[1] >> 4, GenUtils.getBiomesInChunk(tw, coords[0] >> 4, coords[1] >> 4))) {
                    pops[size] = pop;
                    size++;
                    break; //ONLY ONE MEGA DUNGEON.
                }
            }
        }
        //If a Mega Dungeon spawned, don't spawn other large structures.
        if (size == 0) {
            //TerraformGeneratorPlugin.logger.info(ChatColor.YELLOW + "MC: " + mc.getX() + "," + mc.getZ() + " - No Mega Dungeon");
            StructureType[] types = {StructureType.LARGE_CAVE, StructureType.VILLAGE, StructureType.LARGE_MISC};
            types = (StructureType[]) shuffleArray(structRand, types);
            for (StructureType type : types) {
                if (largeStructureRegistry.containsKey(type))
                    for (SingleMegaChunkStructurePopulator pop : largeStructureRegistry.get(type)) {
                        int[] coords = pop.getCoordsFromMegaChunk(tw, mc);
                        if (pop.canSpawn(tw, coords[0] >> 4, coords[1] >> 4, GenUtils.getBiomesInChunk(tw, coords[0] >> 4, coords[1] >> 4))) {
                            pops[0] = pop;
                            size++;
                            break; //ONLY ONE OF EACH TYPE. Do not try to spawn multiple.
                        }
                    }

                //Stop trying if max structures is hit
                if (size >= maxStructures) break;
            }
        }
//        else {
//        	TerraformGeneratorPlugin.logger.info(ChatColor.YELLOW + "MC: " + mc.getX() + "," + mc.getZ() + " - Has Mega Dungeon");
//        }

        SingleMegaChunkStructurePopulator[] returnVal = new SingleMegaChunkStructurePopulator[size];
        System.arraycopy(pops, 0, returnVal, 0, size);

        //cache
        queryCache.put(key, returnVal);
        return returnVal;
    }


    // Implementing FisherYates shuffle
    private static Object[] shuffleArray(Random rand, Object[] ar) {
        ar = ar.clone();
        if (ar.length == 0) return ar;
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
     * @param type
     * @param pop
     */
    public static void registerStructure(StructureType type, StructurePopulator pop) {
        if (!pop.isEnabled()) return;//Don't register disabled features

        if (pop instanceof SingleMegaChunkStructurePopulator) {
            SingleMegaChunkStructurePopulator[] pops = {(SingleMegaChunkStructurePopulator) pop};
            if (largeStructureRegistry.containsKey(type)) {
                StructurePopulator[] existing = largeStructureRegistry.get(type);
                StructurePopulator[] old = pops;
                pops = new SingleMegaChunkStructurePopulator[existing.length + 1];
                System.arraycopy(existing, 0, pops, 0, existing.length);
                System.arraycopy(old, 0, pops, existing.length, 1);
            }
            TerraformGeneratorPlugin.logger.info("[Structure Registry] Registered Large Structure: " + pop.getClass().getSimpleName());
            largeStructureRegistry.put(type, pops);
        } else if (pop instanceof MultiMegaChunkStructurePopulator) {
            TerraformGeneratorPlugin.logger.info("[Structure Registry] Registered Small Structure: " + pop.getClass().getSimpleName());
            smallStructureRegistry.add((MultiMegaChunkStructurePopulator) pop);
        }

    }

    public static StructurePopulator[] getAllPopulators() {
        int size = smallStructureRegistry.size();
        for (StructurePopulator[] types : largeStructureRegistry.values()) {
            size += types.length;
        }
        StructurePopulator[] pops = new StructurePopulator[size];
        int index = 0;

        //Account for all small structures
        for (StructurePopulator pop : smallStructureRegistry) {
            pops[index] = pop;
            index++;
        }

        //Account for all large structures
        for (StructurePopulator[] types : largeStructureRegistry.values()) {
            for (StructurePopulator pop : types) {
                pops[index] = pop;
                index++;
            }
        }
        return pops;
    }
    
    private static class MegaChunkKey {
    	private TerraformWorld tw;
    	private MegaChunk mc;
		public MegaChunkKey(TerraformWorld tw, MegaChunk mc) {
			super();
			this.tw = tw;
			this.mc = mc;
		}

	    @Override
	    public int hashCode() {
	        return tw.hashCode() ^ (mc.getX() + mc.getZ() * 31);
	    }

	    @Override
	    public boolean equals(Object obj) {
	        if (!(obj instanceof MegaChunkKey)) return false;
	        MegaChunkKey other = (MegaChunkKey) obj;
	        return this.tw.equals(other.tw) && mc.getX() == other.mc.getX() && mc.getZ() == other.mc.getZ();
	    }
    }
}
