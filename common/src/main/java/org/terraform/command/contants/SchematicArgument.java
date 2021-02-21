package org.terraform.command.contants;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.terraform.schematic.TerraSchematic;

import java.io.FileNotFoundException;

public class SchematicArgument extends TerraCommandArgument<TerraSchematic> {

    public SchematicArgument(String name, boolean isOptional) {
        super(name, isOptional);
        // TODO Auto-generated constructor stub
    }

    @Override
    public TerraSchematic parse(CommandSender sender, String value) {

        try {
            return TerraSchematic.load(value, ((Player) sender).getLocation());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String validate(CommandSender sender, String value) {
        try {
            TerraSchematic.load(value, ((Player) sender).getLocation());
        } catch (Throwable e) {
            e.printStackTrace();
            return "Problem loading schematic. Check console for error";
        }
        return "";
    }

}
