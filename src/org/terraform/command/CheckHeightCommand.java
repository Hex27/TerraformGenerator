package org.terraform.command;

import java.util.Stack;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.drycell.command.DCCommand;
import org.drycell.command.InvalidArgumentException;
import org.drycell.main.DrycellPlugin;
import org.terraform.coregen.HeightMap;
import org.terraform.data.MegaChunk;
import org.terraform.data.TerraformWorld;

public class CheckHeightCommand extends DCCommand {

	public CheckHeightCommand(DrycellPlugin plugin, String... aliases) {
		super(plugin, aliases);
	}

	@Override
	public String getDefaultDescription() {
		return "Checks the heights of various noise maps";
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
		int x = p.getLocation().getBlockX();
		int z = p.getLocation().getBlockZ();
		
		HeightMap map = new HeightMap();
		TerraformWorld tw = TerraformWorld.get(p.getWorld());
		int h = map.getHeight(tw, x, z);
		double rd = tw.getRiverDepth(x, z);
		p.sendMessage("Core Height: " + map.getCoreHeight(tw, x, z));
		p.sendMessage("Mountainous Height: " + map.getMountainousHeight(tw, x, z));
		p.sendMessage("Attrition Height: " + map.getAttritionHeight(tw, x, z));
		p.sendMessage("Temperature: " + tw.getTemperature(x, z));
		p.sendMessage("Moisture: " + tw.getMoisture(x, z));
		p.sendMessage("Result height: " + h);
		p.sendMessage("River Depth: " + rd);
		p.sendMessage("Mega Chunk: " + new MegaChunk(x,0,z).getX() + "," + new MegaChunk(x,0,z).getZ());
		p.sendMessage("Result Biome: " + tw.getBiomeBank(x, h, z));
		
	}

}
