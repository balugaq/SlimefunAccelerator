package com.balugaq.slimefunaccelerator.api.enums;

import com.balugaq.slimefunaccelerator.api.annotations.Since;
import org.jetbrains.annotations.NotNull;

/**
 * This enum is used to notice the appearance of version of the configuration section
 *
 * @author balugaq
 * @see Since
 * @since 1.0
 */
public enum ConfigVersion {
    C_UNKNOWN,
    C_20250223_1,
    C_20250224_1,
    ;

    ConfigVersion() {

    }

    public static boolean isBefore(@NotNull ConfigVersion version1, @NotNull ConfigVersion version2) {
        return version1.ordinal() < version2.ordinal();
    }

    public static boolean isAfter(@NotNull ConfigVersion version1, @NotNull ConfigVersion version2) {
        return version1.ordinal() > version2.ordinal();
    }

    public static boolean isAtLeast(@NotNull ConfigVersion version1, @NotNull ConfigVersion version2) {
        return version1.ordinal() >= version2.ordinal();
    }

    public static boolean isAtMost(@NotNull ConfigVersion version1, @NotNull ConfigVersion version2) {
        return version1.ordinal() <= version2.ordinal();
    }
}
