package org.terraform.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.terraform.biome.flat.DesertHandler;
import org.terraform.command.contants.InvalidArgumentException;
import org.terraform.command.contants.TerraCommand;
import org.terraform.coregen.populatordata.PopulatorDataPostGen;
import org.terraform.data.SimpleBlock;
import org.terraform.main.TerraformGeneratorPlugin;

import java.util.Random;
import java.util.Stack;

public class RibCageCommand extends TerraCommand {

    public RibCageCommand(TerraformGeneratorPlugin plugin, String... aliases) {
        super(plugin, aliases);
    }

    @Override
    public String getDefaultDescription() {
        return "Spawns a rib cage";
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
        new DesertHandler().spawnRibCage(new Random(), new SimpleBlock(data,x,y,z));
    }

}
