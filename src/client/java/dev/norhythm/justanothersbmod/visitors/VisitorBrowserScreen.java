package dev.norhythm.justanothersbmod.visitors;

import java.util.Comparator;
import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import dev.norhythm.justanothersbmod.visitors.VisitorStateStore.State;

/** Scrollable, filterable overview of the profile's visitor unlock state. */
public final class VisitorBrowserScreen extends Screen {
    private final Screen parent;
    private Filter filter = Filter.ALL;
    private VisitorList list;
    private Button filterButton;
    private Button routeButton;
    private Button eventButton;
    private Button mayorButton;
    private EditBox searchBox;
    private String search = "";

    public VisitorBrowserScreen(Screen parent) {
        super(Component.literal("Garden Visitor Browser"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int buttonWidth = Math.min(150, (this.width - 18) / 2);
        this.filterButton = addRenderableWidget(Button.builder(filter.label(), button -> cycleFilter())
                .bounds(this.width / 2 - buttonWidth - 3, 24, buttonWidth, 20)
                .build());
        this.routeButton = addRenderableWidget(Button.builder(routeLabel(), button -> toggleRoute())
                .bounds(this.width / 2 + 3, 24, buttonWidth, 20)
                .build());
        this.eventButton = addRenderableWidget(Button.builder(eventLabel(), button -> toggleEvents())
                .bounds(this.width / 2 - buttonWidth - 3, 47, buttonWidth, 20)
                .build());
        this.mayorButton = addRenderableWidget(Button.builder(mayorLabel(), button -> toggleMayors())
                .bounds(this.width / 2 + 3, 47, buttonWidth, 20)
                .build());
        int searchWidth = Math.min(300, this.width - 32);
        this.searchBox = addRenderableWidget(new EditBox(this.font,
                (this.width - searchWidth) / 2, 71, searchWidth, 20, Component.literal("Search visitors")));
        this.searchBox.setHint(Component.literal("Search visitors..."));
        this.searchBox.setMaxLength(80);
        this.searchBox.setValue(this.search);
        this.searchBox.setResponder(value -> {
            this.search = value;
            refreshEntries();
        });
        this.list = addRenderableWidget(new VisitorList(this.minecraft, this.width, this.height - 127, 97, 44));
        refreshEntries();
    }

    private void toggleRoute() {
        VisitorStateStore.setRouteEnabled(!VisitorStateStore.isRouteEnabled());
        this.routeButton.setMessage(routeLabel());
    }

    private Component routeLabel() {
        return Component.literal("Route: " + (VisitorStateStore.isRouteEnabled() ? "On" : "Off"));
    }

    private void toggleEvents() {
        VisitorStateStore.setEventWaypointsEnabled(!VisitorStateStore.isEventWaypointsEnabled());
        this.eventButton.setMessage(eventLabel());
    }

    private Component eventLabel() {
        return Component.literal("Events: "
                + (VisitorStateStore.isEventWaypointsEnabled() ? "On" : "Off"));
    }

    private void toggleMayors() {
        VisitorStateStore.setMayorWaypointsEnabled(!VisitorStateStore.isMayorWaypointsEnabled());
        this.mayorButton.setMessage(mayorLabel());
    }

    private Component mayorLabel() {
        return Component.literal("Mayors: "
                + (VisitorStateStore.isMayorWaypointsEnabled() ? "On" : "Off"));
    }

    private void cycleFilter() {
        this.filter = this.filter.next();
        this.filterButton.setMessage(this.filter.label());
        refreshEntries();
    }

    private void refreshEntries() {
        String profile = SkyBlockContext.profile();
        List<VisitorEntry> entries = VisitorCatalog.ALL.stream()
                .filter(visitor -> filter.includes(VisitorStateStore.get(profile, visitor)))
                .filter(this::matchesSearch)
                .sorted(Comparator.comparing(VisitorDefinition::visitorName, String.CASE_INSENSITIVE_ORDER))
                .map(visitor -> new VisitorEntry(visitor, this.font))
                .toList();
        this.list.replaceEntries(entries);
        this.list.setScrollAmount(0);
    }

    private boolean matchesSearch(VisitorDefinition visitor) {
        String query = this.search.strip().toLowerCase(java.util.Locale.ROOT);
        if (query.isEmpty()) {
            return true;
        }
        String syncedRequirement = VisitorStateStore.getRequirement(SkyBlockContext.profile(), visitor);
        return visitor.visitorName().toLowerCase(java.util.Locale.ROOT).contains(query)
                || visitor.npcName().toLowerCase(java.util.Locale.ROOT).contains(query)
                || VisitorEntry.islandName(visitor.island()).toLowerCase(java.util.Locale.ROOT).contains(query)
                || syncedRequirement != null && syncedRequirement.toLowerCase(java.util.Locale.ROOT).contains(query)
                || VisitorAvailability.label(visitor).toLowerCase(java.util.Locale.ROOT).contains(query);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks) {
        super.extractRenderState(graphics, mouseX, mouseY, deltaTicks);
        graphics.centeredText(this.font, this.title, this.width / 2, 8, 0xFFFFFFFF);
        graphics.centeredText(this.font,
                Component.literal(this.list.children().size() + " visitors • Profile: " + SkyBlockContext.profile())
                        .withStyle(ChatFormatting.GRAY),
                this.width / 2, this.height - 24, 0xFFAAAAAA);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreenAndShow(this.parent);
    }

    private enum Filter {
        ALL("Filter: All"),
        LOCKED("Filter: Locked"),
        UNLOCKED("Filter: Unlocked");

        private final String label;

        Filter(String label) {
            this.label = label;
        }

        Component label() {
            return Component.literal(label);
        }

        Filter next() {
            return values()[(ordinal() + 1) % values().length];
        }

        boolean includes(State state) {
            return this == ALL || this == UNLOCKED && state == State.UNLOCKED
                    || this == LOCKED && state != State.UNLOCKED;
        }
    }

    private static final class VisitorList extends ObjectSelectionList<VisitorEntry> {
        VisitorList(Minecraft minecraft, int width, int height, int y, int entryHeight) {
            super(minecraft, width, height, y, entryHeight);
        }

        @Override
        public int getRowWidth() {
            return Math.min(520, this.width - 32);
        }
    }

    private static final class VisitorEntry extends ObjectSelectionList.Entry<VisitorEntry> {
        private final VisitorDefinition visitor;
        private final Font font;

        VisitorEntry(VisitorDefinition visitor, Font font) {
            this.visitor = visitor;
            this.font = font;
        }

        @Override
        public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered,
                float deltaTicks) {
            State state = VisitorStateStore.get(SkyBlockContext.profile(), visitor);
            String status = state == State.UNLOCKED ? "Unlocked" : "Locked";
            int statusColor = state == State.UNLOCKED ? 0xFF55FF55 : 0xFFFF5555;
            int x = getContentX() + 4;
            int y = getContentY() + 3;
            int right = getContentRight() - 4;

            if (hovered) {
                graphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0x3020A0FF);
            }
            graphics.text(font, visitor.visitorName(), x, y, 0xFFFFFFFF);
            graphics.text(font, status, right - font.width(status), y, statusColor);

            String details = islandName(visitor.island()) + "  •  " + format(visitor.x()) + ", "
                    + format(visitor.y()) + ", " + format(visitor.z());
            graphics.text(font, details, x, y + 13, 0xFFAAAAAA);
            String availability = VisitorAvailability.label(visitor);
            String syncedRequirement = VisitorStateStore.getRequirement(SkyBlockContext.profile(), visitor);
            String extra = syncedRequirement == null ? "" : "Requirement: " + syncedRequirement;
            if (!availability.equals("Always available")) {
                extra += (extra.isEmpty() ? "" : "  •  ") + availability;
            }
            if (!extra.isEmpty()) {
                graphics.text(font, extra, x, y + 26, 0xFFAAAAAA);
            }
        }

        @Override
        public Component getNarration() {
            State state = VisitorStateStore.get(SkyBlockContext.profile(), visitor);
            String syncedRequirement = VisitorStateStore.getRequirement(SkyBlockContext.profile(), visitor);
            return Component.literal(visitor.visitorName() + ", "
                    + (state == State.UNLOCKED ? "Unlocked" : "Locked") + ", "
                    + islandName(visitor.island()) + ", coordinates " + format(visitor.x()) + ", "
                    + format(visitor.y()) + ", " + format(visitor.z())
                    + (syncedRequirement == null ? "" : ", requirement: " + syncedRequirement)
                    + ", availability: " + VisitorAvailability.label(visitor));
        }

        private static String format(double coordinate) {
            return coordinate == Math.rint(coordinate)
                    ? Long.toString(Math.round(coordinate))
                    : Double.toString(coordinate);
        }

        private static String islandName(VisitorDefinition.Island island) {
            return switch (island) {
                case HUB -> "Hub";
                case FARMING_ISLAND -> "The Farming Islands";
                case THE_PARK -> "The Park";
                case SPIDERS_DEN -> "Spider's Den";
                case THE_END -> "The End";
                case CRIMSON_ISLE -> "Crimson Isle";
                case GOLD_MINE -> "Gold Mine";
                case DEEP_CAVERNS -> "Deep Caverns";
                case DWARVEN_MINES -> "Dwarven Mines";
                case CRYSTAL_HOLLOWS -> "Crystal Hollows";
                case DUNGEON_HUB -> "Dungeon Hub";
                case WINTER_ISLAND -> "Jerry's Workshop";
            };
        }
    }
}
