package com.appetir.modules;

public abstract class Module {

    private final String name;
    private final String description;
    private final Category category;
    private boolean enabled = false;
    private int key = -1;

    public Module(String name, String description, Category category) {
        this.name = name;
        this.description = description;
        this.category = category;
    }

    public void onEnable() {}
    public void onDisable() {}
    public void onTick() {}

    public void toggle() {
        enabled = !enabled;
        if (enabled) onEnable();
        else onDisable();
    }

    public boolean isEnabled()              { return enabled; }
    public void setEnabled(boolean v)       { this.enabled = v; }
    public String getName()                 { return name; }
    public String getDescription()          { return description; }
    public Category getCategory()           { return category; }
    public int getKey()                     { return key; }
    public void setKey(int key)             { this.key = key; }

    public enum Category {
        COMBAT, MOVEMENT, RENDER, WORLD, MISC
    }
}
