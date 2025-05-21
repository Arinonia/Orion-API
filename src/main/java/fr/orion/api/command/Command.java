package fr.orion.api.command;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public interface Command {
    String getName();
    String getDescription();
    SlashCommandData buildCommandData();
    void execute(SlashCommandInteractionEvent event);
}
