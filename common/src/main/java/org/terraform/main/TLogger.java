package org.terraform.main;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.terraform.main.config.TConfigOption;


public class TLogger {
	
	private static final Logger LOGGER = Logger.getLogger("TerraformGenerator-Custom");
    private static boolean suppressConsoleLogs = false;
	
	public TLogger() {
		suppressConsoleLogs = TConfigOption.DEVSTUFF_SUPPRESS_CONSOLE_LOGS.getBoolean();
    	if(suppressConsoleLogs) {
            Handler consoleHandler = null;
            Handler fileHandler  = null;
            try{
                //Creating consoleHandler and fileHandler
                consoleHandler = new ConsoleHandler();
                fileHandler  = new FileHandler("plugins" + File.separator + "TerraformGenerator" + File.separator + "terraform.log", true);
                
                //Follow bukkit format
                fileHandler.setFormatter(new SimpleFormatter() {
                    private static final String format = "[%1$tF %1$tT] [%2$-7s] %3$s %n";

                    @Override
                    public synchronized String format(LogRecord lr) {
                        return String.format(format,
                                new Date(lr.getMillis()),
                                lr.getLevel().getLocalizedName(),
                                ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', lr.getMessage()))                        );
                    }
                });
                
                LOGGER.setUseParentHandlers(false);
                
                //Assigning handlers to LOGGER object
                LOGGER.addHandler(consoleHandler);
                LOGGER.addHandler(fileHandler);
                
                //No stdout
                //LOGGER.setLevel(Level.OFF);
                consoleHandler.setLevel(Level.OFF);
                fileHandler.setLevel(Level.ALL);
                 
                LOGGER.config("Configuration done.");
                 
                //Console handler removed
                //LOGGER.removeHandler(consoleHandler);
                 
                this.stdout("Custom Logger Initialized");
            }catch(IOException exception){
            	Bukkit.getLogger().severe("Error occur in FileHandler." + exception);
            	suppressConsoleLogs = false;
            }
    	}
    }
	
    public void stdout(String message) {
    	Bukkit.getConsoleSender().sendMessage("[TerraformGenerator] "
    		+ ChatColor.translateAlternateColorCodes('&', message));
    	
    	if(suppressConsoleLogs) {
    		LOGGER.log(Level.INFO," " + message);
    	}
    }

    public void error(String message) {
    	if(suppressConsoleLogs) {
    		LOGGER.log(Level.SEVERE,"[!] " + message);
    	}else {
        	Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[TerraformGenerator][!] "
        + ChatColor.translateAlternateColorCodes('&', message));
    	}
    }

    public void info(String message) {
    	if(suppressConsoleLogs) {
    		LOGGER.log(Level.INFO,message);
    	}else
    		Bukkit.getConsoleSender().sendMessage("[TerraformGenerator] "
    			+ ChatColor.translateAlternateColorCodes('&', message));
    }
    
    public void debug(String message) {
        if (TConfigOption.DEVSTUFF_DEBUG_MODE.getBoolean())
        	if(suppressConsoleLogs) {
        		LOGGER.log(Level.INFO,"[v] "+message);
        	}else
        		Bukkit.getConsoleSender().sendMessage("[TerraformGenerator][v] "
        				+ ChatColor.translateAlternateColorCodes('&', message));
    }
}
