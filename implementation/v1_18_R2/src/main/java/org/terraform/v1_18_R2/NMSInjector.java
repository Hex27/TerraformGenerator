package org.terraform.v1_18_R2;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.level.GeneratorAccessSeed;
import net.minecraft.world.level.block.entity.TileEntityBeehive;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Beehive;
import org.bukkit.craftbukkit.v1_18_R2.CraftChunk;
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R2.block.CraftBlockEntityState;
import org.bukkit.craftbukkit.v1_18_R2.generator.CraftLimitedRegion;
import org.bukkit.craftbukkit.v1_18_R2.generator.CustomChunkGenerator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.terraform.coregen.BlockDataFixerAbstract;
import org.terraform.coregen.NMSInjectorAbstract;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.coregen.populatordata.PopulatorDataICAAbstract;
import org.terraform.coregen.populatordata.PopulatorDataPostGen;
import org.terraform.coregen.populatordata.PopulatorDataSpigotAPI;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;

import net.minecraft.server.level.PlayerChunkMap;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.IChunkAccess;

public class NMSInjector extends NMSInjectorAbstract {
	
	//private boolean heightInjectSuccess = true;
	
	@Override
	public void startupTasks() {
        //Inject new biomes
        CustomBiomeHandler.init();
	}
	
    @Override
    public @NotNull BlockDataFixerAbstract getBlockDataFixer() {
        return new BlockDataFixer();
    }

    @Override
    public boolean attemptInject(@NotNull World world) {
        CraftWorld cw = (CraftWorld) world;
        WorldServer ws = cw.getHandle();
        
        //Force world to correct height
        TerraformWorld.get(world).minY = -64;
        TerraformWorld.get(world).maxY = 320;
        
        //String worldname,
        //int seed,
        //WorldChunkManager worldchunkmanager,
        //WorldChunkManager worldchunkmanager1,
        //StructureSettings structuresettings,
        //long i
        NMSChunkGenerator bpg = new NMSChunkGenerator(
                world.getName(),
                (int) world.getSeed(),
                ws.k().g());//, //k is getChunkProvider, g is getChunkGenerator()
                //old,
                //old,
                //ws.k().g().d(),
                //world.getSeed());
        TerraformGeneratorPlugin.logger.info("NMSChunkGenerator Delegate is of type " + ws.k().g().getClass().getSimpleName());
        
        if(ws.k().g() instanceof CustomChunkGenerator) {
            try {
            	ChunkGenerator delegate;
            	Field f = CustomChunkGenerator.class.getDeclaredField("delegate");
            	f.setAccessible(true);
            	delegate = (ChunkGenerator) f.get(ws.k().g());
            	TerraformGeneratorPlugin.logger.info("CustomChunkGenerator Delegate is of type " + delegate.getClass().getSimpleName());
            }
            catch(Exception e) {
            	e.printStackTrace();
            }
        }
        //For Changing DimensionManager height
    	TerraformGeneratorPlugin.logger.info("- minY " + ws.q_().k() + "   " + world.getMinHeight());
		TerraformGeneratorPlugin.logger.info("- Height " + ws.q_().l() + "   " + world.getMaxHeight());
		TerraformGeneratorPlugin.logger.info("- LogicalHeight " + ws.q_().m() + "   " + world.getLogicalHeight()); 
        
		//Inject TerraformGenerator NMS chunk generator
		
        PlayerChunkMap pcm = ws.k().a; //getChunkProvider().PlayerChunkMap

        try {
            TerraformGeneratorPlugin.privateFieldHandler.injectField(
                    pcm, "u", bpg); //chunkGenerator
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
        
        return true;
    }

    @Override
    public @NotNull PopulatorDataICAAbstract getICAData(@NotNull Chunk chunk) {
        IChunkAccess ica = ((CraftChunk) chunk).getHandle();
        CraftWorld cw = (CraftWorld) chunk.getWorld();
        WorldServer ws = cw.getHandle();
        
        TerraformWorld tw = TerraformWorld.get(chunk.getWorld());
        //return new PopulatorData(new RegionLimitedWorldAccess(ws, list), null, chunk.getX(), chunk.getZ());
        return new PopulatorDataICA(new PopulatorDataPostGen(chunk), tw, ws, ica, chunk.getX(), chunk.getZ());
    }

    @Override
    public @Nullable PopulatorDataICAAbstract getICAData(PopulatorDataAbstract data) {
        //This is for the damn bees
        if (data instanceof PopulatorDataSpigotAPI pdata) {
            GeneratorAccessSeed gas = ((CraftLimitedRegion) pdata.lr).getHandle();
            WorldServer ws = gas.getMinecraftWorld();
            TerraformWorld tw = TerraformWorld.get(ws.getWorld().getName(), ws.D()); //C is getSeed()
            return new PopulatorDataICA(data, tw, ws, gas.a(data.getChunkX(),data.getChunkZ()), data.getChunkX(), data.getChunkZ());
        }
        if(data instanceof PopulatorDataPostGen gdata)
            return getICAData(gdata.getChunk());

        return null;
    }

    private static @Nullable Method getTileEntity = null;
    @Override
    public void storeBee(Beehive hive) {
        try {
            if(getTileEntity == null)
            {
                getTileEntity = CraftBlockEntityState.class.getDeclaredMethod("getTileEntity");
                getTileEntity.setAccessible(true);
            }
            TileEntityBeehive teb = (TileEntityBeehive) getTileEntity.invoke(hive);

            NBTTagCompound nbttagcompound = new NBTTagCompound();
            nbttagcompound.a("id", "minecraft:bee");
            //TileEntityBeehive.storeBee
            teb.a(nbttagcompound, 0, false);

        } catch(NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
	
	@Override
	public int getMinY() {
		return -64;
	}

	@Override
	public int getMaxY() {
		return 320;
	}

}
