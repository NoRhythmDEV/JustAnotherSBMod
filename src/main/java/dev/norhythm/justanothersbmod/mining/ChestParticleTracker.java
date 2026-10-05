package dev.norhythm.justanothersbmod.mining;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/** Pure tracking logic: one recent lock target per chest, with deterministic association. */
public final class ChestParticleTracker {
    public record Chest(int x, int y, int z) {}
    public record Point(double x, double y, double z) {
        public boolean finite() { return Double.isFinite(x) && Double.isFinite(y) && Double.isFinite(z); }
    }
    public record Target(Chest chest, Point point, long updatedAt) {}
    private final Map<Chest, Target> targets = new HashMap<>();

    public boolean accept(Point point, Collection<Chest> candidates, Chest lookedAt, long now) {
        if (!point.finite()) return false;
        Chest closest = null;
        double best = Double.POSITIVE_INFINITY;
        for (Chest chest : candidates) {
            double score = sideDistanceSquared(point, chest);
            if (!Double.isFinite(score)) continue;
            // Crosshair breaks a geometric tie only; it cannot steal a neighbour's particles.
            if (score < best - 1e-9 || Math.abs(score - best) < 1e-9 && chest.equals(lookedAt)) {
                best = score;
                closest = chest;
            }
        }
        if (closest == null) return false;
        targets.put(closest, new Target(closest, point, now));
        return true;
    }

    static double sideDistanceSquared(Point p, Chest chest) {
        double x = p.x - chest.x, y = p.y - chest.y, z = p.z - chest.z;
        // Vanilla chest body is inset 1/16 block, with a small allowance for particle jitter.
        if (y < -0.15 || y > 1.05 || x < -0.2 || x > 1.2 || z < -0.2 || z > 1.2)
            return Double.POSITIVE_INFINITY;
        double xSide = Math.min(Math.abs(x - 0.0625), Math.abs(x - 0.9375));
        double zSide = Math.min(Math.abs(z - 0.0625), Math.abs(z - 0.9375));
        double side = Math.min(xSide, zSide);
        if (side > 0.22) return Double.POSITIVE_INFINITY;
        double outsideX = Math.max(0, Math.max(0.0625 - x, x - 0.9375));
        double outsideZ = Math.max(0, Math.max(0.0625 - z, z - 0.9375));
        return side * side + outsideX * outsideX + outsideZ * outsideZ;
    }

    public void expire(long now, long lifetime, Predicate<Chest> valid) {
        targets.values().removeIf(target -> now - target.updatedAt >= lifetime || !valid.test(target.chest));
    }
    public List<Target> visible(Chest lookedAt, boolean allNearby) {
        if (allNearby) return List.copyOf(targets.values());
        Target target = targets.get(lookedAt);
        return target == null ? List.of() : List.of(target);
    }
    public void remove(Chest chest) { targets.remove(chest); }
    public void clear() { targets.clear(); }
}
