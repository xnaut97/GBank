package com.github.xnaut97.gbank.api.data;

import org.bukkit.inventory.ItemStack;

public interface BankCurrency {

    String getId();

    String getName();

    String getPrefix();

    ItemStack getIcon();

    double getTax();

}
