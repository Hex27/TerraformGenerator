package org.terraform.command;

import java.util.HashMap;
import java.util.Random;
import java.util.Stack;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.block.data.type.Wall;
import org.bukkit.command.CommandSender;
import org.bukkit.util.noise.SimplexOctaveGenerator;
import org.drycell.command.DCCommand;
import org.drycell.command.InvalidArgumentException;
import org.drycell.main.DrycellPlugin;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeGrid;
import org.terraform.biome.BiomeType;
import org.terraform.data.TerraformWorld;
import org.terraform.utils.FastNoise;
import org.terraform.utils.FastNoise.NoiseType;
import org.terraform.utils.GenUtils;

public class BlockDataTestCommand extends DCCommand {

	public BlockDataTestCommand(DrycellPlugin plugin, String... aliases) {
		super(plugin, aliases);
	}

	@Override
	public String getDefaultDescription() {
		return "Shows some new blockdata values in 1.16";
	}

	@Override
	public boolean canConsoleExec() {
		return true;
	}

	@Override
	public boolean hasPermission(CommandSender sender) {
		
		return sender.isOp();
	}
	
	@Override
	public void execute(CommandSender sender, Stack<String> args)
			throws InvalidArgumentException {
		Wall facing = (Wall) Bukkit.createBlockData(Material.COBBLESTONE_WALL);
		sender.sendMessage(facing.getAsString());
	}
	

}
