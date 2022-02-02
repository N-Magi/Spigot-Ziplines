package net.rikkido;

import org.bukkit.configuration.file.FileConfiguration;

public class Config<type> {
    public String configName;
    public type value;

    public Config(String key, type Value) {
        configName = key;
        value = Value;
    }

    public void setValue(Object value) {
        value = (type) value;
    }
}