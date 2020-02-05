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
import org.terraform.structure.WitchHutPopulator;

public class WitchHutCommand extends DCCommand {

	public WitchHutCommand(DrycellPlugin plugin, String... aliases) {
		super(plugin, aliases);
	}

	@Override
	public String getDefaultDescription() {
		return "Spawns a witch hut";
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
		PopulatorDataPostGen data = new PopulatorDataPostGen(p.getLocation().getChunk());
//		int x = p.getLocation().getBlockX();
//		int y = p.getLocation().getBlockY();
//		int z = p.getLocation().getBlockZ();
//		TreeDB.spawnCoconutTree(new Random(), data, x,y,z);
		new WitchHutPopulator().populate(TerraformWorld.get(p.getWorld()),new Random(), data);
		
	}

}
