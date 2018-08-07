package com.selbovi.util;

import com.selbovi.model.Account;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

public class SaveHelper {

    public static Account saveAccount(Account account, EntityManagerFactory factory) {
        EntityManager entityManager = factory.createEntityManager();
        entityManager.getTransaction().begin();
        entityManager.persist(account);
        entityManager.getTransaction().commit();
        return account;
    }
}
