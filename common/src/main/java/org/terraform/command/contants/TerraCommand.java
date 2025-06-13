package org.terraform.command.contants;


import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;

import java.util.*;

public abstract class TerraCommand {

    public final @NotNull ArrayList<String> aliases = new ArrayList<>();
    public final @NotNull ArrayList<TerraCommandArgument<?>> parameters = new ArrayList<>();
    public final TerraformGeneratorPlugin plugin;

    public TerraCommand(TerraformGeneratorPlugin plugin, String @NotNull ... aliases) {
        this.plugin = plugin;
        this.aliases.addAll(Arrays.asList(aliases));
    }

    public abstract String getDefaultDescription();

    public boolean isInAcceptedParamRange(@NotNull Stack<String> args) {
        if (args.size() > this.parameters.size()) {
            return false;
        }
        if (this.parameters.isEmpty()) {
            return true;
        }
        int lowerBound = 0;
        for (TerraCommandArgument<?> arg : parameters) {
            if (!arg.isOptional()) {
                lowerBound++;
            }
        }
        return args.size() >= lowerBound;
    }

    public @NotNull String getLangPath() {
        return "command." + aliases.get(0) + ".desc";
    }

    public abstract boolean canConsoleExec();

    public abstract boolean hasPermission(CommandSender sender);

    public abstract void execute(CommandSender sender, Stack<String> args) throws InvalidArgumentException;

    /**
     * Call this method to parse an arraylist of objects parsed by the argument handler
     */
    public @NotNull ArrayList<Object> parseArguments(CommandSender sender, @NotNull Stack<String> args)
            throws InvalidArgumentException
    {
        ArrayList<Object> items = new ArrayList<>(args.size());

        int i = 0;
        while (!args.isEmpty()) {
            String arg = args.pop();
            TerraCommandArgument<?> parser = parameters.get(i);
            Object parsed = parser.parse(sender, arg);
            String val = parser.validate(sender, arg);
            if (parsed == null) {
                throw new InvalidArgumentException(val);
            }
            if (!val.isEmpty()) {
                throw new InvalidArgumentException(val);
            }
            items.add(i, parsed);
            i++;
        }
        return items;
    }

    public @Nullable String getNextArg(@NotNull Stack<String> args) {
        if (args.empty()) {
            return null;
        }
        return args.pop();
    }

    public boolean matchCommand(String command) {
        command = command.toLowerCase(Locale.ENGLISH);
        return aliases.contains(command);
    }

    protected void syncSendMessage(final @NotNull UUID uuid,
                                   final @NotNull String prefix,
                                   final @NotNull String message)
    {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getUniqueId().equals(uuid)) {
                p.sendMessage(message);
                break;
            }
        }
        TerraformGeneratorPlugin.logger.info('[' + prefix + "] " + message);
    }

    protected void syncSendMessageTP(final @NotNull UUID uuid,
                                     final @NotNull String prefix,
                                     final @NotNull String message,
                                     int x,
                                     int y,
                                     int z)
    {
        TextComponent chatMsg = new TextComponent(message);
        chatMsg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tp " + x + " " + y + " " + z));

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getUniqueId().equals(uuid)) {
                p.spigot().sendMessage(chatMsg);
                break;
            }
        }
        TerraformGeneratorPlugin.logger.info('[' + prefix + "] " + message);
    }

    protected int getHighestY(@NotNull TerraformWorld tw, int x, int z) {
        final World world = tw.getWorld();
        for (int y = tw.maxY; y > tw.minY; y--) {
            if (!world.getBlockAt(x, y, z).getType().isAir()) {
                return y + 1;
            }
        }

        return tw.minY;
    }
}