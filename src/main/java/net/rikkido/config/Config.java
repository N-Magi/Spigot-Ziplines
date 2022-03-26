package net.rikkido.config;

import org.bukkit.configuration.ConfigurationSection;

public class Config<type> {
    public String configName;
    public type value;

    public Config(String key, type Value) {
        configName = key;
        value = Value;
    }

    public void load(ConfigurationSection sec){
        value = (type)sec.get(configName);
    }

}