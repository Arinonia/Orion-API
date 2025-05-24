package fr.orion.api.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class YamlModuleConfig implements ModuleConfig {
    private static final Logger log = LoggerFactory.getLogger(YamlModuleConfig.class);

    private final Path configFile;
    private final Yaml yaml;
    private Map<String, Object> config;

    public YamlModuleConfig(Path rootPath, String fileName) {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);

        this.yaml = new Yaml(options);
        this.configFile = rootPath.resolve(fileName + ".yml");
        this.config = new HashMap<>();

        load();
    }

    private void load() {
        if (!Files.exists(configFile)) {
            createDefaultConfig();
            return;
        }

        try (InputStream is = Files.newInputStream(configFile)) {
            Map<String, Object> config = yaml.load(is);
            if (config != null) {
                this.config = config;
                ensureDefaultValues();
                save();
            } else {
                createDefaultConfig();
            }
        } catch (Exception e) {
            log.error("Failed to load config file", e);
            createDefaultConfig();
        }
    }

    private void createDefaultConfig() {
        this.config = new HashMap<>();
        ensureDefaultValues();
        save();
    }

    protected void ensureDefaultValues() {}

    @Override
    public Object get(String key) {
        return this.config.get(key);
    }

    @Override
    public Object get(String key, Object defaultValue) {
        return this.config.getOrDefault(key, defaultValue);
    }

    @Override
    public void set(String key, Object value) {
        this.config.put(key, value);
    }

    @Override
    public void save() {
        try {
            Files.createDirectories(this.configFile.getParent());
            try (Writer writer = Files.newBufferedWriter(this.configFile)) {
                this.yaml.dump(this.config, writer);
            } catch (Exception e) {
                log.error("Failed to save config file", e);
            }
        } catch (Exception e) {
            log.error("Failed to create directories for config file", e);
        }
    }

    @Override
    public void reload() {
        load();
    }

    @Override
    public boolean contains(String key) {
        return this.config.containsKey(key);
    }

    @Override
    public void remove(String key) {
        this.config.remove(key);
    }

    @Override
    public Set<String> getKeys() {
        return Collections.unmodifiableSet(this.config.keySet());
    }

    @Override
    public Map<String, Object> getAll() {
        return Collections.unmodifiableMap(this.config);
    }
}
