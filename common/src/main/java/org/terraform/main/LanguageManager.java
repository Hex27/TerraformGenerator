package org.terraform.main;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.terraform.main.config.TConfig;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class LanguageManager {

    private final File file;
    private final @NotNull HashMap<String, String> cache = new HashMap<>();
    private FileConfiguration langFile;

    public LanguageManager(@NotNull TerraformGeneratorPlugin plugin, @NotNull TConfig config) {
        this.file = new File(plugin.getDataFolder(), config.LANGUAGE_FILE);
        reloadLangFile();
        loadDefaults();
    }

    private void loadDefaults() {
        fetchLang("permissions.insufficient", "&cYou don't have enough permissions to perform this action!");
        fetchLang("command.wrong-arg-length", "&cToo many or too little arguments provided!");
        fetchLang("command.unknown", "&cUnknown subcommand.");
        fetchLang("command.help.postive-pages", "&cThe page specified must be a positive number!");
        fetchLang("permissions.console-cannot-exec", "&cOnly players can execute this command.");
    }

    public String fetchLang(@NotNull String langKey) {
        return fetchLang(langKey, null);
    }

    public String fetchLang(@NotNull String langKey, @Nullable String def) {
        if (cache.containsKey(langKey)) {
            return cache.get(langKey);
        }

        String value = langFile.getString(langKey);
        if (value == null) {
            value = def == null ? langKey : def; // if no default is given, the default becomes the `langKey`
            langFile.set(langKey, value);
            saveLangFile();
        }

        value = ChatColor.translateAlternateColorCodes('&', value);
        cache.put(langKey, value);
        return value;
    }

    public void saveLangFile() {
        try {
            langFile.save(file);
        }
        catch (IOException e) {
            TerraformGeneratorPlugin.logger.stackTrace(e);
        }
    }

    public void reloadLangFile() {
        this.cache.clear();
        this.langFile = YamlConfiguration.loadConfiguration(file);
    }


}