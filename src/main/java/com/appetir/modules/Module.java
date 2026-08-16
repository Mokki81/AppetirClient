package com.appetir.modules;

import com.appetir.client.ClientMode;
import com.appetir.config.ConfigManager;
import com.appetir.settings.Setting;
import com.appetir.util.NotificationManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

        // Clean mode blocks restricted modules from enabling
        if (value && !ClientMode.isModuleAllowed(this)) {
            NotificationManager.push(name, "Blocked in Clean mode");
            return;
        }

        boolean previous = this.enabled;
        try {
            if (value) onEnable();
            else onDisable();
            this.enabled = value;
            NotificationManager.pushModule(name, value);
            markConfigDirty();
        } catch (Exception e) {
            this.enabled = previous;
            System.err.println("[Appetir] Error in module " + name
                    + " (" + e.getClass().getSimpleName() + "): "
                    + (e.getMessage() != null ? e.getMessage() : "(no message)")
                    + " — state rolled back to " + previous);
            e.printStackTrace();
        }
    }

    public final void setEnabledRaw(boolean value) {
        this.enabled = value;
    }

    public boolean isEnabled() { return enabled; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Category getCategory() { return category; }
    public int getKey() { return key; }

    public void setKey(int key) {
        if (this.key == key) return;
        if (key >= 0) {
            ModuleManager mm = ModuleManager.getInstance();
            if (mm != null) {
                for (Module m : mm.getModules()) {
                    if (m != this && m.getKey() == key) m.key = -1;
                }
            }
        }
        this.key = key;
        markConfigDirty();
    }

    public void setKeyRaw(int key) {
        this.key = key;
    }

    private static void markConfigDirty() {
        ConfigManager cm = ConfigManager.getInstance();
        if (cm != null) cm.markDirty();
    }

    public enum Category {
        COMBAT("Combat"),
        MOVEMENT("Movement"),
        RENDER("Render"),
        WORLD("World"),
        MISC("Misc");

        public final String displayName;
        Category(String displayName) { this.displayName = displayName; }
    }
}
