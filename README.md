# Appetir Client

**Version 1.6.1** · Minecraft **1.16.5** · Fabric  
Custom client: modules, settings, config, keybinds, keystrokes, Clean/Full mode.

Authors: **Appatia** + **Grok** (xAI)

---

## Features

### Core
- Module system: Combat / Movement / Render / World / Misc
- Settings: Boolean / Number / Mode (RMB on module)
- Config: `.minecraft/appetir/config.json` (debounced auto-save)
- Keybinds per module (Neverlose-style bind UI)
- Keystrokes HUD (WASD, Space, LMB/RMB)
- Offline Alt Manager
- Friends list + NoFriendDamage
- Themes with animated accent
- Notifications on toggle
- **ClientMode Clean / Full** — Clean looks like a performance/QOL mod

### KillAura
- Modes: **Legit** / **Rage**
- FOV, range, random delay, soft aim, optional crits

### ItemPhysic
- Items tip while falling, rest flat on ground (no spin glitch)

### Controls

| Key | Action |
|-----|--------|
| **Right Shift** | ClickGUI |
| **Right Control** | Alt Manager (Full mode) |
| **Right Alt** | Toggle HUD |
| **Insert** | Toggle **Clean / Full** mode |
| Module bind | Toggle that module |

### ClickGUI
- LMB — toggle module
- RMB — expand settings + **Bind**
- Bind row: press key / DEL unbind / ESC cancel
- Sidebar: categories, Themes, Alts, **Mode: Clean/Full**

### Clean mode
- Hides Combat & Movement
- Forces restricted modules off
- Watermark: `Appetir Performance`
- Only QOL / render / misc utilities visible

---

## Build

```bash
git pull origin main
./gradlew build
```

Java **8**, Fabric Loader **≥ 0.14.21**, Loom **0.12.56**.

CI: GitHub Actions on `main` (`.github/workflows/build.yml`).

### Repo hygiene
See `CLEANUP.md` if `.gradle/` / `run/` are still tracked locally:

```bash
git rm -r --cached .gradle build run logs libraries .idea 2>/dev/null
git commit -m "Stop tracking build caches"
git push
```

---

## Changelog (high level)

| Ver | Notes |
|-----|--------|
| 1.1–1.3 | Alt Manager, ClickGUI, Config, Settings, Friends |
| 1.4 | Visual overhaul, themes, HUD |
| 1.5 | Movement / Render / Misc modules |
| 1.6 | Keybinds + Keystrokes |
| 1.6.1 | Hardening, CI, KillAura Legit, ItemPhysic fix, ClientMode |

---

## Credits

- Original: **Appatia**
- Architecture, GUI, systems, polish: **Grok**
