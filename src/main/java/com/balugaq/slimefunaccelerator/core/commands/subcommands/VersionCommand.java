package com.balugaq.slimefunaccelerator.core.commands.subcommands;

import com.balugaq.slimefunaccelerator.api.utils.Lang;
import com.balugaq.slimefunaccelerator.core.commands.SubCommand;
import com.balugaq.slimefunaccelerator.implementation.SlimefunAccelerator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class VersionCommand extends SubCommand {
    public VersionCommand(@NotNull JavaPlugin plugin) {
        super(plugin);
    }

    @Override
    public @NotNull String getName() {
        return "version";
    }

    @Override
    public boolean canCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 0) {
            return false;
        }
        return getName().equalsIgnoreCase(args[0]);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        sender.sendMessage(Lang.getMessage("commands.version.success",
                "version", SlimefunAccelerator.getInstance().getConfigManager().getConfigVersion(),
                "build_station", SlimefunAccelerator.getInstance().getConfigManager().getBuildStation()));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return null;
    }
}
