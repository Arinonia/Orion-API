package fr.orion.api.module;

import net.dv8tion.jda.api.JDA;

public interface Module {
    void onEnable();
    void onDisable();
    void onReload();
    void onLoad(JDA jda, ModuleDescriptor moduleDescriptor);
    void onUnload();
    boolean isEnabled();
    ModuleDescriptor getModuleDescriptor();
}
