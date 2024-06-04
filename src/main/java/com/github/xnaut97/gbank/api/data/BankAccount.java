package com.github.xnaut97.gbank.api.data;

import org.bukkit.OfflinePlayer;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public interface BankAccount {

    UUID getPlayerUniqueId();

    String getPlayerName();

    OfflinePlayer getPlayer();

    void setBalance(BankCurrency currency, double value);

    @Nullable
    BankBalance getBalance(String id);

    List<BankBalance> getAllBalance();

}
