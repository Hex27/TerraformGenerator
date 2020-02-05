package org.terraform.command;

import java.util.Random;
import java.util.Stack;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.drycell.command.DCCommand;
import org.drycell.command.InvalidArgumentException;
import org.drycell.main.DrycellPlugin;

public class CaveCommand extends DCCommand {

	public CaveCommand(DrycellPlugin plugin, String... aliases) {
		super(plugin, aliases);
	}

	@Override
	public String getDefaultDescription() {
		return "Digs a cave at your location";
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
		Random r = new Random();
		int seed = r.nextInt(9999);
//		CaveWorm worm = new CaveWorm(p.getLocation().getBlock(), (int) p.getWorld().getSeed(), (int) p.getLocation().getY(), CaveLiquid.AIR);
//		while(worm.hasNext()){
//			worm.next();
//		}
	}
}
