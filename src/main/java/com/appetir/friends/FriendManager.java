package com.appetir.friends;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Simple friends list. Used by NoFriendDamage, KillAura, etc.
 */
public class FriendManager {

    private static FriendManager instance;
    private final Set<String> friends = new HashSet<>();
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
        boolean added = friends.add(name.trim().toLowerCase(Locale.ROOT));
        if (added) save();
        return added;
    }

    public boolean remove(String name) {
        boolean removed = friends.remove(name.trim().toLowerCase(Locale.ROOT));
        if (removed) save();
        return removed;
    }

    public boolean isFriend(String name) {
        return name != null && friends.contains(name.toLowerCase(Locale.ROOT));
    }

    public boolean isFriend(PlayerEntity player) {
        return player != null && isFriend(player.getEntityName());
    }

    public void toggle(String name) {
        if (isFriend(name)) remove(name);
        else add(name);
    }

    public Set<String> getFriends() {
        return Collections.unmodifiableSet(friends);
    }

    private void load() {
        friends.clear();
        if (!file.exists()) return;
        try {
            for (String line : Files.readAllLines(file.toPath(), StandardCharsets.UTF_8)) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    friends.add(line.toLowerCase(Locale.ROOT));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void save() {
        try {
            try (BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
                w.write("# Appetir Friends\n");
                for (String f : friends) {
                    w.write(f + "\n");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
