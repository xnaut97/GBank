package com.github.xnaut97.gbank.core.listener;

import com.github.xnaut97.gbank.api.GBankPlugin;
import com.github.xnaut97.gbank.api.data.BankManager;
import com.github.xnaut97.gbank.core.GBankPluginImpl;
import dev.dejvokep.boostedyaml.YamlDocument;
import lombok.Getter;
import org.bukkit.event.Listener;

@Getter
public abstract class AbstractListener implements Listener {

    private final GBankPlugin plugin;

    public AbstractListener(GBankPlugin plugin) {
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
}
