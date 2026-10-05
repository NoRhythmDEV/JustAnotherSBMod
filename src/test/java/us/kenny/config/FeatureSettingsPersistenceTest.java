package us.kenny.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FeatureSettingsPersistenceTest {
    private final FeatureSettingsMapper mapper = new FeatureSettingsMapper();

    @Test void preservesColorsModeAndDisabledFeatures() {
        var config = new JustAnotherSBModConfig();
        config.mining.powder.mode = JustAnotherSBModConfig.ChestMode.ALL_NEARBY;
        config.mining.powder.enabled = false;
        config.mining.powder.expirySeconds = 1.3f;
        config.farming.route.enabled = false;
        config.misc.bindings.enabled = false;
        String json = mapper.serialize(config);
        var restored = mapper.deserialize(json);
        assertEquals(JustAnotherSBModConfig.ChestMode.ALL_NEARBY, restored.mining.powder.mode);
        assertFalse(restored.mining.powder.enabled);
        assertEquals(1.3f, restored.mining.powder.expirySeconds);
        assertFalse(restored.farming.route.enabled);
        assertFalse(restored.misc.bindings.enabled);
        assertEquals(0xFF55FF55, restored.mining.powder.color.getEffectiveColourRGB());
        assertEquals(0x8055FF55, restored.mining.powder.outlineColor.getEffectiveColourRGB());
        assertFalse(json.contains("browser"));
        assertFalse(json.contains("saveRunnables"));
        assertNotNull(restored.farming.visitors.browser);
    }

    @Test void missingSettingsRetainDefaults() {
        var config = mapper.deserialize("{\"mining\":{\"powder\":{\"outline\":true}}}");
        assertTrue(config.mining.powder.outline);
        assertTrue(config.mining.powder.throughWalls);
        assertEquals(JustAnotherSBModConfig.ChestMode.LOOKED_AT, config.mining.powder.mode);
        assertTrue(config.farming.visitors.enabled);
        assertTrue(config.misc.bindings.enabled);
    }
    @Test void retainsUnknownSettingsWithoutUndoingUserEdits() {
        var config = mapper.deserialize("{\"future\":42,\"mining\":{\"powder\":{\"enabled\":false,\"futureColor\":\"blue\"}}}");
        config.mining.powder.enabled = true;
        var json = com.google.gson.JsonParser.parseString(mapper.serialize(config)).getAsJsonObject();
        assertEquals(42, json.get("future").getAsInt());
        var powder = json.getAsJsonObject("mining").getAsJsonObject("powder");
        assertEquals("blue", powder.get("futureColor").getAsString());
        assertTrue(powder.get("enabled").getAsBoolean());
    }

    @Test void rejectsInvalidCategoryColorAndDisplayMode() {
        assertFalse(mapper.valid("null"));
        assertFalse(mapper.valid("{\"mining\":null}"));
        assertFalse(mapper.valid("{\"mining\":{\"powder\":{\"color\":null}}}"));
        assertFalse(mapper.valid("{\"mining\":{\"powder\":{\"mode\":\"unknown\"}}}"));
    }

}
