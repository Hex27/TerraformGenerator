package org.terraform.v1_17_R1;
import java.lang.reflect.Field;
import java.util.OptionalLong;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_17_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.terraform.coregen.BlockDataFixerAbstract;
import org.terraform.coregen.NMSInjectorAbstract;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.coregen.PopulatorDataICAAbstract;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;

import net.minecraft.core.BlockPosition;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.level.PlayerChunkMap;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.level.biome.GenLayerZoomer;
import net.minecraft.world.level.chunk.IChunkAccess;
import net.minecraft.world.level.dimension.DimensionManager;

public class NMSInjector extends NMSInjectorAbstract {
	
	private boolean heightInjectSuccess = true;
	
	@Override
	public void startupTasks() {
        //Inject new biomes
        CustomBiomeHandler.init();
	}
	
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
                ws.getChunkProvider().getChunkGenerator(),
                ws.getChunkProvider().getChunkGenerator().getWorldChunkManager(),
                ws.getChunkProvider().getChunkGenerator().getWorldChunkManager(),
                ws.getChunkProvider().getChunkGenerator().getSettings(),
                world.getSeed());
        
        //Change DimensionManager height
        //For now, leave at 0-256 until you can put something interesting down there.
        DimensionManager delegate = ws.getDimensionManager();
        
		DimensionManager replacement = DimensionManager.a(
		(OptionalLong) queryDimensionManagerPrivateField("u",delegate),
		(boolean) queryDimensionManagerPrivateField("v",delegate),
		(boolean) queryDimensionManagerPrivateField("w",delegate),
		(boolean) queryDimensionManagerPrivateField("x",delegate),
		(boolean) queryDimensionManagerPrivateField("y",delegate),
		(double) queryDimensionManagerPrivateField("z",delegate),
		(boolean) queryDimensionManagerPrivateField("A",delegate),
		(boolean) queryDimensionManagerPrivateField("B",delegate),
		(boolean) queryDimensionManagerPrivateField("C",delegate),
		(boolean) queryDimensionManagerPrivateField("D",delegate),
		(boolean) queryDimensionManagerPrivateField("E",delegate),
		(int) 0,//queryDimensionManagerPrivateField("F",delegate), //minY
		(int) 256,//queryDimensionManagerPrivateField("G",delegate), //Height
		(int) 256,//queryDimensionManagerPrivateField("H",delegate), //Logical Height
		(GenLayerZoomer) queryDimensionManagerPrivateField("I",delegate),
		(MinecraftKey) queryDimensionManagerPrivateField("J",delegate),
		(MinecraftKey) queryDimensionManagerPrivateField("K",delegate),
		(float) queryDimensionManagerPrivateField("L",delegate)
		);
		
		try {
            TerraformGeneratorPlugin.privateFieldHandler.injectField(
                    (net.minecraft.world.level.World) ws, 
                    net.minecraft.world.level.World.class.getDeclaredField("C"),
                    replacement);
    		TerraformGeneratorPlugin.logger.info("&aSuccessfully injected custom world height!");
    		TerraformGeneratorPlugin.logger.info("&aNew Heights:");
    		TerraformGeneratorPlugin.logger.info("- minY " + ws.getDimensionManager().getMinY());
    		TerraformGeneratorPlugin.logger.info("- Height " + ws.getDimensionManager().getHeight());
    		TerraformGeneratorPlugin.logger.info("- LogicalHeight " + ws.getDimensionManager().getLogicalHeight());
        } catch (Throwable e) {
        	heightInjectSuccess = false;
            e.printStackTrace();
        }
        
		//Inject TerraformGenerator NMS chunk generator
		
        PlayerChunkMap pcm = ws.getChunkProvider().a; //PlayerChunkMap

        try {
            TerraformGeneratorPlugin.privateFieldHandler.injectField(
                    pcm, "r", bpg); //chunkGenerator
            TerraformGeneratorPlugin.privateFieldHandler.injectField(
                    ws.getChunkProvider(), "d", bpg); //chunkGenerator
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

	@Override
	public void updatePhysics(World world, org.bukkit.block.Block block) {
		BlockPosition pos = new BlockPosition(block.getX(),block.getY(),block.getZ());
		((CraftWorld) world).getHandle().applyPhysics(
				pos,
				((CraftChunk) block.getChunk()).getHandle().getType(pos).getBlock());
	}
	
	@Override
	public int getMinY() {
		if(this.heightInjectSuccess)
			return -32;
		else
			return 0;
	}

	@Override
	public int getMaxY() {
		if(this.heightInjectSuccess)
			return 256;
		else
			return 256;
	}
	
	private static Object queryDimensionManagerPrivateField(String fieldName, DimensionManager delegate) {
		try {
			Field field = delegate.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			return field.get(delegate);
		}
		catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
