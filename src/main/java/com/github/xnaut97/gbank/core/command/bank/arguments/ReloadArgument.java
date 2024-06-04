package com.github.xnaut97.gbank.core.command.bank.arguments;

import com.github.xnaut97.gbank.api.GBankPlugin;
import com.github.xnaut97.gbank.core.command.AbstractArgument;
import com.github.xnaut97.gbank.core.utils.MessageUtils;
import org.bukkit.command.CommandSender;

public class ReloadArgument extends AbstractArgument {

    public ReloadArgument(GBankPlugin plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public String getDescription() {
        return "Reload configuration.";
    }

    @Override
    public String getUsage() {
        return "bank reload";
    }

    @Override
    public void playerExecute(CommandSender sender, String[] args) {
        getBankManager().reload();
        MessageUtils.sendMessages(sender, "&aReloaded plugin & configuration!");
    }
}
