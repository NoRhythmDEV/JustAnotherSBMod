# JustAnotherSBMod

[GitHub](https://github.com/NoRhythmDEV/JustAnotherSBMod)

**Farming, mining, and keybinding helpers for Minecraft 26.2 / Fabric.**

### Settings

Run `/jasbm` or `/jasbm config` to open the MoulConfig menu. It has
three root categories with feature subentries. `/justanothersbmod` and the legacy
`/farmthingy` command also work:

- **Farming:** Visitor Waypoints, Visitor Route, and Hold / Toggle.
- **Mining:** Powder Chest Particles.
- **Misc:** Keybindings and Interface.

Feature settings are saved in `config/justanothersbmod.json`. Existing bindings and
profile unlock state remain in their existing files. Previous route, event, and
mayor settings are imported automatically when the feature config is created.

Feature configs migrate automatically from older names such as `farmthingy.json`,
`jasbm.json`, and `farmhelper.json`. Other renamed JSON files are recognized by
this mod's config identity or its distinctive feature settings. Known names take
priority; otherwise the newest matching file is imported. Existing valid
`justanothersbmod.json` settings take priority over every legacy file.

Original files are retained. An invalid current config is backed up as
`justanothersbmod.json.invalid-<id>.bak` before recovery or defaults are written.
If preservation or migration fails, saving is disabled for that session to
protect existing files. Unknown settings are retained across saves, and older
visitor flags only fill missing feature settings. Bindings and visitor profile
state continue using their existing files.

### Powder Chest Particles

Highlights the current lockpicking **crit particle origin** on any of a chest's
four sides, including the far side through the chest. Each nearby chest has its
own target; new particles replace its old position immediately. Stale markers
expire, and targets clear when chests disappear, you leave range, or worlds change.

The default shows only the chest directly under your crosshair, in green, with
through-chest rendering enabled. Switch **Display Mode** to **All nearby chests**
to see every tracked target. Configure color/opacity, marker size, through-chest
rendering, optional chest outlines and their color, particle hiding, detection
range, and target expiry under **Mining → Powder Chest Particles**.

Detection is restricted to Crystal Hollows by default. It observes incoming
particles and draws markers; you aim at the highlighted position yourself.

Install **Fabric API** and **Fabric Language Kotlin 1.14.1+kotlin.2.4.20 or newer**
alongside the mod. MoulConfig and the Hypixel Mod API are bundled. Use the plain
`JustAnotherSBMod-…jar` build; the `-thin.jar` is an intermediate without MoulConfig.

For an in-game check, disable **Crystal Hollows Only** in a local creative world,
place two separate nearby chests, and send `minecraft:crit` particles with
`/particle` at different side coordinates. Check focused/all-chest modes, moving
targets, back-side visibility, colors, expiry, and feature toggles. Restore the
island restriction before normal SkyBlock use.

This mod adds extra functionality to the native key binds screen. Simply click the "+" button next to any key binding and a new binding for the selected action should appear.
This mod also adds support for modifier keys (left/right) shift, control, and alt.

Each added alternative binding has its own **Hold/Toggle** button. In Hold mode,
the action follows the physical key as usual. In Toggle mode, pressing the
binding once keeps the action logically held and pressing it again releases it.
For example, Attack/Destroy can keep Right Click as a normal alternative while
Control independently toggles Attack/Destroy held on and off.

Like Minecraft's own held-key state, an active direct toggle is safely released
when the game resets input (for example, when focus is lost or a screen opens).

Direct toggles only drive Minecraft's normal local key-mapping state and do not
send custom action packets. Servers can still observe the resulting normal
gameplay actions, so their rules on toggles/macros still apply. The visitor
waypoint feature only subscribes to the official Hypixel Mod API location update
so it can keep waypoints on the correct island.

### Garden Visitor Waypoints (26.2)

Garden visitors with fixed NPC locations have through-wall waypoints and bright
beacon-style columns showing the NPC name and distance on the island where that NPC actually lives. Open every page of
the **Visitor's Logbook** once to sync the current SkyBlock profile. Reopening a
page is authoritative: visitors marked locked there reappear, and unlocked ones
disappear. Talking to a marked NPC hides it immediately.

Hold **Left Alt** (configurable under **JustAnotherSBMod** in Controls) to show synced
unlock requirements. Visitors blocked by another requirement are hidden during
normal play and appear as locked markers while that key is held. Requirements
are learned from locked Visitor's Logbook tooltips and remain hidden until that
entry has been synced; wiki requirement text is not displayed.

Press **V** to open the Visitor Browser. It lists each static visitor's current
locked/unlocked status, island, coordinates, and unlock requirement, and can be filtered to All,
Locked, or Unlocked. Its search box filters by visitor/NPC name, island,
requirement, or availability group. The browser, next-route, and requirements keys can each be
remapped in Minecraft's normal Controls menu under the **JustAnotherSBMod** category.

A solid cyan HUD tracer starts at the crosshair and automatically locks onto the nearest missing visitor on the
current island. Press **N** to move the route to the next-nearest visitor, which
is useful for skipping event NPCs that are not currently present. The route is
visual only and never moves the player. Use the persistent **Route: On/Off**
button in the Visitor Browser to disable or restore route navigation without
disabling the regular visitor waypoints or beacon beams.

The selected visitor also gets a dedicated HUD label that grows with distance
between clamped minimum and maximum sizes, keeping faraway targets readable
without allowing the marker to become excessively large.

JustAnotherSBMod also provides client-only `/jasbm` chat commands. Running
`/jasbm` opens settings; use `/jasbm help`, `config`, `browser`, `next`,
`route on|off|toggle`, `events on|off|toggle`, `mayors on|off|toggle`, or
`status`. Command feedback is visible only to the local player, and these
commands are intercepted by Fabric rather than sent to the server.

Clicking a nearby matching NPC or receiving its NPC dialogue immediately marks
that visitor unlocked and removes its waypoint. Both game/system and chat
message channels are handled; reopening the logbook can still authoritatively
restore the locked state if the interaction did not actually unlock it.

Unlock state and learned requirements are persisted per SkyBlock profile in
`config/visitor-waypoints.json`, so restarting Minecraft does not require a new
sync. Reopen logbook pages only to learn unseen entries or refresh changed state.

Event-only NPC waypoints are disabled by default and can be enabled with the
**Events** button in the browser when their event is active. This group includes
Chantelle, Fear Mongerer, Hoppity, Oringo, Sirius, Tyashoi Alchemist, and Vargul.
The separate **Mayors** button controls static visitors whose Garden availability
depends on an elected mayor; currently this applies to Baker during Finnegan's
Blooming Business perk. Visitors without any fixed NPC coordinate remain absent.

## Compatibility

Targets Minecraft 26.2 with Fabric and Java 25. Modifier chords and alternative
keybindings can be configured in Minecraft's Controls menu. Controlling integration
is optional; its compile-time dependencies are downloaded through Gradle.

## License

GPL-3.0-only. See [LICENSE](LICENSE) and [NOTICE](NOTICE) for retained component notices.

## Adding commands

Client command registration lives in `us.kenny.commands.JustAnotherSBModCommands`.
Feature modules attach their menu action and subcommands to a root builder.
Add an independent command prefix with:

```java
registry.add("yourprefix", YourFeatureCommands::attachTo, "optionalalias");
```

Use `root.executes(...)` for the bare `/yourprefix` menu action, and
`root.then(literal("action").executes(...))` for `/yourprefix action`.
Aliases redirect to the same command tree. Duplicate or invalid prefixes are
rejected, and fresh trees are built whenever Fabric creates a client dispatcher.
`/jasbm` and `/jasbm config` continue opening the main config menu.
