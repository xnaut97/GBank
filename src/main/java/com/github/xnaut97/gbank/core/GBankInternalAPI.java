package com.github.xnaut97.gbank.core;

import com.github.xnaut97.gbank.api.GBankAPI;
import com.github.xnaut97.gbank.api.GBankPlugin;
import com.github.xnaut97.gbank.api.data.BankAccount;
import com.github.xnaut97.gbank.api.data.BankBalance;
import com.github.xnaut97.gbank.api.data.BankCurrency;
import com.github.xnaut97.gbank.api.data.BankManager;
import com.github.xnaut97.gbank.core.data.BankHelper;
import com.github.xnaut97.gbank.core.data.BankManagerImpl;
import org.bukkit.OfflinePlayer;

import java.util.Optional;

public class GBankInternalAPI implements GBankAPI {

    private final GBankPlugin plugin;


    public GBankInternalAPI(GBankPlugin plugin) {
        this.plugin = plugin;
    }

    public BankManager getBankManager() {
        return plugin.getBankManager();
    }

    @Override
    public Optional<BankBalance> getBalance(OfflinePlayer player, String currency) {
        return getBalance0(player, currency);
    }

    @Override
    public void give(OfflinePlayer player, String currency, double amount) {
        getBalance0(player, currency).ifPresent(balance -> BankHelper.give(balance, amount));
        save(player);
    }

    @Override
    public void set(OfflinePlayer player, String currency, double amount) {
        getBalance0(player, currency).ifPresent(balance -> BankHelper.set(balance, amount));
        save(player);
    }

    @Override
    public void take(OfflinePlayer player, String currency, double amount) {
        getBalance0(player, currency).ifPresent(balance -> BankHelper.take(balance, amount));
        save(player);
    }

    private void save(OfflinePlayer player) {
        BankAccount account = getBankManager().getAccount(player);
        if(account != null)
            getBankManager().saveAccount(account);
    }

    private Optional<BankBalance> getBalance0(OfflinePlayer player, String currency) {
        BankAccount account = getBankManager().getAccount(player);
        return account == null ? Optional.empty() : Optional.ofNullable(account.getBalance(currency));
    }

}
