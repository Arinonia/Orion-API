package fr.orion.api.command;

import java.util.Collection;

public interface CommandRegistry {
    /**
     * Registers a command in the registry.
     *
     * @param command the command to register
     */
    void registerCommand(Command command);
    /**
     * Unregisters a command from the registry.
     *
     * @param command the command to unregister
     */
    void unregisterCommand(Command command);
    /**
     * Gets a command by its name.
     *
     * @param name the name of the command
     * @return the command, or null if not found
     */
    Command getCommand(String name);
    /**
     * Gets all registered commands.
     *
     * @return a collection of all commands
     */
    Collection<Command> getCommands();
    /**
     * Synchronizes the commands with the Discord API.
     * This should be called after all commands have been registered or updated.
     */
    void synchronizeCommands();
}
