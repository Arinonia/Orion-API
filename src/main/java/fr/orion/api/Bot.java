package fr.orion.api;

import fr.orion.api.command.CommandRegistry;
import fr.orion.api.event.EventRegistry;
import fr.orion.api.module.ModuleManager;
import net.dv8tion.jda.api.JDA;

public interface Bot {
    CommandRegistry getCommandRegistry();
    EventRegistry getEventRegistry();
    JDA getJDA();
    ModuleManager getModuleManager();
}
