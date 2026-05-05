package com.appetir.modules.impl;

import com.appetir.modules.Module;
import com.appetir.AppetirClient;

public class Hud extends Module {

    public Hud() {
        super("Hud", "Интерфейс клиента", Category.RENDER);
        setEnabled(true); // включён по умолчанию
    }

    @Override
    public void onEnable()  { AppetirClient.hudVisible = true;  }
    @Override
    public void onDisable() { AppetirClient.hudVisible = false; }
}
