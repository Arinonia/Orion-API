package fr.orion.api.module.loader;

import fr.orion.api.Bot;
import fr.orion.api.module.AbstractModule;
import fr.orion.api.module.Module;
import fr.orion.api.module.ModuleDescriptor;
import fr.orion.api.module.ModuleManager;
import fr.orion.api.module.loader.exception.ModuleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class DefaultModuleLoader implements ModuleManager {
    private static final Logger logger = LoggerFactory.getLogger(DefaultModuleLoader.class);

    private final Map<String, ModuleInfo> modulesById = new ConcurrentHashMap<>();
    private final Path modulesDirectory;
    private final Bot bot;

    public DefaultModuleLoader(Path modulesDirectory, Bot bot) {
        this.modulesDirectory = modulesDirectory;
        this.bot = bot;

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

        try {
            List<Path> moduleJars = Files.list(this.modulesDirectory)
                    .filter(path -> path.toString().toLowerCase().endsWith(".jar"))
                    .toList();

            Map<String, ModuleDescriptor> descriptors = new HashMap<>();
            Map<String, Path> jarPaths = new HashMap<>();

            for (Path jarPath : moduleJars) {
                try {
                    ModuleDescriptor descriptor = loadModuleDescriptor(jarPath);
                    if (descriptor != null) {
                        descriptors.put(descriptor.id(), descriptor);
                        jarPaths.put(descriptor.id(), jarPath);
                        logger.debug("Loaded module descriptor for module: {}", descriptor.id());
                        logger.debug("Module descriptor: {}", descriptor);
                    }
                } catch (ModuleException e) {
                    logger.error("Failed to load module descriptor from JAR: {}", jarPath.getFileName(), e);
                }
            }

            List<String> loadOrder = calculateLoadOrder(descriptors);

            for (String moduleId : loadOrder) {
                try {
                    Path jarPath = jarPaths.get(moduleId);
                    ModuleDescriptor descriptor = descriptors.get(moduleId);

                    loadModule(descriptor, jarPath);
                    loadedModules++;
                } catch (ModuleException e) {
                    logger.error("Failed to load module {}: {}", moduleId, e.getMessage());
                }
            }
        } catch (IOException e) {
            logger.error("Failed to list modules in directory: {}", this.modulesDirectory, e);
        } catch (ModuleException e) {
            logger.error("Failed to calculate load order for modules", e);
        }

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

    protected void loadModule(ModuleDescriptor descriptor, Path jarPath) throws ModuleException {
        logger.info("Loading module: {} v{}", descriptor.name(), descriptor.version());

        if (this.modulesById.containsKey(descriptor.id())) {
            throw new ModuleException("Module already loaded: " + descriptor.id());
        }

        URLClassLoader urlClassLoader = null;

        try {
            urlClassLoader = new URLClassLoader(
                    new URL[] { jarPath.toUri().toURL() },
                    getClass().getClassLoader()
            );

            Class<?> mainClass = urlClassLoader.loadClass(descriptor.main());
            if (!Module.class.isAssignableFrom(mainClass)) {
                throw new ModuleException("Main class does not implement Module interface: " + descriptor.main());
            }

            @SuppressWarnings("unchecked")
            Class<? extends Module> moduleClass = (Class<? extends Module>) mainClass;

            Module module = moduleClass.getDeclaredConstructor().newInstance();

            if (!(module instanceof AbstractModule)) {
                throw new ModuleException("Module must extend AbstractModule: " + descriptor.main());
            }

            AbstractModule abstractModule = (AbstractModule) module;
            abstractModule.init(this.bot, descriptor);

            ModuleInfo moduleInfo = new ModuleInfo(descriptor, jarPath, module, urlClassLoader);
            this.modulesById.put(descriptor.id(), moduleInfo);

            logger.info("Successfully loaded module: {}", descriptor.name());
        } catch (ReflectiveOperationException e) {
            closeClassLoader(urlClassLoader);
            throw new ModuleException("Failed to load module class: " + descriptor.main(), e);
        } catch (IOException e) {
            throw new ModuleException("Failed to create ClassLoader for module: " + descriptor.id(), e);
        }
    }

    private void closeClassLoader(URLClassLoader classLoader) {
        if (classLoader != null) {
            try {
                classLoader.close();
                logger.debug("ClassLoader closed successfully");
            } catch (IOException e) {
                logger.error("Failed to close ClassLoader", e);
            }
        }
    }

    @Override
    public int enableModules() {
        int enabledModules = 0;
        for (String moduleId : this.modulesById.keySet()) {
            if (enableModule(moduleId)) {
                enabledModules++;
            }
        }
        return enabledModules;
    }

    @Override
    public int disableModules() {
        int disabledModules = 0;
        List<String> moduleIds = new ArrayList<>(this.modulesById.keySet());
        Collections.reverse(moduleIds);

        for (String moduleId : moduleIds) {
            if (disableModule(moduleId)) {
                disabledModules++;
            }
        }
        return disabledModules;
    }

    @Override
    public boolean enableModule(String moduleId) {
        ModuleInfo info = this.modulesById.get(moduleId);

        if (info == null) {
            logger.warn("Cannot enable unknown module: {}", moduleId);
            return false;
        }

        if (info.module().isEnabled()) {
            logger.debug("Module {} is already enabled", moduleId);
            return false;
        }

        try {
            for (String dependencyId : info.descriptor().dependencies()) {
                ModuleInfo dependencyInfo = this.modulesById.get(dependencyId);

                if (dependencyInfo != null && !dependencyInfo.module().isEnabled()) {
                    logger.info("Enabling dependency {} for module {}", dependencyId, moduleId);
                    if (!enableModule(dependencyId)) {
                        throw new ModuleException("Failed to enable dependency: " + dependencyId);
                    }
                }
            }

            logger.info("Enabling module: {}", info.descriptor().name());

            AbstractModule abstractModule = (AbstractModule) info.module();
            abstractModule.enable();

            logger.info("Module {} enabled successfully", info.descriptor().name());
            return true;
        } catch (Exception e) {
            logger.error("Failed to enable module {}: {}", moduleId, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean disableModule(String moduleId) {
        ModuleInfo info = this.modulesById.get(moduleId);

        if (info == null) {
            logger.warn("Cannot disable unknown module: {}", moduleId);
            return false;
        }

        if (!info.module().isEnabled()) {
            logger.debug("Module {} is already disabled", moduleId);
            return false;
        }

        List<String> dependentModules = findDependentModules(moduleId);
        if (!dependentModules.isEmpty()) {
            logger.info("Module {} is required by: {}", moduleId, dependentModules);

            for (String dependentId : dependentModules) {
                if (!disableModule(dependentId)) {
                    logger.error("Failed to disable dependent module: {}", dependentId);
                    return false;
                }
            }
        }

        try {
            logger.info("Disabling module: {}", info.descriptor().name());

            AbstractModule abstractModule = (AbstractModule) info.module();
            abstractModule.disable();

            logger.info("Module {} disabled successfully", info.descriptor().name());
            return true;
        } catch (Exception e) {
            logger.error("Failed to disable module {}: {}", moduleId, e.getMessage(), e);
            return false;
        }
    }

    private List<String> findDependentModules(String moduleId) {
        List<String> dependents = new ArrayList<>();

        for (Map.Entry<String, ModuleInfo> entry : this.modulesById.entrySet()) {
            if (entry.getValue().descriptor().dependencies().contains(moduleId)) {
                dependents.add(entry.getKey());
            }
        }

        return dependents;
    }

    @Override
    public boolean reloadModule(String moduleId) {
        ModuleInfo info = this.modulesById.get(moduleId);

        if (info == null) {
            logger.warn("Cannot reload unknown module: {}", moduleId);
            return false;
        }

        boolean wasEnabled = info.module().isEnabled();

        if (wasEnabled && !disableModule(moduleId)) {
            logger.error("Failed to disable module {} during reload", moduleId);
            return false;
        }

        Path jarPath = info.jarPath();
        ModuleDescriptor descriptor = info.descriptor();

        if (!unloadModule(moduleId)) {
            logger.error("Failed to unload module {} during reload", moduleId);
            return false;
        }

        try {
            loadModule(descriptor, jarPath);

            if (wasEnabled) {
                return enableModule(moduleId);
            }

            return true;
        } catch (ModuleException e) {
            logger.error("Failed to reload module {}: {}", moduleId, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean unloadModule(String moduleId) {
        ModuleInfo info = this.modulesById.get(moduleId);

        if (info == null) {
            logger.warn("Cannot unload unknown module: {}", moduleId);
            return false;
        }

        if (info.module().isEnabled()) {
            if (!disableModule(moduleId)) {
                logger.error("Failed to disable module {} before unloading", moduleId);
                return false;
            }
        }

        List<String> dependentModules = findDependentModules(moduleId);
        if (!dependentModules.isEmpty()) {
            logger.error("Cannot unload module {} - required by: {}", moduleId, dependentModules);
            return false;
        }

        try {
            logger.info("Unloading module: {}", info.descriptor().name());

            AbstractModule abstractModule = (AbstractModule) info.module();
            abstractModule.onUnload();

            this.modulesById.remove(moduleId);

            closeClassLoader(info.classLoader());

            System.gc();

            logger.info("Module {} unloaded successfully", info.descriptor().name());
            return true;

        } catch (Exception e) {
            logger.error("Error during module unload: {}", moduleId, e);
            this.modulesById.put(moduleId, info);
            return false;
        }
    }

    @Override
    public void unloadAllModules() {
        logger.info("Unloading all modules...");

        disableModules();

        List<String> moduleIds = new ArrayList<>(this.modulesById.keySet());
        Collections.reverse(moduleIds);

        for (String moduleId : moduleIds) {
            unloadModule(moduleId);
        }

        logger.info("All modules unloaded");
    }

    @Override
    public Module getModule(String moduleId) {
        ModuleInfo info = this.modulesById.get(moduleId);
        return info != null ? info.module() : null;
    }

    @Override
    public Collection<Module> getModules() {
        return this.modulesById.values().stream()
                .map(ModuleInfo::module)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<Module> getEnabledModules() {
        return this.modulesById.values().stream()
                .map(ModuleInfo::module)
                .filter(Module::isEnabled)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isModuleLoaded(String moduleId) {
        return this.modulesById.containsKey(moduleId);
    }

    @Override
    public List<String> getModuleDependencies(String moduleId) {
        ModuleInfo info = this.modulesById.get(moduleId);
        return info != null ?
                new ArrayList<>(info.descriptor().dependencies()) :
                Collections.emptyList();
    }

    protected record ModuleInfo(ModuleDescriptor descriptor, Path jarPath, Module module, URLClassLoader classLoader) {}
}