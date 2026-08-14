package com.appetir;

import com.appetir.alt.AltManager;
import com.appetir.config.ConfigManager;
import com.appetir.friends.FriendManager;
import com.appetir.modules.ModuleManager;
import net.fabricmc.api.ClientModInitializer;

public class AppetirClient implements ClientModInitializer {

    public static final String NAME    = "Appetir";
    public static final String VERSION = "1.5";
    public static final String AUTHOR  = "Appatia + Grok";

    public static boolean hudVisible = true;

    private static AppetirClient instance;
    private ModuleManager moduleManager;
    private AltManager altManager;
    private ConfigManager configManager;
    private FriendManager friendManager;

    @Override
    public void onInitializeClient() {
        instance = this;

        this.moduleManager = new ModuleManager();
        this.configManager = new ConfigManager();
        this.configManager.load();
        this.altManager = new AltManager();
        this.friendManager = new FriendManager();

        System.out.println("========================================");
        System.out.println("[" + NAME + "] v" + VERSION + " loaded");
        System.out.println("[" + NAME + "] Modules : " + moduleManager.getModules().size());
        System.out.println("[" + NAME + "] Alts    : " + altManager.getAlts().size());
        System.out.println("[" + NAME + "] Friends : " + friendManager.getFriends().size());
        System.out.println("[" + NAME + "] Author  : " + AUTHOR);
        System.out.println("========================================");
    }

    public static AppetirClient getInstance() { return instance; }
    public ModuleManager getModuleManager() { return moduleManager; }
    public AltManager getAltManager() { return altManager; }
    public ConfigManager getConfigManager() { return configManager; }
    public FriendManager getFriendManager() { return friendManager; }
}
