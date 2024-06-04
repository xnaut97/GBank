package com.github.xnaut97.gbank.api.data;

import org.bukkit.OfflinePlayer;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public interface BankManager {

    /**
     * Get all registered currencies.
     */
    List<BankCurrency> getCurrencies();

    /**
     * Get currency by prefix.
     */
    @Nullable
    BankCurrency getCurrency(String id);

    /**
     * Get all bank accounts.
     */
    List<BankAccount> getAccounts();

    /**
     * Get bank account of player.
     */
    BankAccount getAccount(OfflinePlayer player);

    /**
     * Get bank account by player unique id.
     */
    @Nullable
    BankAccount getAccount(UUID uuid);

    /**
     * Get bank account by player name.
     */
    @Nullable
    BankAccount getAccount(String playerName);

    /**
     * Register bank account to system.
     */
    void addAccount(BankAccount account);

    /**
     * Delete player account.
     */
    void deleteAccount(OfflinePlayer player);

    /**
     * Delete player account by unique id.
     */
    void deleteAccount(UUID uuid);

    /**
     * Delete player account by name.
     */
    void deleteAccount(String playerName);

    /**
     * Save player bank account.
     */
    void saveAccount(BankAccount account);

    /**
     * Load all player account
     */
    void loadAccounts();

    void reload();

}
