package dev.norhythm.justanothersbmod.visitors;

import static dev.norhythm.justanothersbmod.visitors.VisitorDefinition.Island.CRYSTAL_HOLLOWS;
import static dev.norhythm.justanothersbmod.visitors.VisitorDefinition.Island.CRIMSON_ISLE;
import static dev.norhythm.justanothersbmod.visitors.VisitorDefinition.Island.DEEP_CAVERNS;
import static dev.norhythm.justanothersbmod.visitors.VisitorDefinition.Island.DUNGEON_HUB;
import static dev.norhythm.justanothersbmod.visitors.VisitorDefinition.Island.DWARVEN_MINES;
import static dev.norhythm.justanothersbmod.visitors.VisitorDefinition.Island.FARMING_ISLAND;
import static dev.norhythm.justanothersbmod.visitors.VisitorDefinition.Island.GOLD_MINE;
import static dev.norhythm.justanothersbmod.visitors.VisitorDefinition.Island.HUB;
import static dev.norhythm.justanothersbmod.visitors.VisitorDefinition.Island.SPIDERS_DEN;
import static dev.norhythm.justanothersbmod.visitors.VisitorDefinition.Island.THE_END;
import static dev.norhythm.justanothersbmod.visitors.VisitorDefinition.Island.THE_PARK;
import static dev.norhythm.justanothersbmod.visitors.VisitorDefinition.Island.WINTER_ISLAND;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Garden visitors with a current static NPC location. Coordinates were checked
 * against the Hypixel SkyBlock Wiki for the 26.2 release.
 *
 * <p>Odawa and Xalx are intentionally absent: their Crystal Hollows structures
 * are generated at different coordinates in every lobby. Visitors that exist
 * only in the Garden, dungeons, or another non-static context are absent too.
 * Wiki rows marked More Info Needed use the requirement label "Unknown".</p>
 */
