package org.terraform.command;

import java.util.Stack;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.drycell.command.DCCommand;
import org.drycell.command.InvalidArgumentException;
import org.drycell.main.DrycellPlugin;
import org.terraform.coregen.PopulatorDataPostGen;
import org.terraform.structure.monument.MonumentDesign;

public class LargeMonumentLampCommand extends DCCommand {

	public LargeMonumentLampCommand(DrycellPlugin plugin, String... aliases) {
		super(plugin, aliases);
	}

	@Override
	public String getDefaultDescription() {
		return "Spawntest for monument lamps";
	}

	@Override
	public boolean canConsoleExec() {
		return false;
	}

	@Override
	public boolean hasPermission(CommandSender sender) {
		
		return sender.isOp();
	}

	@Override
	public void execute(CommandSender sender, Stack<String> args)
			throws InvalidArgumentException {
		
		Player p = (Player) sender;		
//		PopulatorDataAbstract gen = 
//				new org.terraform.coregen.v1_15_R1.PopulatorData(
//						((org.bukkit.craftbukkit.v1_15_R1.CraftChunk)p.getChunk()).getHandle(),
//						null,p.getLocation().getChunk().getX(),p.getLocation().getChunk().getZ());
		PopulatorDataPostGen data = new PopulatorDataPostGen(p.getLocation().getChunk());
		int x = p.getLocation().getBlockX();
		int y = p.getLocation().getBlockY();
		int z = p.getLocation().getBlockZ();
		//		RoomLayoutGenerator gen = new RoomLayoutGenerator(tw.getRand(8),50,x,y,z,100);
//		gen.setPathPopulator(new StrongholdPathPopulator(tw.getRand(13)));
//		gen.generate();
//		gen.fill(data, tw, Material.STONE_BRICKS, Material.STONE_BRICKS, Material.MOSSY_STONE_BRICKS, Material.CRACKED_STONE_BRICKS);
//		
		MonumentDesign.PRISMARINE_LANTERNS.spawnLargeLight(data, x, y, z);
		MonumentDesign.DARK_PRISMARINE_CORNERS.spawnLargeLight(data, x+10, y, z);
		MonumentDesign.DARK_LIGHTLESS.spawnLargeLight(data, x-10, y, z);
	}

}
