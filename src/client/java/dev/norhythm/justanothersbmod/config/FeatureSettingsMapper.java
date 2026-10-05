package dev.norhythm.justanothersbmod.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.Category;
import io.github.notenoughupdates.moulconfig.managed.DataMapper;
import io.github.notenoughupdates.moulconfig.managed.GsonMapper;
import java.lang.reflect.Modifier;

/** Validates loaded settings and retains unrecognized settings across saves. */
public final class FeatureSettingsMapper implements DataMapper<JustAnotherSBModConfig> {
    private final GsonMapper<JustAnotherSBModConfig> delegate = new GsonMapper<>(JustAnotherSBModConfig.class);
    private JsonObject original = new JsonObject();

    @Override public JustAnotherSBModConfig createDefault() { return delegate.createDefault(); }

    @Override public JustAnotherSBModConfig deserialize(String text) {
        JsonObject parsed = JsonParser.parseString(text).getAsJsonObject();
        JustAnotherSBModConfig config = delegate.deserialize(text);
        validate(config);
        original = parsed.deepCopy();
        return config;
    }

    public boolean valid(String text) {
        try {
            JsonParser.parseString(text).getAsJsonObject();
            validate(delegate.deserialize(text));
            return true;
        } catch (RuntimeException error) { return false; }
    }

    private static void validate(Object settings) {
        if (settings == null) throw new IllegalArgumentException("Config object is null");
        for (var field : settings.getClass().getFields()) {
            if (field.getAnnotation(Expose.class) == null || Modifier.isTransient(field.getModifiers())) continue;
            try {
                Object value = field.get(settings);
                if (value == null) throw new IllegalArgumentException("Null config value: " + field.getName());
                if (value instanceof Float number && !Float.isFinite(number))
                    throw new IllegalArgumentException("Non-finite config value: " + field.getName());
                if (field.getAnnotation(Category.class) != null) validate(value);
            } catch (IllegalAccessException error) { throw new IllegalStateException(error); }
        }
    }

    @Override public String serialize(JustAnotherSBModConfig config) {
        JsonObject result = JsonParser.parseString(delegate.serialize(config)).getAsJsonObject();
        retainUnknown(result, original);
        return result.toString();
    }

    private static void retainUnknown(JsonObject destination, JsonObject source) {
        for (var entry : source.entrySet()) {
            if (!destination.has(entry.getKey())) destination.add(entry.getKey(), entry.getValue().deepCopy());
            else if (destination.get(entry.getKey()).isJsonObject() && entry.getValue().isJsonObject())
                retainUnknown(destination.getAsJsonObject(entry.getKey()), entry.getValue().getAsJsonObject());
        }
    }
}
