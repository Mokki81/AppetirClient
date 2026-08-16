package com.appetir.modules;

import com.appetir.config.ConfigManager;
import com.appetir.friends.FriendManager;
import com.appetir.modules.impl.*;
import com.appetir.util.BindManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class ModuleManager {

    private static volatile ModuleManager instance;
    private final List<Module> modules = new ArrayList<>();
    private final Map<Integer, Module> keyMap = new HashMap<>();

    public ModuleManager() {
        if (instance != null) {
            throw new IllegalStateException("[Appetir] ModuleManager already constructed");
        }
        instance = this;
        registerAll();
    }

    private void registerAll() {
        add(new Aura());
        add(new AutoPotion());
        add(new AutoSwap());
        add(new AutoTotem());
        add(new BowHelper());
        add(new HitBox());
        add(new KillAura());
        add(new NoFriendDamage());
        add(new TriggerBot());

        add(new AirStuck());
        add(new Fly());
        add(new InvMove());
        add(new NoSlow());
        add(new Speed());
        add(new Spider());
        add(new Sprint());
        add(new WaterSpeed());

        add(new Arrows());
        add(new AspectRatio());
        add(new BlockESP());
        add(new Cosmetics());
        add(new CustomHand());
        add(new CustomWorld());
        add(new ESP());
        add(new Fullbright());
        add(new GlassHands());
        add(new Hud());
        add(new ItemPhysic());
        add(new Keystrokes());
        add(new NameTags());
        add(new NightVision());
        add(new NoRender());
        add(new Particles());
        add(new Projectiles());
        add(new ShulkerViewer());
        add(new WorldParticles());

        add(new AntiAFK());
        add(new AutoAccept());
        add(new AutoEat());
        add(new ClientSounds());
        add(new ElytraHelper());
        add(new Fixer());
        add(new FreeCamera());
        add(new ItemScroller());
        add(new MiddleClick());
        add(new MineHelper());
        add(new NoDelay());
        add(new NoPush());
        add(new Optimization());
        add(new TargetPearl());
        add(new TapeMouse());
    }

    private void add(Module module) {
        modules.add(module);
    }

    public void onTick() {
        ConfigManager cm = ConfigManager.getInstance();
        if (cm != null) cm.flushDirty();

        FriendManager fm = FriendManager.getInstance();
        if (fm != null) fm.flushDirty();

        for (Module m : modules) {
            if (!m.isEnabled()) continue;
            try {
                m.onTick();
            } catch (Exception e) {
                System.err.println("[Appetir] Tick error in " + m.getName()
                        + " (" + e.getClass().getSimpleName() + "): "
                        + (e.getMessage() != null ? e.getMessage() : "(no message)"));
                e.printStackTrace();
                // Soft fail: log only. forceDisable reserved for repeated/fatal via Module itself.
            }
        }
    }

    public void rebuildKeyMap() {
        keyMap.clear();
        boolean changed = false;
        for (Module m : modules) {
            int k = m.getKey();
            if (k < 0) continue;
            if (BindManager.isReserved(k)) {
                m.setKeyRaw(-1);
                changed = true;
                continue;
            }
            if (!keyMap.containsKey(k)) {
                keyMap.put(k, m);
            } else {
                m.setKeyRaw(-1);
                changed = true;
            }
        }
        if (changed) {
            ConfigManager cm = ConfigManager.getInstance();
            if (cm != null) cm.markDirty();
        }
    }

    public void registerKey(Module module, int key) {
        keyMap.entrySet().removeIf(e -> e.getValue() == module);
        if (key < 0 || BindManager.isReserved(key)) return;
        Module prev = keyMap.put(key, module);
        if (prev != null && prev != module) {
            prev.setKeyRaw(-1);
            ConfigManager cm = ConfigManager.getInstance();
            if (cm != null) cm.markDirty();
        }
    }

    public void onKeyPress(int key) {
        Module m = keyMap.get(key);
        if (m == null) {
            rebuildKeyMap();
            m = keyMap.get(key);
        }
        if (m != null) m.toggle();
    }

    public List<Module> getModules() {
        return Collections.unmodifiableList(modules);
    }

    public List<Module> getByCategory(Module.Category category) {
        return modules.stream()
                .filter(m -> m.getCategory() == category)
                .collect(Collectors.toList());
    }

    public List<Module> getEnabled() {
        return modules.stream()
                .filter(Module::isEnabled)
                .collect(Collectors.toList());
    }

    public Module getByName(String name) {
        for (Module m : modules) {
            if (m.getName().equalsIgnoreCase(name)) return m;
        }
        return null;
    }

    public static ModuleManager getInstance() {
        return instance;
    }
}
