package net.rikkido.config;

import org.bukkit.configuration.ConfigurationSection;

public class ItemConfig {

    private static String SECTION_ITEM = "Item";

    public ZiplineItemConfig ziplineItemconf;
    public DebugStickItemConfig debugStickItemConfig;

    public ItemConfig() {
        ziplineItemconf = new ZiplineItemConfig();
        debugStickItemConfig = new DebugStickItemConfig();
    }

    public void load(ConfigurationSection sec){
        var inside = sec.getConfigurationSection(SECTION_ITEM);
        ziplineItemconf.load(inside);
        debugStickItemConfig.load(inside);
    }
}
