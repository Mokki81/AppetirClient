package com.appetir;

import com.appetir.alt.AltManager;
import com.appetir.config.ConfigManager;
import com.appetir.modules.ModuleManager;
import net.fabricmc.api.ClientModInitializer;

/**
 * Appetir Client — main entry point.
 * Improved by Grok · Offline AltManager · Modern ClickGUI · Config System
 */
public class AppetirClient implements ClientModInitializer {

    public static final String NAME    = "Appetir";
    public static final String VERSION = "1.2";
    public static final String AUTHOR  = "Appatia + Grok";

    public static boolean hudVisible = true;

    private static AppetirClient instance;
    private ModuleManager moduleManager;
    private AltManager altManager;
    private ConfigManager configManager;

    @Override
    public void onInitializeClient() {
        instance = this;

        // Order matters: modules first, then config (applies states), then alts
        this.moduleManager = new ModuleManager();
        this.configManager = new ConfigManager();
        this.configManager.load();          // apply saved enabled/keys/theme
        this.altManager = new AltManager();

        System.out.println("========================================");
        System.out.println("[" + NAME + "] v" + VERSION + " loaded");
        System.out.println("[" + NAME + "] Modules: " + moduleManager.getModules().size());
        System.out.println("[" + NAME + "] Alts: " + altManager.getAlts().size());
        System.out.println("[" + NAME + "] Config: " + configManager.getConfigFile().getName());
        System.out.println("[" + NAME + "] Author: " + AUTHOR);
        System.out.println("========================================");
    }

    public static AppetirClient getInstance() {
        return instance;
    }

    public ModuleManager getModuleManager() {
        return moduleManager;
    }

    public AltManager getAltManager() {
        return altManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}
