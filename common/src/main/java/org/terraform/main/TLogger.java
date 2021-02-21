package org.terraform.main;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;

public class TLogger {
	
	private JavaPlugin plugin;
    public TLogger(JavaPlugin plugin) {
    	this.plugin = plugin;
    }

    public void error(String message) {
    	Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[TerraformGenerator][!] "
    + ChatColor.translateAlternateColorCodes('&', message));
    }

    public void info(String message) {
    	Bukkit.getConsoleSender().sendMessage("[TerraformGenerator] "
    + ChatColor.translateAlternateColorCodes('&', message));
    }
    
    public void debug(String message) {
        if (TConfigOption.DEVSTUFF_DEBUG_MODE.getBoolean())
        	Bukkit.getConsoleSender().sendMessage("[TerraformGenerator][v] "
        		    + ChatColor.translateAlternateColorCodes('&', message));
    }

    public void logToFile(String message) {
    	//TODO: Make file logging.
    }
}
