<p align="center">
  <img src="src/main/resources/logo.png" alt="Holy Staff" width="160">
</p>

<h1 align="center">Holy Staff</h1>

<p align="center">
  A holy staff with three healing skills, cooldowns and floating heal numbers.
</p>

<p align="center">
  <a href="https://www.curseforge.com/minecraft/mc-mods/holy-staff"><img alt="CurseForge downloads" src="https://img.shields.io/curseforge/dt/1725465?logo=curseforge&label=CurseForge&color=F16436"></a>
  <img alt="Minecraft 1.21.1" src="https://img.shields.io/badge/Minecraft-1.21.1-62B47A">
  <a href="https://neoforged.net"><img alt="NeoForge" src="https://img.shields.io/badge/Loader-NeoForge-F16436"></a>
  <a href="https://www.curseforge.com/minecraft/mc-mods/geckolib"><img alt="Requires GeckoLib" src="https://img.shields.io/badge/Requires-GeckoLib-8A5CF6"></a>
  <img alt="License MIT" src="https://img.shields.io/badge/License-MIT-blue">
</p>

## Features

- **Holy Staff**: an animated 3D staff with a floating, glowing crystal inside a golden halo, taller than the
  player. Held forward like a mage ready to cast, pointed at the target while beaming and planted in the ground during Sanctuary.
- **Three skills, one button**: right click casts the selected skill. Switch skills with **left click** or
  **sneak + mouse wheel**; the skill bar next to the hotbar shows which one is selected.
  - **Blessed Ground**: while it is selected, a translucent circle shows where it will land, so you can aim it.
    Casting marks a rune circle under the aimed ally or block. Light fills it from the centre for
    0.6 s, then the circle flashes, light rays shoot up and every ally inside is healed at once.
  - **Holy Beam**: a beam of light locks onto the aimed ally and heals it strongly for up to 3 s, ideal for keeping
    a tank alive. While channelling you walk slowly and cannot jump; click again to cancel.
  - **Sanctuary**: you plant the staff and cannot move for 3 s. Enemies around are thrown back by a shockwave, a
    dome of light rises around you and every pulse heals all allies inside. Longest cooldown.
- **Floating heal numbers**: every heal makes a green number (`+6`) fly out of the healed entity, showing how much
  health was restored, with golden sparkles. Heals you receive pop up right next to your health bar.
- **Skill bar**: icons of the three skills with cooldown sweeps and seconds left, the selected one framed in gold;
  a cast bar while channelling and the skill name when you switch.
- **Sounds**: chimes, a humming beam and a deep bell when the staff strikes the ground.
- **Creative Holy Staff**: a pink-and-rose-gold staff for creative mode with the same skills, but no cooldowns,
  double healing and twice the beam range. Not craftable; find it in the creative tabs.
- Heals players, pets, animals, villagers and golems; hostile mobs are not healed (configurable).
- Right click still opens chests and doors; right clicking a mob heals it instead of trading or making a pet sit.
  The staff never attacks or breaks blocks.

## Controls

| Input | Action |
|---|---|
| Right click | Cast the selected skill |
| Left click / sneak + mouse wheel | Switch skill (an extra key can be bound in Controls) |
| Left or right click during Holy Beam | Cancel the beam |

| Skill | Default effect | Cooldown |
|---|---|---|
| Blessed Ground | heal 7 to everyone in radius 3 after 0.6 s | 3 s |
| Holy Beam | 8 health per second for 3 s on one ally | 10 s |
| Sanctuary | knockback, 6 pulses of 2.5 health in radius 7 | 20 s |

## Crafting

```
 . G A
 . B G
 B . .
```

`A` Golden Apple, `G` Gold Ingot, `B` Blaze Rod. The recipe unlocks when you get a golden apple.
In creative mode the staff and the Creative Holy Staff are in the **Pocky Mods** tab and in **Combat**.

## Configuration

`config/holy_staff-common.toml` (also in the in-game config screen):

| Option | Default | Description |
|---|---|---|
| `healMonsters` | `false` | Whether hostile mobs can be healed |
| `blessedGround.amount` / `radius` / `delay` / `range` / `cooldown` | `7` / `3` / `0.6` s / `20` / `3` s | Blessed Ground |
| `holyBeam.healPerSecond` / `duration` / `range` / `movementMultiplier` / `cooldown` | `8` / `3` s / `16` / `0.3` / `10` s | Holy Beam |
| `sanctuary.healPerPulse` / `pulseInterval` / `duration` / `radius` / `knockbackRadius` / `knockbackStrength` / `cooldown` | `2.5` / `0.5` s / `3` s / `7` / `5` / `1.5` / `20` s | Sanctuary |

`config/holy_staff-client.toml`: `showHealNumbers`, `showSkillHud`.

## Installation

