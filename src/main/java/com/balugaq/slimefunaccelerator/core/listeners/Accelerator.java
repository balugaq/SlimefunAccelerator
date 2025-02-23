package com.balugaq.slimefunaccelerator.core.listeners;

import com.balugaq.slimefunaccelerator.api.AcceleratorSettings;
import com.balugaq.slimefunaccelerator.api.utils.Accelerates;
import com.balugaq.slimefunaccelerator.api.utils.ReflectionUtil;
import com.balugaq.slimefunaccelerator.core.managers.AcceleratesLoader;
import com.balugaq.slimefunaccelerator.implementation.SlimefunAccelerator;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.events.SlimefunItemRegistryFinalizedEvent;
import io.github.thebusybiscuit.slimefun4.api.items.ItemState;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

public class Accelerator implements Listener {
    public static final Map<SlimefunItem, BlockTicker> originalTickers = new ConcurrentHashMap<>(16);
    public static final boolean isCNSlimefun = SlimefunAccelerator.getInstance().getIntegrationManager().isCNSlimefun();
    public static final Map<String, AtomicBoolean> running = new ConcurrentHashMap<>(16);
    public static final Map<String, List<Location>> tickLocations = new ConcurrentHashMap<>(16);
    public static final BiConsumer<String, Set<SlimefunItem>> onAccelerate = (group, items) -> {
        if (running.get(group).compareAndSet(false, true)) {
            return;
        }

        for (SlimefunItem slimefunItem : items) {
            if (slimefunItem.isDisabled()) {
                continue;
            }
            BlockTicker blockTicker = Accelerates.getTickers().get(slimefunItem.getId());
            if (blockTicker == null) {
                continue;
            }

            blockTicker.uniqueTick();
        }

        Queue<Location> queue = new ConcurrentLinkedQueue<>(tickLocations.get(group));

        while (!queue.isEmpty()) {
            Location location = queue.poll();
            SlimefunItem item;
            if (isCNSlimefun) {
                item = StorageCacheUtils.getSfItem(location);
            } else {
                item = BlockStorage.check(location);
            }

            if (item == null) {
                continue;
            }

            if (item.isDisabledIn(location.getWorld())) {
                continue;
            }

            if (isCNSlimefun) {
                SlimefunBlockData config = StorageCacheUtils.getBlock(location);
                Accelerates.getTickers().get(item.getId()).tick(location.getBlock(), item, config);
            } else {
                Config config = BlockStorage.getLocationInfo(location);
                Accelerates.getTickers().get(item.getId()).tick(location.getBlock(), item, config);
            }
        }

        tickLocations.get(group).clear();

        running.get(group).set(false);
    };

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInit(SlimefunItemRegistryFinalizedEvent event) {
        load();
    }

    public static void load() {
        SlimefunAccelerator.getInstance().getLogger().info("Loading accelerates...");
        AcceleratesLoader.loadAccelerates();
        Map<String, Set<SlimefunItem>> accelerates = Accelerates.getAccelerates();
        for (Map.Entry<String, Set<SlimefunItem>> entry : accelerates.entrySet()) {
            String group = entry.getKey();
            tickLocations.put(group, new ArrayList<>(256));
            running.put(group, new AtomicBoolean(false));
            Set<SlimefunItem> items = entry.getValue();
            AcceleratorSettings settings = Accelerates.getAccelerateSettings().get(group);
            if (settings == null) {
                settings = new AcceleratorSettings();
            }

            for (SlimefunItem slimefunItem : items) {
                BlockTicker blockTicker = slimefunItem.getBlockTicker();
                if (blockTicker == null) {
                    continue;
                }
                originalTickers.put(slimefunItem, blockTicker);
                ItemState state = slimefunItem.getState();
                ReflectionUtil.setValue(slimefunItem, SlimefunItem.class, "state", ItemState.UNREGISTERED);

                if (isCNSlimefun) {
                    slimefunItem.addItemHandler(new BlockTicker() {
                        @Override
                        public boolean isSynchronized() {
                            return blockTicker.isSynchronized();
                        }

                        @Override
                        public void tick(Block block, SlimefunItem slimefunItem, SlimefunBlockData config) {
                            Location location = block.getLocation();
                            List<Location> queue = tickLocations.get(group);
                            if (!queue.contains(location)) {
                                queue.add(location);
                            }
                        }
                    });
                } else {
                    slimefunItem.addItemHandler(new BlockTicker() {
                        @Override
                        public boolean isSynchronized() {
                            return blockTicker.isSynchronized();
                        }

                        @Override
                        public void tick(Block block, SlimefunItem slimefunItem, Config config) {
                            Location location = block.getLocation();
                            List<Location> queue = tickLocations.get(group);
                            if (!queue.contains(location)) {
                                queue.add(location);
                            }
                        }
                    });
                }

                ReflectionUtil.setValue(slimefunItem, SlimefunItem.class, "state", state);
            }

            int taskId;
            if (settings.isAsync()) {
                taskId = Bukkit.getScheduler().runTaskTimerAsynchronously(SlimefunAccelerator.getInstance(),
                        () -> onAccelerate.accept(group, items),
                        settings.getDelay(),
                        settings.getPeriod()
                ).getTaskId();
            } else {
                taskId = Bukkit.getScheduler().runTaskTimer(SlimefunAccelerator.getInstance(),
                        () -> onAccelerate.accept(group, items),
                        settings.getDelay(),
                        settings.getPeriod()
                ).getTaskId();
            }
            Accelerates.getTaskIds().put(group, taskId);
        }
    }

    public static void shutdown() {
        for (int taskId : Accelerates.getTaskIds().values()) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
        rollback();
        originalTickers.clear();
        running.clear();
        tickLocations.clear();
    }

    public static void rollback() {
        for (Map.Entry<SlimefunItem, BlockTicker> entry : originalTickers.entrySet()) {
            SlimefunItem slimefunItem = entry.getKey();
            BlockTicker blockTicker = entry.getValue();
            ItemState state = slimefunItem.getState();
            ReflectionUtil.setValue(slimefunItem, SlimefunItem.class, "state", ItemState.UNREGISTERED);
            slimefunItem.addItemHandler(blockTicker);
            ReflectionUtil.setValue(slimefunItem, SlimefunItem.class, "state", state);
        }
        originalTickers.clear();
    }
}
