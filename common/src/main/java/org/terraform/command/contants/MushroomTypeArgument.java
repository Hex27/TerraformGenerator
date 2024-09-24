package org.terraform.command.contants;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.terraform.tree.FractalTypes;
import org.terraform.tree.FractalTypes.Mushroom;

import java.util.ArrayList;
import java.util.Locale;

public class MushroomTypeArgument extends TerraCommandArgument<FractalTypes.Mushroom> {

    public MushroomTypeArgument(String name, boolean isOptional) {
        super(name, isOptional);
        // TODO Auto-generated constructor stub
    }

    @Override
    public FractalTypes.@NotNull Mushroom parse(CommandSender sender, @NotNull String value) {

        return FractalTypes.Mushroom.valueOf(value.toUpperCase(Locale.ENGLISH));
    }

    @Override
    public @NotNull String validate(CommandSender sender, @NotNull String value) {
        try {
            parse(sender, value);
        }
        catch (IllegalArgumentException e) {
            return "Mushroom type does not exist!";
        }
        return "";
    }


    @Override
    public @NotNull ArrayList<String> getTabOptions(String @NotNull [] args) {
        if (args.length != 2) {
            return new ArrayList<>();
        }
        ArrayList<String> values = new ArrayList<>();

        for (Mushroom type : FractalTypes.Mushroom.values()) {
            if (type.toString().startsWith(args[1].toUpperCase(Locale.ENGLISH))) {
                values.add(type.toString());
            }
        }

        return values;
    }

}
