package com.appetir.alt;

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
        // Offline UUID (same algorithm as Minecraft)
        this.uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + this.name).getBytes()).toString();
        this.lastUsed = System.currentTimeMillis();
    }

    public Alt(String name, String uuid, long lastUsed) {
        this.name = name;
        this.uuid = uuid;
        this.lastUsed = lastUsed;
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
