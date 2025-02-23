package com.balugaq.slimefunaccelerator.core.commands.subcommands;

import com.balugaq.slimefunaccelerator.api.utils.ClipboardUtil;
import com.balugaq.slimefunaccelerator.api.utils.Lang;
import com.balugaq.slimefunaccelerator.core.commands.PlayerOnlyCommand;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class IdCommand extends PlayerOnlyCommand {
    public IdCommand(@NotNull JavaPlugin plugin) {
        super(plugin);
    }

    @Override
    public @NotNull String getName() {
        return "id";
    }

    @Override
    public boolean canCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 0) {
            return false;
        }
        return getName().equalsIgnoreCase(args[0]);
    }

    @Override
    public boolean onCommand(@NotNull Player player, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        SlimefunItem item = SlimefunItem.getByItem(player.getInventory().getItemInMainHand());
        if (item == null) {
            player.sendMessage(Lang.getMessage("commands.not-slimefun-item-in-hand"));
            return true;
        } else {
            ClipboardUtil.send(player, Lang.getMessage("commands.id.success",
                    "id", item.getId()), Lang.getMessage("commands.click-to-copy"), item.getId());
            return true;
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull Player player, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return null;
    }
}
