package com.appetir.alt;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Offline alt account data.
 */
public class Alt {

    private final String name;
    private final String uuid;
    private long lastUsed;

    public Alt(String name) {
        this.name = name.trim();
        this.uuid = offlineUuid(this.name);
        this.lastUsed = System.currentTimeMillis();
    }

    public Alt(String name, String uuid, long lastUsed) {
        this.name = name;
        this.uuid = uuid;
        this.lastUsed = lastUsed;
    }

    /** Minecraft offline UUID algorithm with fixed UTF-8. */
    public static String offlineUuid(String name) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8)).toString();
    }

    public String getName() {
        return name;
    }

    public String getUuid() {
        return uuid;
    }

    public long getLastUsed() {
        return lastUsed;
    }

    public void touch() {
        this.lastUsed = System.currentTimeMillis();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Alt)) return false;
        return name.equalsIgnoreCase(((Alt) o).name);
    }

    @Override
    public int hashCode() {
        return name.toLowerCase().hashCode();
    }
}
