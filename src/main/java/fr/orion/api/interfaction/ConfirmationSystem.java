package fr.orion.api.interfaction;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ConfirmationSystem extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(ConfirmationSystem.class);
    private static final Map<String, ConfirmationData> confirmationSystems = new ConcurrentHashMap<>();
    private static ConfirmationSystem instance;
    private static final String confirmPrefix = "confirm:";
    private static final String cancelPrefix = "cancel:";


    public ConfirmationSystem() {
        instance = this;
    }

    public static ConfirmationMessage createConfirmation(String message,
                                                         Consumer<ButtonInteractionEvent> onConfirm,
                                                         Consumer<ButtonInteractionEvent> onCancel) {
        if (instance == null) {
            throw new IllegalStateException("ConfirmationSystem not initialized. Make sure it's registered as an event listener.");
        }
        String confirmationId = UUID.randomUUID().toString();

        confirmationSystems.put(confirmationId, new ConfirmationData(onConfirm, onCancel));
        scheduleCleaning(confirmationId, 5, TimeUnit.MINUTES);

        MessageEmbed embed = EmbedTemplate.warning("Confirmation required", message).build();

        Button confirmButton = Button.success(confirmPrefix + confirmationId, "Confirm");
        Button cancelButton = Button.danger(cancelPrefix + confirmationId, "Cancel");

        return new ConfirmationMessage(confirmationId, embed, confirmButton, cancelButton);
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String componentId = event.getComponentId();

        boolean isConfirmButton = componentId.startsWith(confirmPrefix);
        boolean isCancelButton = componentId.startsWith(cancelPrefix);

        if (!isConfirmButton && !isCancelButton) {
            return;
        }

        String confirmationId = null;
        String action = null;

        if (isConfirmButton) {
            confirmationId = componentId.substring(confirmPrefix.length());
            action = "confirm";
        } else if (isCancelButton) {
            confirmationId = componentId.substring(cancelPrefix.length());
            action = "cancel";
        }

        if (confirmationId == null) {
            return;
        }

        ConfirmationData data = confirmationSystems.get(confirmationId);
        if (data == null) {
            logger.debug("Trying to access inactive confirmation: {}", confirmationId);
            event.reply("This confirmation is not active anymore.").setEphemeral(true).queue();
            return;
        }

        confirmationSystems.remove(confirmationId);

        if ("confirm".equals(action)) {
            data.onConfirm().accept(event);
        } else if ("cancel".equals(action)) {
            data.onCancel().accept(event);
        }
    }

    private static void scheduleCleaning(String confirmationId, long delay, TimeUnit unit) {
        new Thread(() -> {
            try {
                unit.sleep(delay);
                confirmationSystems.remove(confirmationId);
                logger.debug("Cleaning confirmation: " + confirmationId);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private record ConfirmationData(Consumer<ButtonInteractionEvent> onConfirm,
                                    Consumer<ButtonInteractionEvent> onCancel) {
    }

    public record ConfirmationMessage(String confirmationId, MessageEmbed embed, Button confirmButton,
                                      Button cancelButton) {
    }
}
