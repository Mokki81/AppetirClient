package com.appetir.config;

import com.appetir.AppetirClient;
import com.appetir.client.ClientMode;
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

public class ConfigManager {

    private static ConfigManager instance;
    private final File configDir;
    private final File configFile;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private boolean dirty = false;
    private long dirtySince = 0L;
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

    public static ConfigManager getInstance() { return instance; }

    public void load() {
        if (!configFile.exists()) {
            System.out.println("[Appetir] No config found, using defaults");
            return;
        }
        try {
            String json = new String(Files.readAllBytes(configFile.toPath()), StandardCharsets.UTF_8);
            JsonObject root = new JsonParser().parse(json).getAsJsonObject();

            if (root.has("hudVisible"))
                AppetirClient.hudVisible = root.get("hudVisible").getAsBoolean();

            if (root.has("clientMode")) {
                try {
                    ClientMode.setRaw(ClientMode.Mode.valueOf(root.get("clientMode").getAsString()));
                } catch (IllegalArgumentException e) {
                    System.err.println("[Appetir] Unknown clientMode in config");
                }
            }

            if (root.has("theme")) {
                String themeName = root.get("theme").getAsString();
                try {
                    ThemeManager.setCurrent(ThemeManager.Theme.valueOf(themeName));
                } catch (IllegalArgumentException e) {
                    System.err.println("[Appetir] Unknown theme: " + themeName);
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
                            if (entry.has("key")) mod.setKeyRaw(entry.get("key").getAsInt());
                            if (entry.has("enabled") && entry.get("enabled").getAsBoolean()) {
                                if (ClientMode.isModuleAllowed(mod)) {
                                    mod.setEnabled(true);
                                }
                            }
                        } catch (Exception e) {
                            System.err.println("[Appetir] Config module " + mod.getName() + ": " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }
            }

            if (ClientMode.isClean()) {
                // ensure restricted stay off after load
                ModuleManager mm = ModuleManager.getInstance();
                if (mm != null) {
                    for (Module m : mm.getModules()) {
                        if (!ClientMode.isModuleAllowed(m) && m.isEnabled()) m.setEnabled(false);
                    }
                }
            }

            dirty = false;
            System.out.println("[Appetir] Config loaded");
        } catch (Exception e) {
            System.err.println("[Appetir] Failed to load config: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void markDirty() {
        if (!dirty) {
            dirty = true;
            dirtySince = System.currentTimeMillis();
        }
    }

    public void flushDirty() {
        if (!dirty) return;
        if (System.currentTimeMillis() - dirtySince < DEBOUNCE_MS) return;
        dirty = false;
        save();
    }

    public void saveNow() {
        dirty = false;
        save();
    }

    public void save() {
        try {
            if (!configDir.exists() && !configDir.mkdirs()) return;

            JsonObject root = new JsonObject();
            root.addProperty("version", AppetirClient.VERSION);
            root.addProperty("hudVisible", AppetirClient.hudVisible);
            root.addProperty("clientMode", ClientMode.get().name());
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

            try (Writer writer = new OutputStreamWriter(new FileOutputStream(configFile), StandardCharsets.UTF_8)) {
                writer.write(gson.toJson(root));
            }
        } catch (Exception e) {
            System.err.println("[Appetir] Failed to save config: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void saveQuiet() { markDirty(); }
    public File getConfigFile() { return configFile; }
}
