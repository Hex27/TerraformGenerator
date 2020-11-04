package org.terraform.coregen.bukkit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Random;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.generator.BlockPopulator;
import org.terraform.data.SimpleChunkLocation;
import org.terraform.main.TerraformGeneratorPlugin;

public class NativeGeneratorPatcherPopulator extends BlockPopulator{
	
	//SimpleChunkLocation to a collection of location:blockdata entries marked for repair.
	public static HashMap<SimpleChunkLocation, Collection<Object[]>> cache = new HashMap<SimpleChunkLocation, Collection<Object[]>>();
	//private final TerraformWorld tw;

    public NativeGeneratorPatcherPopulator() {
        //this.tw = tw;
    }
    
	@Override
	 public void populate(World world, Random random, Chunk chunk) {
		SimpleChunkLocation scl = new SimpleChunkLocation(chunk);
		Collection<Object[]> changes = cache.remove(scl);
		if(changes != null) {
			TerraformGeneratorPlugin.logger.info("[NativeGeneratorPatcher] Detected anomalous generation by NMS on " + scl.toString() + ". Running repairs. Check at those chunk coords after generation to ensure that nothing strange happened.");
			for(Object[] entry:changes) {
				int[] loc = (int[])entry[0];
				BlockData data = (BlockData)entry[1];
				world.getBlockAt(loc[0], loc[1], loc[2])
				.setBlockData(data);
			}
		}
	}
	
	public static void pushChange(String world, int x, int y, int z, BlockData data) {
		SimpleChunkLocation scl = new SimpleChunkLocation(world,x,y,z);
		if(!cache.containsKey(scl))
			cache.put(scl, new ArrayList<Object[]>());
		
		cache.get(scl).add(new Object[] {
				new int[] {x,y,z},
				data
		});
	}
	

}
