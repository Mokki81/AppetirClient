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

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Config system — modules, keybinds, theme, HUD.
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

        if (!configDir.exists() && !configDir.mkdirs()) {
            System.err.println("[Appetir] Failed to create config dir: " + configDir.getAbsolutePath());
        }
    }

    public static ConfigManager getInstance() {
        return instance;
    }

    public void load() {
        if (!configFile.exists()) {
            System.out.println("[Appetir] No config found, using defaults");
            return;
        }

        try {
            String json = new String(Files.readAllBytes(configFile.toPath()), StandardCharsets.UTF_8);
            JsonObject root = new JsonParser().parse(json).getAsJsonObject();

            if (root.has("hudVisible")) {
                AppetirClient.hudVisible = root.get("hudVisible").getAsBoolean();
            }

            if (root.has("theme")) {
                String themeName = root.get("theme").getAsString();
                try {
                    ThemeManager.Theme theme = ThemeManager.Theme.valueOf(themeName);
                    ThemeManager.setCurrent(theme);
                } catch (IllegalArgumentException e) {
                    System.err.println("[Appetir] Unknown theme in config: '" + themeName
                            + "' — keeping default " + ThemeManager.getCurrent().name());
                }
            }

            if (root.has("modules")) {
                JsonObject modules = root.getAsJsonObject("modules");
                ModuleManager mm = ModuleManager.getInstance();
                if (mm != null) {
                    for (Module mod : mm.getModules()) {
                        if (!modules.has(mod.getName())) continue;
                        JsonObject entry = modules.getAsJsonObject(mod.getName());
                        try {
                            if (entry.has("enabled") && entry.get("enabled").getAsBoolean()) {
                                mod.setEnabled(true);
                            }
                            if (entry.has("key")) {
                                mod.setKey(entry.get("key").getAsInt());
                            }
                        } catch (Exception e) {
                            System.err.println("[Appetir] Failed to apply config for module "
                                    + mod.getName() + ": " + e.getMessage());
                            e.printStackTrace();
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

    public void save() {
        try {
            if (!configDir.exists() && !configDir.mkdirs()) {
                System.err.println("[Appetir] Cannot create config directory");
                return;
            }

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
        } catch (Exception e) {
            System.err.println("[Appetir] Failed to save config: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Save without spamming success logs. Errors still go to stderr via save().
     */
    public void saveQuiet() {
        save();
    }

    public File getConfigFile() {
        return configFile;
    }
}
