package com.github.xnaut97.gbank.core.thread;

import com.github.xnaut97.gbank.api.GBankPlugin;
import com.github.xnaut97.gbank.api.data.BankAccount;
import com.github.xnaut97.gbank.api.data.BankBalance;
import com.github.xnaut97.gbank.api.data.BankCurrency;
import com.github.xnaut97.gbank.core.GBankPluginImpl;
import com.github.xnaut97.gbank.core.data.BankAccountImpl;
import com.github.xnaut97.gbank.core.data.BankHelper;
import com.github.xnaut97.gbank.core.utils.TimeUtils;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class BankOnlineReward extends BukkitRunnable {

    private final GBankPlugin plugin;

    private String interval;

    private long nextInterval = TimeUtils.newInstance().getNewTime();

    public BankOnlineReward(GBankPlugin plugin) {
        this.plugin = plugin;
        YamlDocument settings = ((GBankPluginImpl) plugin).getSettings();
        settings.getOptionalString("online-reward.interval").ifPresentOrElse(
                interval -> this.interval = interval,
                () -> this.interval = "30m");
        updateNextInterval();

        settings.getOptionalBoolean("online-reward").ifPresent(toggle -> {
            if (toggle) runTaskTimerAsynchronously(plugin, 20, 20);
        });
    }

    @Override
    public void run() {
        long current = TimeUtils.newInstance().getNewTime();
        if(current < nextInterval) return;

        ((GBankPluginImpl) plugin).getSettings().getOptionalSection("online-reward.currencies").ifPresent(section -> {
            section.getRoutesAsStrings(false).forEach(name -> {
                BankCurrency currency = plugin.getBankManager().getCurrency(name);
                if(currency == null) return;
                double amount = section.getDouble(name);
                Bukkit.getOnlinePlayers().forEach(player -> {
                    BankAccount account = plugin.getBankManager().getAccount(player);
                    if(account == null) return;
                    BankBalance balance = account.getBalance(name);
                    if(balance == null) return;
                    BankHelper.give(balance, amount);
                });
            });
        });

        updateNextInterval();
    }

    private void updateNextInterval() {
        this.nextInterval = TimeUtils.of(nextInterval).add(interval).getNewTime();
    }
}
