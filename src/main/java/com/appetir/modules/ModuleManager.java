package com.appetir.modules;

import com.appetir.modules.impl.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Central registry and tick dispatcher for all modules.
 */
public class ModuleManager {

    private static ModuleManager instance;
    private final List<Module> modules = new ArrayList<>();

    public ModuleManager() {
        instance = this;
        registerAll();
    }

    private void registerAll() {
        // Combat
        add(new Aura());
        add(new AutoPotion());
        add(new AutoSwap());
        add(new AutoTotem());
        add(new BowHelper());
        add(new HitBox());
        add(new KillAura());
        add(new NoFriendDamage());
        add(new TriggerBot());

        // Movement
        add(new AirStuck());
        add(new Fly());
        add(new InvMove());
        add(new NoSlow());
        add(new Speed());
        add(new Spider());
        add(new Sprint());
        add(new WaterSpeed());

        // Render
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
        add(new NameTags());
        add(new NightVision());
        add(new NoRender());
        add(new Particles());
        add(new Projectiles());
        add(new ShulkerViewer());
        add(new WorldParticles());

        // Misc
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
        for (Module m : modules) {
            if (m.isEnabled()) {
                try {
                    m.onTick();
                } catch (Exception e) {
                    System.err.println("[Appetir] Tick error in " + m.getName() + ": " + e.getMessage());
                }
            }
        }
    }

    public void onKeyPress(int key) {
        for (Module m : modules) {
            if (m.getKey() == key) {
                m.toggle();
            }
        }
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
