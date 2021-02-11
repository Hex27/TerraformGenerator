package org.terraform.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.drycell.command.DCCommand;
import org.drycell.command.InvalidArgumentException;
import org.drycell.main.DrycellPlugin;
import org.terraform.coregen.PopulatorDataPostGen;
import org.terraform.structure.village.plains.PlainsVillagePopulator;
import org.terraform.structure.village.plains.forge.PlainsVillageForgeJigsawBuilder;

import java.util.Random;
import java.util.Stack;

public class JigsawBuilderTestCommand extends DCCommand {

    public JigsawBuilderTestCommand(DrycellPlugin plugin, String... aliases) {
        super(plugin, aliases);
    }

    @Override
    public String getDefaultDescription() {
        return "Jigsaw Build Test";
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
        PlainsVillageForgeJigsawBuilder builder = new PlainsVillageForgeJigsawBuilder(
                new PlainsVillagePopulator(),
        		15, 15, data, x, y - 1, z
        );
        builder.generate(new Random());
        builder.build(new Random());
        p.sendMessage("Complete.");
    }

}
