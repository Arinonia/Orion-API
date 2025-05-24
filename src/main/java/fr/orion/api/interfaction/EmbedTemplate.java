package fr.orion.api.interfaction;

import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;
import java.time.Instant;

public class EmbedTemplate {
    public static final Color SUCCESS_COLOR = new Color(67, 181, 129); // Green Discord
    public static final Color ERROR_COLOR = new Color(240, 71, 71);    // Red Discord
    public static final Color INFO_COLOR = new Color(88, 101, 242);    // Blue Discord
    public static final Color WARNING_COLOR = new Color(255, 177, 66); // Orange Discord

    public static EmbedBuilder success(String title, String description) {
        return new EmbedBuilder()
                .setTitle("✅ " + title)
                .setDescription(description)
                .setColor(SUCCESS_COLOR)
                .setTimestamp(Instant.now());
    }

    public static EmbedBuilder error(String title, String description) {
        return new EmbedBuilder()
                .setTitle("❌ " + title)
                .setDescription(description)
                .setColor(ERROR_COLOR)
                .setTimestamp(Instant.now());
    }

    public static EmbedBuilder info(String title, String description) {
        return new EmbedBuilder()
                .setTitle("ℹ️ " + title)
                .setDescription(description)
                .setColor(INFO_COLOR)
                .setTimestamp(Instant.now());
    }

    public static EmbedBuilder warning(String title, String description) {
        return new EmbedBuilder()
                .setTitle("⚠️ " + title)
                .setDescription(description)
                .setColor(WARNING_COLOR)
                .setTimestamp(Instant.now());
    }

    public static EmbedBuilder withFooter(EmbedBuilder builder, String text) {
        return builder.setFooter(text, null);
    }
}
