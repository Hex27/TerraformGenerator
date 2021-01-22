package org.terraform.command;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.drycell.command.DCCommand;
import org.drycell.command.InvalidArgumentException;
import org.drycell.main.DrycellPlugin;
import org.terraform.coregen.PopulatorDataPostGen;
import org.terraform.data.TerraformWorld;
import org.terraform.tree.FractalTypes;
import org.terraform.tree.MushroomBuilder;

import java.util.Random;
import java.util.Stack;

public class MushroomCommand extends DCCommand {

    public MushroomCommand(DrycellPlugin plugin, String... aliases) {
        super(plugin, aliases);
        this.parameters.add(new MushroomTypeArgument("type", true));
    }

    @Override
    public String getDefaultDescription() {
        return "Spawns a giant mushroom (red or brown)";
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
        TerraformWorld tw = TerraformWorld.get(p.getWorld());

        if(args.size() != 0) {
            try {
                new MushroomBuilder((FractalTypes.Mushroom) this.parseArguments(sender, args).get(0))
                        .build(tw, data, x, y, z);
            } catch(IllegalArgumentException e) {
                sender.sendMessage(ChatColor.RED + "Invalid mushroom type.");
                sender.sendMessage(ChatColor.RED + "Valid types:");
                StringBuilder types = new StringBuilder();
                boolean b = true;
                for(FractalTypes.Mushroom type : FractalTypes.Mushroom.VALUES) {
                    ChatColor col = ChatColor.RED;
                    if(b) col = ChatColor.DARK_RED;
                    b = !b;
                    types.append(col).append(type).append(' ');
                }

                sender.sendMessage(types.toString());
            }

            return;
        }

        if(new Random().nextBoolean())
            new MushroomBuilder(FractalTypes.Mushroom.GIANT_RED_MUSHROOM).build(TerraformWorld.get(p.getWorld()), data, x, y, z);
        else
            new MushroomBuilder(FractalTypes.Mushroom.GIANT_BROWN_MUSHROOM).build(TerraformWorld.get(p.getWorld()), data, x, y, z);
    }

}
