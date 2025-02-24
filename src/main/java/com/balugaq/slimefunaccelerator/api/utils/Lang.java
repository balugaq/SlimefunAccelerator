package com.balugaq.slimefunaccelerator.api.utils;

import com.balugaq.slimefunaccelerator.implementation.SlimefunAccelerator;
import org.jetbrains.annotations.NotNull;

public class Lang {
    public static @NotNull String getMessage(String path) {
        return SlimefunAccelerator.getInstance().getLocalizationService().getString("messages." + path);
    }

    public static @NotNull String getMessage(String path, Object @NotNull ... args) {
        String message = getMessage(path);
        for (int i = 0; i < args.length; i += 2) {
            message = message.replace("{" + args[i] + "}", args[i + 1].toString());
        }
        return message;
    }
}
