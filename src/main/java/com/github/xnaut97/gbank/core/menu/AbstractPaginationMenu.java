package com.github.xnaut97.gbank.core.menu;

import com.github.xnaut97.gbank.api.GBankPlugin;
import com.github.xnaut97.gbank.api.data.BankManager;
import com.github.xnaut97.gbank.core.GBankPluginImpl;
import com.github.xnaut97.gbank.core.framework.menu.type.PaginationMenu;
import dev.dejvokep.boostedyaml.YamlDocument;

public abstract class AbstractPaginationMenu<T> extends PaginationMenu<T> {

    public AbstractPaginationMenu(int page, int row, String title) {
        super(page, row, title);
    }

    public BankManager getBankManager() {
        return ((GBankPlugin) getPlugin()).getBankManager();
    }

    public YamlDocument getLanguage () {
        return ((GBankPluginImpl) getPlugin()).getLanguage();
    }

    public YamlDocument getConfig() {
        return ((GBankPluginImpl) getPlugin()).getSettings();
    }
    
}
