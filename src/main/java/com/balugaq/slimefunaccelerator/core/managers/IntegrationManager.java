package com.balugaq.slimefunaccelerator.core.managers;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

@Getter
public class IntegrationManager {
    public final @NotNull JavaPlugin plugin;
    public final boolean enabledGuizhanLibPlugin;
    public boolean CNSlimefun;

    public IntegrationManager(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
        this.enabledGuizhanLibPlugin = plugin.getServer().getPluginManager().isPluginEnabled("GuizhanLibPlugin");
        try {
            Class.forName("com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils");
            this.CNSlimefun = true;
        } catch (Throwable e) {
            this.CNSlimefun = false;
        }
    }
}
