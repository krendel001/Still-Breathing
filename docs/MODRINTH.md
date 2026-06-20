# Modrinth page content

Copy the sections below into your Modrinth project settings.

## Summary (short description)

```
Lethal damage leaves you down, not dead. Hold on, get revived, loot the fallen, or finish them off — every second counts.
```

## Description

```markdown
# Still-Breathing

**You're not dead yet — but the clock is ticking.**

Still-Breathing replaces instant death with a tense **downed state**. When lethal damage would kill you, you collapse with 1 HP and start bleeding out. Allies can save you, enemies can finish you, and anyone nearby can take your gear before time runs out.

---

## How it works

### Knocked out
- Fatal damage is **cancelled** — you enter a **downed state** instead of dying
- You lie on the ground with a **bleed-out timer** (default: 90 seconds)
- You cannot move, attack, or use items while down
- Smooth **lying-down animation** powered by GeckoLib

### Get back up
- **Self-revive:** hold **Sneak** to struggle back up — risky, because **failure means death**
- **Passive recovery:** small random chance over time while you wait
- **Ally revive:** teammates hold **Shift + Right-click** on you to pull you back up
- **Give up:** accept death if no help is coming

### For enemies
- **Finish off** downed players with repeated melee hits
- **Loot their inventory** while they are knocked out (including hotbar, configurable)
- Environmental damage speeds up their bleed-out

### After recovery
- Wake up with **low health**, **Weakness**, and **Slowness**
- **Grace period:** the next lethal hit within a short window is **final death**, not another knockout
- Repeated knockouts stack **exhaustion**, making self-revive harder over time

---

## Perfect for
- **Hardcore survival** servers where death should hurt, but not always end the run
- **Squad / co-op** gameplay — someone can always save a teammate
- **PvP** — finish kills, steal gear, or leave rivals bleeding out
- **RP / military** packs that want a more cinematic combat loop

---

## Features
- Fully **server-side configurable** — timers, chances, finish hits, loot rules, and more
- **HUD overlays** with bleed timer, revive progress, and on-screen hints
- **Admin test dummies** for trying the system without players (`/knockout dummy ...`)
- Optional **Epic Fight** compatibility (client-side, not required)
- English and Russian localization

---

## Controls
| Action | Input |
|---|---|
| Try to get up | Hold **Sneak** |
| Revive ally | Hold **Shift + RMB** on downed player |
| Loot downed player | **RMB** on downed player |
| Give up | Use the on-screen button |

---

## Dependencies
- **Minecraft** 1.20.1
- **Forge** 47+
- **GeckoLib** 4.4+ (required)
- **Epic Fight** 20.13+ (optional, client)

---

*Still breathing? Good. Don't waste it.*
```

## Tags

```
game-mechanics
multiplayer
pvp
survival
hardcore
```

## Categories

- Game Mechanics
- Utility

## Environment

- Client
- Server

## Loaders

- Forge

## Game versions

- 1.20.1

## Dependencies

| Project | Type |
|---|---|
| [GeckoLib](https://modrinth.com/mod/geckolib) | Required |
| [Epic Fight](https://modrinth.com/mod/epic-fight) | Optional |
