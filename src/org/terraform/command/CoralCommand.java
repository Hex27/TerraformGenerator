package org.terraform.command;

import java.util.Stack;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.drycell.command.DCCommand;
import org.drycell.command.InvalidArgumentException;
import org.drycell.main.DrycellPlugin;
import org.terraform.coregen.PopulatorDataPostGen;
import org.terraform.data.TerraformWorld;
import org.terraform.utils.CoralGenerator;

public class CoralCommand extends DCCommand {

	public CoralCommand(DrycellPlugin plugin, String... aliases) {
		super(plugin, aliases);
	}

	@Override
	public String getDefaultDescription() {
		return "Spawns a coral";
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
		//TreeDB.spawnFractalTree(new Random(), new SimpleBlock(data,p.getLocation().getBlock()));
		TerraformWorld tw = TerraformWorld.get(p.getWorld());
		int x = p.getLocation().getBlockX();
		int y = p.getLocation().getBlockY();
		int z = p.getLocation().getBlockZ();
		CoralGenerator.generateCoral(data, x, y, z);
	}

}
