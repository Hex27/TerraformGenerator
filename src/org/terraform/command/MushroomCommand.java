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
import org.terraform.tree.FractalTreeType;
import org.terraform.tree.TreeDB;

public class MushroomCommand extends DCCommand {

	public MushroomCommand(DrycellPlugin plugin, String... aliases) {
		super(plugin, aliases);
	}

	@Override
	public String getDefaultDescription() {
		return "Spawns a giant mushroom (red or brown)";
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
		int x = p.getLocation().getBlockX();
		int y = p.getLocation().getBlockY();
		int z = p.getLocation().getBlockZ();
//		TreeDB.spawnCoconutTree(new Random(), data, x,y,z);
		if(new Random().nextBoolean())
			TreeDB.spawnGiantMushroom(TerraformWorld.get(p.getWorld()), data, x, y, z, FractalTreeType.RED_MUSHROOM_BASE);
		else
			TreeDB.spawnGiantMushroom(TerraformWorld.get(p.getWorld()), data, x, y, z, FractalTreeType.BROWN_MUSHROOM_BASE);
	}

}
