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
import org.terraform.structure.StrongholdPopulator;
import org.terraform.structure.animalfarm.AnimalFarmPopulator;
import org.terraform.structure.farmhouse.FarmhousePopulator;

public class AnimalSpawnerCommand extends DCCommand {

	public AnimalSpawnerCommand(DrycellPlugin plugin, String... aliases) {
		super(plugin, aliases);
	}

	@Override
	public String getDefaultDescription() {
		return "Spawntest for animals";
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
		//new TerraformAnimalSpawner().populate(p.getWorld(), new Random(), p.getLocation().getChunk());		
		p.sendMessage("Unsupported now.");
	}

}
