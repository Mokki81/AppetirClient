package com.appetir.alt;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Session;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Offline-only Alt Manager.
 * Saves alts to .minecraft/appetir_alts.txt
 */
public class AltManager {

    private static AltManager instance;
    private final List<Alt> alts = new ArrayList<>();
    private final File saveFile;

    public AltManager() {
        instance = this;
        File gameDir = MinecraftClient.getInstance().runDirectory;
        this.saveFile = new File(gameDir, "appetir_alts.txt");
        load();
    }

    public static AltManager getInstance() {
        return instance;
    }

    public List<Alt> getAlts() {
        return Collections.unmodifiableList(alts);
    }

    public boolean addAlt(String name) {
        if (name == null || name.trim().isEmpty() || name.length() > 16) return false;
        name = name.trim();
        if (!name.matches("[a-zA-Z0-9_]{3,16}")) return false;

        Alt alt = new Alt(name);
        if (alts.contains(alt)) return false;

        alts.add(0, alt);
        if (!save()) {
            System.err.println("[Appetir] Alt added in memory but failed to persist: " + name);
        }
        return true;
    }

    public boolean removeAlt(Alt alt) {
        boolean removed = alts.remove(alt);
        if (removed && !save()) {
            System.err.println("[Appetir] Alt removed in memory but failed to persist");
        }
        return removed;
    }

    /**
     * Removes alt by name. Returns true if something was removed from the list.
     * Persistence failure is logged but does not invert the remove result.
     */
    public boolean removeByName(String name) {
        if (name == null) return false;
        boolean removed = alts.removeIf(a -> a.getName().equalsIgnoreCase(name));
        if (removed && !save()) {
            System.err.println("[Appetir] Alt(s) removed in memory but failed to persist: " + name);
        }
        return removed;
    }

    public boolean login(Alt alt) {
        if (alt == null) return false;
        try {
            Session session = new Session(alt.getName(), alt.getUuid(), "0", "legacy");
            Field field = MinecraftClient.class.getDeclaredField("session");
            field.setAccessible(true);
            field.set(MinecraftClient.getInstance(), session);
            alt.touch();
            alts.remove(alt);
            alts.add(0, alt);
            if (!save()) {
                System.err.println("[Appetir] Login ok but alt list failed to persist");
            }
            return true;
        } catch (Exception e) {
            System.err.println("[Appetir] Alt login failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public String getCurrentName() {
        try {
            return MinecraftClient.getInstance().getSession().getUsername();
        } catch (Exception e) {
            return "?";
        }
    }

    private void load() {
        alts.clear();
        if (!saveFile.exists()) return;
        try {
            List<String> lines = Files.readAllLines(saveFile.toPath(), StandardCharsets.UTF_8);
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split(":", 3);
                if (parts.length >= 1) {
                    String n = parts[0];
                    String uuid = parts.length > 1 ? parts[1] : UUIDFromName(n);
                    long last = parts.length > 2 ? Long.parseLong(parts[2]) : System.currentTimeMillis();
                    alts.add(new Alt(n, uuid, last));
                }
            }
        } catch (Exception e) {
            System.err.println("[Appetir] Failed to load alts: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean save() {
        try {
            File parent = saveFile.getParentFile();
            if (parent != null && !parent.exists() && !parent.mkdirs()) {
                System.err.println("[Appetir] Cannot create alt save directory: " + parent);
                return false;
            }
            try (BufferedWriter w = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(saveFile), StandardCharsets.UTF_8))) {
                w.write("# Appetir Offline Alts - format: name:uuid:lastUsed\n");
                for (Alt a : alts) {
                    w.write(a.getName() + ":" + a.getUuid() + ":" + a.getLastUsed() + "\n");
                }
            }
            return true;
        } catch (Exception e) {
            System.err.println("[Appetir] Failed to save alts: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private static String UUIDFromName(String name) {
        return java.util.UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes()).toString();
    }
}
