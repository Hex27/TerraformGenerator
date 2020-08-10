package org.terraform.command;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.entity.Player;
import org.drycell.command.DCCommand;
import org.drycell.command.InvalidArgumentException;
import org.drycell.main.DrycellPlugin;

import net.minecraft.server.v1_15_R1.BaseBlockPosition;
import net.minecraft.server.v1_15_R1.BlockPosition;
import net.minecraft.server.v1_15_R1.GeneratorAccess;
import net.minecraft.server.v1_15_R1.StructureAbstract;
import net.minecraft.server.v1_15_R1.StructureGenerator;
import net.minecraft.server.v1_15_R1.StructurePiece;
import net.minecraft.server.v1_15_R1.StructureStart;
import net.minecraft.server.v1_15_R1.WorldGenerator;

public class GuardianSpawnCheckCommand extends DCCommand {

	public GuardianSpawnCheckCommand(DrycellPlugin plugin, String... aliases) {
		super(plugin, aliases);
	}

	@Override
	public String getDefaultDescription() {
		return "1.15.2 only command that does NMS stuff to figure NMS stuff out";
	}

	@Override
	public boolean canConsoleExec() {
		return false;
	}

	@Override
	public boolean hasPermission(CommandSender sender) {
		
		return sender.isOp();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void execute(CommandSender sender, Stack<String> args)
			throws InvalidArgumentException {
		
		Player p = (Player) sender;
		int x = p.getLocation().getBlockX();
		int z = p.getLocation().getBlockZ();
		BlockPosition blockposition = new BlockPosition(x,p.getLocation().getBlockY(),z);
		boolean flag = false;
		
		GeneratorAccess access = ((CraftWorld) p.getWorld()).getHandle();
		
		List<StructureAbstract> list;
		try {
			//private List<StructureStart> dereferenceStructureStarts
			//(GeneratorAccess generatoraccess, int chunkX, int chunkZ) {
			//List<StructureAbstract> list = StructureGenerator.a(access, x >> 4, z >> 4);
			Method method = StructureGenerator.class.getDeclaredMethod("a",GeneratorAccess.class,int.class,int.class);
			method.setAccessible(true);
			list = (List<StructureAbstract>) method
					.invoke(WorldGenerator.OCEAN_MONUMENT, access, x>>4, z>>4);
	
			Iterator<StructureAbstract> iterator = list.iterator();
			p.sendMessage("Iterator size: " + list.size());
	        while (iterator.hasNext()) {
	            StructureStart structurestart = (StructureStart) iterator.next();
	            	//isValid
	            p.sendMessage("Looking at " + structurestart.l().b());
	            if(!structurestart.e()){ //isValid()
	            	p.sendMessage("Not valid");
	            }
	            	//GetBoundingBox, isInside position
	            if(!structurestart.c().b((BaseBlockPosition) blockposition)){ //isValid()
	            	p.sendMessage("Not valid. Bounds are: ");
	            	p.sendMessage(structurestart.c().a+","+structurestart.c().b+","+structurestart.c().c
	            			+":"+structurestart.c().d+","+structurestart.c().e+","+structurestart.c().f);
	            }
	            if (structurestart.e() && structurestart.c().b((BaseBlockPosition) blockposition)) {
	                if (!flag) {
	                    //return structurestart;
	                	p.sendMessage("A structurestart was returned!");
	                	return;
	                }
	                								//getPieces
	                Iterator<StructurePiece> iterator1 = structurestart.d().iterator();
	
	                while (iterator1.hasNext()) {
	                    StructurePiece structurepiece = (StructurePiece) iterator1.next();
	
	                    if (structurepiece.g().b((BaseBlockPosition) blockposition)) {
	                    	p.sendMessage("A structurestart was returned!");
	                    	return;
	                    }
	                }
	            }
	        }
        	//No Structure
        	p.sendMessage("No structure.");
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
