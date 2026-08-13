# Appetir Client

**Version 1.1** · Minecraft 1.16.5 Fabric  
Custom client with modules, modern ClickGUI and Offline Alt Manager.

Improved by **Grok** (xAI).

---

## Features

### Core
- Full module system (Combat / Movement / Render / Misc)
- Modern ClickGUI (Right Shift)
- Offline Alt Manager (Right Control)
- Theme system with animated Gradient
- Clean HUD arraylist + watermark

### Controls
| Key              | Action                    |
|------------------|---------------------------|
| Right Shift      | Open / Close ClickGUI     |
| Right Control    | Open Alt Manager          |
| Right Alt        | Toggle HUD                |

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
- Quality, visuals, AltManager & structure: **Grok**
