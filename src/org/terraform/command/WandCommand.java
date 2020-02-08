package org.terraform.command;

import java.util.Random;
import java.util.Stack;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.drycell.command.DCCommand;
import org.drycell.command.InvalidArgumentException;
import org.drycell.main.DrycellPlugin;
import org.terraform.schematic.SchematicListener;
import org.terraform.utils.FastNoise;
import org.terraform.utils.FastNoise.NoiseType;
import org.terraform.utils.GenUtils;

public class WandCommand extends DCCommand {

	public WandCommand(DrycellPlugin plugin, String... aliases) {
		super(plugin, aliases);
	}

	@Override
	public String getDefaultDescription() {
		return "Gives a schematic wand";
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
		p.getInventory().addItem(SchematicListener.getWand());
		p.sendMessage(ChatColor.GREEN + "Wand added.");
	}
}
