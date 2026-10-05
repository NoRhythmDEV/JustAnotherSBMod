package dev.norhythm.justanothersbmod.visitors;

import java.util.List;

/** A static, wiki-sourced location for a Garden visitor NPC. */
public record VisitorDefinition(
        String visitorName,
        String npcName,
        Island island,
        double x,
        double y,
        double z,
        String requirement,
        List<String> dialogueNames) {

    public VisitorDefinition(String visitorName, String npcName, Island island, double x, double y, double z,
            String requirement, String... dialogueNames) {
        this(visitorName, npcName, island, x, y, z, requirement, List.of(dialogueNames));
    }

    public enum Island {
        HUB("hub"),
        FARMING_ISLAND("farming_1"),
        THE_PARK("foraging_1"),
        SPIDERS_DEN("combat_1"),
        THE_END("combat_3"),
        CRIMSON_ISLE("crimson_isle"),
        GOLD_MINE("mining_1"),
        DEEP_CAVERNS("mining_2"),
        DWARVEN_MINES("mining_3"),
        CRYSTAL_HOLLOWS("crystal_hollows"),
        DUNGEON_HUB("dungeon_hub"),
        WINTER_ISLAND("winter");

        private final String mode;

        Island(String mode) {
            this.mode = mode;
        }

        public String mode() {
            return mode;
        }
    }
}
