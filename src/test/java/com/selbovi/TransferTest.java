package com.selbovi;

import com.selbovi.exception.InvalidAccountException;
import com.selbovi.exception.InvalidAmountForTransferException;
import com.selbovi.exception.NotEnoughFundsException;
import com.selbovi.exception.SameAccountProhibitedOperationException;
import com.selbovi.impl.TransferServiceImpl;
import com.selbovi.model.Account;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.selbovi.util.SaveHelper.saveAccount;
import static org.junit.Assert.assertEquals;

public class TransferTest {
    //TODO stdout make operations visible
    //TODO account empty or NotSpecified
    //TODO game transfer between all accounts and then check consistency
    //TODO rest api test
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private EntityManagerFactory entityManagerFactory;

    @Before
    public void setUp() {
        entityManagerFactory = Persistence.createEntityManagerFactory("com.selbovi.jpa");
    }

    @After
    public void cleanup() {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        Query q = entityManager.createQuery("DELETE FROM Account");
        q.executeUpdate();
        entityManager.getTransaction().commit();
    }

    /**
     * Throw exception if account doesn't really exists.
     *
     * @throws InvalidAccountException exception
     */
    @Test
    public void throwExceptionForNonExistingAccount() throws InvalidAccountException {
        //given:
        TransferServiceImpl service = new TransferServiceImpl(entityManagerFactory);
        String wrongAccountOwnerName = "AccountOne";

        //expected:
        thrown.expect(InvalidAccountException.class);
        thrown.expectMessage(InvalidAccountException.ATTEMPT_MSG + wrongAccountOwnerName);

        //when:
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        service.findAccount(wrongAccountOwnerName, entityManager);
    }

    @Test
    public void throwExceptionIfTransferBetweenSameAccount() throws InvalidAmountForTransferException, InvalidAccountException, NotEnoughFundsException, SameAccountProhibitedOperationException {
        //given:
        TransferServiceImpl service = new TransferServiceImpl(entityManagerFactory);
        String sameAccountName = "owner";

        //expected:
        thrown.expect(SameAccountProhibitedOperationException.class);
        thrown.expectMessage(SameAccountProhibitedOperationException.OPERATION_MSG + sameAccountName);

        //when:
        service.transfer(sameAccountName, sameAccountName, Integer.MAX_VALUE);
    }

    /**
     * Checks whether exception is thrown, when we send incorrect (eg negative) amount for withdrawing.
     *
     * @throws InvalidAmountForTransferException exception
     */
    @Test
    public void checkBalanceIsPositive() throws InvalidAmountForTransferException, InvalidAccountException, NotEnoughFundsException, SameAccountProhibitedOperationException {
        //given:
        TransferServiceImpl service = new TransferServiceImpl(entityManagerFactory);
        double invalidAmount = -1;

        //expected:
        thrown.expect(InvalidAmountForTransferException.class);
        thrown.expectMessage(InvalidAmountForTransferException.AMOUNT_MSG + invalidAmount);

        //when:
        service.transfer("", "", invalidAmount);
    }

    /**
     * Checks whether exception is thrown, when accounts balance is too small to withdraw from).
     */
    @Test
    public void withdrawFromAccountWhenFundsNotEnough() throws NotEnoughFundsException {
        //given:
        TransferServiceImpl service = new TransferServiceImpl(entityManagerFactory);
        String ownerName = "owner";
        Account account = new Account(ownerName, 1);
        double requested = 100;

        //expect:
        thrown.expect(NotEnoughFundsException.class);
        thrown.expectMessage("Account " + ownerName + " has not enough funds to complete operation, requsted amount: " + requested + ", available: " + account.getBalance());

        //when:
        service.withdraw(account, requested);
    }

    /**
     * Checks whether accounts balance correctly updated, when balance is enough to withdraw requested amount).
     */
    @Test
    public void withdrawFromAccount() throws NotEnoughFundsException {
        //given:
        TransferServiceImpl service = new TransferServiceImpl(entityManagerFactory);
        Account account = new Account("owner", 100);
        double requested = 1;

        //when:
        service.withdraw(account, requested);

        //then:
        int result = Double.compare(99, account.getBalance());
        assertEquals(0, result);
    }

    @Test
    public void successfulTransfer() throws InvalidAmountForTransferException, InvalidAccountException, NotEnoughFundsException, SameAccountProhibitedOperationException {
        //given:
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        Account accountFrom = new Account("ownerFrom", 100);
        Account accountTo = new Account("ownerTo", 0);
        entityManager.persist(accountFrom);
        entityManager.persist(accountTo);
        entityManager.getTransaction().commit();
        entityManager.close();
        TransferServiceImpl service = new TransferServiceImpl(entityManagerFactory);

        //when:
        service.transfer(accountFrom.getOwner(), accountTo.getOwner(), 100);

        //then:
        entityManager = entityManagerFactory.createEntityManager();
        assertEquals(0, Double.compare(0, entityManager.find(Account.class, accountFrom.getOwner()).getBalance()));
        assertEquals(0, Double.compare(100, entityManager.find(Account.class, accountTo.getOwner()).getBalance()));
    }

    @Test
    public void concurrentWithdrawalTest() throws InterruptedException, ExecutionException {
        //given:
        int maxUnits = 1000;
        Account accountFrom = new Account("ownerFrom", maxUnits);
        Account accountTo = new Account("ownerTo", 0);
        saveAccount(accountFrom, entityManagerFactory);
        saveAccount(accountTo, entityManagerFactory);

        //when:
        performParallelWithdraws(maxUnits, accountFrom, accountTo);

        //then:
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        accountFrom = entityManager.find(Account.class, accountFrom.getOwner());
        assertEquals(0, Double.compare(0, accountFrom.getBalance()));
        accountTo = entityManager.find(Account.class, accountTo.getOwner());
        assertEquals(0, Double.compare(maxUnits, accountTo.getBalance()));
    }

    private void performParallelWithdraws(int maxUnits, Account accountFrom, Account accountTo) throws InterruptedException, ExecutionException {
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        final TransferServiceImpl service = new TransferServiceImpl(entityManagerFactory);

        List<Callable<Object>> tasks = new ArrayList<Callable<Object>>();
        for (int i = 0; i < maxUnits; i++) {
            Callable<Object> callable = Executors.callable(new Thread() {
                @Override
                public void run() {
                    try {
                        service.transfer(accountFrom.getOwner(), accountTo.getOwner(), 1);
                    } catch (InvalidAmountForTransferException e) {
                        e.printStackTrace();
                    } catch (InvalidAccountException e) {
                        e.printStackTrace();
                    } catch (NotEnoughFundsException e) {
                        e.printStackTrace();
                    } catch (SameAccountProhibitedOperationException e) {
                        e.printStackTrace();
                    }
                }
            });
            tasks.add(callable);


        }
        List<Future<Object>> futures = executorService.invokeAll(tasks);
        for (Future<Object> future : futures) {
            future.get();
        }
    }
}
