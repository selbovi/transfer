package com.selbovi.exception;

import com.selbovi.model.Account;

public class NotEnoughFundsException extends Exception {
    public NotEnoughFundsException(Account account, double withdrawAmount) {
        super("Account \"" + account.getOwner() + "\" has not enough funds to complete operation, requsted amount: " + withdrawAmount + ", available: " + account.getBalance());
    }
}
