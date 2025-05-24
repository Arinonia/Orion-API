package fr.orion.api.module;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Represents the metadata descriptor for a module.
 * <p>
 *
 * <h2>Example YAML structure:</h2>
 * <pre>{@code
 * id: "my_module"
 * name: "My Awesome Module"
 * version: "1.0.0"
 * main: "com.example.mymodule.ModuleMain"
 * dependencies:
 *   - "required_module"
 * softDependencies: "optional_module"
 * custom_field: "extra_data"
 * }
 * </pre>
 *
 * @param id                 The unique module identifier (lowercase, no spaces, required)
 * @param name               Human-readable module name
 * @param version            Module version (default: "1.0.0")
 * @param main               Fully qualified main class name (required)
 * @param description        Brief module description
 * @param author             Module author(s)
 * @param website            Project website URL
 * @param license            Distribution license
 * @param dependencies       List of hard required module IDs
 * @param softDependencies   List of optional module IDs
 * @param rawData            Additional unprocessed configuration data
 *
 * @throws IllegalArgumentException if {@code id} or {@code main} are invalid
 *
 * @see AbstractModule
 * @author Arinonia
 */
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
