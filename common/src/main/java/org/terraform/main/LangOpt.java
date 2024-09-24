package org.terraform.main;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public enum LangOpt {
    COMMAND_LOCATE_NOVANILLA("&c&lFor terraformgenerator worlds, use &e&l/terra locate &c&linstead!"),
    COMMAND_LOCATE_STRUCTURE_NOT_ENABLED("&cThe specified structure was not enabled!"),
    COMMAND_LOCATE_LOCATE_COORDS("&aLocated at X: %x% Z: %z%"),
    COMMAND_LOCATE_SEARCHING("&bSearching for structure asynchronously. Please wait..."),
    COMMAND_LOCATEBIOME_INVALIDBIOME("&cInvalid Biome. Valid Biomes:"),
    COMMAND_LOCATEBIOME_NOT_IN_5000("&cCould not find this biome within 5000 blocks."),
    COMMAND_LOCATEBIOME_DISABLED("&cThis biome is disabled."),
    COMMAND_LOCATE_LIST_HEADER("&e-==[&bStructure Handlers&e]==-"),
    COMMAND_LOCATE_LIST_ENTRY("&e - &b%entry%"),
    COMMAND_LOCATE_COMPLETED_TASK("&aCompleted Locate task (%time%ms)");

    private final String path;
    private String value;

    LangOpt(String lang) {
        this.value = lang;
        this.path = this.toString().toLowerCase(Locale.ENGLISH).replace('_', '.');
    }

    LangOpt(String path, String lang) {
        this.path = path;
        this.value = lang;
    }

    public static void init(@NotNull TerraformGeneratorPlugin plugin) {
        for (LangOpt lang : LangOpt.values()) {
            lang.value = plugin.getLang().fetchLang(lang.path, lang.value);
        }
    }

    public static String fetchLang(@NotNull String path) {
        TerraformGeneratorPlugin plugin = TerraformGeneratorPlugin.get();
        return plugin.getLang().fetchLang(path);
    }

    public String getPath() {
        return this.path;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String parse(String @NotNull ... placeholders) {
        String parsed = this.value;
        String placeholder = "";

        for (int i = 0; i < placeholders.length; i++) {
            if (i % 2 == 0) {
                placeholder = placeholders[i];
            }
            else {
                parsed = parsed.replaceAll(placeholder, placeholders[i]);
            }
        }
        return parsed;
    }

    public void send(@NotNull CommandSender sender, String... placeholders) {
        sender.sendMessage(parse(placeholders));
    }
}
