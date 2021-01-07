package org.terraform.command;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.drycell.command.DCArgument;
import org.drycell.command.DCCommand;
import org.drycell.command.InvalidArgumentException;
import org.drycell.main.DrycellPlugin;
import org.terraform.biome.BiomeBank;
import org.terraform.data.TerraformWorld;
import org.terraform.main.LangOpt;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.utils.GenUtils;
import org.terraform.utils.Vector2f;

import java.util.Stack;

public class LocateBiomeCommand extends DCCommand {

    public LocateBiomeCommand(DrycellPlugin plugin, String... aliases) {
        super(plugin, aliases);
        this.parameters.add(new LocateBiomeTypeArgument("biomeType", false));
    }

    @Override
    public String getDefaultDescription() {
        return "Tries to locate a certain biome";
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

        if (args.size() != 0) {
            try {
                new Task(p, (BiomeBank) this.parseArguments(sender, args).get(0)).runTaskAsynchronously(TerraformGeneratorPlugin.get());
            } catch (IllegalArgumentException e) {
                sender.sendMessage(ChatColor.RED + "Invalid biome.");
                sender.sendMessage(ChatColor.RED + "Valid types:");

                StringBuilder types = new StringBuilder();
                boolean b = true;

                for (BiomeBank type : BiomeBank.VALUES) {
                    ChatColor col = ChatColor.RED;
                    if (b) col = ChatColor.DARK_RED;
                    b = !b;
                    types.append(col).append(type).append(' ');
                }

                sender.sendMessage(types.toString());
            }
        }
    }

    private class Task extends BukkitRunnable {
        Player p;
        BiomeBank b;

        public Task(Player player, BiomeBank targetBiome) {
            this.p = player;
            this.b = targetBiome;
        }

        @Override
        public void run() {
            Vector2f location = GenUtils.locateBiome(TerraformWorld.get(p.getWorld()), b,
                    new Vector2f(p.getLocation().getBlockX(), p.getLocation().getBlockZ()), 2500, 25);

            if (location == null) p.sendMessage("Could not find that biome nearby.");
            else p.sendMessage(ChatColor.GREEN + LangOpt.COMMAND_LOCATE_LOCATE_COORDS.parse("%x%", location.x + "", "%z%", location.y + ""));
        }
    }

    private static class LocateBiomeTypeArgument extends DCArgument<BiomeBank> {
        public LocateBiomeTypeArgument(String name, boolean isOptional) {
            super(name, isOptional);
            // TODO Auto-generated constructor stub
        }

        @Override
        public BiomeBank parse(CommandSender sender, String value) {

            return BiomeBank.valueOf(value.toUpperCase());
        }

        @Override
        public String validate(CommandSender sender, String value) {
            try {
                parse(sender, value);
            } catch (IllegalArgumentException e) {
                return "That biome type does not exist!";
            }
            return "";
        }

    }

}
