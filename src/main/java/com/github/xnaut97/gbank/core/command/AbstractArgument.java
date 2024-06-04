package com.github.xnaut97.gbank.core.command;

import com.github.xnaut97.gbank.api.GBankPlugin;
import com.github.xnaut97.gbank.api.data.BankManager;
import com.github.xnaut97.gbank.core.GBankPluginImpl;
import com.github.xnaut97.gbank.core.framework.command.CommandArgument;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;

public abstract class AbstractArgument extends CommandArgument {

    private final GBankPlugin plugin;

    public AbstractArgument(GBankPlugin plugin) {
        this.plugin = plugin;
    }

    public BankManager getBankManager() {
        return this.plugin.getBankManager();
    }

    public YamlDocument getLanguage () {
        return ((GBankPluginImpl) this.plugin).getLanguage();
    }

    public YamlDocument getConfig() {
        return ((GBankPluginImpl) this.plugin).getSettings();
    }

    public YamlDocument getCommands() {
        return ((GBankPluginImpl) this.plugin).getCommand();
    }

    @Override
    public String getPermission() {
        return getCommands().getString(getName(), "");
    }

    @Override
    public String getPermissionDescription() {
        return "";
    }

    @Override
    public PermissionDefault getPermissionDefault() {
        return PermissionDefault.OP;
    }

    @Override
    public List<String> getAliases() {
        return List.of();
    }

}
