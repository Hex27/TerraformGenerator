package org.terraform.command;

import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.entity.Player;
import org.drycell.command.DCCommand;
import org.drycell.command.InvalidArgumentException;
import org.drycell.main.DrycellPlugin;
import org.terraform.coregen.PopulatorDataPostGen;
import org.terraform.coregen.TerraformPopulator;
import org.terraform.data.TerraformWorld;

import java.util.Stack;

public class PopulateCommand extends DCCommand {

    public PopulateCommand(DrycellPlugin plugin, String... aliases) {
        super(plugin, aliases);
    }

    @Override
    public String getDefaultDescription() {
        return "Populates the chunk that you are in.";
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
        TerraformWorld tw = TerraformWorld.get(p.getWorld());
        new TerraformPopulator(tw).populate(tw, ((CraftWorld) p.getWorld()).getHandle().getRandom(), data);
    }
}
