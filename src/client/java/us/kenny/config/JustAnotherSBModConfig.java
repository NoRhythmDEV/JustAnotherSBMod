package us.kenny.config;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.ChromaColour;
import io.github.notenoughupdates.moulconfig.Config;
import io.github.notenoughupdates.moulconfig.annotations.*;
import io.github.notenoughupdates.moulconfig.common.text.StructuredText;
import us.kenny.visitors.VisitorBrowser;

/** Three top-level categories, with one subcategory for each feature. */
public final class JustAnotherSBModConfig extends Config {
    @Expose public String configId = "justanothersbmod";
    @Expose @Category(name = "Farming", desc = "Garden visitors and farming controls.")
    public Farming farming = new Farming();
    @Expose @Category(name = "Mining", desc = "Crystal Hollows mining helpers.")
    public Mining mining = new Mining();
    @Expose @Category(name = "Misc", desc = "Keybinding utilities and interface settings.")
    public Misc misc = new Misc();

    @Override public StructuredText getTitle() { return StructuredText.of("JustAnotherSBMod"); }

    public static final class Farming {
        @Expose @Category(name = "Visitor Waypoints", desc = "Find and unlock Garden visitors.")
        public Visitors visitors = new Visitors();
        @Expose @Category(name = "Visitor Route", desc = "Visual navigation to missing visitors.")
        public Route route = new Route();
        @Expose @Category(name = "Hold / Toggle", desc = "Configure alternative farming keybinds in Controls.")
        public Toggles toggles = new Toggles();
    }
    public static final class Visitors {
        @Expose @ConfigOption(name = "Enabled", desc = "Enable visitor waypoints, browser, and unlock tracking.") @ConfigEditorBoolean
        public boolean enabled = true;
        @Expose @ConfigOption(name = "Waypoint Labels", desc = "Show visitor names and distances.") @ConfigEditorBoolean
        public boolean labels = true;
        @Expose @ConfigOption(name = "Beacon Beams", desc = "Show vertical visitor beacon beams.") @ConfigEditorBoolean
        public boolean beams = true;
        @Expose @ConfigOption(name = "Through Walls", desc = "Keep waypoint labels and beam markers visible through terrain.") @ConfigEditorBoolean
        public boolean throughWalls = true;
        @Expose @ConfigOption(name = "Show Requirements", desc = "Show learned requirements while holding the requirements key.") @ConfigEditorBoolean
        public boolean requirements = true;
        @Expose @ConfigOption(name = "Logbook Sync", desc = "Read visitor unlock states from opened Visitor's Logbook pages.") @ConfigEditorBoolean
        public boolean logbookSync = true;
        @Expose @ConfigOption(name = "Interaction Sync", desc = "Mark visitors unlocked when you interact or receive their dialogue.") @ConfigEditorBoolean
        public boolean interactionSync = true;
        @Expose @ConfigOption(name = "Event Visitors", desc = "Show event-only NPCs when you want to visit them.") @ConfigEditorBoolean
        public boolean events = false;
        @Expose @ConfigOption(name = "Mayor Visitors", desc = "Show static visitors that require a mayor perk.") @ConfigEditorBoolean
        public boolean mayors = false;
        @Expose @ConfigOption(name = "Waypoint Color", desc = "Color of ordinary visitor beams and labels.") @ConfigEditorColour
        public ChromaColour color = ChromaColour.fromStaticRGB(0, 229, 255, 255);
        @Expose @ConfigOption(name = "Open Browser", desc = "View and search visitor unlock states.") @ConfigEditorButton(buttonText = "Open")
        public transient Runnable browser = VisitorBrowser::requestOpen;
    }
    public static final class Route {
        @Expose @ConfigOption(name = "Enabled", desc = "Select a missing visitor as your route target.") @ConfigEditorBoolean
        public boolean enabled = true;
        @Expose @ConfigOption(name = "HUD Tracer", desc = "Draw a line from the crosshair to the route target.") @ConfigEditorBoolean
        public boolean tracer = true;
        @Expose @ConfigOption(name = "HUD Label", desc = "Show the selected visitor's name and distance on the HUD.") @ConfigEditorBoolean
        public boolean label = true;
        @Expose @ConfigOption(name = "Tracer Color", desc = "Color of the route HUD line.") @ConfigEditorColour
        public ChromaColour tracerColor = ChromaColour.fromStaticRGB(0, 229, 255, 224);
        @Expose @ConfigOption(name = "Selected Color", desc = "Color of the selected visitor's world waypoint.") @ConfigEditorColour
        public ChromaColour selectedColor = ChromaColour.fromStaticRGB(255, 170, 0, 255);
        @Expose @ConfigOption(name = "Label Scale", desc = "Scale multiplier for the route label.") @ConfigEditorSlider(minValue = 0.5f, maxValue = 2f, minStep = 0.1f)
        public float labelScale = 1f;
    }
    public static final class Toggles {
        @Expose @ConfigOption(name = "Direct Toggles", desc = "Allow alternative bindings to hold an action until pressed again.") @ConfigEditorBoolean
        public boolean enabled = true;
        @Expose @ConfigOption(name = "Toggle Mode Shortcuts", desc = "Enable keybinds that switch Minecraft actions between Hold and Toggle modes.") @ConfigEditorBoolean
        public boolean shortcuts = true;
    }
    public static final class Mining {
        @Expose @Category(name = "Powder Chest Particles", desc = "Highlight where to aim when picking treasure chest locks.")
        public Powder powder = new Powder();
    }
    public enum ChestMode {
        LOOKED_AT("Looked-at chest"), ALL_NEARBY("All nearby chests");
        private final String label;
        ChestMode(String label) { this.label = label; }
        @Override public String toString() { return label; }
    }
    public static final class Powder {
        @Expose @ConfigOption(name = "Enabled", desc = "Highlight server lockpicking particles near treasure chests.") @ConfigEditorBoolean
        public boolean enabled = true;
        @Expose @ConfigOption(name = "Crystal Hollows Only", desc = "Restrict detection to Hypixel's Crystal Hollows. Disable for local testing.") @ConfigEditorBoolean
        public boolean crystalHollowsOnly = true;
        @Expose @ConfigOption(name = "Display Mode", desc = "Show the chest under your crosshair or every nearby tracked chest.") @ConfigEditorDropdown
        public ChestMode mode = ChestMode.LOOKED_AT;
        @Expose @ConfigOption(name = "Particle Color", desc = "Color and opacity of the lockpicking target marker.") @ConfigEditorColour
        public ChromaColour color = ChromaColour.fromStaticRGB(85, 255, 85, 255);
        @Expose @ConfigOption(name = "Marker Size", desc = "Width of the target marker in blocks.") @ConfigEditorSlider(minValue = 0.04f, maxValue = 0.4f, minStep = 0.01f)
        public float markerSize = 0.12f;
        @Expose @ConfigOption(name = "Through Chest", desc = "Show targets on the back and opposite sides through the chest and terrain.") @ConfigEditorBoolean
        public boolean throughWalls = true;
        @Expose @ConfigOption(name = "Chest Outline", desc = "Draw an outline around chests with a current particle target.") @ConfigEditorBoolean
        public boolean outline = false;
        @Expose @ConfigOption(name = "Outline Color", desc = "Color and opacity of chest outlines.") @ConfigEditorColour
        public ChromaColour outlineColor = ChromaColour.fromStaticRGB(85, 255, 85, 128);
        @Expose @ConfigOption(name = "Hide Original Particles", desc = "Hide only particle packets matched to a nearby chest while the helper is active.") @ConfigEditorBoolean
        public boolean hideOriginal = false;
        @Expose @ConfigOption(name = "Detection Range", desc = "Maximum distance from you to a tracked chest, in blocks.") @ConfigEditorSlider(minValue = 3f, maxValue = 24f, minStep = 1f)
        public float range = 8f;
        @Expose @ConfigOption(name = "Target Expiry", desc = "Seconds without new particles before hiding a stale target. Updates replace old positions immediately.") @ConfigEditorSlider(minValue = 0.1f, maxValue = 2f, minStep = 0.1f)
        public float expirySeconds = 0.5f;
    }
    public static final class Misc {
        @Expose @Category(name = "Keybindings", desc = "Alternative bindings and modifier chords.")
        public Bindings bindings = new Bindings();
        @Expose @Category(name = "Interface", desc = "Settings and keybind label display.")
        public Interface ui = new Interface();
    }
    public static final class Bindings {
        @Expose @ConfigOption(name = "Alternative Bindings", desc = "Enable existing additional bindings. Configure keys with the + buttons in Controls.") @ConfigEditorBoolean
        public boolean enabled = true;
        @Expose @ConfigOption(name = "Modifier Chords", desc = "Enable configured Shift, Ctrl, and Alt modifiers. Saved chords are retained when disabled.") @ConfigEditorBoolean
        public boolean modifiers = true;
    }
    public static final class Interface {
        @Expose @ConfigOption(name = "Fit Keybind Labels", desc = "Shrink long button labels so modifier chords fit.") @ConfigEditorBoolean
        public boolean fitLabels = true;
        @Expose @ConfigOption(name = "Wide Config", desc = "Use MoulConfig's wide editor layout next time settings open.") @ConfigEditorBoolean
        public boolean wide = false;
    }
}
