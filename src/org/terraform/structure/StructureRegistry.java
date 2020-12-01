package org.terraform.structure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;

import org.terraform.biome.BiomeBank;
import org.terraform.data.MegaChunk;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TConfigOption;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.structure.caves.LargeCavePopulator;
import org.terraform.structure.dungeon.SmallDungeonPopulator;
import org.terraform.structure.mineshaft.MineshaftPopulator;
import org.terraform.structure.monument.MonumentPopulator;
import org.terraform.structure.pyramid.PyramidPopulator;
import org.terraform.structure.shipwreck.ShipwreckPopulator;
import org.terraform.structure.stronghold.StrongholdPopulator;
import org.terraform.utils.GenUtils;

public class StructureRegistry {

	/**
	 * The difference small and large is that Large structure populators only run when
	 * getLargeStructureForMegaChunk contains the populator. Small structure populators
	 * will always attempt to spawn.
	 */
	public static final HashMap<StructureType, SingleMegaChunkStructurePopulator[]> largeStructureRegistry = new HashMap<>();
	public static final ArrayList<MultiMegaChunkStructurePopulator> smallStructureRegistry = new ArrayList<>();
	private static final HashMap<Integer,SingleMegaChunkStructurePopulator[]> queryCache = new HashMap<>();
	
	
	public static void init() {
		registerStructure(StructureType.VILLAGE, new VillageHousePopulator());
		
		registerStructure(StructureType.MEGA_DUNGEON, new PyramidPopulator());
		registerStructure(StructureType.MEGA_DUNGEON, new MonumentPopulator());
		registerStructure(StructureType.MEGA_DUNGEON, new StrongholdPopulator());
		
		registerStructure(StructureType.LARGE_CAVE, new LargeCavePopulator());
		
		registerStructure(StructureType.LARGE_MISC, new MineshaftPopulator());

		registerStructure(StructureType.SMALL, new SmallDungeonPopulator());
		registerStructure(StructureType.SMALL, new ShipwreckPopulator());
		//registerStructure(StructureType.SMALL, new WitchHutPopulator());
		//registerStructure(StructureType.SMALL, new DesertWellPopulator());
	}
	
	
	/**
	 * 
	 * @param tw
	 * @param mc
	 * @return the structure types that will spawn in this mega chunk
	 */
	public static SingleMegaChunkStructurePopulator[] getLargeStructureForMegaChunk(TerraformWorld tw, MegaChunk mc, ArrayList<BiomeBank> biomes) {
		
		//Clear the cache if it gets big.
		if(queryCache.size() > 50) queryCache.clear();
		
		//Don't re-calculate
		if(queryCache.containsKey(Objects.hash(tw,mc,biomes)))
			return queryCache.get(Objects.hash(tw,mc,biomes));
		
		Random structRand = tw.getRand(9);
		int maxStructures = GenUtils.randInt(structRand, 1, TConfigOption.STRUCTURES_MEGACHUNK_MAXSTRUCTURES.getInt());
		SingleMegaChunkStructurePopulator[] pops = new SingleMegaChunkStructurePopulator[maxStructures];
		int size = 0;
		
		//Check if there are any mega dungeons enabled
		if(largeStructureRegistry.containsKey(StructureType.MEGA_DUNGEON)
				&& largeStructureRegistry.get(StructureType.MEGA_DUNGEON).length > 0) {
			//First check if the megadungeons can spawn. Shuffle the array first.
			shuffleArray(structRand,largeStructureRegistry.get(StructureType.MEGA_DUNGEON));
			for(SingleMegaChunkStructurePopulator pop:largeStructureRegistry.get(StructureType.MEGA_DUNGEON)) {
				int[] coords = pop.getCoordsFromMegaChunk(tw, mc);
				if(coords == null) continue;
				
				if(pop.canSpawn(tw, coords[0]>>4, coords[1]>>4, biomes)) {
					pops[size] = pop;
					size++;
					break; //ONLY ONE MEGA DUNGEON.
				}
			}
		}
		
		//If a Mega Dungeon spawned, don't spawn other large structures.
		if(size == 0) {
			StructureType[] types = new StructureType[] {StructureType.LARGE_CAVE,StructureType.VILLAGE,StructureType.LARGE_MISC};
			shuffleArray(structRand,types);
			for(StructureType type:types) {
				if(largeStructureRegistry.containsKey(type))
					for(SingleMegaChunkStructurePopulator pop:largeStructureRegistry.get(type)) {
						int[] coords = pop.getCoordsFromMegaChunk(tw, mc);
						if(pop.canSpawn(tw, coords[0]>>4, coords[1]>>4, biomes)) {
							pops[size] = pop;
							size++;
							break; //ONLY ONE OF EACH TYPE. Do not try to spawn multiple.
						}
					}
				
				//Stop trying if max structures is hit
				if(size >= maxStructures) break;
			}
		}
		
		SingleMegaChunkStructurePopulator[] returnVal = new SingleMegaChunkStructurePopulator[size];
		System.arraycopy(pops, 0, returnVal, 0, size);
		
		//cache
		queryCache.put(Objects.hash(tw,mc,biomes),returnVal);
		return returnVal;
	}
	
	  // Implementing Fisher–Yates shuffle
	private static void shuffleArray(Random rand, Object[] ar)
	{
		if(ar.length == 0) return;
		  
	    for (int i = ar.length - 1; i > 0; i--)
	    {
	      int index = rand.nextInt(i + 1);
	      // Simple swap
	      Object a = ar[index];
	      ar[index] = ar[i];
	      ar[i] = a;
	    }
	}
	
	/**
	 * Registers small or large structures. Must implement either SingleMegaChunkStructurePopulator or MultiMegaChunkStructurePopulator.
	 * @param type
	 * @param pop
	 */
	public static void registerStructure(StructureType type, StructurePopulator pop) {
		if(!pop.isEnabled()) return;//Don't register disabled features
		
		if(pop instanceof SingleMegaChunkStructurePopulator) {
			SingleMegaChunkStructurePopulator[] pops = new SingleMegaChunkStructurePopulator[] {(SingleMegaChunkStructurePopulator)pop};
			if(largeStructureRegistry.containsKey(type)) {
				StructurePopulator[] existing = largeStructureRegistry.get(type);
				StructurePopulator[] old = pops;
				pops = new SingleMegaChunkStructurePopulator[existing.length + 1];
				System.arraycopy(existing, 0, pops, 0, existing.length);
				System.arraycopy(old, 0, pops, existing.length, 1);
			}
			TerraformGeneratorPlugin.logger.info("[Structure Registry] Registered Large Structure: " + pop.getClass().getSimpleName());
			largeStructureRegistry.put(type, pops);
		}
		else if(pop instanceof MultiMegaChunkStructurePopulator) {
			TerraformGeneratorPlugin.logger.info("[Structure Registry] Registered Small Structure: " + pop.getClass().getSimpleName());
			smallStructureRegistry.add((MultiMegaChunkStructurePopulator) pop);
		}
		
	}
	
	public static StructurePopulator[] getAllPopulators() {
		
		int size = smallStructureRegistry.size();
		for(StructurePopulator[] types:largeStructureRegistry.values()) {
			size += types.length;
		}
		StructurePopulator[] pops = new StructurePopulator[size];
		int index = 0;
		
		//Account for all small structures
		for(StructurePopulator pop:smallStructureRegistry){
			pops[index] = pop;
			index++;
		}
		
		//Account for all large structures
		for(StructurePopulator[] types:largeStructureRegistry.values()) {
			for(StructurePopulator pop:types){
				pops[index] = pop;
				index++;
			}
		}
		return pops;
	}

}
