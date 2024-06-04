package com.github.xnaut97.gbank.core.command.balance;

import com.github.xnaut97.gbank.api.GBankPlugin;
import com.github.xnaut97.gbank.api.data.BankAccount;
import com.github.xnaut97.gbank.api.data.BankBalance;
import com.github.xnaut97.gbank.api.data.BankManager;
import com.github.xnaut97.gbank.core.data.BankHelper;
import com.github.xnaut97.gbank.core.framework.command.AbstractCommand;
import com.github.xnaut97.gbank.core.menu.BalanceMenu;
import com.github.xnaut97.gbank.core.utils.MessageUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class BalanceCommand extends AbstractCommand {

    public BalanceCommand(GBankPlugin plugin) {
        super(plugin, "balance",
                "Bank plugin related commands.",
                "/balance",
                Arrays.asList("bal", "blc"));

        setRegisterHelpCommand(false);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, String[] args) {
        if(sender instanceof Player player) {
            onExecute(player, args);
        }
        return true;
    }

    private void onExecute(Player player, String[] args) {
        if(args.length == 0) {
            new BalanceMenu().open(player);
            return;
        }
        String currencyType = null;
        if(args.length == 1)
            currencyType = args[0];

        BankAccount account = getBankManager().getAccount(player);
        if(args.length == 2) {
            if(!player.hasPermission("bank.command.view.other")) {
                MessageUtils.sendMessage(player, "no-command-permission");
                return;
            }
            account = getBankManager().getAccount(args[1]);
            if(account == null) {
                MessageUtils.sendMessage(player, "player-not-found", "@player-name@:" + args[1]);
                return;
            }
        }
        BankBalance balance = account.getBalance(currencyType);
        if(balance == null) {
            MessageUtils.sendMessage(player, "currency-not-found", "@currency-id@:" + currencyType);
            return;
        }
        BankHelper.sendBalance(player, balance);
    }

    private BankManager getBankManager() {
        return ((GBankPlugin) getPlugin()).getBankManager();
    }
}
