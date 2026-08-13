package com.appetir.settings;

public class BooleanSetting extends Setting {

    private boolean value;

    public BooleanSetting(String name, String description, boolean defaultValue) {
        super(name, description);
        this.value = defaultValue;
    }

    public boolean get() { return value; }
    public void set(boolean value) { this.value = value; }
    public void toggle() { this.value = !this.value; }

    @Override
    public String getDisplayValue() {
        return value ? "ON" : "OFF";
    }
}
