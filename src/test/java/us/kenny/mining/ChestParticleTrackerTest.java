package us.kenny.mining;

import org.junit.jupiter.api.Test;
import java.util.List;
import us.kenny.mining.ChestParticleTracker.Chest;
import us.kenny.mining.ChestParticleTracker.Point;
import static org.junit.jupiter.api.Assertions.*;

class ChestParticleTrackerTest {
    private final Chest chest = new Chest(10, 64, -20);
    private final ChestParticleTracker tracker = new ChestParticleTracker();

    @Test void acceptsEverySideAtDifferentHeights() {
        for (Point point : List.of(new Point(10, 64.2, -19.7), new Point(11, 64.8, -19.4),
                new Point(10.2, 64.5, -20), new Point(10.8, 64.4, -19))) {
            assertTrue(tracker.accept(point, List.of(chest), chest, 100));
            assertEquals(point, tracker.visible(chest, false).getFirst().point());
        }
    }

    @Test void neighbourCannotStealParticleEvenWhenLookedAt() {
        Chest neighbour = new Chest(11, 64, -20);
        Point point = new Point(11.9375, 64.6, -19.5);
        assertTrue(tracker.accept(point, List.of(chest, neighbour), chest, 100));
        assertTrue(tracker.visible(chest, false).isEmpty());
        assertEquals(neighbour, tracker.visible(neighbour, false).getFirst().chest());
    }

    @Test void sharedFaceUsesCrosshairToBreakTie() {
        Chest neighbour = new Chest(11, 64, -20);
        assertTrue(tracker.accept(new Point(11, 64.5, -19.5), List.of(chest, neighbour), neighbour, 100));
        assertEquals(neighbour, tracker.visible(null, true).getFirst().chest());
    }

    @Test void tracksMultipleChestsAndReplacesMovingTargetImmediately() {
        Chest neighbour = new Chest(12, 64, -20);
        Point first = new Point(10, 64.5, -19.5), moved = new Point(10.7, 64.8, -19);
        tracker.accept(first, List.of(chest, neighbour), chest, 100);
        tracker.accept(new Point(12, 64.5, -19.5), List.of(chest, neighbour), chest, 110);
        tracker.accept(moved, List.of(chest, neighbour), chest, 120);
        assertEquals(2, tracker.visible(null, true).size());
        assertEquals(moved, tracker.visible(chest, false).getFirst().point());
        assertEquals(120, tracker.visible(chest, false).getFirst().updatedAt());
        assertTrue(tracker.visible(null, false).isEmpty());
    }

    @Test void expiresIndependentlyAndDropsRemovedChests() {
        Chest neighbour = new Chest(12, 64, -20);
        tracker.accept(new Point(10, 64.5, -19.5), List.of(chest), chest, 100);
        tracker.accept(new Point(12, 64.5, -19.5), List.of(neighbour), neighbour, 400);
        tracker.expire(600, 500, c -> true);
        assertTrue(tracker.visible(chest, false).isEmpty());
        assertEquals(1, tracker.visible(null, true).size());
        tracker.expire(601, 500, c -> false);
        assertTrue(tracker.visible(null, true).isEmpty());
    }

    @Test void rejectsInvalidAndUnrelatedParticles() {
        assertFalse(tracker.accept(new Point(Double.NaN, 64, -20), List.of(chest), chest, 100));
        assertFalse(tracker.accept(new Point(10.5, 64.5, -19.5), List.of(chest), chest, 100));
        assertFalse(tracker.accept(new Point(10, 66, -19.5), List.of(chest), chest, 100));
        assertFalse(tracker.accept(new Point(10, 64.5, -19.5), List.of(), chest, 100));
        assertTrue(tracker.visible(null, true).isEmpty());
    }

    @Test void openingChestAndChangingWorldClearTargets() {
        tracker.accept(new Point(10, 64.5, -19.5), List.of(chest), chest, 100);
        tracker.remove(chest);
        assertTrue(tracker.visible(null, true).isEmpty());
        tracker.accept(new Point(10, 64.5, -19.5), List.of(chest), chest, 200);
        tracker.clear();
        assertTrue(tracker.visible(null, true).isEmpty());
    }
}
