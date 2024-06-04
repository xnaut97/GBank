package com.github.xnaut97.gbank.core.listener;

import com.cryptomorin.xseries.XSound;
import com.github.xnaut97.gbank.api.GBankPlugin;
import com.github.xnaut97.gbank.api.data.BankAccount;
import com.github.xnaut97.gbank.api.data.BankCurrency;
import com.github.xnaut97.gbank.core.data.BankAccountImpl;
import com.github.xnaut97.gbank.core.notification.BankTransfer;
import com.github.xnaut97.gbank.core.notification.NotificationManager;
import com.github.xnaut97.gbank.core.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener extends AbstractListener{

    public PlayerJoinListener(GBankPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        BankAccount account = getBankManager().getAccount(player);
        if(account == null) {
            account = new BankAccountImpl(player);
            for (BankCurrency currency : getBankManager().getCurrencies()) {
                account.setBalance(currency, 0);
            }
            getBankManager().addAccount(account);
            getPlugin().getLogger().warning("Register new account for player '" + player.getName() + "'");
            return;
        }
        BankTransfer transfer = NotificationManager.getTransferData(player);
        if(transfer != null) {
            XSound.ENTITY_EXPERIENCE_ORB_PICKUP.play(player);
            transfer.getData().forEach((uuid, list) -> {
                OfflinePlayer sender = Bukkit.getOfflinePlayer(uuid);
                list.forEach(balance -> {
                    MessageUtils.sendMessage(player, "pay-success.receiver.re-login",
                            "@amount@:" + balance.getAmount(),
                            "@currency-prefix@:" + balance.getCurrency().getPrefix(),
                            "@player-name@:" + sender.getName());
                });
            });
        }
    }
}
