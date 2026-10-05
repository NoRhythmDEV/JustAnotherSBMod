# Changelog

## 1.4.0

- Automatically discover and migrate older/renamed feature configs while retaining originals
- Preserve unknown settings, back up malformed configs, and disable saving if safe migration fails

- Rename the expanded mod to JustAnotherSBMod and add `/jasbm` and `/justanothersbmod` commands

- Add per-chest lockpicking particle targets with through-chest rendering and moving-target expiry
- Add focused/all-nearby modes, marker colors and sizes, optional outlines, range, and particle hiding
- Add the MoulConfig menu with Farming, Mining, and Misc feature categories
- Make visitor visuals, sync, farming toggles, keybindings, and interface helpers configurable
- Migrate existing visitor feature settings into the new feature config
- Bundle MoulConfig and require Fabric Language Kotlin
- Add an independent Hold/Toggle mode to every alternative action binding
- Persist toggle modes in config version 4
- Keep direct toggles client-side by driving the normal `KeyMapping.isDown()` state
- Declare the mod client-only and correct the metadata license to GPL-3.0-only
- Add profile-aware Garden visitor unlock waypoints for Minecraft 26.2
- Sync unlocked and blocked visitors from each opened Visitor's Logbook page
- Filter visitor markers to their actual island and show name, distance, and optional requirements
- Cover every visitor with a fixed wiki location; label undocumented requirements as Unknown
- Add a filterable visitor browser with unlock state, island, and coordinates
- Add a visual nearest-visitor route and a remappable key to cycle its target
- Add beacon beams to visitor waypoints and requirements to the visitor browser
- Add a persistent Visitor Browser setting to enable or disable route navigation
- Make beacon columns visible through walls and move route guidance to a crosshair HUD tracer
- Make the tracer continuous and scale the selected-target HUD marker by distance with safe limits
- Group all visitor controls in a dedicated FarmThingy category in Minecraft's keybind settings
- Add client-only /farmthingy commands with local feedback
- Add persistent event-only and mayor-required waypoint filters to the visitor browser
- Defer command-opened browser display so chat closing cannot immediately dismiss it
- Fix route indicators mirroring to false positions when targets pass behind the camera
- Mark nearby visitors complete from direct NPC interaction or dialogue on either message channel
- Add live visitor/NPC search to the browser
- Learn unlock requirements from logbook lore and persist them per profile instead of displaying wiki requirements
- Rename the visible project and output artifact to FarmThingy

## 1.3.4 (May 23, 2026)

- Add custom key binding entry for toggling auto-jump
- Small renaming refactor from "Sticky Toggles" -> just "Toggles"

## 1.3.3 (May 13, 2026)

- Add custom key binding entries for toggling sticky keys (toggle vs hold)

## 1.3.2 (May 12, 2026)

- Fix sticky modifiers when releasing a chord
- Match only exact modifier combinations to avoid spurious triggers

## 1.3.1 (May 4, 2026)

- Fix broken click handling for modifier-bound keys

## 1.3.0 (May 1, 2026)

- Add support for key binding modifiers (shift, ctrl, alt)
- Add unbound and unset key checks
- Add modifier support for the "Controlling" mod integration
- Relicense under GNU GPL v3

## 1.2.1 (Jan 1, 2026)

- Broader Minecraft version compatibility (through 1.21.11)
- Change conflict indicator color to yellow

## 1.2.0 (Dec 27, 2025)

- Migrate from yarn to mojang mappings
- Reintegrate with "Controlling" mod after version mismatch

## 1.1.1 (Apr 6, 2026)

- Fix Java version mismatch in build for 1.20.x branches

## 1.1.0 (Aug 28, 2025)

- Add support for the mod "Controlling"
- Visual update: better indicators and conflict detection
- Refactor key binding behavior: many bug fixes

## 1.0.2 (Aug 18, 2025)

- Bump version
- Add explicit versioning
- Add release and publish workflows

## 1.0.1 (Mar 17, 2025)

- Bump version
- Add support for hotbar action key bindings
- Add support for migrating old config versions

## 1.0.0 (Feb 18, 2025)

- Initial release
