package com.github.xnaut97.gbank.core.command;

import com.github.xnaut97.gbank.api.GBankPlugin;
import com.github.xnaut97.gbank.core.command.balance.BalanceCommand;
import com.github.xnaut97.gbank.core.command.bank.BankCommand;
import com.github.xnaut97.gbank.core.command.pay.PayCommand;
import com.github.xnaut97.gbank.core.framework.command.AbstractCommand;
import com.google.common.collect.Lists;

import java.util.List;

public class CommandManager {

    private final GBankPlugin plugin;

    private final List<AbstractCommand> commands = Lists.newArrayList();

    public CommandManager(GBankPlugin plugin) {
        this.plugin = plugin;
        this.commands.add(new BalanceCommand(plugin));
        this.commands.add(new BankCommand(plugin));
        this.commands.add(new PayCommand(plugin));
        register();
    }

    public void register() {
        this.commands.forEach(AbstractCommand::register);
    }

    public void unregister() {
        this.commands.forEach(AbstractCommand::unregister);
    }
}
