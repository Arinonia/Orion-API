package fr.orion.api;

import fr.orion.api.command.CommandRegistry;
import fr.orion.api.event.EventRegistry;
import fr.orion.api.module.ModuleManager;
import net.dv8tion.jda.api.JDA;

public interface Bot {
    /**
     * Get command registry. (implementation part, not the API)
     * @return The command registry instance
     */
    CommandRegistry getCommandRegistry();
    /**
     * Get event registry. (implementation part, not the API)
     * @return The event registry instance
     */
    EventRegistry getEventRegistry();
    /**
     * Get the JDA instance.
     * @return The JDA instance
     */
    JDA getJDA();
    /**
     * Get the module manager.
     * @return The module manager instance
     */
    ModuleManager getModuleManager();
}
