package com.github.xnaut97.gbank.core.data;

import com.github.xnaut97.gbank.api.data.BankBalance;
import com.github.xnaut97.gbank.api.data.BankCurrency;
import com.github.xnaut97.gbank.core.GBankPluginImpl;
import org.bukkit.plugin.java.JavaPlugin;

public class BankBalanceImpl implements BankBalance {

    private final BankCurrency currency;

    private double amount;

    public BankBalanceImpl(BankCurrency currency) {
        this(currency, 0);
    }

    public BankBalanceImpl(BankCurrency currency, double amount) {
        this.currency = currency;
        this.amount = amount;
    }

    @Override
    public BankCurrency getCurrency() {
        return this.currency;
    }

    @Override
    public double getAmount() {
        return amount;
    }

    @Override
    public void setAmount(double amount) {
        this.amount = amount;
    }
}
