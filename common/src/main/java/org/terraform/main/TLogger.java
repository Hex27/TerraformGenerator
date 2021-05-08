package org.terraform.main;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.terraform.main.config.TConfigOption;


public class TLogger {
	
	private static final Logger LOGGER = Logger.getLogger("TerraformGenerator");
    public TLogger() {
    	if(TConfigOption.DEVSTUFF_SUPPRESS_CONSOLE_LOGS.getBoolean()) {
            Handler consoleHandler = null;
            Handler fileHandler  = null;
            try{
                //Creating consoleHandler and fileHandler
                consoleHandler = new ConsoleHandler();
                fileHandler  = new FileHandler("./plugins/TerraformGenerator/terraform.log");
                 
                //Assigning handlers to LOGGER object
                LOGGER.addHandler(consoleHandler);
                LOGGER.addHandler(fileHandler);
                
                //No stdout
                consoleHandler.setLevel(Level.OFF);
                fileHandler.setLevel(Level.ALL);
                LOGGER.setLevel(Level.ALL);
                 
                LOGGER.config("Configuration done.");
                 
                //Console handler removed
                LOGGER.removeHandler(consoleHandler);
                 
                LOGGER.log(Level.FINE, "Finer logged");
            }catch(IOException exception){
                LOGGER.log(Level.SEVERE, "Error occur in FileHandler.", exception);
            }
    	}
    }
    
    public void stdout(String message) {
    	Bukkit.getConsoleSender().sendMessage("[TerraformGenerator] "
    		+ ChatColor.translateAlternateColorCodes('&', message));
    	
    	if(TConfigOption.DEVSTUFF_SUPPRESS_CONSOLE_LOGS.getBoolean()) {
    		LOGGER.log(Level.INFO," " + message);
    	}
    }

    public void error(String message) {
    	if(TConfigOption.DEVSTUFF_SUPPRESS_CONSOLE_LOGS.getBoolean()) {
    		LOGGER.log(Level.SEVERE,"[!] " + message);
    	}else {
        	Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[TerraformGenerator][!] "
        + ChatColor.translateAlternateColorCodes('&', message));
    	}
    }

    public void info(String message) {
    	if(TConfigOption.DEVSTUFF_SUPPRESS_CONSOLE_LOGS.getBoolean()) {
    		LOGGER.log(Level.INFO,message);
    	}else
    		Bukkit.getConsoleSender().sendMessage("[TerraformGenerator] "
    			+ ChatColor.translateAlternateColorCodes('&', message));
    }
    
    public void debug(String message) {
        if (TConfigOption.DEVSTUFF_DEBUG_MODE.getBoolean())
        	if(TConfigOption.DEVSTUFF_SUPPRESS_CONSOLE_LOGS.getBoolean()) {
        		LOGGER.log(Level.INFO,"[v] "+message);
        	}else
        		Bukkit.getConsoleSender().sendMessage("[TerraformGenerator][v] "
        				+ ChatColor.translateAlternateColorCodes('&', message));
    }
}
