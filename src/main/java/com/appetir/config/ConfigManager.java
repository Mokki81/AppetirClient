package com.appetir.config;

import com.appetir.AppetirClient;
import com.appetir.gui.ThemeManager;
import com.appetir.modules.Module;
import com.appetir.modules.ModuleManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.MinecraftClient;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Config system — saves/loads module states, keybinds, theme and HUD visibility.
 * File: .minecraft/appetir/config.json
 */
public class ConfigManager {

    private static ConfigManager instance;
    private final File configDir;
    private final File configFile;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public ConfigManager() {
        instance = this;
        File gameDir = MinecraftClient.getInstance().runDirectory;
        this.configDir = new File(gameDir, "appetir");
        this.configFile = new File(configDir, "config.json");

        if (!configDir.exists()) {
            configDir.mkdirs();
        }
    }

    public static ConfigManager getInstance() {
        return instance;
    }

    /**
     * Load config and apply to modules / theme / hud.
     * Call after ModuleManager is created.
     */
    public void load() {
        if (!configFile.exists()) {
            System.out.println("[Appetir] No config found, using defaults");
            return;
        }

        try {
            String json = new String(Files.readAllBytes(configFile.toPath()), StandardCharsets.UTF_8);
            JsonObject root = new JsonParser().parse(json).getAsJsonObject();

            // HUD
            if (root.has("hudVisible")) {
                AppetirClient.hudVisible = root.get("hudVisible").getAsBoolean();
            }

            // Theme
            if (root.has("theme")) {
                try {
                    ThemeManager.Theme theme = ThemeManager.Theme.valueOf(root.get("theme").getAsString());
                    ThemeManager.setCurrent(theme);
                } catch (Exception ignored) {}
            }

            // Modules
            if (root.has("modules")) {
                JsonObject modules = root.getAsJsonObject("modules");
                ModuleManager mm = ModuleManager.getInstance();
                if (mm != null) {
                    for (Module mod : mm.getModules()) {
                        if (modules.has(mod.getName())) {
                            JsonObject entry = modules.getAsJsonObject(mod.getName());
                            if (entry.has("enabled")) {
                                // set without firing onEnable/onDisable during load if possible,
                                // but we want onEnable for things like Fullbright
                                boolean enabled = entry.get("enabled").getAsBoolean();
                                if (enabled) {
                                    mod.setEnabled(true);
                                }
                            }
                            if (entry.has("key")) {
                                mod.setKey(entry.get("key").getAsInt());
                            }
                        }
                    }
                }
            }

            System.out.println("[Appetir] Config loaded from " + configFile.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("[Appetir] Failed to load config: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Save current state to disk.
     */
    public void save() {
        try {
            if (!configDir.exists()) configDir.mkdirs();

            JsonObject root = new JsonObject();
            root.addProperty("version", AppetirClient.VERSION);
            root.addProperty("hudVisible", AppetirClient.hudVisible);
            root.addProperty("theme", ThemeManager.getCurrent().name());

            JsonObject modules = new JsonObject();
            ModuleManager mm = ModuleManager.getInstance();
            if (mm != null) {
                for (Module mod : mm.getModules()) {
                    JsonObject entry = new JsonObject();
                    entry.addProperty("enabled", mod.isEnabled());
                    entry.addProperty("key", mod.getKey());
                    modules.add(mod.getName(), entry);
                }
            }
            root.add("modules", modules);

            String json = gson.toJson(root);
            try (Writer writer = new OutputStreamWriter(new FileOutputStream(configFile), StandardCharsets.UTF_8)) {
                writer.write(json);
            }

            System.out.println("[Appetir] Config saved");
        } catch (Exception e) {
            System.err.println("[Appetir] Failed to save config: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Quick save — called after toggles. Debounced-ish by just writing.
     */
    public void saveQuiet() {
        try {
            save();
        } catch (Exception ignored) {}
    }

    public File getConfigFile() {
        return configFile;
    }
}
