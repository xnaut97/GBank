package com.github.xnaut97.gbank.api;

import com.github.xnaut97.gbank.api.data.BankManager;
import org.bukkit.plugin.Plugin;

public interface GBankPlugin extends Plugin {

    BankManager getBankManager();

}
