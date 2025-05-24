package fr.orion.api.module;

import fr.orion.api.Bot;
import fr.orion.api.config.ModuleConfig;
import net.dv8tion.jda.api.JDA;

import java.nio.file.Path;

public interface Module {
    /**
     * Call when the module is enabled.
     */
    void onEnable();
    /**
     * Call when the module is disabled.
     */
    void onDisable();

    /**
     * Get JDA bot instance
     * @return The JDA instance
     */
    JDA getJDA();

    /**
     * Get Bot instance
     * @return The Bot instance
     */
    Bot getBot();

    /**
     * Get the module descriptor
     * @return The descriptor containing the metadata
     */
    ModuleDescriptor getModuleDescriptor();

    /**
     *  Check if the module is enabled
     * @return true if the module is enabled, false otherwise
     */
    boolean isEnabled();

    /**
     * Get the folder path with module data.
     * @return The path to the module data folder
     */
    Path getPath();

    /**
     * Get the logger for this module. (not sure about using SLF4J directly, consider using a custom logger interface)
     * @return The logger instance
     */
    org.slf4j.Logger getLogger();

    /**
     * Get the module configuration.
     * @return The module configuration
     */
    ModuleConfig getConfig();
}
