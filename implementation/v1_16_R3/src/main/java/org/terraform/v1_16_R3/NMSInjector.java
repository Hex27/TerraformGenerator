package org.terraform.v1_16_R3;

import net.minecraft.server.v1_16_R3.IChunkAccess;
import net.minecraft.server.v1_16_R3.PlayerChunkMap;
import net.minecraft.server.v1_16_R3.WorldServer;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_16_R3.CraftChunk;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.terraform.coregen.BlockDataFixerAbstract;
import org.terraform.coregen.NMSInjectorAbstract;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.coregen.PopulatorDataICAAbstract;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;

public class NMSInjector extends NMSInjectorAbstract {
    @Override
    public BlockDataFixerAbstract getBlockDataFixer() {
        return new BlockDataFixer();
    }

    @SuppressWarnings("resource")
    @Override
    public boolean attemptInject(World world) {
        CraftWorld cw = (CraftWorld) world;
        WorldServer ws = cw.getHandle();

        //String worldname,
        //int seed,
        //WorldChunkManager worldchunkmanager,
        //WorldChunkManager worldchunkmanager1,
        //StructureSettings structuresettings,
        //long i
        NMSChunkGenerator bpg = new NMSChunkGenerator(
                world.getName(),
                (int) world.getSeed(),
                ws.getChunkProvider().getChunkGenerator().getWorldChunkManager(),
                ws.getChunkProvider().getChunkGenerator().getWorldChunkManager(),
                ws.getChunkProvider().getChunkGenerator().getSettings(),
                world.getSeed());
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
        } catch (Throwable e) {
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
        //return new PopulatorData(new RegionLimitedWorldAccess(ws, list), null, chunk.getX(), chunk.getZ());
        return new PopulatorDataICA(tw, ws, ica, chunk.getX(), chunk.getZ());
    }

    @SuppressWarnings("deprecation")
    @Override
    public PopulatorDataICAAbstract getICAData(PopulatorDataAbstract data) {
        if (data instanceof PopulatorData) {
            PopulatorData pdata = (PopulatorData) data;
            IChunkAccess ica = pdata.rlwa.getChunkAt(data.getChunkX(), data.getChunkZ());
            WorldServer ws = ((PopulatorData) data).rlwa.getMinecraftWorld();
            TerraformWorld tw = TerraformWorld.get(ws.getWorld().getName(), ws.getSeed());
            return new PopulatorDataICA(tw, ws, ica, data.getChunkX(), data.getChunkZ());
        }
        return null;
    }
}
