package com.github.xnaut97.gbank.api.data;

public interface BankBalance {

    BankCurrency getCurrency();

    double getAmount();

    void setAmount(double value);

}
