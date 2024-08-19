package org.terraform.command.contants;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.terraform.data.SimpleBlock;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.schematic.TerraSchematic;

import java.io.FileNotFoundException;

public class SchematicArgument extends TerraCommandArgument<TerraSchematic> {

    public SchematicArgument(String name, boolean isOptional) {
        super(name, isOptional);
    }

    @Override
    public TerraSchematic parse(@NotNull CommandSender sender, String value) {

        try {
            return TerraSchematic.load(value, new SimpleBlock(((Player) sender).getLocation()));
        }
        catch (FileNotFoundException e) {
            TerraformGeneratorPlugin.logger.stackTrace(e);
        }
        return null;
    }

    @Override
    public @NotNull String validate(@NotNull CommandSender sender, String value) {
        try {
            // Path validation is done inside TerraSchematic.load
            TerraSchematic.load(value, new SimpleBlock(((Player) sender).getLocation()));
        }
        catch (Throwable e) {
            TerraformGeneratorPlugin.logger.stackTrace(e);
            return "Problem loading schematic. Check console for error";
        }
        return "";
    }

}
