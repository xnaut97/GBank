package com.github.xnaut97.gbank.core.framework.command;

import com.google.common.collect.Maps;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public abstract class CommandArgument {

    private final Map<String, Boolean> childrens = Maps.newHashMap();

    public CommandArgument() {
    }

    public Map<String, Boolean> getChildPermissions() {
        return Collections.unmodifiableMap(this.childrens);
    }

    public void addChildPermission(String permission, boolean child) {
        this.childrens.put(permission, child);
    }

    public void removeChildPermission(String permission) {
        this.childrens.remove(permission);
    }

    /**
     * Get name of sub command
     *
     * @return Sub command name
     */
    public abstract String getName();

    /**
     * Get permission of sub command
     *
     * @return Sub command permission
     */
    public abstract String getPermission();

    /**
     * Get description of permission.
     *
     * @return Permission description.
     */
    public abstract String getPermissionDescription();

    /**
     * Get permission default of command.
     *
     * @return Permission default mode.
     */
    public abstract PermissionDefault getPermissionDefault();

    /**
     * Get description of sub command
     *
     * @return Sub command description
     */
    public abstract String getDescription();

    /**
     * Get usage of sub command
     *
     * @return Sub command usage
     */
    public abstract String getUsage();

    /**
     * Get list of aliases of sub command
     *
     * @return Sub command aliases
     */
    public abstract List<String> getAliases();

    public boolean requireFullArgs() {
        return false;
    }

    public boolean showAliases() {
        return true;
    }

    /**
     * Player execution
     */
    public abstract void playerExecute(CommandSender sender, String[] args);

}
