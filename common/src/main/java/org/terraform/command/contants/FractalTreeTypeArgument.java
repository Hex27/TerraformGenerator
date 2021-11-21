package org.terraform.command.contants;

import java.util.ArrayList;

import org.bukkit.command.CommandSender;
import org.terraform.tree.FractalTypes;
import org.terraform.tree.FractalTypes.Tree;

public class FractalTreeTypeArgument extends TerraCommandArgument<FractalTypes.Tree> {

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
        } catch (IllegalArgumentException e) {
            return "Tree type does not exist!";
        }
        return "";
    }
    
    @Override
    public ArrayList<String> getTabOptions(String[] args) {
        if (args.length != 2) return new ArrayList<>();
        ArrayList<String> values = new ArrayList<>();

//        for (StructurePopulator spop : StructureRegistry.getAllPopulators()) {
//        	if(spop.getClass().getSimpleName().toUpperCase().startsWith(args[1].toUpperCase()))
//        		values.add(spop.getClass().getSimpleName());
//        }
        
        for(Tree type: FractalTypes.Tree.values()) {
        	if(type.toString().startsWith(args[1].toUpperCase()))
        		values.add(type.toString());
        }
        
        return values;
    }

}