public final class VisitorCatalog {
    public static final List<VisitorDefinition> ALL = List.of(
            v("Adventurer", "Combat Merchant", HUB, -49.5, 69, -66.5, "Garden III; talk to the Combat Merchant", "Combat Merchant", "Adventurer"),
            v("Alchemage", "Alchemage", CRIMSON_ISLE, -86, 118, -771, "Unknown", "Alchemage"),
            v("Alchemist", "Alchemist", HUB, 80.5, 72, -90.5, "Garden X; talk to the Alchemist", "Alchemist"),
            v("An", "An", CRIMSON_ISLE, -622, 108, -742, "Unknown", "An"),
            v("Anita", "Anita", HUB, -15.5, 74, -68.5, "Garden II; talk to Anita", "Anita"),
            v("Archaeologist", "Archaeologist", SPIDERS_DEN, -360, 111, -290, "Unknown", "Archaeologist"),
            v("Arthur", "Arthur", HUB, 53.5, 72, -111.5, "Talk to Arthur", "Arthur"),
            v("Baker", "Baker", HUB, -7, 70, -48, "Garden I", "Baker"),
            v("Bartender", "Bartender", HUB, -74.5, 73, -124.5, "Talk to the Bartender", "Bartender"),
            v("Bednom", "Bednom", DWARVEN_MINES, -31, 214, -90, "Unknown", "Bednom"),
            v("Bruuh", "Bruuh", CRIMSON_ISLE, -635, 124, -688, "Unknown", "Bruuh"),
            v("Carpenter", "Carpenter", HUB, -137.5, 74, -41.5, "Unknown", "Carpenter"),
            v("Chantelle", "Chantelle", HUB, -83.5, 71, 11.5, "Unknown", "Chantelle"),
            v("Chief Scorn", "Chief Scorn", CRIMSON_ISLE, -580.5, 115.5, -687.5, "Unknown", "Chief Scorn"),
            v("Clerk Seraphine", "Clerk Seraphine", HUB, -1.5, 79, 10.5, "Garden V; talk to Clerk Seraphine", "Clerk Seraphine", "Seraphine"),
            v("Dusk", "Dusk", HUB, 20.5, 63, -135.5, "Garden VI; talk to Dusk", "Dusk"),
            v("Erihann", "Erihann", HUB, 42.5, 97, 93.5, "Unknown", "Erihann"),
            v("Fann", "Fann", HUB, 9.5, 73, -62.5, "Unknown", "Fann"),
            v("Farm Merchant", "Farm Merchant", HUB, 63.5, 72, -113.5, "Unknown", "Farm Merchant"),
            v("Fear Mongerer", "Fear Mongerer", HUB, -2, 70, -43, "Garden IV; available during the Spooky Festival", "Fear Mongerer"),
            v("Fisherman Gerald", "Fisherman Gerald", HUB, 118.5, 71, -32.5, "Fishing V; progress Gerald's quest", "Fisherman Gerald"),
            v("Guy", "Guy", HUB, 51.5, 78, 20.5, "Garden IX; talk to Guy", "Guy"),
            v("Hendrik", "Hendrik", HUB, 16.5, 62, -30.5, "Unknown", "Hendrik"),
            v("Hoppity", "Hoppity", HUB, 56.5, 70, -2.5, "Unknown", "Hoppity"),
            v("Jacob", "Jacob", HUB, 46.5, 73, -128.5, "Talk to Jacob", "Jacob"),
            v("Jacobus", "Jacobus", HUB, -49.5, 70, -60.5, "Unknown", "Jacobus"),
            v("Librarian", "Librarian", HUB, -68.5, 70, -79.5, "Garden V; talk to the Librarian", "Librarian"),
            v("Ludleth", "Ludleth", HUB, 81.5, 59, -112.5, "Unknown", "Ludleth"),
            v("Lumber Jack", "Lumberjack", HUB, -123.5, 73, -29.5, "Garden IX; talk to the Lumberjack", "Lumberjack", "Lumber Jack"),
            v("Madame Eleanor Q. Goldsworth III", "Madame Eleanor Q. Goldsworth III", HUB, 33.5, 73, 11.5, "Garden II; talk to Madame Eleanor", "Madame Eleanor Q. Goldsworth III"),
            v("Marco", "Marco", HUB, 90.5, 76, 3.5, "Unknown", "Marco"),
            v("Oringo", "Oringo", HUB, -4, 70, -44, "Talk to Oringo during the Traveling Zoo", "Oringo"),
            v("Plumber Joe", "Plumber Joe", HUB, 123.5, 74, -38.5, "Garden VII; talk to Plumber Joe", "Plumber Joe"),
            v("Seymour", "Seymour", HUB, 29, 66, -40, "Garden IV; talk to Seymour", "Seymour"),
            v("Shifty", "Shady Bartender", HUB, 114.5, 73, 175, "Garden VI; talk to the Shady Bartender", "Shady Bartender", "Shifty"),
            v("Sirius", "Sirius", HUB, 91, 75, 177, "Talk to Sirius during the Dark Auction", "Sirius"),
            v("Tia the Fairy", "Tia the Fairy", HUB, 119.5, 65, 147.5, "Talk to Tia the Fairy", "Tia the Fairy"),
            v("Tyashoi Alchemist", "Tyashoi Alchemist", HUB, 41, 68, -55, "Unknown", "Tyashoi Alchemist"),
            v("Vargul", "Vargul", HUB, -3, 74, 150, "Unknown", "Vargul"),
            v("Vincent", "Vincent", HUB, 79.5, 74, 53.5, "Garden I", "Vincent"),
            v("Weaponsmith", "Weaponsmith", HUB, -50.5, 69, -85.5, "Garden III; talk to the Weaponsmith", "Weaponsmith"),
            v("Wizard", "Wizard", HUB, 48.5, 119, 99.5, "Garden III; talk to the Wizard", "Wizard"),
            v("Zog", "Zog", HUB, 10, 69, -71, "Garden VII; talk to Zog", "Zog"),

            v("Beth", "Beth", FARMING_ISLAND, 163, 77, -359, "Talk to Beth", "Beth"),
            v("Farmer Jon", "Farmer Jon", FARMING_ISLAND, 167, 91, -598, "Talk to Farmer Jon", "Farmer Jon"),
            v("Farmhand", "Farmhand", FARMING_ISLAND, 144, 73, -240, "Garden III; talk to Farmhand", "Farmhand"),
            v("Friendly Hiker", "Friendly Hiker", FARMING_ISLAND, 181, 77, -381, "Talk to the Friendly Hiker", "Friendly Hiker"),
            v("Hungry Hiker", "Hungry Hiker", FARMING_ISLAND, 269, 48, -481, "Garden I", "Hungry Hiker"),
            v("Mason", "Mason", FARMING_ISLAND, 177, 77, -356, "Talk to Mason", "Mason"),
            v("Moby", "Moby", FARMING_ISLAND, 206, 43, -500, "Unknown", "Moby"),
            v("Tammy", "Tammy", FARMING_ISLAND, 284, 104, -545, "Garden VII; talk to Tammy", "Tammy"),
            v("Trevor", "Trevor", FARMING_ISLAND, 281, 104, -543, "Talk to Trevor", "Trevor"),

            v("Grandma Wolf", "Grandma Wolf", SPIDERS_DEN, -282, 121, -191, "Talk to Grandma Wolf", "Grandma Wolf"),
            v("Shaggy", "Shaggy", SPIDERS_DEN, -278, 121, -182, "Talk to Shaggy", "Shaggy"),
            v("Spider Tamer", "Spider Tamer", SPIDERS_DEN, -298, 62, -194, "Unknown", "Spider Tamer"),

            v("Gold Forger", "Gold Forger", GOLD_MINE, -27, 74, -294, "Garden II; talk to the Gold Forger", "Gold Forger"),
            v("Iron Forger", "Iron Forger", GOLD_MINE, -2, 75, -308, "Garden II; talk to the Iron Forger", "Iron Forger"),
            v("Lazy Miner", "Lazy Miner", GOLD_MINE, -11, 78, -337, "Garden VIII; talk to the Lazy Miner", "Lazy Miner"),
            v("Rusty", "Rusty", GOLD_MINE, -19, 78, -325.5, "Talk to Rusty", "Rusty"),

            v("Lift Operator", "Lift Operator", DEEP_CAVERNS, 45.5, 150, 15.5, "Unknown", "Lift Operator"),
            v("Rhys", "Rhys", DEEP_CAVERNS, 31.5, 12, 14.5, "Garden IV; talk to Rhys", "Rhys"),

            v("Banker Broadjaw", "Banker Broadjaw", DWARVEN_MINES, 13, 201, -149, "Garden VII; talk to Banker Broadjaw", "Banker Broadjaw"),
            v("Dalbrek", "Dalbrek", DWARVEN_MINES, 190, 216, 154, "Talk to Dalbrek", "Dalbrek"),
            v("Dulin", "Dulin", DWARVEN_MINES, 80.25, 187.3, 127.5, "Unknown", "Dulin"),
            v("Emissary Carlton", "Emissary Carlton", DWARVEN_MINES, -73, 153, -11, "Garden VIII; Commission Milestone I", "Emissary Carlton"),
            v("Emissary Ceanna", "Emissary Ceanna", DWARVEN_MINES, 42, 134, 22, "Garden III; Commission Milestone I", "Emissary Ceanna"),
            v("Emissary Fraiser", "Emissary Fraiser", DWARVEN_MINES, -132, 174, -50, "Garden VI; Commission Milestone I", "Emissary Fraiser"),
            v("Emissary Wilson", "Emissary Wilson", DWARVEN_MINES, 171, 150, 31, "Garden II; Commission Milestone I", "Emissary Wilson"),
            v("Cold Enjoyer", "Cold Enjoyer", DWARVEN_MINES, -15, 137, 217, "Unknown", "Cold Enjoyer"),
            v("Fragilis", "Fragilis", DWARVEN_MINES, 88, 199, -108, "Garden VII; talk to Fragilis", "Fragilis"),
            v("Gimley", "Gimley", DWARVEN_MINES, 29, 202, -148, "Talk to Gimley", "Gimley"),
            v("Gwendolyn", "Gwendolyn", DWARVEN_MINES, 88.5, 198, -98.5, "Talk to Gwendolyn", "Gwendolyn"),
            v("Hornum", "Hornum", DWARVEN_MINES, 34, 202, -145, "Talk to Hornum", "Hornum"),
            v("Jotraeline Greatforge", "Jotraeline Greatforge", DWARVEN_MINES, -6, 145, -18, "Talk to Jotraeline Greatforge", "Jotraeline Greatforge"),
            v("Lumina", "Lumina", DWARVEN_MINES, 77, 199, -110, "Garden IV; talk to Lumina", "Lumina"),
            v("Marigold", "Marigold", DWARVEN_MINES, 181.5, 150, 60.5, "Unknown", "Marigold"),
            v("Old Man Garry", "Old Man Garry", DWARVEN_MINES, 5, 200, -109, "Garden VIII; talk to Old Man Garry", "Old Man Garry"),
            v("Puzzler", "Puzzler", DWARVEN_MINES, 181, 196, 135, "Garden II; talk to the Puzzler", "Puzzler"),
            v("Queen Mismyla", "Queen Mismyla", DWARVEN_MINES, 126, 195, 195, "Garden II; talk to Queen Mismyla", "Queen Mismyla"),
            v("Royal Resident", "Royal Resident", DWARVEN_MINES, 62, 204, 200, "Garden VII", "Royal Resident"),
            v("Resident Neighbor", "Resident Neighbor", DWARVEN_MINES, 165, 202, 279, "Talk to the Royal Resident (Neighbor)", "Resident Neighbor", "Royal Resident"),
            v("Resident Snooty", "Resident Snooty", DWARVEN_MINES, 92, 202, 277, "Talk to the Royal Resident (Snooty)", "Resident Snooty", "Royal Resident"),
            v("Sargwyn", "Sargwyn", DWARVEN_MINES, 34, 202, -150, "Talk to Sargwyn", "Sargwyn"),
            v("Scout Scardius", "Scout Scardius", DWARVEN_MINES, -16, 121, 232, "Unknown", "Scout Scardius", "Scardius"),
            v("Tarwen", "Tarwen", DWARVEN_MINES, 34, 202, -140, "Talk to Tarwen", "Tarwen"),

            v("Chunk", "Chunk", CRYSTAL_HOLLOWS, 797, 128, 708, "Unknown", "Chunk"),
            v("Emissary Sisko", "Emissary Sisko", CRYSTAL_HOLLOWS, 495, 106, 556, "Heart of the Mountain IV; talk to Emissary Sisko", "Emissary Sisko"),
            v("Gemma", "Gemma", CRYSTAL_HOLLOWS, 475.5, 106, 513.5, "Unknown", "Gemma"),
            v("Geonathan Greatforge", "Geonathan Greatforge", CRYSTAL_HOLLOWS, 530, 106, 556, "Reach the Crystal Nucleus; talk to Geonathan", "Geonathan Greatforge"),

            v("Elle", "Elle", CRIMSON_ISLE, -364.5, 80, -476.5, "Unknown", "Elle"),
            v("Queen Nyx", "Queen Nyx", CRIMSON_ISLE, -117.5, 105, -754.5, "Unknown", "Queen Nyx"),

            v("Master Tactician Funk", "Master Tactician Funk", THE_PARK, -452.5, 110, 29.5, "Unknown", "Master Tactician Funk"),
            v("Old Shaman Nyko", "Old Shaman Nyko", THE_PARK, -370.5, 84, -64.5, "Unknown", "Old Shaman Nyko"),
            v("Romero", "Romero", THE_PARK, -456.5, 100, 30.5, "Unknown", "Romero"),
            v("Ryan", "Ryan", THE_PARK, -364.5, 102.5, -90.5, "Unknown", "Ryan"),

            v("Pearl Dealer", "Pearl Dealer", THE_END, -505, 101, -285, "Unknown", "Pearl Dealer"),
            v("Tyzzo", "Tyzzo", THE_END, -597, 5, -272, "Unknown", "Tyzzo"),

            v("Ophelia", "Ophelia", DUNGEON_HUB, -85.5, 55, -139.5, "Unknown", "Ophelia"),

            v("Frozen Alex", "Frozen Alex", WINTER_ISLAND, -43, 84, -1, "Unknown", "Frozen Alex"),
            v("Gary", "Gary", WINTER_ISLAND, 53, 103, 56, "Unknown", "Gary"),
            v("Sherry", "Sherry", WINTER_ISLAND, 6.5, 76, 95.5, "Unknown", "Sherry"),
            v("St. Jerry", "St. Jerry", WINTER_ISLAND, -22, 76, 92, "Unknown", "St. Jerry"),
            v("Terry", "Terry", WINTER_ISLAND, -92, 78, 25, "Garden VI; talk to Terry", "Terry"));

    public static final Map<String, VisitorDefinition> BY_NORMALIZED_NAME = ALL.stream()
            .collect(Collectors.toUnmodifiableMap(v -> normalize(v.visitorName()), Function.identity()));

    private VisitorCatalog() {
    }

    public static String normalize(String value) {
        return value.strip().toLowerCase(Locale.ROOT);
    }

    private static VisitorDefinition v(String visitorName, String npcName, VisitorDefinition.Island island,
            double x, double y, double z, String requirement, String... dialogueNames) {
        return new VisitorDefinition(visitorName, npcName, island, x, y, z, requirement, dialogueNames);
    }
}
