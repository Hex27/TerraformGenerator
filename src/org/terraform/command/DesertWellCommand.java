package org.terraform.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.drycell.command.DCCommand;
import org.drycell.command.InvalidArgumentException;
import org.drycell.main.DrycellPlugin;
import org.terraform.coregen.PopulatorDataPostGen;
import org.terraform.data.TerraformWorld;
import org.terraform.structure.small.DesertWellPopulator;

import java.util.Random;
import java.util.Stack;

public class DesertWellCommand extends DCCommand {

    public DesertWellCommand(DrycellPlugin plugin, String... aliases) {
        super(plugin, aliases);
    }

    @Override
    public String getDefaultDescription() {
        return "Spawns a desert well";
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

        new DesertWellPopulator().spawnDesertWell(TerraformWorld.get(p.getWorld()), new Random(), data,
                p.getLocation().getBlockX(),
                p.getLocation().getBlockY(),
                p.getLocation().getBlockZ(),
                new Random().nextBoolean());

    }

}
