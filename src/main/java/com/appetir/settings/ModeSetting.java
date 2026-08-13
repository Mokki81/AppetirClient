package com.appetir.settings;

import java.util.Arrays;
import java.util.List;

public class ModeSetting extends Setting {

    private final List<String> modes;
    private int index;

    public ModeSetting(String name, String description, String defaultMode, String... modes) {
        super(name, description);
        this.modes = Arrays.asList(modes);
        this.index = Math.max(0, this.modes.indexOf(defaultMode));
        if (this.index < 0) this.index = 0;
    }

    public String get() { return modes.get(index); }
    public int getIndex() { return index; }
    public List<String> getModes() { return modes; }

    public void set(String mode) {
        int i = modes.indexOf(mode);
        if (i >= 0) index = i;
    }

    public void cycle() {
        index = (index + 1) % modes.size();
    }

    public boolean is(String mode) {
        return get().equalsIgnoreCase(mode);
    }

    @Override
    public String getDisplayValue() {
        return get();
    }
}
