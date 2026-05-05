package com.appetir;

import com.appetir.modules.ModuleManager;
import net.fabricmc.api.ClientModInitializer;

public class AppetirClient implements ClientModInitializer {

    public static final String NAME    = "Appetir";
    public static final String VERSION = "1.0";

    public static boolean hudVisible = true;

    private static AppetirClient instance;

    @Override
    public void onInitializeClient() {
        instance = this;
        new ModuleManager();
        System.out.println("[" + NAME + "] v" + VERSION + " loaded! Modules: "
            + ModuleManager.getInstance().getModules().size());
    }

    public static AppetirClient getInstance() { return instance; }
}
