package com.github.xnaut97.gbank.core.menu;

import com.github.xnaut97.gbank.api.data.BankAccount;
import com.github.xnaut97.gbank.api.data.BankBalance;
import com.github.xnaut97.gbank.api.data.BankCurrency;
import com.github.xnaut97.gbank.core.data.BankHelper;
import com.github.xnaut97.gbank.core.framework.menu.MenuElement;
import com.github.xnaut97.gbank.core.utils.ItemCreator;
import com.google.common.collect.Lists;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;

public class BalanceMenu extends AbstractPaginationMenu<BankCurrency> {

    public BalanceMenu() {
        super(0, 6, "&lYOUR BALANCE");
    }

    @Override
    public List<BankCurrency> getObjects() {
        return getBankManager().getCurrencies();
    }

    @Override
    public MenuElement getObjectItem(int index, BankCurrency currency) {
        return new MenuElement(new ItemCreator(currency.getIcon())
                .setDisplayName(currency.getName())
                .setLore(Lists.newArrayList("&7Click to view balance."))
                .build()) {
            @Override
            public void onClick(InventoryClickEvent event) {
                event.setCancelled(true);
                Player player = (Player) event.getWhoClicked();
                BankAccount account = getBankManager().getAccount(player);
                if(account == null) return;
                BankBalance balance = account.getBalance(currency.getId());
                if(balance == null) return;
                close(player);
                BankHelper.sendBalance(player, balance);
            }
        };
    }
}
