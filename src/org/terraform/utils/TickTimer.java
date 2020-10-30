package org.terraform.utils;

import java.util.HashMap;

public class TickTimer {
    public static final HashMap<String, Long> TIMINGS = new HashMap<>();

    private final String key;
    private final long start = System.currentTimeMillis();
    private long duration = -1;

    public TickTimer(String key) {
        this.key = key;
    }

    public void finish() {
        duration = System.currentTimeMillis() - this.start;
        TIMINGS.compute(key, (k, v) -> v == null ? duration : duration + v);
    }
}
