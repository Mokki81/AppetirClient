package com.appetir.settings;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ModeSetting extends Setting {

    private final List<String> modes;
    private int index;

    public ModeSetting(String name, String description, String defaultMode, String... modes) {
        super(name, description);
        if (modes == null || modes.length == 0) {
            this.modes = Collections.singletonList(defaultMode != null ? defaultMode : "Default");
        } else {
            this.modes = Arrays.asList(modes);
        }
        int i = defaultMode != null ? this.modes.indexOf(defaultMode) : -1;
        this.index = i >= 0 ? i : 0;
    }

    public String get() {
        if (modes.isEmpty()) return "";
        if (index < 0 || index >= modes.size()) index = 0;
        return modes.get(index);
    }

    public int getIndex() { return index; }
    public List<String> getModes() { return modes; }

    public void set(String mode) {
        int i = modes.indexOf(mode);
        if (i >= 0) index = i;
    }

    public void cycle() {
        if (modes.isEmpty()) return;
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
