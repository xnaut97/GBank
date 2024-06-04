package com.github.xnaut97.gbank.core;

import com.github.xnaut97.gbank.api.GBankAPI;
import lombok.Getter;

public class GBankAPIProvider {

    private static GBankAPI instance;

    protected static void setInstance(GBankAPI api) {
        instance = api;
    }

    public static GBankAPI getInstance() {
        if(instance == null)
            throw new NullPointerException("API is not loaded!");
        return instance;
    }
}
