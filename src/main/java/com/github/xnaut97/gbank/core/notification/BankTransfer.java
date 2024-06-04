package com.github.xnaut97.gbank.core.notification;

import com.github.xnaut97.gbank.api.data.BankAccount;
import com.github.xnaut97.gbank.api.data.BankBalance;
import com.github.xnaut97.gbank.api.data.BankCurrency;
import com.github.xnaut97.gbank.core.data.BankBalanceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Getter;
import org.bukkit.OfflinePlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
public class BankTransfer {

    private final Map<UUID, List<BankBalance>> data = Maps.newHashMap();

    public BankTransfer() {}

    public void update(OfflinePlayer sender, BankCurrency currency, double value) {
        List<BankBalance> list = data.getOrDefault(sender.getUniqueId(), Lists.newArrayList());
        list.stream().filter(balance -> balance.getCurrency().getId().equals(currency.getId())).findAny()
                .ifPresentOrElse(balance -> balance.setAmount(balance.getAmount() + value),
                        () -> {
                            BankBalance balance = new BankBalanceImpl(currency);
                            balance.setAmount(value);
                            list.add(balance);
                        });
    }
}
