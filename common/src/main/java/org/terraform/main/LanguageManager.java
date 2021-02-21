package org.terraform.main;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class LanguageManager {
	
	private FileConfiguration langFile;
	private File file;
	
	public LanguageManager(TerraformGeneratorPlugin plugin){
		this.file = new File(plugin.getDataFolder(),plugin.getConfigLoader().getString("lang"));
		reloadLangFile();
		loadDefaults();
	}
	
	private HashMap<String,String> cache = new HashMap<>();
	
	private void loadDefaults(){
		fetchLang("permissions.insufficient","&cYou don't have enough permissions to perform this action!");
		fetchLang("command.wrong-arg-length","&cToo many or too little arguments provided!");
		fetchLang("command.unknown","&cUnknown subcommand.");
		fetchLang("command.help.postive-pages","&cThe page specified must be a positive number!");
		fetchLang("permissions.console-cannot-exec","&cOnly players can execute this command.");
	}
	
	public String fetchLang(String langKey){
		return fetchLang(langKey,null);
	}
	
	public String fetchLang(String langKey, String def){
		if(cache.containsKey(langKey)){
			return cache.get(langKey);
		}
		if(langFile.isSet(langKey)){
			String value = ChatColor.translateAlternateColorCodes('&', langFile.getString(langKey));
			cache.put(langKey, value);
			return value;
		}else if(def != null){
			langFile.set(langKey, def);
			saveLangFile();
		}
		cache.put(langKey, ChatColor.translateAlternateColorCodes('&', def));
		return ChatColor.translateAlternateColorCodes('&', def);
	}
	
    public FileConfiguration getLangFile() {
        return langFile;
    }

    public void saveLangFile() {
        try {
        	langFile.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reloadLangFile() {
    	this.cache.clear();
        this.langFile = YamlConfiguration.loadConfiguration(file);
    }


}