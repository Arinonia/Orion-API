package fr.orion.api.module;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public record ModuleDescriptor(
        String id,
        String name,
        String version,
        String main,
        String description,
        String author,
        String website,
        String license,
        List<String> dependencies,
        List<String> softDependencies,
        Map<String, Object> rawData
) {
    public ModuleDescriptor {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Module ID cannot be null or empty");
        }
        if (main == null || main.isBlank()) {
            throw new IllegalArgumentException("Main class cannot be null or empty");
        }

        dependencies = dependencies == null ? Collections.emptyList() : Collections.unmodifiableList(dependencies);
        softDependencies = softDependencies == null ? Collections.emptyList() : Collections.unmodifiableList(softDependencies);
        rawData = rawData == null ? Collections.emptyMap() : Collections.unmodifiableMap(rawData);
    }

    public static ModuleDescriptor fromMap(Map<String, Object> map) {
        return new ModuleDescriptor(
                getString(map, "id", ""),
                getString(map, "name", ""),
                getString(map, "version", "1.0.0"),
                getString(map, "main", ""),
                getString(map, "description", ""),
                getString(map, "author", "Unknown"),
                getString(map, "website", ""),
                getString(map, "license", ""),
                getStringList(map, "dependencies"),
                getStringList(map, "softDependencies"),
                map
        );
    }

    private static List<String> getStringList(Map<String, Object> map, String key) {
        if (map.containsKey(key)) {
            Object value = map.get(key);
            if (value instanceof List) {
                return (List<String>) value;
            } else if (value instanceof String) {
                return List.of((String) value);
            }
        }
        return Collections.emptyList();
    }

    private static String getString(Map<String, Object> map, String key, String defaultValue) {
        Object value = map.get(key);
        return value == null ? defaultValue : value.toString();
    }
}
