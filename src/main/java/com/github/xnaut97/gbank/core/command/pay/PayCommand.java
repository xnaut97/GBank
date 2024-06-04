package com.github.xnaut97.gbank.core.command.pay;

import com.github.xnaut97.gbank.api.GBankPlugin;
import com.github.xnaut97.gbank.api.data.BankAccount;
import com.github.xnaut97.gbank.api.data.BankBalance;
import com.github.xnaut97.gbank.api.data.BankManager;
import com.github.xnaut97.gbank.core.data.BankHelper;
import com.github.xnaut97.gbank.core.framework.command.AbstractCommand;
import com.github.xnaut97.gbank.core.notification.NotificationManager;
import com.github.xnaut97.gbank.core.utils.MessageUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class PayCommand extends AbstractCommand {

    public PayCommand(GBankPlugin plugin) {
        super(plugin, "pay", "Pay to other player", "pay", Collections.emptyList());
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, String[] args) {
        if(sender instanceof Player player) {
            onPay(player, args);
        }
        return true;
    }

    private void onPay(Player player, String[] args) {
        BankAccount playerAccount = getBankManager().getAccount(player);
        if(playerAccount == null) return;
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
        BankBalance playerBalance = playerAccount.getBalance(currencyId);
        if (balance == null || playerBalance == null) {
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
        if(!BankHelper.compare(playerBalance, balance)) {
            MessageUtils.sendMessage(player, "not-enough-to-pay");
            return;
        }
        double toTake = amount + BankHelper.getTax(playerBalance, amount);
        BankHelper.give(balance, amount);
        BankHelper.take(playerBalance, toTake);
        MessageUtils.sendMessage(player, "pay-success.sender",
                "@amount@:" + toTake,
                "@currency-prefix@:" + balance.getCurrency().getPrefix(),
                "@player-name@:" + account.getPlayerName());
        if(account.getPlayer().isOnline()) {
            MessageUtils.sendMessage(account.getPlayer().getPlayer(),"pay-success.receiver.online",
                    "@amount@:" + amount,
                    "@currency-prefix@:" + balance.getCurrency().getPrefix(),
                    "@player-name@:" + account.getPlayerName());
        }else NotificationManager.addTransferData(account.getPlayer(), player, balance.getCurrency(), amount);
    }

    private BankManager getBankManager() {
        return ((GBankPlugin) getPlugin()).getBankManager();
    }
}
