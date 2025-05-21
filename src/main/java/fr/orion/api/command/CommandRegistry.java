package fr.orion.api.command;

import java.util.Collection;

public interface CommandRegistry {
    void registerCommand(Command command);
    void unregisterCommand(Command command);
    Command getCommand(String name);
    Collection<Command> getCommands();
    void synchronizeCommands();
}
