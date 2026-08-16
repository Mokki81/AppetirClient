package com.appetir.friends;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * UUID is the primary identity. Nickname map is secondary and may be ambiguous
 * under rare name collisions — always prefer UUID for add/remove/toggle of online players.
 */
public final class FriendManager {

    private static volatile FriendManager instance;

    /** UUID string → display name */
    private final Map<String, String> byUuid = new HashMap<>();
    /** lowercase name → uuid OR null for legacy-only */
    private final Map<String, String> byName = new HashMap<>();
    /** lowercase name → original casing for legacy friends */
    private final Map<String, String> legacyDisplay = new HashMap<>();

    private final File file;
    private boolean dirty;
    private long dirtySince;
    private static final long SAVE_DEBOUNCE_MS = 500L;

    public FriendManager() {
        if (instance != null) {
            throw new IllegalStateException("[Appetir] FriendManager already constructed");
        }
        instance = this;
        File dir = new File(MinecraftClient.getInstance().runDirectory, "appetir");
        if (!dir.exists()) dir.mkdirs();
        this.file = new File(dir, "friends.txt");
        load();
    }

    public static FriendManager getInstance() {
        return instance;
    }

    public boolean add(String name) {
        if (name == null || name.trim().isEmpty()) return false;
        String n = name.trim();
        String key = n.toLowerCase(Locale.ROOT);
        if (byName.containsKey(key)) return false;
        byName.put(key, null);
        legacyDisplay.put(key, n);
        markDirty();
        return true;
    }

    public boolean add(PlayerEntity player) {
        if (player == null) return false;
        promoteToUuid(player.getUuid().toString(), player.getGameProfile().getName());
        markDirty();
        return true;
    }

    private void promoteToUuid(String uuid, String name) {
        String nameKey = name.toLowerCase(Locale.ROOT);

        // Drop any name keys that pointed at THIS uuid
        byName.entrySet().removeIf(e -> uuid.equals(e.getValue()));

        String prevOwner = byName.get(nameKey);
        if (prevOwner != null && !prevOwner.equals(uuid)) {
            // Collision: both UUIDs remain friends; name index points at newest
            System.err.println("[Appetir] Friend nickname collision: " + name
                    + " was " + prevOwner + ", now " + uuid);
        }

        legacyDisplay.remove(nameKey);
        byUuid.put(uuid, name);
        byName.put(nameKey, uuid);
    }

    /** Remove by display name (legacy or single UUID owner of that name). */
    public boolean remove(String name) {
        if (name == null) return false;
        String key = name.trim().toLowerCase(Locale.ROOT);
        if (!byName.containsKey(key)) return false;

        String id = byName.remove(key);
        legacyDisplay.remove(key);

        if (id != null) {
            // Only remove the UUID that currently owns this name index
            removeUuidInternal(id);
        }
        markDirty();
        return true;
    }

    /** Remove by player identity (UUID-first). */
    public boolean remove(PlayerEntity player) {
        if (player == null) return false;
        String uuid = player.getUuid().toString();
        if (byUuid.containsKey(uuid)) {
            return removeUuid(uuid);
        }
        // Legacy name-only entry
        return remove(player.getGameProfile().getName());
    }

    public boolean removeUuid(String uuid) {
        if (uuid == null || !byUuid.containsKey(uuid)) return false;
        removeUuidInternal(uuid);
        markDirty();
        return true;
    }

    private void removeUuidInternal(String uuid) {
        String name = byUuid.remove(uuid);
        // Clear name index only if it still points at this uuid
        if (name != null) {
            String key = name.toLowerCase(Locale.ROOT);
            if (uuid.equals(byName.get(key))) {
                byName.remove(key);
            }
        }
        byName.entrySet().removeIf(e -> uuid.equals(e.getValue()));
    }

    public boolean isFriend(String name) {
        return name != null && byName.containsKey(name.toLowerCase(Locale.ROOT));
    }

