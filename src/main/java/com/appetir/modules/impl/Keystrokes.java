package com.appetir.modules.impl;

import com.appetir.modules.Module;
import com.appetir.settings.BooleanSetting;
import com.appetir.settings.NumberSetting;

public class Keystrokes extends Module {

    private final NumberSetting x = new NumberSetting("X", "Offset X from left", 8, 0, 500, 1);
    private final NumberSetting y = new NumberSetting("Y", "Offset Y from bottom", 8, 0, 400, 1);
    private final NumberSetting size = new NumberSetting("Size", "Key box size", 22, 14, 36, 1);
    private final BooleanSetting mouse = new BooleanSetting("Mouse", "Show LMB/RMB", true);
    private final BooleanSetting space = new BooleanSetting("Space", "Show spacebar", true);

    public Keystrokes() {
        super("Keystrokes", "Показ нажатых клавиш", Category.RENDER);
        addSetting(x);
        addSetting(y);
        addSetting(size);
        addSetting(mouse);
        addSetting(space);
        setEnabled(true);
    }

    public int getX() { return x.getInt(); }
    public int getY() { return y.getInt(); }
    public int getSize() { return size.getInt(); }
    public boolean showMouse() { return mouse.get(); }
    public boolean showSpace() { return space.get(); }
}
