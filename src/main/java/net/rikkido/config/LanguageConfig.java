package net.rikkido.config;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;

public class LanguageConfig {

    private static String SECTION_LANGUAGE = "Language";

    public Config<String> Lang;

    public LanguageConfig() {
        Lang = new Config<String>("lang", "ja");
    }

    public void load(ConfigurationSection sec) {
        var inside = sec.getConfigurationSection(SECTION_LANGUAGE);
        Lang.load(inside);
    }
}
