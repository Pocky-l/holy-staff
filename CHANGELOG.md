# Changelog

All notable changes to this mod are documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.1.0+1.20.1] - 2026-10-08
### Added
- Ported to Minecraft 1.20.1 (Forge).

### Changed
- On Forge there is no in-game config screen: change the settings in `config/holy_staff-common.toml` and
  `config/holy_staff-client.toml`.

## [1.1.0] - 2026-10-07
### Added
- Creative Holy Staff: a pink-and-rose-gold staff for creative mode with the same three skills, but no cooldowns,
  double healing and twice the Holy Beam range. It is not craftable and sits in the Pocky Mods and Combat creative
  tabs.

## [1.0.0] - 2026-10-04
### Added
- Holy Staff: an animated staff taller than the player with a glowing crystal, crafted from a golden apple, gold ingots and blaze rods.
- Three skills on right click, switched with left click or sneak + mouse wheel:
  - Blessed Ground: a rune circle fills with light, then heals every ally inside; a translucent preview circle
    shows where it will land.
  - Holy Beam: a channelled beam that strongly heals one ally; you walk slowly and can cancel it.
  - Sanctuary: plant the staff, throw enemies back and heal everyone around in pulses.
- Casting poses: the staff is held forward, points at the beam target and is planted in the ground during Sanctuary.
- Light effects for every skill: rune circles, light rays, a beam with flares, a dome of light and shockwaves.
- Sounds for casting, healing, the beam and Sanctuary.
- Green heal numbers flying out of every healed entity, with golden sparkles; your own heals pop up next to your
  health bar.
- Skill bar next to the hotbar with the selected skill, cooldowns and a cast bar.
- Config options for every heal amount, radius, duration and cooldown, and for healing hostile mobs.
