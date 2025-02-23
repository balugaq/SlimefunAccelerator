package com.balugaq.slimefunaccelerator.core.managers;

import com.balugaq.slimefunaccelerator.api.utils.Lang;
import com.balugaq.slimefunaccelerator.core.commands.SubCommand;
import com.balugaq.slimefunaccelerator.core.commands.subcommands.HelpCommand;
import com.balugaq.slimefunaccelerator.core.commands.subcommands.IdCommand;
import com.balugaq.slimefunaccelerator.core.commands.subcommands.ReloadCommand;
import com.balugaq.slimefunaccelerator.core.commands.subcommands.VersionCommand;
import com.google.common.base.Preconditions;
import lombok.Getter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Getter
public class CommandManager implements TabExecutor {
    private static final String ROOT_COMMAND = "slimefunaccelerator";
    private final @NotNull JavaPlugin plugin;
    private final List<SubCommand> subCommands = new ArrayList<>();
    private final @NotNull SubCommand defaultCommand;

    public CommandManager(@NotNull JavaPlugin plugin) {
        Preconditions.checkNotNull(plugin, "Plugin cannot be null");
        this.plugin = plugin;
        this.defaultCommand = new HelpCommand(plugin);
        setup();
        registerCommand();
    }

    public void setup() {
        subCommands.add(new HelpCommand(plugin));
        subCommands.add(new ReloadCommand(plugin));
        subCommands.add(new VersionCommand(plugin));
        subCommands.add(new IdCommand(plugin));
    }

    public void registerCommand() {
        PluginCommand command = plugin.getCommand(ROOT_COMMAND);
        if (command == null) {
            plugin.getLogger().severe("Command \"" + ROOT_COMMAND + "\" not found!");
            return;
        } else {
            command.setExecutor(this);
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(Lang.getMessage("commands.no-permission"));
            return true;
        }

        for (SubCommand subCommand : subCommands) {
            if (subCommand.canCommand(sender, command, label, args)) {
                subCommand.onCommand(sender, command, label, args);
                return true;
            }
        }

        return defaultCommand.onCommand(sender, command, label, args);
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        List<String> subCommandNames = subCommands.stream().map(SubCommand::getName).toList();
        if (args.length <= 1) {
            return subCommandNames;
        }

        List<String> completions = new ArrayList<>();
        for (SubCommand subCommand : subCommands) {
            List<String> subCompletion = subCommand.onTabComplete(sender, command, label, args);
            if (subCompletion != null) {
                completions.addAll(subCompletion);
            }
        }
        return completions;
    }
}
