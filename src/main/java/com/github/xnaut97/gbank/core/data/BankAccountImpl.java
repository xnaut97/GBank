package com.github.xnaut97.gbank.core.data;

import com.github.xnaut97.gbank.api.data.BankAccount;
import com.github.xnaut97.gbank.api.data.BankBalance;
import com.github.xnaut97.gbank.api.data.BankCurrency;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BankAccountImpl implements BankAccount {

    private final OfflinePlayer player;

    private final Map<String, BankBalance> balances = Maps.newHashMap();

    public BankAccountImpl(OfflinePlayer player) {
        this.player = player;
    }

    @Override
    public UUID getPlayerUniqueId() {
        return getPlayer().getUniqueId();
    }

    @Override
    public String getPlayerName() {
        return getPlayer().getName();
    }

    @Override
    public OfflinePlayer getPlayer() {
        return this.player;
    }

    @Override
    public void setBalance(BankCurrency currency, double value) {
        this.balances.put(currency.getId(), new BankBalanceImpl(currency, value));
    }

    @Nullable
    @Override
    public BankBalance getBalance(String id) {
        return this.balances.getOrDefault(id, null);
    }

    @Override
    public List<BankBalance> getAllBalance() {
        return Lists.newArrayList(this.balances.values());
    }

}
