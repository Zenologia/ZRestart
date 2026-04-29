package dev.zenologia.zrestart.placeholders;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class PlaceholderContext {
    private final Map<String, String> values;

    private PlaceholderContext(Map<String, String> values) {
        this.values = Map.copyOf(values);
    }

    public static PlaceholderContext empty() {
        return new PlaceholderContext(Map.of());
    }

    public static Builder builder() {
        return new Builder();
    }

    public PlaceholderContext with(String key, Object value) {
        LinkedHashMap<String, String> copy = new LinkedHashMap<>(this.values);
        copy.put(normalize(key), value == null ? "" : String.valueOf(value));
        return new PlaceholderContext(copy);
    }

    public Map<String, String> values() {
        return Collections.unmodifiableMap(this.values);
    }

    private static String normalize(String key) {
        if (key.startsWith("{") && key.endsWith("}")) {
            return key;
        }
        return "{" + key + "}";
    }

    public static final class Builder {
        private final LinkedHashMap<String, String> values = new LinkedHashMap<>();

        public Builder put(String key, Object value) {
            this.values.put(normalize(key), value == null ? "" : String.valueOf(value));
            return this;
        }

        public PlaceholderContext build() {
            return new PlaceholderContext(this.values);
        }
    }
}
