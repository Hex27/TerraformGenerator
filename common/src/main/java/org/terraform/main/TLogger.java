package org.terraform.main;

import org.bukkit.plugin.java.JavaPlugin;
import org.drycell.logger.DCLogger;

public class TLogger extends DCLogger {
    public TLogger(JavaPlugin plugin) {
        super(plugin);
    }

    @Override
    public void debug(String message) {
        if(TConfigOption.DEVSTUFF_DEBUG_MODE.getBoolean()) super.debug(message);
    }
}
