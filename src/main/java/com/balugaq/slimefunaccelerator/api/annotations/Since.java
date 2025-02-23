package com.balugaq.slimefunaccelerator.api.annotations;

import com.balugaq.slimefunaccelerator.api.enums.ConfigVersion;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This annotation is used to notice the appearance of version of the configuration section
 *
 * @author balugaq
 * @see ConfigVersion
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Since {
    ConfigVersion value();
}
