# Orion Discord Bot API

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.java.net/)
[![JDA](https://img.shields.io/badge/JDA-5.5.1-blue.svg)](https://github.com/discord-jda/JDA)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![Version](https://img.shields.io/badge/Version-0.0.1--SNAPSHOT-red.svg)]()

A clean, modular Discord bot API framework built with Java and JDA. Orion-API provides the core interfaces and contracts for building scalable Discord bots with a plugin-like module system, comprehensive permission management, and hot-reloadable components.

> **Note**: This is the API layer providing interfaces and contracts. For a complete implementation, see [Orion-Core](https://github.com/Arinonia/orion-core).

## 🚀 Features

### 🔧 **Core API Framework**
- **Modular Architecture**: Clean interfaces for plugin-like module system
- **Command System**: Interfaces for slash commands with sub-command support
- **Event System**: Clean event handling contracts
- **Configuration Management**: Interfaces for YAML-based configuration
- **Permission System**: Comprehensive permission management interfaces

### 🛠️ **Developer Experience**
- **Clean Contracts**: Well-defined interfaces for all components
- **Flexible Implementation**: Multiple implementations possible
- **Configuration**: Flexible configuration system interfaces

## 📋 Table of Contents

- [Architecture](#-architecture)
- [Installation](#-installation)
- [Core Concepts](#-core-concepts)
- [Module Development](#-module-development)
- [API Reference](#-api-reference)
- [Examples](#-examples)
- [Implementations](#-implementations)
- [Contributing](#-contributing)
- [License](#-license)

## 🏗️ Architecture

Orion-API follows a clean architecture pattern with pure interface definitions:

```
fr.orion.api/              # Pure API interfaces
├── command/               # Command system interfaces
│   ├── Command.java
│   ├── CommandRegistry.java
│   └── ParentCommand.java
├── config/                # Configuration interfaces
│   ├── ModuleConfig.java
│   └── YamlModuleConfig.java
├── event/                 # Event system interfaces
│   └── EventRegistry.java
├── module/                # Module system interfaces
│   ├── Module.java
│   ├── AbstractModule.java
│   ├── ModuleManager.java
│   ├── ModuleDescriptor.java
│   └── loader/
├── permission/            # Permission system interfaces
│   ├── PermissionManager.java
│   └── PermissionNode.java
├── interfaction/          # UI interaction helpers
│   ├── EmbedTemplate.java
│   └── ConfirmationSystem.java
└── Bot.java               # Main bot interface
```

### Design Principles

- **Interface Segregation**: Small, focused interfaces
- **Dependency Inversion**: Depend on abstractions, not concretions
- **Open/Closed**: Open for extension, closed for modification
- **Single Responsibility**: Each interface has one clear purpose

## 📦 Installation

### Gradle

Add to your `build.gradle`:

```gradle
dependencies {
    implementation 'fr.orion:orion-api:0.1.0-SNAPSHOT'
}
```

### Maven

Add to your `pom.xml`:

```xml
<dependency>
   <groupId>fr.orion</groupId>
   <artifactId>orion-api</artifactId>
   <version>0.1.0-SNAPSHOT</version>
</dependency>
```

## 🧩 Core Concepts

### Bot Interface

The main entry point providing access to all subsystems:

```java
public interface Bot {
    CommandRegistry getCommandRegistry();
    EventRegistry getEventRegistry();
    JDA getJDA();
    ModuleManager getModuleManager();
    PermissionManager getPermissionManager();
}
```

### Module System

Modules are the building blocks of functionality:

```java
public interface Module {
    void onEnable();
    void onDisable();
    boolean isEnabled();
    ModuleConfig getConfig();
    boolean hasPermission(Member member, String permission);
}
```

### Permission System

Granular permission control with wildcards:

```java
public interface PermissionManager {
    boolean hasPermission(Member member, String permission);
    void addUserPermission(String userId, String permission);
    void addRolePermission(String roleId, String permission);
    Set<String> getEffectivePermissions(Member member);
}
```

## 📦 Module Development

### Creating a Module

Extend the provided `AbstractModule` class:

```java
public class MyModule extends AbstractModule {
    
    @Override
    public void onEnable() {
        // Register commands
        registerCommand(new MyCommand());
        
        // Register event listeners
        registerListener(new MyEventListener());
        
        getLogger().info("MyModule enabled!");
    }
    
    @Override
    public void onDisable() {
        getLogger().info("MyModule disabled!");
    }
}
```

### Module Descriptor

Create `module.yml` in your resources:

```yaml
id: "my_module"
name: "My Awesome Module"
version: "1.0.0"
main: "com.example.MyModule"
description: "A sample module for Orion"
author: "YourName"
dependencies: []
softDependencies: []
```

### Commands with Permissions

```java
public class MyCommand extends ParentCommand {
    
    public MyCommand(MyModule module) {
        registerSubcommand("test", "Test command", 
            null,
            new SubcommandHandler() {
                @Override
                public void execute(SlashCommandInteractionEvent event) {
                    // Check permission using the API
                    if (!module.hasPermission(event.getMember(), "test")) {
                        event.reply("❌ Insufficient permissions").setEphemeral(true).queue();
                        return;
                    }
                    
                    event.reply("✅ Test successful!").queue();
                }
                
                @Override
                public SubcommandData getSubcommandData() {
                    return new SubcommandData("test", "Test command");
                }
            }
        );
    }
    
    @Override
    public String getName() { return "mycommand"; }
    
    @Override
    public String getDescription() { return "My custom command"; }
}
```

### Event Listeners

```java
public class MyEventListener extends ListenerAdapter {
    private final MyModule module;
    
    public MyEventListener(MyModule module) {
        this.module = module;
    }
    
    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        // Use module's logger
        module.getLogger().info("New member joined: {}", event.getUser().getAsTag());
        
        // Access configuration
        String welcomeMessage = module.getConfig().getString("welcomeMessage");
        
        // Send welcome message
        TextChannel channel = event.getGuild().getTextChannelById("CHANNEL_ID");
        if (channel != null) {
            channel.sendMessage(welcomeMessage + " " + event.getMember().getAsMention()).queue();
        }
    }
}
```
## 🔒 Permission System

### Permission Structure

The API defines a flexible permission system following the pattern: `module.action`

- `my_module.test` - Specific action permission
- `my_module.*` - All permissions for the module  
- `*` - Global administrator access

### Permission Interfaces

```java
public interface PermissionManager {
    // Check permissions
    boolean hasPermission(Member member, String permission);
    boolean hasPermission(User user, String permission);
    boolean hasPermission(Role role, String permission);
    
    // Manage user permissions
    void addUserPermission(String userId, String permission);
    void removeUserPermission(String userId, String permission);
    Set<String> getUserPermissions(String userId);
    
    // Manage role permissions
    void addRolePermission(String roleId, String permission);
    void removeRolePermission(String roleId, String permission);
    Set<String> getRolePermissions(String roleId);
    
    // Utility methods
    Set<String> getEffectivePermissions(Member member);
    void clearUserPermissions(String userId);
    void clearRolePermissions(String roleId);
}
```

### Using Permissions in Modules

```java
// In your module - automatic prefix with module ID
if (!hasPermission(member, "action")) {
    // Permission denied - checks "my_module.action"
}

// Cross-module permission check - full permission string
if (getPermissionManager().hasPermission(member, "other_module.action")) {
    // Advanced features
}

// Check wildcard permissions
if (getPermissionManager().hasPermission(member, "my_module.*")) {
    // User has all module permissions
}
```

### Permission Node Utility

```java
public class PermissionNode {
    public PermissionNode(String permission);
    public boolean matches(String requiredPermission);
    public boolean isWildcard();
    public String getModule();
}

// Usage
PermissionNode node = new PermissionNode("my_module.*");
boolean matches = node.matches("my_module.test"); // true
```

## 📚 API Reference

### Core Interfaces

#### Bot Interface
```java
public interface Bot {
    CommandRegistry getCommandRegistry();
    EventRegistry getEventRegistry();
    JDA getJDA();
    ModuleManager getModuleManager();
    PermissionManager getPermissionManager();
}
```

#### Module Interface
```java
public interface Module {
    void onEnable();
    void onDisable();
    boolean isEnabled();
    ModuleConfig getConfig();
    boolean hasPermission(Member member, String permission);
}
```

#### PermissionManager Interface
```java
public interface PermissionManager {
    boolean hasPermission(Member member, String permission);
    void addUserPermission(String userId, String permission);
    void addRolePermission(String roleId, String permission);
    Set<String> getEffectivePermissions(Member member);
}
```

### Utility Classes

#### EmbedTemplate
```java
// Success message
EmbedTemplate.success("Title", "Description").build()

// Error message
EmbedTemplate.error("Title", "Description").build()

// Info message
EmbedTemplate.info("Title", "Description").build()

// Warning message
EmbedTemplate.warning("Title", "Description").build()
```

#### ConfirmationSystem
```java
ConfirmationSystem.ConfirmationMessage confirmation = ConfirmationSystem.createConfirmation(
    "Are you sure?",
    confirmEvent -> { /* Handle confirm */ },
    cancelEvent -> { /* Handle cancel */ }
);

event.replyEmbeds(confirmation.embed())
     .addActionRow(confirmation.confirmButton(), confirmation.cancelButton())
     .queue();
```

## 💡 Examples

### Example 1: Simple Command Module

```java
public class GreetingModule extends AbstractModule {
    
    @Override
    public void onEnable() {
        registerCommand(new GreetingCommand(this));
    }
    
    @Override
    public void onDisable() {
        // Cleanup if needed
    }
}

public class GreetingCommand extends ParentCommand {
    private final GreetingModule module;
    
    public GreetingCommand(GreetingModule module) {
        this.module = module;
        
        registerSubcommand("hello", "Say hello", null, new SubcommandHandler() {
            @Override
            public void execute(SlashCommandInteractionEvent event) {
                if (!module.hasPermission(event.getMember(), "greet")) {
                    event.reply("❌ No permission").setEphemeral(true).queue();
                    return;
                }
                
                event.reply("👋 Hello, " + event.getUser().getAsMention() + "!").queue();
            }
            
            @Override
            public SubcommandData getSubcommandData() {
                return new SubcommandData("hello", "Say hello");
            }
        });
    }
    
    @Override
    public String getName() { return "greeting"; }
    
    @Override
    public String getDescription() { return "Greeting commands"; }
}
```

### Example 2: Event Listener Module

```java
public class WelcomeModule extends AbstractModule {
    
    @Override
    public void onEnable() {
        registerListener(new WelcomeListener(this));
    }
    
    @Override
    public void onDisable() {
        // Auto cleanup
    }
}

public class WelcomeListener extends ListenerAdapter {
    private final WelcomeModule module;
    
    public WelcomeListener(WelcomeModule module) {
        this.module = module;
    }
    
    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        TextChannel welcomeChannel = event.getGuild().getTextChannelById("CHANNEL_ID");
        if (welcomeChannel != null) {
            welcomeChannel.sendMessage("Welcome " + event.getMember().getAsMention() + "!").queue();
        }
    }
}
```

### Example 3: Configuration Usage

```java
public class ConfigTestModule extends YamlModuleConfig {

    public ConfigTestModule(Path rootPath) {
        super(rootPath, "config");
    }
    
    @Override
    protected void ensureDefaultValues() {
        if (!this.contains("welcomeMessage")) {
            this.set("welcomeMessage", "");
        }
    }

    public String getWelcomeMessage() {
        return this.getString("welcomeMessage");
    }
}

public class TestModule extends AbstractModule {
    @Override
    protected ModuleConfig createConfig(Path dataDirectory) {
        return new ConfigTestModule(dataDirectory);
    }

    @Override
    public ConfigTestModule getConfig() {
        return (ConfigTestModule) super.getConfig();
    }
}
```

## 🏭 Implementations

Orion-API is a pure interface library. To use it, you need an implementation:

### Official Implementation

**[Orion-Core](https://github.com/yourusername/orion-core)** - The official implementation providing:

- Complete bot implementation with all API contracts
- YAML-based permission storage
- Module hot-reloading system
- Built-in management commands
- File-based configuration system

```gradle
dependencies {
    implementation 'fr.orion:orion-api:0.0.1-SNAPSHOT'
}
```

### Creating Your Own Implementation

You can implement the API contracts yourself:

```java
public class MyBot implements Bot {
    private final JDA jda;
    private final CommandRegistry commandRegistry;
    private final PermissionManager permissionManager;
    // ... other components
    
    @Override
    public CommandRegistry getCommandRegistry() {
        return commandRegistry;
    }
    
    @Override
    public PermissionManager getPermissionManager() {
        return permissionManager;
    }
    
    // ... implement other methods
}
```


## 🛠️ Usage with Implementation

### Custom Implementation

```java
public class Main {
    public static void main(String[] args) {
        OrionBot orionBot = new OrionBot("TOKEN", "GUILD_ID");
        orionBot.start();
    }
}

public class OrionBot implements Bot {
    private static final Logger log = LoggerFactory.getLogger(OrionBot.class);

    private final String token;
    private final String guildId;

    private JDA jda;
    private ModuleManager moduleManager;
    private CommandRegistry commandRegistry;
    private EventRegistry eventRegistry;
    private PermissionManager permissionManager;

    public OrionBot(String token, String guildId) {
        log.info("Initializing OrionBot... v0.0.1");
        this.token = token;
        this.guildId = guildId;
    }

    public void start() {
        log.info("Starting OrionBot...");

        initializeJDA();
        initializeRegistries();
        loadModules();
        registerCommands();
        logBotStatistics();

        log.info("OrionBot started successfully");
    }

    private void initializeJDA() {
        log.info("Initializing JDA...");

        try {
            JDABuilder builder = JDABuilder.createDefault(token)
                    .enableIntents(EnumSet.of(
                            GatewayIntent.GUILD_MESSAGES,
                            GatewayIntent.GUILD_MEMBERS,
                            GatewayIntent.MESSAGE_CONTENT,
                            GatewayIntent.GUILD_VOICE_STATES,
                            GatewayIntent.GUILD_MESSAGE_REACTIONS
                    ));
            this.jda = builder.build().awaitReady();
            log.info("JDA initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize JDA", e);
            throw new RuntimeException("Failed to initialize JDA", e);
        }
    }

    private void initializeRegistries() {
        log.info("Initializing registries...");

        this.eventRegistry = new SimpleEventRegistry(this.jda);
        this.commandRegistry = new SimpleCommandRegistry(this.jda, this.guildId);

        this.eventRegistry.registerListener((EventListener) this.commandRegistry);
        this.eventRegistry.registerListener(new ConfirmationSystem());

        this.permissionManager = new YamlPermissionManager(Path.of("permissions"));
        Path modulePath = Path.of("modules");
        this.moduleManager = new DefaultModuleLoader(modulePath, this);

    }

    private void loadModules() {
        log.info("Loading modules...");

        int loadedModules = this.moduleManager.loadModules();
        int enabledModules = this.moduleManager.enableModules();
        log.info("Loaded {} modules, enabled {} modules", loadedModules, enabledModules);
    }

    private void registerCommands() {
        log.info("Registering commands...");
        //add your built-in commands here
        //this.commandRegistry.registerCommand(new PermissionCommand(this.permissionManager));
        //this.commandRegistry.registerCommand(new ModulesCommand(this.moduleManager, this.permissionManager));
        this.commandRegistry.synchronizeCommands();
        log.info("Commands registered successfully");
    }

    private void logBotStatistics() {
        log.info("=== Orion Bot Statistics ===");
        log.info("Guild ID: {}", this.guildId != null ? this.guildId : "Not specified (using global commands)");
        log.info("Modules: {} ({} enabled)",
                this.moduleManager.getModules().size(),
                this.moduleManager.getEnabledModules().size());
        log.info("Commands: {} registered", this.commandRegistry.getCommands().size());
        log.info("Permissions: {} users, {} roles",
                this.permissionManager.getAllUsersWithPermissions().size(),
                this.permissionManager.getAllRolesWithPermissions().size());
        log.info("============================");

        for (Module module : this.moduleManager.getModules()) {
            String status = module.isEnabled() ? "ENABLED" : "DISABLED";
            if (module.getModuleDescriptor() != null) {
                log.debug("Module: {} [{}] - {}",
                        module.getModuleDescriptor().name(),
                        status,
                        module.getModuleDescriptor().description());
            } else {
                log.debug("Module: {} [{}] - No descriptor available",
                        module.getClass().getSimpleName(),
                        status);
            }
        }
    }

    private void shutdown() {
        log.info("Shutting down OrionBot...");

        if (this.moduleManager != null) {
            this.moduleManager.disableModules();
        }

        if (this.jda != null) {
            this.jda.shutdown();
            log.info("JDA shutdown complete");
        }
        log.info("OrionBot shutdown completed successfully");
    }

    @Override
    public CommandRegistry getCommandRegistry() {
        return this.commandRegistry;
    }

    @Override
    public EventRegistry getEventRegistry() {
        return this.eventRegistry;
    }

    @Override
    public JDA getJDA() {
        return this.jda;
    }

    @Override
    public ModuleManager getModuleManager() {
        return this.moduleManager;
    }

    @Override
    public PermissionManager getPermissionManager() {
        return this.permissionManager;
    }
}
```

## 🤝 Contributing

### Development Setup

1. **Fork the repository**
2. **Clone your fork**:
   ```bash
   git clone https://github.com/Arinonia/Orion-API.git
   ```
3. **Create a feature branch**:
   ```bash
   git checkout -b feature/new-interface
   ```
4. **Make changes and test**
5. **Commit your changes**:
   ```bash
   git commit -m "Add new interface for X"
   ```
6. **Push to your fork**:
   ```bash
   git push origin feature/new-interface
   ```
7. **Create a Pull Request**

### API Design Guidelines

When contributing to the API:

- **Keep interfaces focused**: Single responsibility principle
- **Avoid implementation details**: Pure contracts only
- **Use generics wisely**: Type safety without complexity
- **Consider extensibility**: Will future implementations need flexibility?
- **Backward compatibility**: Don't break existing contracts

### Code Style

- Use Java 17 features in interfaces where appropriate
- Follow standard Java naming conventions
- Use meaningful parameter and return type names
- Keep method signatures simple and intuitive

### Testing

```bash
# Compile API
./gradlew build

# Check JavaDoc generation
./gradlew javadoc

# Run any interface validation tests
./gradlew test
```

### API Evolution

When proposing API changes:

1. **Discuss first**: Open an issue to discuss major changes
2. **Deprecation path**: Provide clear migration for breaking changes
3. **Version appropriately**: Follow semantic versioning
4. **Document changes**: Update README and JavaDoc

## 📈 Roadmap

### API Enhancements

- [ ] **Event System**: More granular event interfaces
- [ ] **Configuration**: Advanced configuration validation interfaces
- [ ] **Metrics**: Built-in metrics and monitoring interfaces
- [ ] **Security**: Enhanced permission system interfaces

### Ecosystem

- [ ] **Plugin Marketplace**: Standard interfaces for plugin distribution
- [ ] **IDE Integration**: Better development tools and templates
- [ ] **Documentation**: Interactive API documentation
- [ ] **Testing Framework**: Standard testing utilities for modules

### Community

- [ ] **Examples Repository**: More comprehensive examples
- [ ] **Best Practices**: Guidelines for API usage
- [ ] **Migration Tools**: Tools for upgrading between API versions

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- [JDA](https://github.com/discord-jda/JDA) - Java Discord API that this builds upon
- [SnakeYAML](https://bitbucket.org/snakeyaml/snakeyaml) - Referenced for configuration interfaces
- [SLF4J](http://www.slf4j.org/) - Logging interfaces

## 📞 Support

- **API Documentation**: Check JavaDoc and this README
- **Issues**: [GitHub Issues](https://github.com/Arinonia/Orion-API/issues)
- **Discussions**: [GitHub Discussions](https://github.com/Arinonia/Orion-API/discussions)
- **Implementation Support**: See [Orion-Core](https://github.com/Arinonia/orion-core) for implementation-specific help TODO

## 🔗 Related Projects

- **[Orion-Core](https://github.com/Arinonia/orion-core)** - Official implementation TODO
- **[Orion-Examples](https://github.com/Arinonia/orion-examples)** - Example modules and usage TODO
- **[Orion-Templates](https://github.com/Arinonia/orion-templates)** - Project templates for quick start TODO

---

**Orion-API: Clean interfaces for Discord bot development** ✨