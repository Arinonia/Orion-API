package fr.orion.api.module;

import fr.orion.api.Bot;
import fr.orion.api.config.ModuleConfig;
import fr.orion.api.permission.PermissionManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;

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

    /**
     * Get the permission manager for this module.
     * @return The permission manager instance
     */
    PermissionManager getPermissionManager();

    /**
     * Check if a member has a specific permission for this module.
     * This is a convenience method that prefixes the permission with the module ID.
     *
     * @param member The member to check
     * @param permission The permission to check (without module prefix)
     * @return true if the member has the permission
     */
    default boolean hasPermission(Member member, String permission) {
        if (getModuleDescriptor() == null) {
            return false;
        }
        String fullPermission = getModuleDescriptor().id() + "." + permission;
        return getPermissionManager().hasPermission(member, fullPermission);
    }
}
