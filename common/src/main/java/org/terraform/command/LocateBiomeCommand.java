package org.terraform.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.terraform.biome.BiomeBank;
import org.terraform.command.contants.InvalidArgumentException;
import org.terraform.command.contants.TerraCommand;
import org.terraform.command.contants.TerraCommandArgument;
import org.terraform.data.TerraformWorld;
import org.terraform.main.LangOpt;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.utils.GenUtils;
import org.terraform.utils.Vector2f;

import java.util.Stack;
import java.util.UUID;

public class LocateBiomeCommand extends TerraCommand {

    public LocateBiomeCommand(TerraformGeneratorPlugin plugin, String... aliases) {
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
                new Task(
                        p.getUniqueId(),
                        TerraformWorld.get(p.getWorld()),
                        p.getLocation().getBlockX(),
                        p.getLocation().getBlockZ(),
                        (BiomeBank) this.parseArguments(sender, args).get(0))
                        .runTaskAsynchronously(TerraformGeneratorPlugin.get());
            } catch (IllegalArgumentException e) {
                sender.sendMessage(LangOpt.COMMAND_LOCATEBIOME_INVALIDBIOME.parse());

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

    private void syncSendMessage(UUID uuid, String message) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getUniqueId() == uuid) {
                p.sendMessage(message);
                break;
            }
        }
        TerraformGeneratorPlugin.logger.info("[Locate Command] " + message);
    }

    private static class LocateBiomeTypeArgument extends TerraCommandArgument<BiomeBank> {
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

    private class Task extends BukkitRunnable {
        UUID p;
        BiomeBank b;
        TerraformWorld tw;
        int x;
        int z;

        public Task(UUID player, TerraformWorld tw, int x, int z, BiomeBank targetBiome) {
            this.p = player;
            this.b = targetBiome;
            this.tw = tw;
            this.x = x;
            this.z = z;
        }

        @Override
        public void run() {
            Vector2f location = GenUtils.locateBiome(tw, b,
                    new Vector2f(x, z), 5000, 25);

            if (location == null)
                syncSendMessage(p, LangOpt.COMMAND_LOCATEBIOME_NOT_IN_5000.parse());
            else
                syncSendMessage(p, LangOpt.COMMAND_LOCATE_LOCATE_COORDS.parse("%x%", location.x + "", "%z%", location.y + ""));
        }
    }

}
