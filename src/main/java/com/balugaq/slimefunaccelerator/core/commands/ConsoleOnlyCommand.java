package com.balugaq.slimefunaccelerator.core.commands;

import com.balugaq.slimefunaccelerator.api.utils.Lang;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class ConsoleOnlyCommand extends SubCommand {
    public ConsoleOnlyCommand(@NotNull JavaPlugin plugin) {
        super(plugin);
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof ConsoleCommandSender consoleCommandSender)) {
            sender.sendMessage(Lang.getMessage("commands.console-only"));
            return false;
        }

        return onCommand(consoleCommandSender, command, label, args);
    }

    @Nullable
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return null;
        }

        return onTabComplete(player, command, label, args);
    }

    public abstract boolean onCommand(@NotNull ConsoleCommandSender console, @NotNull Command command, @NotNull String label, @NotNull String[] args);

    @Nullable
    public abstract List<String> onTabComplete(@NotNull ConsoleCommandSender console, @NotNull Command command, @NotNull String label, @NotNull String[] args);
}
