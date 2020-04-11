package org.terraform.command;

import java.util.Stack;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.drycell.command.DCCommand;
import org.drycell.command.InvalidArgumentException;
import org.drycell.main.DrycellPlugin;
import org.terraform.coregen.PopulatorDataPostGen;
import org.terraform.data.TerraformWorld;
import org.terraform.tree.FractalTreeBuilder;
import org.terraform.tree.FractalTreeType;

public class FractalTreeCommand extends DCCommand {

	public FractalTreeCommand(DrycellPlugin plugin, String... aliases) {
		super(plugin, aliases);
		this.parameters.add(new FractalTreeTypeArgument("type",false));
	}

	@Override
	public String getDefaultDescription() {
		return "Spawns a fractal tree";
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
		try{
			new FractalTreeBuilder((FractalTreeType) this.parseArguments(sender, args).get(0))
			.build(tw, data, x, y, z);
		}catch(IllegalArgumentException e){
			sender.sendMessage(ChatColor.RED + "Invalid tree type.");
			sender.sendMessage(ChatColor.RED + "Valid types:");
			String types = "";
			boolean b = true;
			for(FractalTreeType type:FractalTreeType.values()){
				ChatColor col = ChatColor.RED;
				if(b) col = ChatColor.DARK_RED;
				b = !b;
				types += col + type.toString() + " ";
			}
			
			sender.sendMessage(types);
		}
	}

}
