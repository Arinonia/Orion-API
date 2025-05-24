package fr.orion.api.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

public interface ModuleConfig {
    Logger log = LoggerFactory.getLogger(ModuleConfig.class);

    /**
     * Gets a value from the configuration.
     *
     * @param key the key to get
     * @return the value, or null if not found
     */
    Object get(String key);

    /**
     * Gets a value from the configuration, with a default value if not found.
     *
     * @param key the key to get
     * @param defaultValue the default value
     * @return the value, or the default value if not found
     */
    Object get(String key, Object defaultValue);

    /**
     * Sets a value in the configuration.
     *
     * @param key the key to set
     * @param value the value to set
     */
    void set(String key, Object value);

    /**
     * Saves the configuration to disk.
     */
    void save();

    /**
     * Reloads the configuration from disk.
     */
    void reload();

    /**
     * Checks if the configuration contains a key.
     *
     * @param key the key to check
     * @return true if the key exists, false otherwise
     */
    boolean contains(String key);

    /**
     * Removes a key from the configuration.
     *
     * @param key the key to remove
     */
    void remove(String key);

    /**
     * Gets the keys in the configuration.
     *
     * @return the keys
     */
    Set<String> getKeys();

    /**
     * Gets all configuration entries.
     *
     * @return the entries
     */
    Map<String, Object> getAll();

    /**
     * Get String value from the configuration.
     */
    default String getString(String key) {
        Object value = get(key);
        return value != null ? value.toString() : null;
    }

    /**
     * Get int value from the configuration.
     */
    default int getInt(String key) {
        Object value = get(key);
        if (value == null) return 0;

        if (value instanceof Number) {
            return ((Number) value).intValue();
        } else {
            try {
                return Integer.parseInt(value.toString());
            } catch (NumberFormatException e) {
                //throw new IllegalStateException("Value for key '" + key + "' cannot be converted to int: " + value);
                log.error("Value for key '" + key + "' cannot be converted to int: " + value, e);
                return 0;
            }
        }
    }

    /**
     * Get long value from the configuration.
     */
    default long getLong(String key) {
        Object value = get(key);
        if (value == null) return 0;

        if (value instanceof Number) {
            return ((Number) value).longValue();
        } else {
            try {
                return Long.parseLong(value.toString());
            } catch (NumberFormatException e) {
                //throw new IllegalStateException("Value for key '" + key + "' cannot be converted to long: " + value);
                log.error("Value for key '" + key + "' cannot be converted to int: " + value, e);
                return 0;
            }
        }
    }

    /**
     * Get double value from the configuration.
     */
    default double getDouble(String key) {
        Object value = get(key);
        if (value == null) return 0;

        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        } else {
            try {
                return Double.parseDouble(value.toString());
            } catch (NumberFormatException e) {
                //throw new IllegalStateException("Value for key '" + key + "' cannot be converted to double: " + value);
                log.error("Value for key '" + key + "' cannot be converted to int: " + value, e);
                return 0.0D;
            }
        }
    }

    /**
     * Get boolean value from the configuration.
     */
    default boolean getBoolean(String key) {
        Object value = get(key);
        if (value == null) return false;

        if (value instanceof Boolean) {
            return (Boolean) value;
        } else {
            return Boolean.parseBoolean(value.toString());
        }
    }
}
