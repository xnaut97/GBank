package com.github.xnaut97.gbank.core.notification;

import com.github.xnaut97.gbank.api.data.BankAccount;
import com.github.xnaut97.gbank.api.data.BankCurrency;
import com.google.common.collect.Maps;
import lombok.Getter;
import org.bukkit.OfflinePlayer;

import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;

@Getter
public class NotificationManager {

    private static final Map<UUID, BankTransfer> transferData = Maps.newHashMap();

    public static BankTransfer getTransferData(OfflinePlayer receiver) {
        return transferData.getOrDefault(receiver.getUniqueId(), null);
    }

    public static void addTransferData(OfflinePlayer receiver, OfflinePlayer sender, BankCurrency currency, double amount) {
        getOrCreate(receiver).update(sender, currency, amount);
    }

    private static BankTransfer getOrCreate(OfflinePlayer receiver) {
        BankTransfer transfer = getTransferData(receiver);
        if(transfer == null) {
            transfer = new BankTransfer();
            transferData.put(receiver.getUniqueId(), transfer);
        }
        return transfer;
    }

}
