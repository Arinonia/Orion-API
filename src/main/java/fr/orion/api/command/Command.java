package fr.orion.api.command;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public interface Command {
    /**
     * Get the name of the command.
     * @return The command name
     */
    String getName();
    /**
     * Get the description of the command.
     * @return The command description
     */
    String getDescription();
    /**
     * Build the SlashCommandData for this command.
     * @return The SlashCommandData instance
     */
    SlashCommandData buildCommandData();
    /**
     * Execute the command when a SlashCommandInteractionEvent is received.
     * @param event The SlashCommandInteractionEvent to handle
     */
    void execute(SlashCommandInteractionEvent event);
}