    public boolean isFriendReadOnly(PlayerEntity player) {
        if (player == null) return false;
        if (byUuid.containsKey(player.getUuid().toString())) return true;
        return isFriend(player.getGameProfile().getName());
    }

    public boolean isFriend(PlayerEntity player) {
        if (player == null) return false;
        String uuid = player.getUuid().toString();
        if (byUuid.containsKey(uuid)) {
            String newName = player.getGameProfile().getName();
            String oldName = byUuid.get(uuid);
            if (oldName == null || !oldName.equals(newName)) {
                if (oldName != null) {
                    String oldKey = oldName.toLowerCase(Locale.ROOT);
                    if (uuid.equals(byName.get(oldKey))) {
                        byName.remove(oldKey);
                    }
                }
                byUuid.put(uuid, newName);
                byName.put(newName.toLowerCase(Locale.ROOT), uuid);
                markDirty();
            }
            return true;
        }
        String name = player.getGameProfile().getName();
        if (isFriend(name)) {
            promoteToUuid(uuid, name);
            markDirty();
            return true;
        }
        return false;
    }

    public boolean isFriend(UUID uuid) {
        return uuid != null && byUuid.containsKey(uuid.toString());
    }

    public void toggle(String name) {
        if (isFriend(name)) remove(name);
        else add(name);
    }

    public void toggle(PlayerEntity player) {
        if (player == null) return;
        if (isFriend(player)) {
            remove(player); // UUID-first
        } else {
            add(player);
        }
    }

    public Set<String> getFriends() {
        Set<String> names = new HashSet<>();
        for (String display : byUuid.values()) {
            names.add(display);
        }
        for (Map.Entry<String, String> e : byName.entrySet()) {
            if (e.getValue() == null) {
                names.add(legacyDisplay.getOrDefault(e.getKey(), e.getKey()));
            }
        }
        return Collections.unmodifiableSet(names);
    }

    public void flushDirty() {
        if (!dirty) return;
        if (System.currentTimeMillis() - dirtySince < SAVE_DEBOUNCE_MS) return;
        dirty = false;
        save();
    }

    private void markDirty() {
        dirty = true;
        dirtySince = System.currentTimeMillis();
    }

    private void load() {
        byUuid.clear();
        byName.clear();
        legacyDisplay.clear();
        if (!file.exists()) return;
        try {
            for (String line : Files.readAllLines(file.toPath(), StandardCharsets.UTF_8)) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                int idx = line.indexOf(':');
                if (idx == 36) {
                    String uuid = line.substring(0, idx).trim();
                    String name = line.substring(idx + 1).trim();
                    try {
                        UUID.fromString(uuid);
                        byUuid.put(uuid, name);
                        byName.put(name.toLowerCase(Locale.ROOT), uuid);
                    } catch (IllegalArgumentException e) {
                        String key = line.toLowerCase(Locale.ROOT);
                        byName.put(key, null);
                        legacyDisplay.put(key, line);
                    }
                } else {
                    String key = line.toLowerCase(Locale.ROOT);
                    byName.put(key, null);
                    legacyDisplay.put(key, line);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void save() {
        try (BufferedWriter w = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            w.write("# Appetir Friends — uuid:name or name\n");
            Set<String> writtenUuid = new HashSet<>();
            Set<String> writtenName = new HashSet<>();

            for (Map.Entry<String, String> e : byUuid.entrySet()) {
                if (writtenUuid.add(e.getKey())) {
                    w.write(e.getKey() + ":" + e.getValue() + "\n");
                    writtenName.add(e.getValue().toLowerCase(Locale.ROOT));
                }
            }
            for (Map.Entry<String, String> e : byName.entrySet()) {
                if (e.getValue() != null) continue;
                if (writtenName.add(e.getKey())) {
                    w.write(legacyDisplay.getOrDefault(e.getKey(), e.getKey()) + "\n");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
