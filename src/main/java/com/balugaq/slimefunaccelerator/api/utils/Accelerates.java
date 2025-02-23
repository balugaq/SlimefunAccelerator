package com.balugaq.slimefunaccelerator.api.utils;

import com.balugaq.slimefunaccelerator.api.AcceleratorSettings;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@UtilityClass
public class Accelerates {
    @Getter
    public static final Map<String, BlockTicker> tickers = new HashMap<>();
    @Getter
    public static final Map<String, Integer> taskIds = new HashMap<>();
    @Getter
    public static final Map<String, AcceleratorSettings> accelerateSettings = new HashMap<>();
    @Getter
    public static final Map<String, Set<SlimefunItem>> accelerates = new HashMap<>();

    public static void addAccelerate(String group, Collection<String> ids) {
        for (String id : ids) {
            addAccelerate(group, id);
        }
    }
    public static void addAccelerate(String group, String id) {
        SlimefunItem item = SlimefunItem.getById(id);
        if (!accelerates.containsKey(group)) {
            accelerates.put(group, new HashSet<>());
        }
        accelerates.get(group).add(item);
    }
    public static void accelerate(String group, Collection<SlimefunItem> items) {
        if (!accelerates.containsKey(group)) {
            accelerates.put(group, new HashSet<>());
        }
        accelerates.get(group).addAll(items);
    }
    public static void accelerate(String group, SlimefunItem item) {
        if (!accelerates.containsKey(group)) {
            accelerates.put(group, new HashSet<>());
        }
        accelerates.get(group).add(item);
    }

    @NotNull
    public static Set<SlimefunItem> accelerates(String group) {
        return accelerates.get(group);
    }

    @NotNull
    public static Map<String, Set<SlimefunItem>> accelerateAll() {
        return accelerates;
    }

    public static void removeAccelerate(String group, Collection<String> ids) {
        for (String id : ids) {
            removeAccelerate(group, id);
        }
    }
    public static void removeAccelerate(String group, String id) {
        SlimefunItem item = SlimefunItem.getById(id);
        if (accelerates.containsKey(group)) {
            accelerates.get(group).remove(item);
        }
    }
    public static void removeAccelerate(String group) {
        accelerates.remove(group);
    }
    public static void rmAccelerate(String group, Collection<SlimefunItem> items) {
        for (SlimefunItem item : items) {
            rmAccelerate(group, item);
        }
    }
    public static void rmAccelerate(String group, SlimefunItem item) {
        if (accelerates.containsKey(group)) {
            accelerates.get(group).remove(item);
        }
    }

    public static void clearAccelerates() {
        synchronized (accelerateSettings) {
            accelerates.clear();
        }
    }

    public static void addAccelerateSettings(String group, boolean enabled, boolean async, int period, int delay) {
        addAccelerateSettings(group, new AcceleratorSettings(enabled, async, period, delay));
    }

    public static void addAccelerateSettings(String group, AcceleratorSettings settings) {
        accelerateSettings.put(group, settings);
    }

    public static void shutdown() {
        tickers.clear();
        taskIds.clear();
        accelerateSettings.clear();
        accelerates.clear();
    }
}
