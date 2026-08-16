package com.appetir.config;

import com.appetir.AppetirClient;
import com.appetir.client.ClientMode;
import com.appetir.gui.ThemeManager;
import com.appetir.modules.Module;
import com.appetir.modules.ModuleManager;
import com.appetir.settings.BooleanSetting;
import com.appetir.settings.ModeSetting;
import com.appetir.settings.NumberSetting;
import com.appetir.settings.Setting;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.MinecraftClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {

    private static ConfigManager instance;
    private final File configDir;
    private final File configFile;
    private final File configTmp;
    private final File configBak;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private boolean dirty = false;
    private long dirtySince = 0L;
    private static final long DEBOUNCE_MS = 400L;

    public ConfigManager() {
        instance = this;
        File gameDir = MinecraftClient.getInstance().runDirectory;
        this.configDir = new File(gameDir, "appetir");
        this.configFile = new File(configDir, "config.json");
        this.configTmp = new File(configDir, "config.json.tmp");
        this.configBak = new File(configDir, "config.json.bak");
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
            applyRoot(root);
            dirty = false;
            System.out.println("[Appetir] Config loaded");
        } catch (Exception e) {
            System.err.println("[Appetir] Failed to load config: " + e.getMessage());
            e.printStackTrace();
            quarantineBrokenConfig();
        }
    }

    private void quarantineBrokenConfig() {
        try {
            File broken = new File(configDir, "config.json.broken." + System.currentTimeMillis());
            if (configFile.exists()) {
                Files.move(configFile.toPath(), broken.toPath(), StandardCopyOption.REPLACE_EXISTING);
                System.err.println("[Appetir] Broken config moved to " + broken.getName());
            }
        } catch (Exception e) {
            System.err.println("[Appetir] Could not quarantine broken config: " + e.getMessage());
        }
    }

    private void applyRoot(JsonObject root) {
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
            try {
                ThemeManager.setCurrent(ThemeManager.Theme.valueOf(root.get("theme").getAsString()));
            } catch (IllegalArgumentException e) {
                System.err.println("[Appetir] Unknown theme: " + root.get("theme").getAsString());
            }
        }

        if (!root.has("modules")) return;
        JsonObject modules = root.getAsJsonObject("modules");
        ModuleManager mm = ModuleManager.getInstance();
        if (mm == null) return;

        Map<Integer, Module> keyOwners = new HashMap<>();

        for (Module mod : mm.getModules()) {
            if (!modules.has(mod.getName())) continue;
            JsonObject entry = modules.getAsJsonObject(mod.getName());
            try {
                if (entry.has("settings")) {
                    loadSettings(mod, entry.getAsJsonObject("settings"));
                }
                if (entry.has("key")) {
                    int k = entry.get("key").getAsInt();
                    if (k >= 0) {
                        if (keyOwners.containsKey(k)) {
                            System.err.println("[Appetir] Duplicate key " + k + " for "
                                    + mod.getName() + " (kept on " + keyOwners.get(k).getName() + ")");
                            mod.setKeyRaw(-1);
                        } else {
                            mod.setKeyRaw(k);
                            keyOwners.put(k, mod);
                        }
                    } else {
                        mod.setKeyRaw(-1);
                    }
                }
            } catch (Exception e) {
                System.err.println("[Appetir] Config module " + mod.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        for (Module mod : mm.getModules()) {
            if (!modules.has(mod.getName())) continue;
            JsonObject entry = modules.getAsJsonObject(mod.getName());
            try {
                if (entry.has("enabled") && entry.get("enabled").getAsBoolean()) {
                    if (ClientMode.isModuleAllowed(mod)) {
                        mod.setEnabled(true);
                    }
                }
            } catch (Exception e) {
                System.err.println("[Appetir] Enable " + mod.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        if (ClientMode.isClean()) {
            for (Module m : mm.getModules()) {
                if (!ClientMode.isModuleAllowed(m) && m.isEnabled()) m.setEnabled(false);
            }
        }
    }

    private void loadSettings(Module mod, JsonObject settingsObj) {
        for (Setting s : mod.getSettings()) {
            if (!settingsObj.has(s.getName())) continue;
            JsonElement el = settingsObj.get(s.getName());
            try {
                if (s instanceof BooleanSetting) {
                    ((BooleanSetting) s).set(el.getAsBoolean());
                } else if (s instanceof NumberSetting) {
                    ((NumberSetting) s).set(el.getAsDouble());
                } else if (s instanceof ModeSetting) {
                    ((ModeSetting) s).set(el.getAsString());
                }
            } catch (Exception e) {
                System.err.println("[Appetir] Bad setting " + mod.getName() + "." + s.getName()
                        + ": " + e.getMessage());
            }
        }
    }

    public void markDirty() {
        dirty = true;
        dirtySince = System.currentTimeMillis();
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

                    if (mod.hasSettings()) {
                        JsonObject settings = new JsonObject();
                        for (Setting s : mod.getSettings()) {
                            if (s instanceof BooleanSetting) {
                                settings.addProperty(s.getName(), ((BooleanSetting) s).get());
                            } else if (s instanceof NumberSetting) {
                                settings.addProperty(s.getName(), ((NumberSetting) s).get());
                            } else if (s instanceof ModeSetting) {
                                settings.addProperty(s.getName(), ((ModeSetting) s).get());
                            }
                        }
                        entry.add("settings", settings);
                    }
                    modules.add(mod.getName(), entry);
                }
            }
            root.add("modules", modules);

            String json = gson.toJson(root);

            // Atomic write: tmp → flush → bak old → move tmp to final
            try (Writer writer = new OutputStreamWriter(new FileOutputStream(configTmp), StandardCharsets.UTF_8)) {
                writer.write(json);
                writer.flush();
            }

            if (configFile.exists()) {
                try {
                    Files.copy(configFile.toPath(), configBak.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (Exception e) {
                    System.err.println("[Appetir] Could not write config.bak: " + e.getMessage());
                }
            }

            Files.move(configTmp.toPath(), configFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (Exception e) {
            // ATOMIC_MOVE may fail on some FS — fallback non-atomic
            try {
                if (configTmp.exists()) {
                    Files.move(configTmp.toPath(), configFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (Exception e2) {
                System.err.println("[Appetir] Failed to save config: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void saveQuiet() { markDirty(); }
    public File getConfigFile() { return configFile; }
}
