package org.terraform.command.contants;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.terraform.tree.FractalTypes;
import org.terraform.tree.FractalTypes.Tree;

import java.util.ArrayList;
import java.util.Locale;

public class FractalTreeTypeArgument extends TerraCommandArgument<FractalTypes.Tree> {

    public FractalTreeTypeArgument(String name, boolean isOptional) {
        super(name, isOptional);
        // TODO Auto-generated constructor stub
    }

    @Override
    public FractalTypes.@NotNull Tree parse(CommandSender sender, @NotNull String value) {

        return FractalTypes.Tree.valueOf(value.toUpperCase(Locale.ENGLISH));
    }

    @Override
    public @NotNull String validate(CommandSender sender, @NotNull String value) {
        try {
            parse(sender, value);
        }
        catch (IllegalArgumentException e) {
            return "Tree type does not exist!";
        }
        return "";
    }

    @Override
    public @NotNull ArrayList<String> getTabOptions(String @NotNull [] args) {
        if (args.length != 2) {
            return new ArrayList<>();
        }
        ArrayList<String> values = new ArrayList<>();

        for (Tree type : FractalTypes.Tree.values()) {
            if (type.toString().startsWith(args[1].toUpperCase(Locale.ENGLISH))) {
                values.add(type.toString());
            }
        }

        return values;
    }

}
