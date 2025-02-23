package com.balugaq.slimefunaccelerator.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@Setter
@ToString
@Data
public class AcceleratorSettings {
    private final boolean enabled;
    private final boolean async;
    private final int period;
    private final int delay;
    public AcceleratorSettings() {
        this.enabled = true;
        this.async = true;
        this.period = 10;
        this.delay = 10;
    }
}
