package org.terraform.v1_17_R1;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_17_R1.CraftServer;

import com.mojang.serialization.Codec;

import net.minecraft.core.IRegistry;
import net.minecraft.core.IRegistryWritable;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.world.level.biome.BiomeBase;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.WorldChunkManager;
import net.minecraft.world.level.biome.WorldChunkManagerOverworld;

public class CustomBiomeSource extends WorldChunkManager {

	//private BiomeStorage storage;
	//private WorldChunkManager original;
	protected CustomBiomeSource(WorldChunkManager original) {
		super(original.b());
	}

	/*
	 * Fix to prevent red sand in some surface cave carving
	*/
	@Override
	public BiomeBase getBiome(int x, int y, int z) {
		CraftServer craftserver = (CraftServer)Bukkit.getServer();
		DedicatedServer dedicatedserver = craftserver.getServer();

		IRegistryWritable<BiomeBase> registrywritable = dedicatedserver.getCustomRegistry().b(IRegistry.aO);
		return registrywritable.a(Biomes.u);
	}

	@Override
	protected Codec<? extends WorldChunkManager> a() {
		return WorldChunkManagerOverworld.e;
	}

	@Override
	public WorldChunkManager a(long arg0) {
		return this;
	}

}
