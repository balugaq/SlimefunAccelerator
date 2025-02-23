package com.balugaq.slimefunaccelerator.implementation;

import com.balugaq.slimefunaccelerator.api.utils.Accelerates;
import com.balugaq.slimefunaccelerator.api.utils.Lang;
import com.balugaq.slimefunaccelerator.core.listeners.Accelerator;
import com.balugaq.slimefunaccelerator.core.managers.CommandManager;
import com.balugaq.slimefunaccelerator.core.managers.ConfigManager;
import com.balugaq.slimefunaccelerator.core.managers.IntegrationManager;
import com.balugaq.slimefunaccelerator.core.managers.ListenerManager;
import com.balugaq.slimefunaccelerator.core.services.LocalizationService;
import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import io.github.thebusybiscuit.slimefun4.libraries.dough.updater.BlobBuildUpdater;
import lombok.Getter;
import net.guizhanss.guizhanlibplugin.updater.GuizhanUpdater;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public class SlimefunAccelerator extends JavaPlugin implements SlimefunAddon {
    @Getter
    private static final String DEFAULT_LANGUAGE = "zh-CN";
    @Getter
    private static boolean enabledPlugin = false;
    @Getter
    private static SlimefunAccelerator instance;
    @Getter
    private final String username = "balugaq";
    @Getter
    private final String repo = "SlimefunAccelerator";
    @Getter
    private final String branch = "master";
    @Getter
    private ConfigManager configManager;
    @Getter
    private CommandManager commandManager;
    @Getter
    private IntegrationManager integrationManager;
    @Getter
    private ListenerManager listenerManager;
    @Getter
    private LocalizationService localizationService;

    @Override
    public void onEnable() {
        instance = this;
        enabledPlugin = true;
        getLogger().info("Enabling SlimefunAccelerator...");

        getLogger().info("Loading configuration...");
        configManager = new ConfigManager(this);
        getLogger().info("Loading localization...");
        localizationService = new LocalizationService(this);
        getLogger().info("Loading language: " + getConfigManager().getLanguage());
        localizationService.addLanguage(getConfigManager().getLanguage());
        getLogger().info("Loading default language... (zh-CN)");
        localizationService.addLanguage(DEFAULT_LANGUAGE);
        getLogger().info("Loading integrations...");
        integrationManager = new IntegrationManager(this);
        getLogger().info("Loading commands...");
        commandManager = new CommandManager(this);
        commandManager.setup();
        commandManager.registerCommand();
        getLogger().info("Loading listeners...");
        listenerManager = new ListenerManager(this);
        getListenerManager().setup();
        getListenerManager().load();

        getLogger().info("Checking for updates...");
        switch (getConfigManager().getBuildStation()) {
            case GUIZHAN -> {
                if (getIntegrationManager().isEnabledGuizhanLibPlugin()) {
                    getLogger().info("Auto-updating from Guizhan Build...");
                    tryUpdateFromGuizhan();
                } else {
                    getLogger().severe("GuizhanLibPlugin required for auto-update!");
                    getLogger().severe("Download from: https://50l.cc/gzlib");
                }
            }
            case BLOB -> {
                getLogger().info("Auto-updating from Blob Build...");
                tryUpdateFromBlob();
            }
            default -> {
                getLogger().severe("Unknown build station: " + getConfigManager().getBuildStation());
            }
        }
    }

    public boolean tryUpdateFromGuizhan() {
        try {
            if (SlimefunAccelerator.getInstance().getConfigManager().isAutoUpdate() && getDescription().getVersion().startsWith("Build")) {
                GuizhanUpdater.start(this, getFile(), getInstance().getUsername(), getInstance().getRepo(), getInstance().getBranch());
                return true;
            }
        } catch (NoClassDefFoundError | NullPointerException | UnsupportedClassVersionError e) {
            getLogger().info(Lang.getMessage("load.failed-auto-update", "message", e.getMessage()));
            e.printStackTrace();
        }

        return false;
    }

    public boolean tryUpdateFromBlob() {
        if (getConfigManager().isAutoUpdate() && getDescription().getVersion().startsWith("DEV - ")) {
            new BlobBuildUpdater(this, getFile(), getInstance().getRepo(), "Dev").start();
            return true;
        }

        return false;
    }

    @Override
    public void onDisable() {
        enabledPlugin = false;
        getLogger().info("Disabling SlimefunAccelerator...");
        getLogger().info("Unloading listeners...");
        Accelerator.shutdown();
        Accelerates.shutdown();
        getListenerManager().unload();
        getLogger().info("SlimefunAccelerator disabled.");
    }

    @NotNull
    @Override
    public JavaPlugin getJavaPlugin() {
        return this;
    }

    @Nullable
    @Override
    public String getBugTrackerURL() {
        return "https://github.com/balugaq/SlimefunAccelerator/issues";
    }
}
