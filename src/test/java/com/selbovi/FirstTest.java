package com.selbovi;

import com.selbovi.model.Account;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class FirstTest {

    private EntityManagerFactory entityManagerFactory;

    @Before
    public void setUp() {
        entityManagerFactory = Persistence.createEntityManagerFactory("com.selbovi.jpa");
    }

    @Test
    public void f() {
        EntityManager em = entityManagerFactory.createEntityManager();
        em.getTransaction().begin();
        Account entity = new Account();
        entity.setOwner("1");
        entity.setBalance(23);
        em.persist(entity);
        em.getTransaction().commit();
        em.close();
    }
}
