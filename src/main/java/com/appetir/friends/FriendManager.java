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
 * Friends by UUID (primary) + name for display / offline fallback.
 * File format: uuid:name  OR  name (legacy)
 */
public class FriendManager {

    private static FriendManager instance;

    /** uuid string → last known name */
    private final Map<String, String> byUuid = new HashMap<>();
    /** lowercase name → uuid (or name key if uuid unknown) */
    private final Map<String, String> byName = new HashMap<>();
    private final File file;

    public FriendManager() {
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
        byName.put(key, key); // name-only until we see the player
        byUuid.put("name:" + key, n);
        save();
        return true;
    }

    public boolean add(PlayerEntity player) {
        if (player == null) return false;
        String uuid = player.getUuid().toString();
        String name = player.getGameProfile().getName();
        byUuid.put(uuid, name);
        byName.put(name.toLowerCase(Locale.ROOT), uuid);
        // drop legacy name-only entry if any
        byUuid.remove("name:" + name.toLowerCase(Locale.ROOT));
        save();
        return true;
    }

    public boolean remove(String name) {
        if (name == null) return false;
        String key = name.trim().toLowerCase(Locale.ROOT);
        String uuid = byName.remove(key);
        if (uuid == null) return false;
        byUuid.remove(uuid);
        byUuid.remove("name:" + key);
        save();
        return true;
    }

    public boolean isFriend(String name) {
        return name != null && byName.containsKey(name.toLowerCase(Locale.ROOT));
    }

    public boolean isFriend(PlayerEntity player) {
        if (player == null) return false;
        String uuid = player.getUuid().toString();
        if (byUuid.containsKey(uuid)) {
            // refresh name
            byUuid.put(uuid, player.getGameProfile().getName());
            byName.put(player.getGameProfile().getName().toLowerCase(Locale.ROOT), uuid);
            return true;
        }
        // legacy name match → upgrade to UUID
        String name = player.getGameProfile().getName();
        if (isFriend(name)) {
            add(player);
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

    public Set<String> getFriends() {
        // display names
        Set<String> names = new HashSet<>();
        for (Map.Entry<String, String> e : byUuid.entrySet()) {
            names.add(e.getValue());
        }
        return Collections.unmodifiableSet(names);
    }

    private void load() {
        byUuid.clear();
        byName.clear();
        if (!file.exists()) return;
        try {
            for (String line : Files.readAllLines(file.toPath(), StandardCharsets.UTF_8)) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                if (line.contains(":") && line.indexOf(':') == 36) {
                    // uuid:name
                    int idx = line.indexOf(':');
                    String uuid = line.substring(0, idx).trim();
                    String name = line.substring(idx + 1).trim();
                    try {
                        UUID.fromString(uuid);
                        byUuid.put(uuid, name);
                        byName.put(name.toLowerCase(Locale.ROOT), uuid);
                    } catch (IllegalArgumentException ignored) {
                        // fall through to name-only
                        byName.put(line.toLowerCase(Locale.ROOT), "name:" + line.toLowerCase(Locale.ROOT));
                        byUuid.put("name:" + line.toLowerCase(Locale.ROOT), line);
                    }
                } else {
                    String key = line.toLowerCase(Locale.ROOT);
                    byName.put(key, "name:" + key);
                    byUuid.put("name:" + key, line);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void save() {
        try (BufferedWriter w = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            w.write("# Appetir Friends — format: uuid:name  or  name\n");
            Set<String> written = new HashSet<>();
            for (Map.Entry<String, String> e : byUuid.entrySet()) {
                String id = e.getKey();
                String name = e.getValue();
                if (id.startsWith("name:")) {
                    if (written.add(name.toLowerCase(Locale.ROOT))) {
                        w.write(name + "\n");
                    }
                } else {
                    if (written.add(id)) {
                        w.write(id + ":" + name + "\n");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
