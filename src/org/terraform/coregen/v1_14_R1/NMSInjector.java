package org.terraform.coregen.v1_14_R1;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_14_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_14_R1.CraftWorld;
import org.terraform.coregen.NMSInjectorAbstract;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.coregen.PopulatorDataICAAbstract;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;

import net.minecraft.server.v1_14_R1.GeneratorSettingsFlat;
import net.minecraft.server.v1_14_R1.IChunkAccess;
import net.minecraft.server.v1_14_R1.PlayerChunkMap;
import net.minecraft.server.v1_14_R1.WorldServer;

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
//			Field pcmGenField = pcm.getClass().getField("chunkGenerator");
//			Field cpGenField = ws.getChunkProvider().getClass().getField("chunkGenerator");
//			pcmGenField.setAccessible(true);
//			cpGenField.setAccessible(true);
//			// Remove final modifier
//			Field modifiersField = Field.class.getDeclaredField("modifiers");
//			modifiersField.setAccessible(true);
//			modifiersField.setInt(pcmGenField, pcmGenField.getModifiers() & ~Modifier.FINAL);
//			modifiersField.setInt(cpGenField, cpGenField.getModifiers() & ~Modifier.FINAL);
//			// Get and set field value		
//			pcmGenField.set(pcm,bpg);
//			cpGenField.set(ws.getChunkProvider(),bpg);
			TerraformGeneratorPlugin.privateFieldHandler.injectField(
					pcm, "chunkGenerator", bpg);
			TerraformGeneratorPlugin.privateFieldHandler.injectField(
					ws.getChunkProvider(), "chunkGenerator", bpg);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	@Override
	public PopulatorDataICAAbstract getICAData(Chunk chunk) {
		IChunkAccess ica = ((CraftChunk) chunk).getHandle();
		CraftWorld cw = (CraftWorld) chunk.getWorld();
		WorldServer ws = cw.getHandle();
		TerraformWorld tw = TerraformWorld.get(chunk.getWorld());
		return new PopulatorDataICA(tw, ws, ica, chunk.getX(), chunk.getZ());
	}

	@Override
	public PopulatorDataICAAbstract getICAData(PopulatorDataAbstract data) {
		if(data instanceof PopulatorData){
			PopulatorData pdata = (PopulatorData) data;
			IChunkAccess ica = pdata.rlwa.getChunkAt(data.getChunkX(),data.getChunkZ());
			WorldServer ws = ((PopulatorData) data).rlwa.getMinecraftWorld();
			TerraformWorld tw = TerraformWorld.get(pdata.rlwa.getWorldData().getName(),pdata.rlwa.getWorldData().getSeed());
			return new PopulatorDataICA(tw, ws, ica, data.getChunkX(),data.getChunkZ());
		}
		return null;
	}
}
