package dev.norhythm.justanothersbmod.visitors;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import dev.norhythm.justanothersbmod.visitors.VisitorStateStore.State;

import java.util.List;
import java.util.Locale;
import java.util.ArrayList;

/** Reads the visible Visitor's Logbook page without clicking or changing it. */
public final class VisitorLogbookSync {
    private static AbstractContainerScreen<?> lastScreen;
    private static int lastFingerprint;

    private VisitorLogbookSync() {
    }

    public static void tick(Minecraft client) {
        if (!(client.gui.screen() instanceof AbstractContainerScreen<?> screen)
                || !screen.getTitle().getString().contains("Visitor's Logbook")) {
            lastScreen = null;
            lastFingerprint = 0;
            return;
        }

        int containerSlots = Math.max(0, screen.getMenu().slots.size() - 36);
        int fingerprint = 1;
        for (int i = 0; i < containerSlots; i++) {
            ItemStack stack = screen.getMenu().slots.get(i).getItem();
            fingerprint = 31 * fingerprint + stack.getHoverName().getString().hashCode();
            ItemLore lore = stack.get(DataComponents.LORE);
            if (lore != null) {
                fingerprint = 31 * fingerprint + lore.lines().hashCode();
            }
        }
        if (screen == lastScreen && fingerprint == lastFingerprint) {
            return;
        }
        lastScreen = screen;
        lastFingerprint = fingerprint;

        boolean changed = false;
        for (int i = 0; i < containerSlots; i++) {
            Slot slot = screen.getMenu().slots.get(i);
            ItemStack stack = slot.getItem();
            VisitorDefinition visitor = VisitorCatalog.BY_NORMALIZED_NAME.get(
                    VisitorCatalog.normalize(stack.getHoverName().getString()));
            if (visitor == null) {
                continue;
            }

            List<Component> lore = lore(stack);
            boolean unlocked = lore.stream().map(Component::getString)
                    .anyMatch(line -> line.contains("Times Visited:") || line.contains("Offers Accepted:"));
            State state = unlocked ? State.UNLOCKED : hasOtherUnmetRequirement(lore, visitor)
                    ? State.BLOCKED
                    : State.AVAILABLE;
            changed |= VisitorStateStore.set(SkyBlockContext.profile(), visitor, state);
            changed |= VisitorStateStore.setRequirement(SkyBlockContext.profile(), visitor,
                    unlocked ? null : extractRequirement(lore));
        }

        if (changed) {
            VisitorStateStore.save();
        }
    }

    private static String extractRequirement(List<Component> lore) {
        List<String> requirements = new ArrayList<>();
        boolean readingRequirement = false;
        for (Component lineComponent : lore) {
            String line = lineComponent.getString().strip();
            if (line.equalsIgnoreCase("Requirement:")) {
                readingRequirement = true;
                continue;
            }
            if (!readingRequirement || line.isEmpty()) {
                continue;
            }
            String cleaned = line.replaceFirst("^[✖✕✗❌\\s]+", "").strip();
            if (!cleaned.isEmpty()) {
                requirements.add(cleaned);
            }
        }
        return requirements.isEmpty() ? null : String.join("; ", requirements);
    }

    private static List<Component> lore(ItemStack stack) {
        ItemLore lore = stack.get(DataComponents.LORE);
        return lore == null ? List.of() : lore.lines();
    }

    private static boolean hasOtherUnmetRequirement(List<Component> lore, VisitorDefinition visitor) {
        for (Component lineComponent : lore) {
            String line = lineComponent.getString().strip();
            if (!isUnmet(line)) {
                continue;
            }
            String lower = line.toLowerCase(Locale.ROOT);
            boolean isTalkRequirement = lower.contains("talk to")
                    || visitor.dialogueNames().stream().anyMatch(name -> lower.contains(name.toLowerCase(Locale.ROOT)));
            if (!isTalkRequirement) {
                return true;
            }
        }
        return false;
    }

    private static boolean isUnmet(String line) {
        return line.contains("✖") || line.contains("✕") || line.contains("✗") || line.contains("❌");
    }
}
