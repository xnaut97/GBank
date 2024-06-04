package com.github.xnaut97.gbank.core.data;

import com.github.xnaut97.gbank.api.data.BankCurrency;
import org.bukkit.inventory.ItemStack;

public record BankCurrencyImpl(String id, String name, String prefix, ItemStack icon, double tax) implements BankCurrency {

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public ItemStack getIcon() {
        return icon;
    }

    @Override
    public double getTax() {
        return tax;
    }
}