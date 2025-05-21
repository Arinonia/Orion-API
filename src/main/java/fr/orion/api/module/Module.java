package fr.orion.api.module;

public interface Module {
    void onEnable();
    void onDisable();
    void onReload();
    void onLoad();
    void onUnload();
    boolean isEnabled();
    ModuleDescriptor getModuleDescriptor();
}
