package org.terraform.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ConfigLoader {
	
	private HashMap<String, Object> configOptions = new HashMap<>();
	private TerraformGeneratorPlugin plugin;
	public ConfigLoader(TerraformGeneratorPlugin plugin){
		this.plugin = plugin;
		plugin.getConfig().options().copyDefaults(false);
		plugin.saveConfig();
		configOptions.put("lang","eng.yml");
	}
	
	public void load(){
		for(String key:new ArrayList<String>(configOptions.keySet())){
			if(plugin.getConfig().isSet(key)){
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