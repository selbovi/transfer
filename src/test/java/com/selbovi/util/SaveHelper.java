package com.selbovi.util;

import com.selbovi.model.Account;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class SaveHelper {

    public static void saveAccounts(EntityManagerFactory factory, Account... accounts) {
        EntityManager entityManager = factory.createEntityManager();
        try {
            entityManager.getTransaction().begin();
            for (int i = 0; i < accounts.length; i++) {
                Account account = accounts[i];
                entityManager.persist(account);
            }
        } finally {
            entityManager.getTransaction().commit();
            entityManager.close();
        }
    }

    public static List<Account> generateAndSaveAccounts(
            EntityManagerFactory factory,
            int totalAccountsNum,
            int maxBalance
    ) {
        List<Account> result = new ArrayList<>(totalAccountsNum);
        EntityManager entityManager = factory.createEntityManager();
        try {
            entityManager.getTransaction().begin();
            for (int i = 0; i < totalAccountsNum; i++) {
                Account account = new Account("Account-" + i, new Random().nextInt(maxBalance + 1));
                entityManager.persist(account);
                result.add(account);
            }
        } finally {
            entityManager.getTransaction().commit();
            entityManager.close();
        }

        return result;
    }
}
