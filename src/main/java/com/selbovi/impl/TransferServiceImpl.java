package com.selbovi.impl;

import com.selbovi.TransferService;
import com.selbovi.exception.InvalidAccountException;
import com.selbovi.exception.InvalidAmountForTransferException;
import com.selbovi.exception.NotEnoughFundsException;
import com.selbovi.exception.SameAccountProhibitedOperationException;
import com.selbovi.model.Account;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.LockModeType;

/**
 * Implementation for money transfer operations.
 */
public class TransferServiceImpl implements TransferService {

    private EntityManagerFactory entityManagerFactory;

    /**
     * Constructor of service.
     *
     * @param entityManagerFactory entity manager factory for the persistence unit.
     */
    public TransferServiceImpl(final EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    /**
     * Operation of money transfers between accounts.
     *
     * @param accountOwnerNameFrom from account
     * @param accountOwnerNameTo   toName account
     * @param amount               amount of money toName transfer (units)
     * @throws InvalidAmountForTransferException
     * @throws InvalidAccountException
     * @throws NotEnoughFundsException
     * @throws SameAccountProhibitedOperationException
     */
    @Override
    public void transfer(final String accountOwnerNameFrom, final String accountOwnerNameTo, final double amount)
            throws InvalidAmountForTransferException, InvalidAccountException,
            NotEnoughFundsException, SameAccountProhibitedOperationException {
        if (amount <= 0) {
            throw new InvalidAmountForTransferException(amount);
        }

        if (accountOwnerNameFrom.equals(accountOwnerNameTo)) {
            throw new SameAccountProhibitedOperationException(accountOwnerNameFrom);
        }

        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            entityManager.getTransaction().begin();
            Account accountFrom = findAccount(accountOwnerNameFrom, entityManager);
            Account accountTo = findAccount(accountOwnerNameTo, entityManager);

            withdraw(accountFrom, amount);
            fill(accountTo, amount);
        } finally {
            entityManager.getTransaction().commit();
            entityManager.close();
        }

    }

    private Account findAccount(final String accountOwnerName, final EntityManager entityManager)
            throws InvalidAccountException {

        Account account = entityManager.find(Account.class, accountOwnerName, LockModeType.PESSIMISTIC_WRITE);
        if (account == null) {
            throw new InvalidAccountException(accountOwnerName);
        }
        return account;
    }

    private void withdraw(final Account account, final double withdrawAmount) throws NotEnoughFundsException {
        double balance = account.getBalance();
        if (withdrawAmount > balance) {
            throw new NotEnoughFundsException(account, withdrawAmount);
        }

        double updatedBalance = balance - withdrawAmount;
        account.setBalance(updatedBalance);
    }

    private void fill(final Account account, final double amount) {
        double updatedBalance = account.getBalance() + amount;
        account.setBalance(updatedBalance);
    }
}
