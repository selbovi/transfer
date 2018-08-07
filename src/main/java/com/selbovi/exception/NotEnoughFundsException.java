package com.selbovi.exception;

import com.selbovi.model.Account;

/**
 * Exception is raised while transfer operation, if account is low balance.
 */
public class NotEnoughFundsException extends Exception {

    /**
     * Constructor of exception.
     *
     * @param account        {@link Account} account to withdraw from
     * @param withdrawAmount units attempted to be withdrawed
     */
    public NotEnoughFundsException(final Account account, final double withdrawAmount) {
        super("Account \"" + account.getOwner() + "\" has not enough funds to complete operation, "
                + "requsted amount: " + withdrawAmount + ", available: " + account.getBalance());
    }
}
