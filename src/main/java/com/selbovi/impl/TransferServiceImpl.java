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
import java.text.MessageFormat;

public class TransferServiceImpl implements TransferService {

    private EntityManagerFactory entityManagerFactory;

    public TransferServiceImpl(final EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public void transfer(String from, String to, double amount) throws InvalidAmountForTransferException, InvalidAccountException, NotEnoughFundsException, SameAccountProhibitedOperationException {
        if (amount <= 0) {
            throw new InvalidAmountForTransferException(amount);
        }

        if (from.equals(to)) {
            throw new SameAccountProhibitedOperationException(from);
        }

        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            entityManager.getTransaction().begin();
            Account accountFrom = findAccount(from, entityManager);
            Account accountTo = findAccount(to, entityManager);

            withdraw(accountFrom, amount);
            fill(accountTo, amount);

            System.out.println(
                    MessageFormat.format(
                            "Successfully transferred {0} unit(s), from account = \"{1}\" ({2} units), to account = \"{3}\" ({4} units).", amount, from, accountFrom.getBalance(), to, accountTo.getBalance()
                    )
            );
        } finally {
            entityManager.getTransaction().commit();
            entityManager.close();
        }

    }

    private Account findAccount(String accountOwnerName, EntityManager entityManager) throws InvalidAccountException {

        Account account = entityManager.find(Account.class, accountOwnerName, LockModeType.PESSIMISTIC_WRITE);
        if (account == null) {
            throw new InvalidAccountException(accountOwnerName);
        }
        return account;
    }

    private void withdraw(Account account, double withdrawAmount) throws NotEnoughFundsException {
        double balance = account.getBalance();
        if (withdrawAmount > balance) {
            throw new NotEnoughFundsException(account, withdrawAmount);
        }

        double updatedBalance = balance - withdrawAmount;
        account.setBalance(updatedBalance);
    }

    private void fill(Account account, double amount) {
        double updatedBalance = account.getBalance() + amount;
        account.setBalance(updatedBalance);
    }
}
