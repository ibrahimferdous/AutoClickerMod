# AutoClicker Mod — Minecraft 1.8.9 Forge

## What it does
- Detects your real click rate using a 1-second sliding window.
- **Only activates when you are already clicking ≥ 10 CPS** — completely off below that.
- Each real click has a **random chance between 20 % and 80 %** to fire one extra synthetic click on the very next tick.
- **Automatically disabled** whenever any GUI or inventory screen is open.
- **Only fires on blocks** — the crosshair must be pointing at a block face (not air, not an entity).
- Goes through Vanilla's normal click code path (non-invasive, respects hit-delay).

---

## Requirements
| Tool | Version |
|------|---------|
| JDK  | 8 (Java 1.8) |
| Gradle | 4.x – 6.x |
| Minecraft Forge | 1.8.9-11.15.1.2318 |

---

## Building from source

```bash
# 1. Clone / unzip this project
cd AutoClickerMod

# 2. Set up the Forge workspace (downloads MC + Forge sources, ~5 min first time)
./gradlew setupDecompWorkspace

# 3. Compile and produce the .jar
./gradlew build
```

The compiled `.jar` will be at:
```
build/libs/AutoClickerMod-1.0.0.jar
```

---

## Installation

1. Install **Minecraft Forge 1.8.9** (recommended build: `1.8.9-11.15.1.2318`).
2. Copy `AutoClickerMod-1.0.0.jar` into your `.minecraft/mods/` folder.
3. Launch Minecraft with the Forge 1.8.9 profile.

---

## Configuration (optional tweaks)

All tunables are constants at the top of `AutoClickerHandler.java`:

```java
private static final int    MIN_CPS_THRESHOLD      = 10;   // activate only at ≥ 10 CPS
private static final double EXTRA_CLICK_CHANCE_MIN = 0.20; // 20 % minimum roll
private static final double EXTRA_CLICK_CHANCE_MAX = 0.80; // 80 % maximum roll
```

Change these values and rebuild to customise the behaviour.

---

## How the CPS gate works

A deque stores the nanosecond timestamp of every real left-click.
Before each evaluation, entries older than 1 second are discarded.
The remaining count is the current CPS — if it is below 10, the mod
does nothing at all for that click.

## License
Do whatever you want with this code.
