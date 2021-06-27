package org.terraform.v1_17_R1;

import java.lang.reflect.Field;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_17_R1.CraftServer;
import org.terraform.biome.custombiomes.CustomBiomeType;
import org.terraform.main.TerraformGeneratorPlugin;

import com.mojang.serialization.Lifecycle;

import net.minecraft.core.IRegistry;
import net.minecraft.core.IRegistryWritable;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.world.level.biome.BiomeBase;
import net.minecraft.world.level.biome.BiomeFog;
import net.minecraft.world.level.biome.BiomeFog.GrassColor;
import net.minecraft.world.level.biome.BiomeSettingsGeneration;
import net.minecraft.world.level.biome.BiomeSettingsMobs;
import net.minecraft.world.level.biome.Biomes;

public class CustomBiomeHandler {

	public static void init() {
		CraftServer craftserver = (CraftServer)Bukkit.getServer();
		DedicatedServer dedicatedserver = craftserver.getServer();

		IRegistryWritable<BiomeBase> registrywritable = dedicatedserver.getCustomRegistry().b(IRegistry.aO);
		BiomeBase forestbiome = registrywritable.a(Biomes.e);
	
		for(CustomBiomeType type:CustomBiomeType.values()) {
			if(type == CustomBiomeType.NONE)
				continue;
			
			try {
				registerCustomBiomeBase(
						type,
						dedicatedserver,
						registrywritable,
						forestbiome
						);
				TerraformGeneratorPlugin.logger.info("Registered custom biome: " + type.toString().toLowerCase());
			} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
				TerraformGeneratorPlugin.logger.error("Failed to register custom biome: " + type.getKey());
				e.printStackTrace();
			}
		}

//		MinecraftServer ms = DedicatedServer.getServer();
//		ms.getCustomRegistry().d(IRegistry.aO).forEach(biomeBase -> {
//			TerraformGeneratorPlugin.logger.info("biome id " + ms.getCustomRegistry().d(IRegistry.aO).getKey(biomeBase) + " " +  ms.getCustomRegistry().d(IRegistry.aO).getId(biomeBase) );
//        });
		
	}
	
	private static void registerCustomBiomeBase(CustomBiomeType biomeType, DedicatedServer dedicatedserver, IRegistryWritable<BiomeBase> registrywritable, BiomeBase forestbiome) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

		ResourceKey<BiomeBase> newKey = ResourceKey.a(IRegistry.aO, new MinecraftKey("terraformgenerator", biomeType.toString().toLowerCase()));

		BiomeBase.a newBiome = new BiomeBase.a();
		newBiome.a(forestbiome.t());
		newBiome.a(forestbiome.c());

		Field biomeSettingMobsField = BiomeBase.class.getDeclaredField("m");
		biomeSettingMobsField.setAccessible(true);
		BiomeSettingsMobs biomeSettingMobs = (BiomeSettingsMobs) biomeSettingMobsField.get(forestbiome);
		newBiome.a(biomeSettingMobs);

		Field biomeSettingGenField = BiomeBase.class.getDeclaredField("l");
		biomeSettingGenField.setAccessible(true);
		BiomeSettingsGeneration biomeSettingGen = (BiomeSettingsGeneration) biomeSettingGenField.get(forestbiome);
		newBiome.a(biomeSettingGen);
		
		newBiome.a(0.2F); //Depth of biome
		newBiome.b(0.05F); //Scale of biome
		newBiome.c(0.7F); //Temperature of biome
		newBiome.d(biomeType.getRainFall()); //Downfall of biome

		//BiomeBase.TemperatureModifier.a will make your biome normal
		//BiomeBase.TemperatureModifier.b will make your biome frozen
		if(biomeType.isCold())
			newBiome.a(BiomeBase.TemperatureModifier.b); 
		else
			newBiome.a(BiomeBase.TemperatureModifier.a); 
		
		BiomeFog.a newFog = new BiomeFog.a();
		newFog.a(GrassColor.a); //This doesn't affect the actual final grass color, just leave this line as it is or you will get errors
		
		//Set biome colours. If field is empty, default to forest color
		
		//fogcolor
		newFog.a(biomeType.getFogColor().equals("") ? forestbiome.f():Integer.parseInt(biomeType.getFogColor(),16));
		
		//water color
		newFog.b(biomeType.getWaterColor().equals("") ? forestbiome.m():Integer.parseInt(biomeType.getWaterColor(),16)); 
		
		//water fog color
		newFog.c(biomeType.getWaterFogColor().equals("") ? forestbiome.n():Integer.parseInt(biomeType.getWaterFogColor(),16)); 
		
		//sky color
		newFog.d(biomeType.getSkyColor().equals("") ? forestbiome.a():Integer.parseInt(biomeType.getSkyColor(),16)); 

		//Unnecessary values; can be removed safely if you don't want to change them
		
		//foliage color (leaves, fines and more)
		newFog.e(biomeType.getFoliageColor().equals("") ? forestbiome.g():Integer.parseInt(biomeType.getFoliageColor(),16)); 
		
		//grass blocks color
		newFog.f(biomeType.getGrassColor().equals("") ? Integer.parseInt("79C05A",16):Integer.parseInt(biomeType.getGrassColor(),16)); 
		
		newBiome.a(newFog.a());
		dedicatedserver.getCustomRegistry().b(IRegistry.aO).a(newKey, newBiome.a(), Lifecycle.stable());
	
	}
}
