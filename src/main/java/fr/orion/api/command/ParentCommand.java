package fr.orion.api.command;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public abstract class ParentCommand implements Command {

    private final Map<String, SubcommandInfo> subcommands = new HashMap<>();

    protected void registerSubcommand(String name, String description,
                                      Consumer<SubcommandData> optionsConfigurator,
                                      SubcommandHandler handler) {
        SubcommandData data = new SubcommandData(name, description);

        if (optionsConfigurator != null) {
            optionsConfigurator.accept(data);
        }

        this.subcommands.put(name, new SubcommandInfo(data, handler));
    }

    protected void registerSubcommand(String name, String description, SubcommandHandler handler) {
        registerSubcommand(name, description, null, handler);
    }

    @Override
    public SlashCommandData buildCommandData() {
        SlashCommandData commandData = Commands.slash(getName(), getDescription());

        this.subcommands.forEach((name, info) -> {
            commandData.addSubcommands(info.subcommandData);
        });

        return commandData;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        String subcommandName = event.getSubcommandName();

        if (subcommandName == null) {
            event.reply("This command requires a subcommand.").setEphemeral(true).queue();
            return;
        }

        SubcommandInfo info = this.subcommands.get(subcommandName);
        if (info == null) {
            event.reply("Unknown subcommand: " + subcommandName).setEphemeral(true).queue();
            return;
        }

        try {
            info.handler.execute(event);
        } catch (Exception e) {
            event.reply("An error occurred while executing this command: " + e.getMessage())
                    .setEphemeral(true).queue();
        }
    }

    public interface SubcommandHandler {
        void execute(SlashCommandInteractionEvent event);
        SubcommandData getSubcommandData();
    }

    private record SubcommandInfo(SubcommandData subcommandData, SubcommandHandler handler) {}
}