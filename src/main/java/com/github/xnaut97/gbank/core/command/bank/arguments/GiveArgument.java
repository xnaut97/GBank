package com.github.xnaut97.gbank.core.command.bank.arguments;

import com.github.xnaut97.gbank.api.GBankPlugin;
import com.github.xnaut97.gbank.api.data.BankAccount;
import com.github.xnaut97.gbank.api.data.BankBalance;
import com.github.xnaut97.gbank.api.data.BankCurrency;
import com.github.xnaut97.gbank.core.command.AbstractArgument;
import com.github.xnaut97.gbank.core.data.BankHelper;
import com.github.xnaut97.gbank.core.framework.command.AutoCompletion;
import com.github.xnaut97.gbank.core.utils.MessageUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class GiveArgument extends AbstractArgument implements AutoCompletion {

    public GiveArgument(GBankPlugin plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "give";
    }

    @Override
    public String getDescription() {
        return "Give currency amount to specific player.";
    }

    @Override
    public String getUsage() {
        return "bank give [player] [currency] [amount]";
    }

    @Override
    public void playerExecute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        if (args.length == 0) {
            MessageUtils.sendMessage(player, "empty-player");
            return;
        }
        String name = args[0];
        BankAccount account = getBankManager().getAccount(name);
        if (account == null) {
            MessageUtils.sendMessage(player, "player-not-found", "@player-name@:" + name);
            return;
        }
        if (args.length == 1) {
            MessageUtils.sendMessage(player, "empty-currency");
            return;
        }
        String currencyId = args[1];
        BankBalance balance = account.getBalance(currencyId);
        if (balance == null) {
            MessageUtils.sendMessage(player, "currency-not-found", "@currency-id@:" + currencyId);
            return;
        }
        if (args.length == 2) {
            MessageUtils.sendMessage(player, "empty-amount");
            return;
        }
        double amount = BankHelper.parseAmount(args[2]);
        if (amount == -1) {
            MessageUtils.sendMessage(player, "invalid-amount");
            return;
        }
        if (amount < 1) {
            MessageUtils.sendMessage(player, "negative-amount");
            return;
        }
        BankHelper.give(balance, amount);
        MessageUtils.sendMessage(player, "give-success",
                "@amount@:" + amount,
                "@currency-prefix@:" + balance.getCurrency().getPrefix(),
                "@player-name@:" + account.getPlayerName());

        getBankManager().saveAccount(account);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if(args.length == 1)
            return getBankManager().getAccounts().stream()
                    .map(BankAccount::getPlayerName)
                    .filter(name -> name.contains(args[0]))
                    .toList();
        if(args.length == 2)
            return getBankManager().getCurrencies().stream()
                    .map(BankCurrency::getId)
                    .filter(id -> id.contains(args[1]))
                    .toList();
        if(args.length == 3)
            return Collections.singletonList("amount");
        return null;
    }
}
