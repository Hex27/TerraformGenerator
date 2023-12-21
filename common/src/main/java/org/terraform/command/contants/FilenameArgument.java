package org.terraform.command.contants;

import org.bukkit.command.CommandSender;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.schematic.TerraSchematic;

import java.io.File;

public class FilenameArgument extends TerraCommandArgument<String> {
    public FilenameArgument(String name, boolean isOptional) {
        super(name, isOptional);
    }

    @Override
    public String parse(CommandSender sender, String value) {
        return value;
    }

    @Override
    public String validate(CommandSender sender, String value) {
        File schematicFolder = new File(TerraformGeneratorPlugin.get().getDataFolder(), TerraSchematic.SCHEMATIC_FOLDER);
        File file = new File(schematicFolder, value);
        try {
            if(file.getName().endsWith(File.pathSeparator)
                    || !file.getCanonicalPath().startsWith(schematicFolder.getCanonicalPath()))
                return "Schematic name contained illegal characters (i.e. periods)";
        } catch(Exception e) {
            return "Schematic name contained illegal characters (i.e. periods)";
        }
        return "";
    }
}
