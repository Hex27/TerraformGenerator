package org.terraform.command;

import org.bukkit.command.CommandSender;
import org.drycell.command.DCArgument;
import org.terraform.tree.FractalTypes;

public class FractalTreeTypeArgument extends DCArgument<FractalTypes.Tree> {

    public FractalTreeTypeArgument(String name, boolean isOptional) {
        super(name, isOptional);
        // TODO Auto-generated constructor stub
    }

    @Override
    public FractalTypes.Tree parse(CommandSender sender, String value) {

        return FractalTypes.Tree.valueOf(value.toUpperCase());
    }

    @Override
    public String validate(CommandSender sender, String value) {
        try {
            parse(sender, value);
        } catch(IllegalArgumentException e) {
            return "Tree type does not exist!";
        }
        return "";
    }

}
