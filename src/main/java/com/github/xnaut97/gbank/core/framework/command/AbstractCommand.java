package com.github.xnaut97.gbank.core.framework.command;

import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author TezVN
 */
@Getter
@Accessors(chain = true)
public abstract class AbstractCommand extends BukkitCommand {

    private final Plugin plugin;

    private final UUID uniqueId = UUID.randomUUID();

    private final Map<String, CommandArgument> arguments = Maps.newHashMap();

    @Setter
    private String noPermissionsMessage = "&cYou don't have permission to access.";

    @Setter
    private String noSubCommandFoundMessage = "&cCommand not found, please use /" + getName() + " help for more.";

    @Setter
    private String noConsoleAllowMessage = "&cThis command is for console only.";

    @Setter
    private String helpHeader;

    @Setter
    private String helpFooter;

    @Setter
    private String helpCommandColor = "&a";

    @Setter
    private String helpDescriptionColor = "&7";

    @Setter
    private int helpSuggestions = 5;

    @Setter
    private boolean registerHelpCommand;

    public AbstractCommand(Plugin plugin, String name, String description, String usageMessage, List<String> aliases) {
        super(name.toLowerCase(), description, usageMessage,
                aliases.stream()
                        .map(String::toLowerCase)
                        .collect(Collectors.toList()));
        this.plugin = plugin;
        this.helpHeader = "- - - - - - - - - -=[ " + plugin.getName() + " ]=- - - - - - - - - -";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < plugin.getName().length(); i++) {
            sb.append(" ");
        }
        this.helpFooter = "- - - - - - - - - -=[ " + sb + " ]=- - - - - - - - - -";
    }

    public void onSingleExecute(CommandSender sender) {

    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, String[] args) {
        if (!(sender instanceof Player player)) {
            if (args.length == 0)
                onSingleExecute(sender);
            else {
                String name = args[0];
                CommandArgument command = this.arguments.entrySet().stream()
                        .filter(entry -> entry.getKey().equalsIgnoreCase(name))
                        .map(Map.Entry::getValue).findAny().orElse(null);
                if (command == null) {
                    sender.sendMessage(this.noSubCommandFoundMessage);
                    return true;
                }
                if (!(command instanceof ConsoleExecutor console)) {
                    sender.sendMessage(this.noConsoleAllowMessage);
                    return true;
                }
                console.consoleExecute(sender, Arrays.copyOfRange(args, 1, args.length));
            }
            return true;
        }

        if (args.length == 0)
            onSingleExecute(sender);
        else {
            String name = args[0];
            CommandArgument argument = this.arguments.entrySet().stream()
                    .filter(entry -> entry.getKey().equalsIgnoreCase(name))
                    .map(Map.Entry::getValue).findAny().orElse(null);
            if (argument == null) {
                sender.sendMessage(this.noSubCommandFoundMessage);
                return true;
            }
            String permission = argument.getPermission();
            if (permission == null) {
                argument.playerExecute(sender, args);
                return true;
            }
            if (!player.hasPermission(argument.getPermission())) {
                sender.sendMessage(this.noPermissionsMessage);
                return true;
            }
            argument.playerExecute(sender, argument.requireFullArgs() ? args : Arrays.copyOfRange(args, 1, args.length));
        }
        return true;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, String[] args)
            throws IllegalArgumentException {
        return new CommandCompleter(this).onTabComplete(sender, args);
    }

    /**
     * Add sub command to your main command
     *
     * @param commands Sub command to add.
     */
    public AbstractCommand addArguments(CommandArgument... commands) {
        for (CommandArgument command : commands) {
            if (command.getName() == null || command.getName().isEmpty())
                continue;
            this.arguments.putIfAbsent(command.getName(), command);
            if (command.getAliases() != null && !command.getAliases().isEmpty()) {
                for (String alias : command.getAliases()) {
                    this.arguments.putIfAbsent(alias, command);
                }
            }
            if (command.getPermission() == null)
                continue;
            registerPermission(command);
        }
        return this;
    }

    private void registerPermission(CommandArgument command) {
        Permission permission = new Permission(command.getPermission(), command.getPermissionDescription(),
                command.getPermissionDefault(), command.getChildPermissions());
        getPermissionMap().put(permission.getName().toLowerCase(), permission);
        calculatePermission(permission);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Permission> getPermissionMap() {
        try {
            SimplePluginManager pluginManager = (SimplePluginManager) Bukkit.getPluginManager();
            Field field = SimplePluginManager.class.getDeclaredField("permissions");
            field.setAccessible(true);
            return (Map<String, Permission>) field.get(pluginManager);
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    private void calculatePermission(Permission permission) {
        try {
            SimplePluginManager pluginManager = (SimplePluginManager) Bukkit.getPluginManager();
            Method method = SimplePluginManager.class.getDeclaredMethod(
                    "calculatePermissionDefault", Permission.class, boolean.class);
            method.setAccessible(true);
            method.invoke(pluginManager, permission, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Register command to server in {@code onEnable()} method
     */
    public AbstractCommand register() {
        try {
            if (registerHelpCommand)
                addArguments(new AbstractHelpCommand(this));
            if (!getKnownCommands().containsKey(getName())) {
                getKnownCommands().put(getName(), this);
                getKnownCommands().put(plugin.getDescription().getName().toLowerCase() + ":" + getName(), this);
            }
            for (String alias : getAliases()) {
                if (getKnownCommands().containsKey(alias))
                    continue;
                getKnownCommands().put(alias, this);
                getKnownCommands().put(plugin.getDescription().getName().toLowerCase() + ":" + alias, this);
            }
            register(getCommandMap());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    /**
     * Unregister command from server in {@code onDisable()} method
     */
    public AbstractCommand unregister() {
        try {
            unregister(getCommandMap());
            getKnownCommands().entrySet().removeIf(entry -> {
                if (!(entry.getValue() instanceof AbstractCommand))
                    return false;
                AbstractCommand command = (AbstractCommand) entry.getValue();
                command.getArguments().forEach((name, commandArgument) -> {
                    String permission = commandArgument.getPermission();
                    if (permission == null || !permission.isEmpty())
                        return;
                    getPermissionMap().remove(permission);
                });
                return command.getUniqueId().equals(this.getUniqueId());
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    private CommandMap getCommandMap() throws Exception {
        Field field = Bukkit.getServer().getClass().getDeclaredField("commandMap");
        field.setAccessible(true);
        return (CommandMap) field.get(Bukkit.getServer());
    }

    @SuppressWarnings("unchecked")
    private Map<String, Command> getKnownCommands() throws Exception {
        Field cmField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
        cmField.setAccessible(true);
        CommandMap cm = (CommandMap) cmField.get(Bukkit.getServer());
        cmField.setAccessible(false);
        Map<String, Command> knownCommands;
        try {
            knownCommands = (Map<String, Command>) cm.getClass().getDeclaredMethod("getKnownCommands").invoke(cm);
        } catch (Exception e) {
            Field field = SimpleCommandMap.class.getDeclaredField("knownCommands");
            field.setAccessible(true);
            knownCommands = (Map<String, Command>) field.get(cm);
        }
        return knownCommands;
    }


    public String getHelpCommandColor() {
        return helpCommandColor == null ? "&a" : this.helpCommandColor;
    }

    public String getHelpDescriptionColor() {
        return helpDescriptionColor == null ? "&7" : this.helpDescriptionColor;
    }

    /**
     * Get list of registered sub commands
     *
     * @return List of sub commands
     */
    public Map<String, CommandArgument> getArguments() {
        return Collections.unmodifiableMap(this.arguments);
    }

}
