package org.terraform.main.config;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.terraform.main.TerraformGeneratorPlugin;

public class ConfigLoader {
	
	private final @NotNull HashMap<String, Object> configOptions = new HashMap<>();
	private final TerraformGeneratorPlugin plugin;
	private static final int configVersion = 4;
	public ConfigLoader(@NotNull TerraformGeneratorPlugin plugin){
		this.plugin = plugin;
		plugin.getConfig().options().copyDefaults(false);
		plugin.saveConfig();
		configOptions.put("lang","eng.yml");
		configOptions.put("config-version", configVersion);
	}
	
	public void load(){
		
		//If config version is older, overwrite the whole config.
		boolean overwrite = false;
		if(!plugin.getConfig().isSet("config-version"))
			overwrite = true;
		else if(plugin.getConfig().getInt("config-version") < configVersion)
			overwrite = true;
		
		//Make backup of old config before overwriting
		if(overwrite) {
			try {
				if(new File("./plugins/TerraformGenerator/config.yml").exists()) {
					Bukkit.getLogger().info("New config version detected. Overwriting old config. A backup will be made");
                    Files.copy(
				    		new File("./plugins/TerraformGenerator/config.yml").toPath(),
				    		new File("./plugins/TerraformGenerator/config.yml-" + System.currentTimeMillis() + ".bak").toPath()
                    );
				}
			}catch(Exception e) {
				TerraformGeneratorPlugin.logger.stackTrace(e);
				Bukkit.getLogger().info("Failed to backup old config before overwrite.");
			}
		}
			
		for(String key: new ArrayList<>(configOptions.keySet())){
			if(!overwrite && plugin.getConfig().isSet(key)){
				configOptions.put(key, plugin.getConfig().get(key));
			}else{
				plugin.getConfig().set(key, configOptions.get(key));
				plugin.saveConfig();
			}
		}
	}
	
	public void reload(){
		plugin.reloadConfig();
		load();
	}
	
	/**
	 * Registers a new config option
	 * @param key the key of the new option
	 * @param defaultValue the default value of the new option
	 */
	public void reg(String key, Object defaultValue){
		this.configOptions.put(key,defaultValue);
	}
	
	public void save(){
		for(String key:configOptions.keySet()){
			plugin.getConfig().set(key, configOptions.get(key));
			plugin.saveConfig();
		}
	}
	
	public String getString(String key){
		return (String) configOptions.get(key);
	}
	
	public Object get(String key){
		return configOptions.get(key);
	}
	
	public boolean getBoolean(String key){
		return (Boolean) configOptions.get(key);
	}
	
	public int getInt(String key){
		return (Integer) configOptions.get(key);
	}
	
	public double getDouble(String key){
		return (Double) configOptions.get(key);
	}
	
	@SuppressWarnings("unchecked")
	public List<String> getStringList(String key){
		return (List<String>) configOptions.get(key);
	}
	
}