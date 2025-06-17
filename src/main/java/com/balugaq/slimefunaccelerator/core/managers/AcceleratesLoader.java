package com.balugaq.slimefunaccelerator.core.managers;

import com.balugaq.slimefunaccelerator.api.utils.Accelerates;
import com.balugaq.slimefunaccelerator.api.utils.Debug;
import com.balugaq.slimefunaccelerator.api.utils.Lang;
import com.balugaq.slimefunaccelerator.implementation.SlimefunAccelerator;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import lombok.experimental.UtilityClass;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

@UtilityClass
public class AcceleratesLoader {
    public static final String ACCELERATES_KEY = "accelerates";
    public static final String ENABLED_KEY = "enabled";
    public static final String ASYNC_KEY = "async";
    public static final String DELAY_KEY = "delay";
    public static final String PERIOD_KEY = "period";
    public static final String ADDONS_KEY = "addons";
    public static final String ITEMS_KEY = "items";
    public static final String EXCLUDE_KEY = "excludes";
    public static final String REMOVE_ORIGINAL_TICKER_KEY = "remove-original-ticker";
    public static final String EXTRA_TICKER_ENABLED_KEY = "extra-ticker.enabled";
    public static final String TICK_UNLOAD_KEY = "extra-ticker.tick-unload";
    public static final String EXTRA_TICKER_DELAY_KEY = "extra-ticker.delay";
    public static final String EXTRA_TICKER_PERIOD_KEY = "extra-ticker.period";
    public static final String EXAMPLE_ITEM = "__EXAMPLE_ITEM";
    public static final String EXAMPLE_ADDON = "__ExampleAddon";

    public static void loadAccelerates() {
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
            boolean async = groupSection.getBoolean(ASYNC_KEY, true);
            int period = groupSection.getInt(PERIOD_KEY, 10);
            int delay = groupSection.getInt(DELAY_KEY, 10);
            boolean removeOriginalTicker = groupSection.getBoolean(REMOVE_ORIGINAL_TICKER_KEY, false);
            boolean extraTickerEnabled = groupSection.getBoolean(EXTRA_TICKER_ENABLED_KEY, false);
            boolean tickUnload = groupSection.getBoolean(TICK_UNLOAD_KEY, false);
            int extraTickerDelay = groupSection.getInt(EXTRA_TICKER_DELAY_KEY, 10);
            int extraTickerPeriod = groupSection.getInt(EXTRA_TICKER_PERIOD_KEY, 10);

            List<String> excludes = groupSection.getStringList(EXCLUDE_KEY);
            excludes.replaceAll(String::toUpperCase);
            List<String> items = groupSection.getStringList(ITEMS_KEY);
            for (String rid : items) {
                String id = rid.toUpperCase();
                if (excludes.contains(id)) {
                    continue;
                }
                SlimefunItem slimefunItem = SlimefunItem.getById(id);
                if (slimefunItem == null) {
                    if (id.equalsIgnoreCase(EXAMPLE_ITEM)) {
                        configured = false;
                        continue;
                    }

                    invalidKey(ACCELERATES_KEY + "." + threadKey + "." + ITEMS_KEY, id);
                    continue;
                }

                BlockTicker ticker = slimefunItem.getBlockTicker();
                if (ticker == null) {
                    invalidKey(ACCELERATES_KEY + "." + threadKey + "." + ITEMS_KEY, id);
                    continue;
                }

                if (async && ticker.isSynchronized()) {
                    incompatibleTicker(ACCELERATES_KEY + "." + threadKey + "." + ITEMS_KEY, id, async, ticker);
                    continue;
                }

                Accelerates.addAccelerate(threadKey, id);
                Accelerates.addAccelerateSettings(threadKey, enabled, async, delay, period, removeOriginalTicker, extraTickerEnabled, tickUnload, extraTickerDelay, extraTickerPeriod);
                Accelerates.getTickers().put(id, ticker);
                SlimefunAccelerator.getInstance().getLogger().info(Lang.getMessage("load.added-accelerates", "id", id));
                configuredDifferentItem = true;
            }
            List<String> addons = groupSection.getStringList(ADDONS_KEY);
            if (addons.size() == 1 && addons.get(0).equalsIgnoreCase(EXAMPLE_ADDON)) {
                configured = false;
                continue;
            }

            for (SlimefunItem slimefunItem : Slimefun.getRegistry().getAllSlimefunItems()) {
                if (excludes.contains(slimefunItem.getId().toUpperCase())) {
                    continue;
                }
                for (String addon : addons) {
                    if (slimefunItem.getAddon().getName().equalsIgnoreCase(addon)) {
                        String id = slimefunItem.getId();
                        BlockTicker ticker = slimefunItem.getBlockTicker();
                        if (ticker == null) {
                            invalidKey(ACCELERATES_KEY + "." + threadKey + "." + ITEMS_KEY, id);
                            continue;
                        }

                        if (async && ticker.isSynchronized()) {
                            incompatibleTicker(ACCELERATES_KEY + "." + threadKey + "." + ITEMS_KEY, id, async, ticker);
                            continue;
                        }

                        Accelerates.addAccelerate(threadKey, id);
                        Accelerates.addAccelerateSettings(threadKey, enabled, async, delay, period, removeOriginalTicker, extraTickerEnabled, tickUnload, extraTickerDelay, extraTickerPeriod);
                        Accelerates.getTickers().put(id, ticker);
                        SlimefunAccelerator.getInstance().getLogger().info(Lang.getMessage("load.added-accelerates", "id", slimefunItem.getId()));
                        configuredDifferentItem = true;
                    }
                }
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

    public static void incompatibleTicker(String path, String value, boolean async, BlockTicker ticker) {
        SlimefunAccelerator.getInstance().getLogger().severe(Lang.getMessage("load.incompatible-ticker"));
        SlimefunAccelerator.getInstance().getLogger().severe(Lang.getMessage("load.path", "path", path));
        SlimefunAccelerator.getInstance().getLogger().severe(Lang.getMessage("load.value", "value", value));
        SlimefunAccelerator.getInstance().getLogger().severe(Lang.getMessage("load.async", "async", async));
        SlimefunAccelerator.getInstance().getLogger().severe(Lang.getMessage("load.ticker.synchronized", "synchronized", ticker.isSynchronized()));
    }
}
