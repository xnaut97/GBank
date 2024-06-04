package com.github.xnaut97.gbank.api;

import com.github.xnaut97.gbank.api.data.BankAccount;
import com.github.xnaut97.gbank.api.data.BankBalance;
import org.bukkit.OfflinePlayer;

import java.util.Optional;
import java.util.UUID;

public interface GBankAPI {

    /**
     * Get player balance
     */
    Optional<BankBalance> getBalance(OfflinePlayer player, String currency);

    void give(OfflinePlayer player, String currency, double amount);

    void set(OfflinePlayer player, String currency, double amount);

    void take(OfflinePlayer player, String currency, double amount);

}
