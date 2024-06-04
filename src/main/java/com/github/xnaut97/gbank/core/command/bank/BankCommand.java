package com.github.xnaut97.gbank.core.command.bank;

import com.github.xnaut97.gbank.api.GBankPlugin;
import com.github.xnaut97.gbank.core.command.bank.arguments.GiveArgument;
import com.github.xnaut97.gbank.core.command.bank.arguments.ReloadArgument;
import com.github.xnaut97.gbank.core.command.bank.arguments.SetArgument;
import com.github.xnaut97.gbank.core.command.bank.arguments.TakeArgument;
import com.github.xnaut97.gbank.core.framework.command.AbstractCommand;
import com.google.common.collect.Lists;

public class BankCommand extends AbstractCommand {

    public BankCommand(GBankPlugin plugin) {
        super(plugin, "bank", "Bank related commands", "/bank", Lists.newArrayList());

        addArguments(
                new GiveArgument(plugin),
                new SetArgument(plugin),
                new TakeArgument(plugin),
                new ReloadArgument(plugin)
        );
    }

}
