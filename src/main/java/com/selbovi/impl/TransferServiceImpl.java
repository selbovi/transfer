package com.selbovi.impl;

import com.selbovi.TransferService;
import com.selbovi.exception.InvalidAccountException;
import com.selbovi.exception.InvalidAmountForTransferException;
import com.selbovi.exception.NotEnoughFundsException;
import com.selbovi.model.Account;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.LockModeType;

public class TransferServiceImpl implements TransferService {

    private EntityManagerFactory entityManagerFactory;

    public TransferServiceImpl(final EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public void transfer(String from, String to, double amount) throws InvalidAmountForTransferException, InvalidAccountException, NotEnoughFundsException {
        if (amount <= 0) {
            throw new InvalidAmountForTransferException(amount);
        }

        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        Account accountFrom = findAccount(from, entityManager);
        Account accountTo = findAccount(to, entityManager);
        withdraw(accountFrom, amount);
        fill(accountTo, amount);

        entityManager.getTransaction().commit();

        entityManager.close();
    }

    public Account findAccount(String accountOwnerName, EntityManager entityManager) throws InvalidAccountException {

        Account account = entityManager.find(Account.class, accountOwnerName);
        if (account == null) {
            throw new InvalidAccountException(accountOwnerName);
        }
        return account;
    }

    public void withdraw(Account account, double withdrawAmount) throws NotEnoughFundsException {
        double balance = account.getBalance();
        if (withdrawAmount > balance) {
            throw new NotEnoughFundsException(account, withdrawAmount);
        }

        double updatedBalance = balance - withdrawAmount;
        account.setBalance(updatedBalance);
    }

    public void fill(Account account, double amount) {
        double updatedBalance = account.getBalance() + amount;
        account.setBalance(updatedBalance);
    }
}
