package com.github.xnaut97.gbank.core.data;

import com.github.xnaut97.gbank.api.data.BankBalance;
import eu.endercentral.crazy_advancements.JSONMessage;
import eu.endercentral.crazy_advancements.NameKey;
import eu.endercentral.crazy_advancements.advancement.Advancement;
import eu.endercentral.crazy_advancements.advancement.AdvancementDisplay;
import eu.endercentral.crazy_advancements.advancement.AdvancementFlag;
import eu.endercentral.crazy_advancements.advancement.AdvancementVisibility;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

public class BankHelper {

    public static void sendBalance(Player player, BankBalance balance) {
        JSONMessage title = new JSONMessage(new TextComponent("§b" + balance.getAmount() + balance.getCurrency().getPrefix()));
        JSONMessage description = new JSONMessage(new TextComponent("§b" + balance.getAmount() + balance.getCurrency().getPrefix()));
        AdvancementDisplay.AdvancementFrame frame = AdvancementDisplay.AdvancementFrame.GOAL;
        AdvancementVisibility visibility = AdvancementVisibility.ALWAYS;
        AdvancementDisplay display = new AdvancementDisplay(balance.getCurrency().getIcon(), title, description, frame, visibility);

        NameKey nameKey = new NameKey("gbank", player.getName());
        Advancement advancement = new Advancement(null, nameKey, display,
                AdvancementFlag.DISPLAY_MESSAGE, AdvancementFlag.SHOW_TOAST, AdvancementFlag.SEND_WITH_HIDDEN_BOOLEAN);
        advancement.displayToast(player);
    }

    public static boolean compare(BankBalance balance1, BankBalance balance2) {
        return balance1.getAmount() > balance2.getAmount();
    }

    public static double getTax(BankBalance balance, double amount) {
        double tax = balance.getCurrency().getTax()/100;
        return amount * tax;
    }

    public static void give(BankBalance balance, double amount) {
        handleAmount(balance, amount, 0);
    }

    public static void set(BankBalance balance, double amount) {
        handleAmount(balance, amount, 1);
    }

    public static void take(BankBalance balance, double amount) {
        handleAmount(balance, amount, 2);
    }

    private static void handleAmount(BankBalance balance, double amount, int operation) {
        switch (operation) {
            // Give
            case 0 -> {
                balance.setAmount(balance.getAmount() + amount);
            }
            // Set
            case 1 -> {
                balance.setAmount(amount);
            }
            // Take
            case 2 -> {
                balance.setAmount(balance.getAmount() - amount);
            }
        }
    }

    public static double parseAmount(String str) {
        try {
            return Double.parseDouble(str);
        }catch (Exception e) {
            return -1;
        }
    }

}
