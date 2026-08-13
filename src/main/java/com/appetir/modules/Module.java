package com.appetir.modules;

/**
 * Base class for all client modules.
 * Clean, simple and extensible.
 */
public abstract class Module {

    private final String name;
    private final String description;
    private final Category category;
    private boolean enabled;
    private int key = -1;

    public Module(String name, String description, Category category) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.enabled = false;
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
        } catch (Exception e) {
            System.err.println("[Appetir] Error in module " + name + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean isEnabled() { return enabled; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Category getCategory() { return category; }
    public int getKey() { return key; }
    public void setKey(int key) { this.key = key; }

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
