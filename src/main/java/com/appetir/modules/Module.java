package com.appetir.modules;

import com.appetir.config.ConfigManager;
import com.appetir.settings.Setting;
import com.appetir.util.NotificationManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Base class for all client modules.
 */
public abstract class Module {

    private final String name;
    private final String description;
    private final Category category;
    private boolean enabled;
    private int key = -1;
    private final List<Setting> settings = new ArrayList<>();

    public Module(String name, String description, Category category) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.enabled = false;
    }

    protected void addSetting(Setting setting) {
        settings.add(setting);
    }

    public List<Setting> getSettings() {
        return Collections.unmodifiableList(settings);
    }

    public boolean hasSettings() {
        return !settings.isEmpty();
    }

    public void onEnable() {}
    public void onDisable() {}
    public void onTick() {}

    public final void toggle() {
        setEnabled(!enabled);
    }

    public final void setEnabled(boolean value) {
        if (this.enabled == value) return;
        this.enabled = value;
        try {
            if (value) onEnable();
            else onDisable();
            NotificationManager.pushModule(name, value);
        } catch (Exception e) {
            System.err.println("[Appetir] Error in module " + name + ": " + e.getMessage());
            e.printStackTrace();
        }

        ConfigManager cm = ConfigManager.getInstance();
        if (cm != null) cm.saveQuiet();
    }

    public boolean isEnabled() { return enabled; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Category getCategory() { return category; }
    public int getKey() { return key; }

    public void setKey(int key) {
        this.key = key;
        ConfigManager cm = ConfigManager.getInstance();
        if (cm != null) cm.saveQuiet();
    }

    public enum Category {
        COMBAT("Combat"),
        MOVEMENT("Movement"),
        RENDER("Render"),
        WORLD("World"),
        MISC("Misc");

        public final String displayName;

        Category(String displayName) {
            this.displayName = displayName;
        }
    }
}
