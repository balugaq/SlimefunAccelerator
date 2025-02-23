package com.balugaq.slimefunaccelerator.core.managers;

import com.balugaq.slimefunaccelerator.api.utils.Debug;
import com.balugaq.slimefunaccelerator.api.utils.Lang;
import com.balugaq.slimefunaccelerator.implementation.SlimefunAccelerator;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import lombok.experimental.UtilityClass;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

@UtilityClass
public class AcceleratesLoader {
    public static final String ACCELERATES_KEY = "accelerates";
    public static final String ENABLED_KEY = "enabled";
    public static final String ASYNC_KEY = "async";
    public static final String PERIOD_KEY = "period";
    public static final String DELAY_KEY = "delay";
    public static final String ITEMS_KEY = "items";
    public static final String EXAMPLE_ITEM = "__EXAMPLE_ITEM";

    public static void loadPredications() {
        boolean configured = true;
        boolean configuredDifferentItem = false;
        FileConfiguration configuration = SlimefunAccelerator.getInstance().getConfigManager().getBans();
        Debug.debug("Loading accelerates");
        for (String key : configuration.getKeys(false)) {
            Debug.debug("Key: " + key);
        }
        ConfigurationSection accelerates = configuration.getConfigurationSection(ACCELERATES_KEY);
        if (accelerates == null) {
            return;
        }

        for (String threadKey : accelerates.getKeys(false)) {
            ConfigurationSection groupSection = accelerates.getConfigurationSection(threadKey);
            if (groupSection == null) {
                continue;
            }

            boolean enabled = groupSection.getBoolean(ENABLED_KEY, true);
            if (!enabled) {
                continue;
            }

            List<String> items = groupSection.getStringList(ITEMS_KEY);
            for (String rid : items) {
                String id = rid.toUpperCase();
                SlimefunItem slimefunItem = SlimefunItem.getById(id);
                if (slimefunItem == null) {
                    if (id.equalsIgnoreCase(EXAMPLE_ITEM)) {
                        configured = false;
                        continue;
                    }

                    invalidKey(ACCELERATES_KEY + "." + threadKey + "." + ITEMS_KEY, id);
                    continue;
                }


                SlimefunAccelerator.getInstance().getLogger().info(Lang.getMessage("load.added-accelerates", "id", id));
                configuredDifferentItem = true;
            }
        }

        if (!configured && !configuredDifferentItem) {
            SlimefunAccelerator.getInstance().getLogger().warning(Lang.getMessage("load.no-configured-accelerates"));
        }
    }

    public static void invalidKey(String path, String value) {
        SlimefunAccelerator.getInstance().getLogger().severe(Lang.getMessage("load.invalid-accelerate-key"));
        SlimefunAccelerator.getInstance().getLogger().severe(Lang.getMessage("load.path", "path", path));
        SlimefunAccelerator.getInstance().getLogger().severe(Lang.getMessage("load.value", "value", value));
    }
}
