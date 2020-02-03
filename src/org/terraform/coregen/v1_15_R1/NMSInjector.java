package org.terraform.coregen.v1_15_R1;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import net.minecraft.server.v1_15_R1.GeneratorSettingsFlat;
import net.minecraft.server.v1_15_R1.PlayerChunkMap;
import net.minecraft.server.v1_15_R1.WorldServer;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.terraform.coregen.NMSInjectorAbstract;

public class NMSInjector extends NMSInjectorAbstract {

	@Override
	public boolean attemptInject(World world) {
		CraftWorld cw = (CraftWorld) world;
		WorldServer ws = cw.getHandle();
//		for(Field f:ws.getClass().getFields()){
//			Bukkit.getLogger().info(f.getType().getName() + " " + f.getName());
//		}
		NMSChunkGenerator bpg = new NMSChunkGenerator(ws, ws.getChunkProvider().getChunkGenerator().getWorldChunkManager(), GeneratorSettingsFlat.z());
		PlayerChunkMap pcm = ws.getChunkProvider().playerChunkMap;
		
		try {
			Field pcmGenField = pcm.getClass().getField("chunkGenerator");
			Field cpGenField = ws.getChunkProvider().getClass().getField("chunkGenerator");
			pcmGenField.setAccessible(true);
			cpGenField.setAccessible(true);
			// Remove final modifier
			Field modifiersField = Field.class.getDeclaredField("modifiers");
			modifiersField.setAccessible(true);
			modifiersField.setInt(pcmGenField, pcmGenField.getModifiers() & ~Modifier.FINAL);
			modifiersField.setInt(cpGenField, cpGenField.getModifiers() & ~Modifier.FINAL);
			// Get and set field value		
			pcmGenField.set(pcm,bpg);
			cpGenField.set(ws.getChunkProvider(),bpg);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}

}
