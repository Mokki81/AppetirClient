package com.appetir.settings;

/**
 * Base setting attached to a module.
 */
public abstract class Setting {

    private final String name;
    private final String description;

    public Setting(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }

    public abstract String getDisplayValue();
}
