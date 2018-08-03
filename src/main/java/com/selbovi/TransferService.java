package com.selbovi;

import com.selbovi.exception.InvalidAccountException;
import com.selbovi.exception.InvalidAmountForTransferException;
import com.selbovi.exception.NotEnoughFundsException;
import com.selbovi.exception.SameAccountProhibitedOperationException;

/**
 * Contract for money transfer operations.
 */
public interface TransferService {
    /**
     * Money transfers between accounts.
     *
     * @param from      owner name of account from
     * @param to        owner name of account to
     * @param amount    how much money to transfer
     */
    void transfer(String from, String to, double amount) throws InvalidAmountForTransferException, InvalidAccountException, NotEnoughFundsException, SameAccountProhibitedOperationException;

}
