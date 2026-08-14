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
 * Config system with debounced disk writes.
 * File: .minecraft/appetir/config.json
 */
public class ConfigManager {

    private static ConfigManager instance;
    private final File configDir;
    private final File configFile;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private boolean dirty = false;
    private long dirtySince = 0L;
    /** Minimum ms between markDirty and actual write (coalesce rapid toggles). */
    private static final long DEBOUNCE_MS = 400L;

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
                            if (entry.has("key")) {
                                mod.setKeyRaw(entry.get("key").getAsInt());
                            }
                            if (entry.has("enabled") && entry.get("enabled").getAsBoolean()) {
                                // Full enable with callbacks (Fullbright etc.)
                                mod.setEnabled(true);
                            }
                        } catch (Exception e) {
                            System.err.println("[Appetir] Failed to apply config for module "
                                    + mod.getName() + ": " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }
            }

            dirty = false;
            System.out.println("[Appetir] Config loaded from " + configFile.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("[Appetir] Failed to load config: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /** Schedule a save; actual write happens in flushDirty() after debounce. */
    public void markDirty() {
        if (!dirty) {
            dirty = true;
            dirtySince = System.currentTimeMillis();
        }
    }

    /** Called each client tick — writes only after DEBOUNCE_MS of inactivity. */
    public void flushDirty() {
        if (!dirty) return;
        if (System.currentTimeMillis() - dirtySince < DEBOUNCE_MS) return;
        dirty = false;
        save();
    }

    /** Force immediate write (shutdown / explicit). */
    public void saveNow() {
        dirty = false;
        save();
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

    /** @deprecated use markDirty(); kept for call sites */
    public void saveQuiet() {
        markDirty();
    }

    public File getConfigFile() {
        return configFile;
    }
}
