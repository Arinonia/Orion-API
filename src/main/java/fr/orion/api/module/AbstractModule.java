package fr.orion.api.module;

import fr.orion.api.Bot;
import fr.orion.api.command.Command;
import fr.orion.api.config.ModuleConfig;
import fr.orion.api.config.YamlModuleConfig;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.hooks.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractModule implements Module {
    private Bot bot;
    private ModuleDescriptor descriptor;
    private boolean enabled = false;
    private Path dataFolder;
    private Logger logger;
    private ModuleConfig config;
    private final List<Command> registeredCommands = new ArrayList<>();
    private final List<EventListener> registeredListeners = new ArrayList<>();

    public AbstractModule() {
        // empty, initialization is done in init()
    }

    public final void init(Bot bot, ModuleDescriptor descriptor) {
        this.bot = bot;
        this.descriptor = descriptor;
        this.logger = LoggerFactory.getLogger(descriptor.name());
        Path modulesDataDir = Paths.get("modules-data");
        this.dataFolder = modulesDataDir.resolve(descriptor.id());

        try {
            Files.createDirectories(this.dataFolder);
        } catch (IOException e) {
            logger.error("Failed to create data folder for module {}", descriptor.id(), e);
        }
        this.config = createConfig(this.dataFolder);
        onLoad();
    }

    protected void onLoad() {}

    public void onUnload() {}

    public final void enable() {
        if (this.enabled) {
            logger.warn("Module {} is already enabled", this.descriptor.id());
            return;
        }

        this.enabled = true;

        try {
            onEnable();
        } catch (Exception e) {
            logger.error("Error enabling module {}", this.descriptor.id(), e);
            this.enabled = false;
            throw new RuntimeException("Failed to enable module " + this.descriptor.id(), e);
        }
    }

    public final void disable() {
        if (!this.enabled) {
            logger.warn("Module {} is not enabled", this.descriptor.id());
            return;
        }

        try {
            onDisable();
        } catch (Exception e) {
            logger.error("Error disabling module {}", this.descriptor.id(), e);
        }

        unregisterAllCommands();
        unregisterAllListeners();
    }

    protected ModuleConfig createConfig(Path dataDirectory) {
        return new YamlModuleConfig(dataDirectory, "config");
    }

    @Override
    public final JDA getJDA() {
        return this.bot.getJDA();
    }

    @Override
    public final Bot getBot() {
        return this.bot;
    }

    @Override
    public final ModuleDescriptor getModuleDescriptor() {
        return this.descriptor;
    }

    @Override
    public final boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public final Path getPath() {
        return this.dataFolder;
    }

    @Override
    public final Logger getLogger() {
        return this.logger;
    }

    @Override
    public ModuleConfig getConfig() {
        return this.config;
    }

    // ====== Utility methods ======

    /**
     * Get the module ID.
     * @return The module ID
     */
    public final String getId() {
        return this.descriptor.id();
    }

    /**
     * Get the module name.
     * @return The module name
     */
    public final String getName() {
        return this.descriptor.name();
    }

    /**
     * Get the module version.
     * @return The module version
     */
    public final String getVersion() {
        return this.descriptor.version();
    }

    /**
     * Get the module description.
     * @return The module description
     */
    public final String getDescription() {
        return this.descriptor.description();
    }

    /**
     * Get the module author.
     * @return The module author
     */
    public final String getAuthor() {
        return this.descriptor.author();
    }

    /**
     * Register a command for this module.
     * @param command The command to register
     */
    protected final void registerCommand(Command command) {
        if (command == null) {
            throw new IllegalArgumentException("Command cannot be null");
        }

        this.registeredCommands.add(command);
        this.bot.getCommandRegistry().registerCommand(command);
        this.logger.debug("Registered command: {}", command.getName());
    }

    /**
     * Register an event listener for this module.
     * @param listener The event listener to register
     */
    protected final void registerListener(EventListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener cannot be null");
        }

        this.registeredListeners.add(listener);
        this.bot.getJDA().addEventListener(listener);
        this.logger.debug("Registered listener: {}", listener.getClass().getSimpleName());
    }

    /**
     * Unregister all commands registered by this module.
     */
    private void unregisterAllCommands() {
        for (Command command : this.registeredCommands) {
            this.bot.getCommandRegistry().unregisterCommand(command);
            this.logger.debug("Unregistered command: {}", command.getName());
        }
        this.registeredCommands.clear();
    }

    /**
     *  Unregister all event listeners registered by this module.
     */
    private void unregisterAllListeners() {
        for (EventListener listener : this.registeredListeners) {
            this.bot.getJDA().removeEventListener(listener);
            logger.debug("Unregistered listener: {}", listener.getClass().getSimpleName());
        }
        this.registeredListeners.clear();
    }
}
