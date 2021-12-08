package org.terraform.coregen.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.scheduler.BukkitRunnable;
import org.terraform.data.SimpleChunkLocation;
import org.terraform.data.SimpleLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.main.config.TConfigOption;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class BiomeUpdaterPopulator extends BlockPopulator{
	
	TerraformWorld tw;
    public BiomeUpdaterPopulator(TerraformWorld tw) {
    	this.tw = tw;
    }
    @Override
    public void populate(World world, Random random, Chunk chunk) {
    	
    	for(int x = chunk.getX()*16; x < chunk.getX()*16+16; x++) {
    		for(int z = chunk.getZ()*16; z < chunk.getZ()*16+16; z++) {
        		for(int y = tw.minY; y < tw.maxY; y+=4) {
        			world.setBiome(x, y, z, tw.getBiomeBank(x, z).getHandler().getBiome());
        		}
        	}
    	}
    }

}
