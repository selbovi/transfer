package com.selbovi.util;

import com.selbovi.model.Account;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

public class SaveHelper {

    public static void saveAccounts(EntityManagerFactory factory, Account... accounts) {
        EntityManager entityManager = factory.createEntityManager();
        entityManager.getTransaction().begin();
        for (int i = 0; i < accounts.length; i++) {
            Account account = accounts[i];
            entityManager.persist(account);
        }
        entityManager.getTransaction().commit();
        entityManager.close();
    }
}
