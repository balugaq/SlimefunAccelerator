package com.balugaq.slimefunaccelerator.core.listeners;

import com.balugaq.slimefunaccelerator.api.AcceleratorSettings;
import com.balugaq.slimefunaccelerator.api.utils.Accelerates;
import com.balugaq.slimefunaccelerator.api.utils.ReflectionUtil;
import com.balugaq.slimefunaccelerator.core.managers.AcceleratesLoader;
import com.balugaq.slimefunaccelerator.implementation.SlimefunAccelerator;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.events.BlockPlacerPlaceEvent;
import io.github.thebusybiscuit.slimefun4.api.events.SlimefunItemRegistryFinalizedEvent;
import io.github.thebusybiscuit.slimefun4.api.items.ItemState;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

@SuppressWarnings("deprecation")
public class Accelerator implements Listener {
    public static final int EXTRA_TICKER_FLAG = 0b00000001;
    public static final Map<String, Set<Location>> allTickerLocations = new ConcurrentHashMap<>(16);
    public static final Map<SlimefunItem, BlockTicker> originalTickers = new ConcurrentHashMap<>(16);
    public static final boolean isCNSlimefun = SlimefunAccelerator.getInstance().getIntegrationManager().isCNSlimefun();
    public static final Map<String, AtomicBoolean> running = new ConcurrentHashMap<>(16);
    public static final Map<String, Set<Location>> tickLocations = new ConcurrentHashMap<>(16);
    public static final Set<String> extraTickers = new HashSet<>(16);
    public static final BiConsumer<String, Set<SlimefunItem>> onAccelerate = (group, items) -> {
        var s = running.computeIfAbsent(group, k -> new AtomicBoolean(false));
        if (s.compareAndSet(false, true)) {
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

        AcceleratorSettings settings = Accelerates.getAccelerateSettings().get(group);
        if (settings == null) {
            return;
        }

        Set<Location> groupSet = tickLocations.get(group);
        Set<Location> queue;
        synchronized (groupSet) {
            queue = new HashSet<>(groupSet);
        }

        for (Location location : queue) {
            if (location == null) {
                continue;
            }
            SlimefunItem item;
            if (isCNSlimefun) {
                item = StorageCacheUtils.getSfItem(location);
            } else {
                item = BlockStorage.check(location);
            }

            if (item == null) {
                continue;
            }

            if (!settings.isTickUnload() && !location.getChunk().isLoaded()) {
                continue;
            }

            if (item.isDisabledIn(location.getWorld())) {
                continue;
            }

            if (isCNSlimefun) {
                SlimefunBlockData config = StorageCacheUtils.getBlock(location);
                if (config == null) {
                    if (((int) location.getYaw() & EXTRA_TICKER_FLAG) != 0) {
                        Set<Location> extra = allTickerLocations.get(item.getId());
                        if (extra != null) {
                            synchronized (extra) {
                                extra.remove(location);
                            }
                        }
                    }
                    continue;
                }
                BlockTicker ticker = Accelerates.getTickers().get(item.getId());
                if (ticker != null) {
                    try {
                        ticker.tick(location.getBlock(), item, config);
                    } catch (Throwable e) {
                        SlimefunAccelerator.getInstance().getLogger().severe("An error occurred while ticking " + item.getId());
                        SlimefunAccelerator.getInstance().getLogger().severe(e.toString());
                        e.printStackTrace();
                    }
                }
            } else {
                Config config = BlockStorage.getLocationInfo(location);
                if (config == null) {
                    if (((int) location.getYaw() & EXTRA_TICKER_FLAG) != 0) {
                        Set<Location> extra = allTickerLocations.get(item.getId());
                        if (extra != null) {
                            synchronized (extra) {
                                extra.remove(location);
                            }
                        }
                    }
                    continue;
                }
                BlockTicker ticker = Accelerates.getTickers().get(item.getId());
                if (ticker != null) {
                    try {
                        ticker.tick(location.getBlock(), item, config);
                    } catch (Throwable e) {
                        SlimefunAccelerator.getInstance().getLogger().severe("An error occurred while ticking " + item.getId());
                        SlimefunAccelerator.getInstance().getLogger().severe(e.toString());
                        e.printStackTrace();
                    }
                }
            }
        }

        tickLocations.get(group).clear();

        running.get(group).set(false);
    };

    public static void load() {
        SlimefunAccelerator.getInstance().getLogger().info("Loading accelerates...");
        AcceleratesLoader.loadAccelerates();

        Map<String, AcceleratorSettings> allSettings = new HashMap<>();
        Map<String, Set<SlimefunItem>> accelerates = Accelerates.getAccelerates();
        for (Map.Entry<String, Set<SlimefunItem>> entry : accelerates.entrySet()) {
            String group = entry.getKey();
            tickLocations.put(group, ConcurrentHashMap.newKeySet());
            running.put(group, new AtomicBoolean(false));
            Set<SlimefunItem> items = entry.getValue();
            AcceleratorSettings settings = Accelerates.getAccelerateSettings().get(group);
            if (settings == null) {
                settings = new AcceleratorSettings();
            }

            allSettings.put(group, settings);
            if (settings.isEnabledExtraTicker()) {
                for (SlimefunItem slimefunItem : items) {
                    extraTickers.add(slimefunItem.getId());
                }
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
                    if (settings.isRemoveOriginalTicker()) {
                        slimefunItem.addItemHandler(new BlockTicker() {
                            @Override
                            public boolean isSynchronized() {
                                return false;
                            }

                            @Override
                            public void tick(Block block, SlimefunItem slimefunItem, SlimefunBlockData config) {
                                // do nothing
                            }
                        });
                    } else {
                        slimefunItem.addItemHandler(new BlockTicker() {
                            @Override
                            public boolean isSynchronized() {
                                return blockTicker.isSynchronized();
                            }

                            @Override
                            public void tick(@NotNull Block block, SlimefunItem slimefunItem, SlimefunBlockData config) {
                                Location location = block.getLocation();
                                Set<Location> queue = tickLocations.get(group);
                                queue.add(location);
                            }
                        });
                    }
                } else {
                    if (settings.isRemoveOriginalTicker()) {
                        slimefunItem.addItemHandler(new BlockTicker() {
                            @Override
                            public boolean isSynchronized() {
                                return false;
                            }

                            @Override
                            public void tick(Block block, SlimefunItem slimefunItem, Config config) {
                                // do nothing
                            }
                        });
                    } else {
                        slimefunItem.addItemHandler(new BlockTicker() {
                            @Override
                            public boolean isSynchronized() {
                                return blockTicker.isSynchronized();
                            }

                            @Override
                            public void tick(@NotNull Block block, SlimefunItem slimefunItem, Config config) {
                                Location location = block.getLocation();
                                Set<Location> queue = tickLocations.get(group);
                                queue.add(location);
                            }
                        });
                    }
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

        if (isCNSlimefun) {
            allTickerLocations.putAll(ExtraTickerCNVersion.getAllTickLocations());
        } else {
            for (World world : Bukkit.getWorlds()) {
                BlockStorage blockStorage = BlockStorage.getStorage(world);
                // just ignore this warning
                if (blockStorage == null) {
                    continue;
                }
                @SuppressWarnings("unchecked") Map<Location, Config> storage = (Map<Location, Config>) ReflectionUtil.invokeMethod(blockStorage, "getRawStorage");
                if (storage == null) {
                    continue;
                }
                for (Map.Entry<Location, Config> entry : storage.entrySet()) {
                    Location location = entry.getKey();
                    SlimefunItem slimefunItem = BlockStorage.check(location);
                    if (slimefunItem == null) {
                        continue;
                    }

                    if (!extraTickers.contains(slimefunItem.getId())) {
                        continue;
                    }

                    allTickerLocations.computeIfAbsent(slimefunItem.getId(), k -> ConcurrentHashMap.newKeySet()).add(location);
                }
            }
        }

        for (Map.Entry<String, AcceleratorSettings> entry : allSettings.entrySet()) {
            String group = entry.getKey();
            AcceleratorSettings settings = entry.getValue();
            Set<SlimefunItem> items = accelerates.get(entry.getKey());
            Set<String> ids = new HashSet<>();
            for (SlimefunItem slimefunItem : items) {
                ids.add(slimefunItem.getId());
            }
            Bukkit.getScheduler().runTaskTimerAsynchronously(SlimefunAccelerator.getInstance(), () -> {
                for (String id : ids) {
                    SlimefunItem slimefunItem = SlimefunItem.getById(id);
                    if (slimefunItem == null) {
                        continue;
                    }
                    if (slimefunItem.isDisabled()) {
                        continue;
                    }

                    Set<Location> locations = allTickerLocations.get(id);
                    if (locations == null) {
                        continue;
                    }
                    for (Location location : locations) {
                        if (!settings.isTickUnload() && !location.getChunk().isLoaded()) {
                            continue;
                        }

                        if (slimefunItem.isDisabledIn(location.getWorld())) {
                            continue;
                        }

                        Location clone = location.clone();
                        clone.setYaw(EXTRA_TICKER_FLAG);
                        Set<Location> queue = tickLocations.get(group);
                        synchronized (queue) {
                            queue.add(clone);
                        }
                    }
                }
            }, settings.getExtraTickerDelay(), settings.getExtraTickerPeriod());
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

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInit(SlimefunItemRegistryFinalizedEvent event) {
        load();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPlace(@NotNull BlockPlaceEvent event) {
        if (isCNSlimefun) {
            SlimefunBlockData config = StorageCacheUtils.getBlock(event.getBlock().getLocation());
            if (config == null) {
                return;
            }

            Set<Location> locations = allTickerLocations.computeIfAbsent(config.getSfId(), k -> ConcurrentHashMap.newKeySet());
            synchronized (locations) {
                locations.add(event.getBlock().getLocation());
            }
        } else {
            Config config = BlockStorage.getLocationInfo(event.getBlock().getLocation());
            if (config == null) {
                return;
            }

            String id = config.getString("id");
            if (id == null) {
                return;
            }

            Set<Location> locations = allTickerLocations.computeIfAbsent(id, k -> ConcurrentHashMap.newKeySet());
            synchronized (locations) {
                locations.add(event.getBlock().getLocation());
            }
        }
    }

    @EventHandler
    public void onBlockPlacerPlace(@NotNull BlockPlacerPlaceEvent event) {
        SlimefunItem slimefunItem = SlimefunItem.getByItem(event.getItemStack());
        if (slimefunItem != null) {
            Set<Location> locations = allTickerLocations.computeIfAbsent(slimefunItem.getId(), k -> ConcurrentHashMap.newKeySet());
            synchronized (locations) {
                locations.add(event.getBlockPlacer().getLocation());
            }
        }
    }
}
