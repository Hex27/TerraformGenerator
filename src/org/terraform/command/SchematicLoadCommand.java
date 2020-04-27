package org.terraform.command;

import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.drycell.command.DCCommand;
import org.drycell.command.InvalidArgumentException;
import org.drycell.main.DrycellPlugin;
import org.terraform.schematic.TerraSchematic;
import org.terraform.utils.BlockUtils;

public class SchematicLoadCommand extends DCCommand {

	public SchematicLoadCommand(DrycellPlugin plugin, String... aliases) {
		super(plugin, aliases);
		this.parameters.add(new SchematicArgument("schem-name",false));
	}

	@Override
	public String getDefaultDescription() {
		return "Loads and pastes schematic from inside the jar file";
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
		ArrayList<Object> parsed = this.parseArguments(sender, args);
		TerraSchematic schem = (TerraSchematic) parsed.get(0);
		schem.setFace(BlockUtils.getDirectBlockFace(new Random()));
		schem.apply();
	}
}
