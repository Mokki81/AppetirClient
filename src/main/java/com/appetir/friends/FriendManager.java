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

public final class FriendManager {

    private static volatile FriendManager instance;

    /** Real UUID string → display name */
    private final Map<String, String> byUuid = new HashMap<>();
    /** lowercase name → uuid string OR "name:key" for legacy */
    private final Map<String, String> byName = new HashMap<>();
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
        byName.put(key, "name:" + key);
        markDirty();
        return true;
    }

    public boolean add(PlayerEntity player) {
        if (player == null) return false;
        String uuid = player.getUuid().toString();
        String name = player.getGameProfile().getName();
        promoteToUuid(uuid, name);
        markDirty();
        return true;
    }

    /** Move any legacy name entries for this player under the real UUID. */
    private void promoteToUuid(String uuid, String name) {
        String nameKey = name.toLowerCase(Locale.ROOT);

        // Remove all name keys that pointed at this uuid
        byName.entrySet().removeIf(e -> uuid.equals(e.getValue()));

        // Remove legacy name: entry for this name
        byName.remove(nameKey);

        // Drop any leftover legacy keys that stored the same display name as value under name:
        byUuid.entrySet().removeIf(e ->
                e.getKey().startsWith("name:") && name.equalsIgnoreCase(e.getValue()));

        byUuid.put(uuid, name);
        byName.put(nameKey, uuid);
    }

    public boolean remove(String name) {
        if (name == null) return false;
        String key = name.trim().toLowerCase(Locale.ROOT);
        String id = byName.remove(key);
        if (id == null) return false;

        if (id.startsWith("name:")) {
            // pure legacy
        } else {
            byUuid.remove(id);
            byName.entrySet().removeIf(e -> id.equals(e.getValue()));
        }
        markDirty();
        return true;
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
                    byName.remove(oldName.toLowerCase(Locale.ROOT));
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
        if (isFriend(player)) {
            remove(player.getGameProfile().getName());
        } else {
            add(player);
        }
    }

    /** Unique display names only (no duplicate legacy + UUID). */
    public Set<String> getFriends() {
        Set<String> names = new HashSet<>();
        // Prefer UUID entries
        for (Map.Entry<String, String> e : byUuid.entrySet()) {
            if (!e.getKey().startsWith("name:")) {
                names.add(e.getValue());
            }
        }
        // Legacy name-only not yet promoted
        for (Map.Entry<String, String> e : byName.entrySet()) {
            if (e.getValue().startsWith("name:")) {
                names.add(e.getKey()); // stored lowercase — improve: keep original casing if needed
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
                        byName.put(key, "name:" + key);
                    }
                } else {
                    String key = line.toLowerCase(Locale.ROOT);
                    byName.put(key, "name:" + key);
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
                if (e.getKey().startsWith("name:")) continue;
                if (writtenUuid.add(e.getKey())) {
                    w.write(e.getKey() + ":" + e.getValue() + "\n");
                    writtenName.add(e.getValue().toLowerCase(Locale.ROOT));
                }
            }
            for (Map.Entry<String, String> e : byName.entrySet()) {
                if (!e.getValue().startsWith("name:")) continue;
                if (writtenName.add(e.getKey())) {
                    w.write(e.getKey() + "\n");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
