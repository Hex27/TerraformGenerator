package org.terraform.command.contants;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

public class IntegerArgument extends TerraCommandArgument<Integer>{
    public IntegerArgument(String name, boolean isOptional) {
        super(name, isOptional);
    }

    @Override
    public @Nullable Integer parse(CommandSender sender, String value) {
        try{
            return Integer.parseInt(value);
        }catch(NumberFormatException e){
            return 0;
        }
    }

    @Override
    public String validate(CommandSender sender, String value) {
        try{
            Integer.parseInt(value);
            return "";
        }catch(NumberFormatException e){
            return value + " is not an integer";
        }
    }
}
