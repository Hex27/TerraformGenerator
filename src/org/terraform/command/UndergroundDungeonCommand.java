package org.terraform.command;

import java.util.Random;
import java.util.Stack;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.drycell.command.DCCommand;
import org.drycell.command.InvalidArgumentException;
import org.drycell.main.DrycellPlugin;
import org.terraform.coregen.PopulatorDataPostGen;
import org.terraform.data.TerraformWorld;
import org.terraform.structure.FarmhousePopulator;
import org.terraform.structure.StrongholdPopulator;
import org.terraform.structure.dungeon.UndergroundDungeonPopulator;

public class UndergroundDungeonCommand extends DCCommand {

	public UndergroundDungeonCommand(DrycellPlugin plugin, String... aliases) {
		super(plugin, aliases);
	}

	@Override
	public String getDefaultDescription() {
		return "Spawntest for underground dungeons";
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
		TerraformWorld tw = TerraformWorld.get(p.getWorld());
		
//		RoomLayoutGenerator gen = new RoomLayoutGenerator(tw.getRand(8),50,x,y,z,100);
//		gen.setPathPopulator(new StrongholdPathPopulator(tw.getRand(13)));
//		gen.generate();
//		gen.fill(data, tw, Material.STONE_BRICKS, Material.STONE_BRICKS, Material.MOSSY_STONE_BRICKS, Material.CRACKED_STONE_BRICKS);
//		
		new UndergroundDungeonPopulator().spawnDungeonRoom(x,y,z,tw, new Random(), data);
	}

}
