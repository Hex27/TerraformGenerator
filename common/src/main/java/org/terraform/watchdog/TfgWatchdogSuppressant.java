package org.terraform.watchdog;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.main.config.TConfigOption;

public class TfgWatchdogSuppressant {
	Field instanceField = null;
	Field lastTickField = null;
	Class<?> watchdogThreadClass = null;
	Method tickMethod = null;
	Object watchdogThreadInstance = null;
	
	public TfgWatchdogSuppressant(){
		if(TConfigOption.DEVSTUFF_SUPPRESS_WATCHDOG.getBoolean())
			try {
				TerraformGeneratorPlugin.logger.info("[NOTICE] TerraformGenerator will suppress the server's watchdog "
						+ "while generating chunks to prevent unnecessary stacktrace warnings. Unless you specifically need the"
						+ "watchdog now (to take aikar timings or debug lag), you don't need to take any action.");
				TerraformGeneratorPlugin.logger.info("It is recommended to pregenerate to reduce lag problems.");
				Class<?> watchdogThreadClass = Class.forName("org.spigotmc.WatchdogThread");
				
		        instanceField = watchdogThreadClass.getDeclaredField("instance");
		        instanceField.setAccessible(true);
		
		        lastTickField = watchdogThreadClass.getDeclaredField("lastTick");
		        lastTickField.setAccessible(true);
		        
		        tickMethod = watchdogThreadClass.getDeclaredMethod("tick");
		        tickMethod.setAccessible(true);
		        
		        watchdogThreadInstance = this.instanceField.get(null);
	        	TerraformGeneratorPlugin.logger.info("Watchdog Thread hooked.");
			}
	        catch(SecurityException | NoSuchFieldException | ClassNotFoundException | NoSuchMethodException | IllegalArgumentException | IllegalAccessException e) {
	        	TerraformGeneratorPlugin.logger.info("Watchdog instance could not be found.");
	        	e.printStackTrace();
	        	instanceField = null;
	        	lastTickField = null;
	        	watchdogThreadClass = null;
	        	watchdogThreadInstance = null;
	        	tickMethod = null;
	        }
	}
	
	public void tickWatchdog() {
		if(watchdogThreadInstance == null) return;
        try {
            if ((long) lastTickField.get(watchdogThreadInstance) != 0) {
            	tickMethod.invoke(watchdogThreadInstance);
            }
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
        	e.printStackTrace();
            TerraformGeneratorPlugin.logger.info("Failed to tick watchdog");
        }
    }
	
}
