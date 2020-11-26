package org.terraform.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.drycell.command.DCCommand;
import org.drycell.command.InvalidArgumentException;
import org.drycell.main.DrycellPlugin;
import org.terraform.coregen.PopulatorDataPostGen;
import org.terraform.data.TerraformWorld;
import org.terraform.structure.small.GiantPumpkinPopulator;

import java.util.Random;
import java.util.Stack;

public class GiantPumpkinCommand extends DCCommand {

    public GiantPumpkinCommand(DrycellPlugin plugin, String... aliases) {
        super(plugin, aliases);
    }

    @Override
    public String getDefaultDescription() {
        return "Spawns a giant pumpkin patch";
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
        new GiantPumpkinPopulator().spawnGiantPumpkin(TerraformWorld.get(p.getWorld()), new Random(), data, x, y, z);
    }
}