1. Install [NeoForge](https://neoforged.net) for Minecraft 1.21.1.
2. Install [GeckoLib](https://www.curseforge.com/minecraft/mc-mods/geckolib) 4.9 or newer.
3. Put this mod into the `mods` folder (on the client and on the server).

## Building

```sh
./gradlew build
```

The jar is written to `build/libs/`.

## Credits

- Author: **Pocky**.
- Model, textures and particles: made for this mod.
- Sounds: made for this mod from CC0 sources: [Cure Magic](https://opengameart.org/content/cure-magic) by Someoneman
  (OpenGameArt) and the [Kenney](https://kenney.nl) Sci-Fi, Impact and Interface sound packs, layered with
  synthesized chords.
- Animated model rendering: [GeckoLib](https://github.com/bernie-g/geckolib).

<!-- more-mods:start -->
## More mods by Pocky

<table>
  <tr>
    <td align="center" width="112"><a href="https://www.curseforge.com/minecraft/mc-mods/lumen-rigs"><img src="https://raw.githubusercontent.com/Pocky-l/lumen-rigs/main/docs/icon.png" width="96" alt="Lumen Rigs"></a></td>
    <td>
      <a href="https://www.curseforge.com/minecraft/mc-mods/lumen-rigs"><b>Lumen Rigs</b></a><br>
      Aimable spotlights, floodlights, searchlights and soft panels with colored light and visible beams.<br>
      <a href="https://www.curseforge.com/minecraft/mc-mods/lumen-rigs"><img alt="CurseForge" src="https://img.shields.io/curseforge/dt/1727739?logo=curseforge&label=CurseForge&color=F16436"></a>
      <a href="https://github.com/Pocky-l/lumen-rigs"><img alt="GitHub" src="https://img.shields.io/badge/GitHub-source-181717?logo=github"></a>
    </td>
  </tr>
  <tr>
    <td align="center" width="112"><a href="https://www.curseforge.com/minecraft/mc-mods/neon-glowsticks"><img src="https://raw.githubusercontent.com/Pocky-l/neon-glowsticks/main/docs/icon.png" width="96" alt="Neon Glowsticks"></a></td>
    <td>
      <a href="https://www.curseforge.com/minecraft/mc-mods/neon-glowsticks"><b>Neon Glowsticks</b></a><br>
      Throwable glowsticks that bounce, roll and light up the dark with colored light.<br>
      <a href="https://www.curseforge.com/minecraft/mc-mods/neon-glowsticks"><img alt="CurseForge" src="https://img.shields.io/curseforge/dt/1727688?logo=curseforge&label=CurseForge&color=F16436"></a>
      <a href="https://github.com/Pocky-l/neon-glowsticks"><img alt="GitHub" src="https://img.shields.io/badge/GitHub-source-181717?logo=github"></a>
    </td>
  </tr>
  <tr>
    <td align="center" width="112"><a href="https://www.curseforge.com/minecraft/mc-mods/petrichor-rain-storms"><img src="https://raw.githubusercontent.com/Pocky-l/petrichor/main/docs/icon.png" width="96" alt="Petrichor: Rain & Storms"></a></td>
    <td>
      <a href="https://www.curseforge.com/minecraft/mc-mods/petrichor-rain-storms"><b>Petrichor: Rain & Storms</b></a><br>
      Realistic rain and storms: rain types, puddles, runoff and drips, branching lightning with delayed thunder.<br>
      <a href="https://www.curseforge.com/minecraft/mc-mods/petrichor-rain-storms"><img alt="CurseForge" src="https://img.shields.io/curseforge/dt/1729574?logo=curseforge&label=CurseForge&color=F16436"></a>
      <a href="https://github.com/Pocky-l/petrichor"><img alt="GitHub" src="https://img.shields.io/badge/GitHub-source-181717?logo=github"></a>
    </td>
  </tr>
  <tr>
    <td align="center" width="112"><a href="https://www.curseforge.com/minecraft/mc-mods/rustling-leaves"><img src="https://raw.githubusercontent.com/Pocky-l/rustling-leaves/main/docs/icon.png" width="96" alt="Rustling Leaves"></a></td>
    <td>
      <a href="https://www.curseforge.com/minecraft/mc-mods/rustling-leaves"><b>Rustling Leaves</b></a><br>
      Physically simulated leaves: falling leaves, leaf piles you can wade through, rake and blow away, gusts, whirlwinds and leaf tools.<br>
      <a href="https://www.curseforge.com/minecraft/mc-mods/rustling-leaves"><img alt="CurseForge" src="https://img.shields.io/curseforge/dt/1729578?logo=curseforge&label=CurseForge&color=F16436"></a>
      <a href="https://github.com/Pocky-l/rustling-leaves"><img alt="GitHub" src="https://img.shields.io/badge/GitHub-source-181717?logo=github"></a>
    </td>
  </tr>
  <tr>
    <td align="center" width="112"><a href="https://www.curseforge.com/minecraft/mc-mods/ranchers-vacpack"><img src="https://raw.githubusercontent.com/Pocky-l/ranchers-vacpack/main/docs/icon.png" width="96" alt="Rancher's Vacpack"></a></td>
    <td>
      <a href="https://www.curseforge.com/minecraft/mc-mods/ranchers-vacpack"><b>Rancher's Vacpack</b></a><br>
      A Slime Rancher inspired vacuum gun: suck up items and small mobs, store them in a tank and shoot them back out.<br>
      <a href="https://www.curseforge.com/minecraft/mc-mods/ranchers-vacpack"><img alt="CurseForge" src="https://img.shields.io/curseforge/dt/1725381?logo=curseforge&label=CurseForge&color=F16436"></a>
      <a href="https://github.com/Pocky-l/ranchers-vacpack"><img alt="GitHub" src="https://img.shields.io/badge/GitHub-source-181717?logo=github"></a>
    </td>
  </tr>
</table>
<!-- more-mods:end -->

## License

[MIT](LICENSE)
