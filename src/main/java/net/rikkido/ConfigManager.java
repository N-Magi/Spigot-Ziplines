package net.rikkido;

import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

    private Zipline _plugin;

    public Config<Double> Speed = new Config<Double>("speed", 1.0);

    public Config<Double> MaxRadiusZipline = new Config<Double>("zipline_max_radius", -1.0);//negative value means infinity

    public ConfigManager(Zipline plugin) {
        _plugin = plugin;
        plugin.saveDefaultConfig();
        FileConfiguration config = plugin.getConfig();
        loadConfig(config);
    }

    private void loadConfig(FileConfiguration config) {
        Speed.value = config.getDouble(Speed.configName);
        MaxRadiusZipline.value = config.getDouble(MaxRadiusZipline.configName);
    }
}
