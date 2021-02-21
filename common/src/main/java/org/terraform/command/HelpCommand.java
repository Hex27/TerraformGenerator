package org.terraform.command;

import java.util.ArrayList;
import java.util.Stack;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.terraform.command.contants.InvalidArgumentException;
import org.terraform.command.contants.TerraCommand;
import org.terraform.command.contants.TerraCommandArgument;
import org.terraform.main.TerraformCommandManager;
import org.terraform.main.TerraformGeneratorPlugin;

public class HelpCommand extends TerraCommand {
	
	private TerraformCommandManager man;
	public HelpCommand(TerraformGeneratorPlugin plugin, TerraformCommandManager man, String... string){
		super(plugin, string);
		this.man = man;
	}
	
	@Override
	public boolean isInAcceptedParamRange(Stack<String> args){
		return args.size() <= 1;
	}

	@Override
	public boolean canConsoleExec() {
		return true;
	}

	@Override
	public boolean hasPermission(CommandSender sender) {
		return true;
	}

	@Override
	public void execute(CommandSender sender, Stack<String> args)
			throws InvalidArgumentException {
		ArrayList<TerraCommand> cmds = new ArrayList<>();
		for(TerraCommand cmd:man.getCommands()){
			if(cmd.hasPermission(sender)) cmds.add(cmd);
		}
		int maxPages =  (int) Math.ceil(cmds.size()/6);
		int page = 0;
		
		if(args.size() > 0){
			try{
				page = Integer.parseInt(args.pop());
				if(page <= 0){
					sender.sendMessage(plugin.getLang().fetchLang("command.help.postive-pages"));
					return;
				}
				page--;
			}catch(NumberFormatException e){
				sender.sendMessage(plugin.getLang().fetchLang("command.help.postive-pages"));
				return;
			}
		}
		if(page > maxPages){
			page = maxPages;
		}
		String base = man.bases.get(0);
		sender.sendMessage(ChatColor.GOLD + "============[" + ChatColor.AQUA + plugin.getName() + ChatColor.GOLD + "][" + ChatColor.YELLOW + "Pg. " + (page + 1) + ChatColor.GOLD +
                "]============");
		sender.sendMessage("");
        for (int i = 0; i < 6; i++) {
            if (cmds.size() > page * 5 + i) {
            	TerraCommand cmd = cmds.get(page * 5 + i);
                String subCmd = ChatColor.YELLOW + "/" + base + " " + String.join("/", cmd.aliases);
				String params = " ";
				for(TerraCommandArgument<?> param:cmd.parameters){
					if(param.isOptional()){
						params += ChatColor.GRAY + "<" + param.getName() + "> ";
					}else
						params += ChatColor.AQUA + "[" + param.getName() + "] ";
				}
				sender.sendMessage(subCmd + params + ChatColor.DARK_GRAY + "- " + ChatColor.YELLOW + plugin.getLang().fetchLang(cmd.getLangPath()));
            }
        }
		sender.sendMessage("");
        if (page != maxPages) {
        	sender.sendMessage(ChatColor.GRAY + "/" + base + " h " + (page+2) 
    				+ "" + ChatColor.DARK_GRAY + "- " + ChatColor.YELLOW + (page+1) + "/" + (maxPages+1));
        } else {
        	sender.sendMessage(ChatColor.AQUA + "" + (page+1) + "/" + (maxPages+1));
        }
	}

	@Override
	public String getDefaultDescription() {
		return "Displays a list of commands for this plugin.";
	}

}