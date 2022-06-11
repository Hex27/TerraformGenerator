package org.terraform.v1_19_R1;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_19_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.entity.Player;
import org.terraform.coregen.BlockDataFixerAbstract;
import org.terraform.coregen.NMSInjectorAbstract;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.coregen.populatordata.PopulatorDataICAAbstract;
import org.terraform.coregen.populatordata.PopulatorDataPostGen;
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
    public BlockDataFixerAbstract getBlockDataFixer() {
        return new BlockDataFixer();
    }

    @SuppressWarnings("resource")
    @Override
    public boolean attemptInject(World world) {
        CraftWorld cw = (CraftWorld) world;
        WorldServer ws = cw.getHandle();
        
        //Force world to correct height
        TerraformWorld.get(world).minY = -64;
        TerraformWorld.get(world).maxY = 320;

        //k is getChunkProvider, g is getChunkGenerator()
        ChunkGenerator delegate = ws.k().g();
        
//        if(delegate instanceof CustomChunkGenerator) {
//            try {
//            	
//            	Field f = CustomChunkGenerator.class.getDeclaredField("delegate");
//            	f.setAccessible(true);
//            	delegate = (ChunkGenerator) f.get(ws.k().g());
//            }
//            catch(Exception e) {
//            	e.printStackTrace();
//            }
//        }
        TerraformGeneratorPlugin.logger.info("NMSChunkGenerator Delegate is of type " + delegate.getClass().getSimpleName());
        
        //String worldname,
        //int seed,
        //WorldChunkManager worldchunkmanager,
        //WorldChunkManager worldchunkmanager1,
        //StructureSettings structuresettings,
        //long i
        NMSChunkGenerator bpg = new NMSChunkGenerator(
                world.getName(),
                (int) world.getSeed(),
                delegate); 
                //old,
                //old,
                //ws.k().g().d(),
                //world.getSeed());
        
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
    public PopulatorDataICAAbstract getICAData(Chunk chunk) {
        IChunkAccess ica = ((CraftChunk) chunk).getHandle();
        CraftWorld cw = (CraftWorld) chunk.getWorld();
        WorldServer ws = cw.getHandle();
        
        TerraformWorld tw = TerraformWorld.get(chunk.getWorld());
        //return new PopulatorData(new RegionLimitedWorldAccess(ws, list), null, chunk.getX(), chunk.getZ());
        return new PopulatorDataICA(new PopulatorDataPostGen(chunk), tw, ws, ica, chunk.getX(), chunk.getZ());
    }

    @Override
    public PopulatorDataICAAbstract getICAData(PopulatorDataAbstract data) {
        if (data instanceof PopulatorData) {
            PopulatorData pdata = (PopulatorData) data;
            IChunkAccess ica = pdata.ica;//pdata.rlwa.getChunkAt(data.getChunkX(), data.getChunkZ());
            //funny if this explodes.
            WorldServer ws = ((PopulatorData) data).rlwa.getMinecraftWorld();
            TerraformWorld tw = TerraformWorld.get(ws.getWorld().getName(), ws.B()); //B is getSeed()
            return new PopulatorDataICA(data, tw, ws, ica, data.getChunkX(), data.getChunkZ());
        }
        return null;
    }
//
//	@Override
//	public void updatePhysics(World world, org.bukkit.block.Block block) {
//		BlockPosition pos = new BlockPosition(block.getX(),block.getY(),block.getZ());
//		((CraftWorld) world).getHandle()..applyPhysics(
//				pos,
//				((CraftChunk) block.getChunk()).getHandle().a_(pos).b()); //a_ is getBlockState, b is getBlock
//	}
	
	@Override
	public int getMinY() {
		return -64;
	}

	@Override
	public int getMaxY() {
		return 320;
	}
	
	@Override
	public void debugTest(Player p) {
	}
	
}
