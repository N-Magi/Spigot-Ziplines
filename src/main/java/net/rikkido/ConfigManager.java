package net.rikkido;

import org.bukkit.configuration.file.FileConfiguration;

import net.rikkido.config.ItemConfig;
import net.rikkido.config.LanguageConfig;
import net.rikkido.config.ZiplineConfig;
import net.rikkido.config.ZipliningConfig;

public class ConfigManager {

    private Zipline _plugin;

    public ZiplineConfig ziplineConfig = new ZiplineConfig();
    public ZipliningConfig zipliningConfig = new ZipliningConfig();
    public ItemConfig itemConfig = new ItemConfig();
    public LanguageConfig language = new LanguageConfig();

    public ConfigManager(Zipline plugin) {
        _plugin = plugin;
        plugin.saveDefaultConfig();
        FileConfiguration config = plugin.getConfig();
        loadConfig(config);
    }

    private void loadConfig(FileConfiguration config) {
        ziplineConfig.load(config);
        zipliningConfig.load(config);
        itemConfig.load(config);
        language.load(config);

        _plugin.getLogger().info("size :" + itemConfig.ziplineItemconf.itemshapeConfig.value.size() + "shape :"
                + itemConfig.ziplineItemconf.itemshapeConfig.value);
        _plugin.getLogger().info("size :" + itemConfig.ziplineItemconf.itemPair.value.size()
                + itemConfig.ziplineItemconf.itemPair.value.get(0).toString());
    }
}
