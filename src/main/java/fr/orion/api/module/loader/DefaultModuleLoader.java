package fr.orion.api.module.loader;

import fr.orion.api.module.Module;
import fr.orion.api.module.ModuleDescriptor;
import fr.orion.api.module.ModuleManager;
import fr.orion.api.module.loader.exception.ModuleException;
import net.dv8tion.jda.api.JDA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class DefaultModuleLoader implements ModuleManager {
    private static final Logger logger = LoggerFactory.getLogger(DefaultModuleLoader.class);

    private final Map<String, ModuleInfo> modulesById = new ConcurrentHashMap<>();
    private final Path modulesDirectory;
    private final JDA jda;

    public DefaultModuleLoader(Path modulesDirectory, JDA jda) {
        this.modulesDirectory = modulesDirectory;
        this.jda = jda;

        try {
            Files.createDirectories(modulesDirectory);
        } catch (Exception e) {
            logger.error("Failed to create modules directory", e);
        }
    }

    @Override
    public int loadModules() {
        logger.info("Loading modules from directory: {}", this.modulesDirectory);
        int loadedModules = 0;
        return loadedModules;
    }

    protected ModuleDescriptor loadModuleDescriptor(Path jarPath) throws ModuleException {
        try (JarFile jarFile = new JarFile(jarPath.toFile())) {
            JarEntry moduleYmlEntry = jarFile.getJarEntry("module.yml");
            if (moduleYmlEntry == null) {
                return null;
            }

            try (InputStream is = jarFile.getInputStream(moduleYmlEntry)) {
                Yaml yaml = new Yaml();
                Map<String, Object> data = yaml.load(is);
                return ModuleDescriptor.fromMap(data);
            }
        } catch (IOException e) {
            throw new ModuleException("Failed to read JAR file: " + jarPath.getFileName(), e);
        }
    }

    protected List<String> calculateLoadOrder(Map<String, ModuleDescriptor> descriptors) throws ModuleException {
        Map<String, Set<String>> graph = new HashMap<>();

        for (ModuleDescriptor descriptor : descriptors.values()) {
            String moduleId = descriptor.id();
            Set<String> dependencies = new HashSet<>();

            for (String dependency : descriptor.dependencies()) {
                if (descriptors.containsKey(dependency)) {
                    dependencies.add(dependency);
                } else {
                    logger.warn("Module {} has an unknown dependency: {}", moduleId, dependency);
                }
            }

            graph.put(moduleId, dependencies);
        }

        List<String> result = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Set<String> temp = new HashSet<>();

        for (String node : graph.keySet()) {
            if (!visited.contains(node)) {
                List<String> cycle = new ArrayList<>();
                if (!topologicalSort(node, graph, visited, temp, result, cycle)) {
                    throw new ModuleException("Circular dependency detected: " + String.join(" -> ", cycle));
                }
            }
        }
        Collections.reverse(result);
        return result;
    }

    protected boolean topologicalSort(String node, Map<String, Set<String>> graph, Set<String> visited,
                                      Set<String> temp, List<String> result, List<String> cycle) {

        if (temp.contains(node)) {
            cycle.add(node);
            return false;
        }

        if (visited.contains(node)) {
            return true;
        }

        temp.add(node);

        Set<String> dependencies = graph.getOrDefault(node, Collections.emptySet());
        for (String dependency : dependencies) {
            if (!topologicalSort(dependency, graph, visited, temp, result, cycle)) {
                cycle.add(0, node);
                return false;
            }
        }
        temp.remove(node);
        visited.add(node);
        result.add(node);
        return true;
    }

    @Override
    public int enableModules() {
        return 0;
    }

    @Override
    public int disableModules() {
        return 0;
    }

    @Override
    public boolean enableModule(String moduleId) {
        return false;
    }

    @Override
    public boolean disableModule(String moduleId) {
        return false;
    }

    @Override
    public boolean reloadModule(String moduleId) {
        return false;
    }

    @Override
    public Module getModule(String moduleId) {
        return null;
    }

    @Override
    public Collection<Module> getModules() {
        return List.of();
    }

    @Override
    public Collection<Module> getEnabledModules() {
        return List.of();
    }

    @Override
    public boolean isModuleLoaded(String moduleId) {
        return false;
    }

    @Override
    public List<String> getModuleDependencies(String moduleId) {
        return List.of();
    }


    protected record ModuleInfo(ModuleDescriptor descriptor, Path jarPath, Module module, URLClassLoader classLoader) {}
}
