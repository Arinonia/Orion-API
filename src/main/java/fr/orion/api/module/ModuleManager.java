package fr.orion.api.module;

import java.util.Collection;
import java.util.List;

public interface ModuleManager {
    int loadModules();
    int enableModules();
    int disableModules();

    boolean enableModule(String moduleId);
    boolean disableModule(String moduleId);
    boolean reloadModule(String moduleId);
    boolean unloadModule(String moduleId);

    void unloadAllModules();

    Module getModule(String moduleId);

    Collection<Module> getModules();
    Collection<Module> getEnabledModules();

    boolean isModuleLoaded(String moduleId);

    List<String> getModuleDependencies(String moduleId);
}
