package com.balugaq.slimefunaccelerator.api;

import lombok.AllArgsConstructor;
import lombok.Data;
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
    private final int delay;
    private final int period;
    private final boolean removeOriginalTicker;
    private final boolean enabledExtraTicker;
    private final boolean tickUnload;
    private final int extraTickerPeriod;
    private final int extraTickerDelay;

    public AcceleratorSettings() {
        this.enabled = true;
        this.async = true;
        this.delay = 10;
        this.period = 10;
        this.removeOriginalTicker = false;
        this.enabledExtraTicker = false;
        this.tickUnload = false;
        this.extraTickerPeriod = 10;
        this.extraTickerDelay = 10;
    }
}
