# Appetir Client

**Version 1.2** · Minecraft 1.16.5 Fabric  
Custom client with modules, modern ClickGUI, Offline Alt Manager and Config system.

Improved by **Grok** (xAI).

---

## Features

### Core
- Full module system (Combat / Movement / Render / Misc)
- Modern ClickGUI (Right Shift)
- Offline Alt Manager (Right Control)
- **Config system** — auto-saves modules, keybinds, theme, HUD
- Theme system with animated Gradient
- Clean HUD arraylist + watermark

### Controls
| Key              | Action                    |
|------------------|---------------------------|
| Right Shift      | Open / Close ClickGUI     |
| Right Control    | Open Alt Manager          |
| Right Alt        | Toggle HUD                |

### Config
- Path: `.minecraft/appetir/config.json`
- Auto-saves when you toggle modules / change theme / HUD
- Loads automatically on startup

### Alt Manager
- Offline accounts only (nickname)
- Add / Remove / Login
- Persistent save (`appetir_alts.txt`)
- Instant session switch

---

## Build

```bash
./gradlew build
```

Requires Java 8+.

---

## Credits

- Original: Appatia
- Quality, visuals, AltManager, Config & structure: **Grok**
