package org.terraform.command.contants;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Stack;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.terraform.main.TerraformGeneratorPlugin;

public abstract class TerraCommand {
	
	public  @NotNull ArrayList<String> aliases = new ArrayList<>();
	public  @NotNull ArrayList<TerraCommandArgument<?>> parameters = new ArrayList<>();
	public TerraformGeneratorPlugin plugin;
	public TerraCommand(TerraformGeneratorPlugin plugin, String @NotNull ... aliases){
		this.plugin = plugin;
        this.aliases.addAll(Arrays.asList(aliases));
	}
	
	public abstract String getDefaultDescription();
	
	public boolean isInAcceptedParamRange(@NotNull Stack<String> args){
		if(args.size() > this.parameters.size()) return false;
		if(this.parameters.isEmpty()) return true;
		int lowerBound = 0;
		for(TerraCommandArgument<?> arg:parameters){
			if(!arg.isOptional()) lowerBound++;
		}
		return args.size() >= lowerBound;
	}
	
	public @NotNull String getLangPath(){
		return "command." + aliases.get(0) + ".desc";
	}
	public abstract boolean canConsoleExec();
	
	public abstract boolean hasPermission(CommandSender sender);
	
	public abstract void execute(CommandSender sender, Stack<String> args) throws InvalidArgumentException;
	
	/**
	 * Call this method to parse an arraylist of objects parsed by the argument handler
	 * @param sender
	 * @param args
	 * @return
	 * @throws InvalidArgumentException
	 */
	public @NotNull ArrayList<Object> parseArguments(CommandSender sender, @NotNull Stack<String> args) throws InvalidArgumentException{
		ArrayList<Object> items = new ArrayList<>(args.size());
		
		int i = 0;
		while(!args.isEmpty()){
			String arg = args.pop();
			TerraCommandArgument<?> parser = parameters.get(i);
			Object parsed = parser.parse(sender, arg);
			String val = parser.validate(sender, arg);
			if(parsed == null) throw new InvalidArgumentException(val);
			if(!val.isEmpty()) throw new InvalidArgumentException(val);
			items.add(i, parsed);
			i++;
		}
		return items;
	}
	
	public @Nullable String getNextArg(@NotNull Stack<String> args){
		if(args.empty()) return null;
		return args.pop();
	}
	
	public boolean matchCommand(String command){
		command = command.toLowerCase(Locale.ENGLISH);
		return aliases.contains(command);
	}

}