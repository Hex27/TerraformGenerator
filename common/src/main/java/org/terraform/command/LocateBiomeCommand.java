package org.terraform.command;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeType;
import org.terraform.command.contants.InvalidArgumentException;
import org.terraform.command.contants.TerraCommand;
import org.terraform.command.contants.TerraCommandArgument;
import org.terraform.data.TerraformWorld;
import org.terraform.main.LangOpt;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.utils.GenUtils;
import org.terraform.utils.Vector2f;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Stack;
import java.util.UUID;

public class LocateBiomeCommand extends TerraCommand {

    public LocateBiomeCommand(TerraformGeneratorPlugin plugin, String... aliases) {
        super(plugin, aliases);
        this.parameters.add(new LocateBiomeTypeArgument("biomeType", false));
    }

    @Override
    public @NotNull String getDefaultDescription() {
        return "Tries to locate a certain biome";
    }

    @Override
    public boolean canConsoleExec() {
        return false;
    }

    @Override
    public boolean hasPermission(@NotNull CommandSender sender) {

        return sender.isOp() || sender.hasPermission("terraformgenerator.locatebiome");
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull Stack<String> args) throws InvalidArgumentException {
        Player p = (Player) sender;

        if (!args.isEmpty()) {
            try {
                new Task(
                        p.getUniqueId(),
                        TerraformWorld.get(p.getWorld()),
                        p.getLocation().getBlockX(),
                        p.getLocation().getBlockZ(),
                        (BiomeBank) this.parseArguments(sender, args).get(0)
                ).runTaskAsynchronously(TerraformGeneratorPlugin.get());
            }
            catch (IllegalArgumentException e) {
                sender.sendMessage(LangOpt.COMMAND_LOCATEBIOME_INVALIDBIOME.parse());

                StringBuilder types = new StringBuilder();
                boolean b = true;

                for (BiomeBank type : BiomeBank.values()) {
                    ChatColor col = ChatColor.RED;
                    if (b) {
                        col = ChatColor.DARK_RED;
                    }
                    b = !b;
                    types.append(col).append(type).append(' ');
                }

                sender.sendMessage(types.toString());
            }
        }
    }

    private void syncSendMessage(UUID uuid, String msg) {
        super.syncSendMessage(uuid, "Locate Biome", msg);
    }

    private void syncSendMessage(UUID uuid, String msg, int x, int y, int z) {
        super.syncSendMessageTP(uuid, "Locate Biome", msg, x, y, z);
    }

    private static class LocateBiomeTypeArgument extends TerraCommandArgument<BiomeBank> {
        public LocateBiomeTypeArgument(String name, boolean isOptional) {
            super(name, isOptional);
            // TODO Auto-generated constructor stub
        }

        @Override
        public @NotNull BiomeBank parse(CommandSender sender, @NotNull String value) {

            return BiomeBank.valueOf(value.toUpperCase(Locale.ENGLISH));
        }

        @Override
        public @NotNull String validate(CommandSender sender, @NotNull String value) {
            try {
                parse(sender, value);
            }
            catch (IllegalArgumentException e) {
                return "That biome type does not exist!";
            }
            return "";
        }

        @Override
        public @NotNull ArrayList<String> getTabOptions(String @NotNull [] args) {
            if (args.length != 2) {
                return new ArrayList<>();
            }
            ArrayList<String> values = new ArrayList<>();

            for (BiomeBank bank : BiomeBank.values()) {
                if (bank.name().startsWith(args[1].toUpperCase(Locale.ENGLISH))) {
                    values.add(bank.name());
                }
            }

            return values;
        }
    }

    private class Task extends BukkitRunnable {
        final UUID p;
        final BiomeBank b;
        final TerraformWorld tw;
        final int x;
        final int z;

        public Task(UUID player, TerraformWorld tw, int x, int z, BiomeBank targetBiome) {
            this.p = player;
            this.b = targetBiome;
            this.tw = tw;
            this.x = x;
            this.z = z;
        }

        @Override
        public void run() {
            Vector2f location;
            if (b.getType() == BiomeType.BEACH || b.getType() == BiomeType.RIVER) {
                location = GenUtils.locateHeightDependentBiome(tw, b, new Vector2f(x, z), 5000, 25);
                if (location == null) {
                    syncSendMessage(p, LangOpt.COMMAND_LOCATEBIOME_NOT_IN_5000.parse());
                }
            }
            else {
                location = GenUtils.locateHeightIndependentBiome(tw, b, new Vector2f(x, z));
            }

            if (location != null) {
                int x = (int) location.x;
                int z = (int) location.y;

                final String message = LangOpt.COMMAND_LOCATE_LOCATE_COORDS.parse("%x%", x + "", "%z%", z + "");

                syncSendMessage(p, message, x, getHighestY(tw, x, z), z);
            }
            else {
                syncSendMessage(p, LangOpt.COMMAND_LOCATEBIOME_DISABLED.parse());
            }

        }
    }
}