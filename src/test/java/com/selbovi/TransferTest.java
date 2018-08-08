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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.selbovi.util.SaveHelper.saveAccounts;
import static org.junit.Assert.assertEquals;

public class TransferTest {
    //TODO stdout make operations visible
    //TODO account empty or NotSpecified
    //TODO game transfer between all accounts and then check consistency
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
        entityManagerFactory.close();
    }

    /**
     * Throw exception if account doesn't really exists.
     *
     * @throws InvalidAccountException exception
     */
    @Test
    public void throwExceptionForNonExistingAccount() throws Exception {
        //given:
        String wrongAccountOwnerName = "AccountOne";

        //expected:
        thrown.expect(InvalidAccountException.class);
        thrown.expectMessage(InvalidAccountException.ATTEMPT_MSG + wrongAccountOwnerName);

        //when:
        new TransferServiceImpl(entityManagerFactory).transfer(wrongAccountOwnerName, "ownerTo", Integer.MAX_VALUE);
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
    public void throwInvalidAmountForTransferException() throws InvalidAmountForTransferException, InvalidAccountException, NotEnoughFundsException, SameAccountProhibitedOperationException {
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
    public void throwNotEnoughFundsException() throws Exception {
        //given:
        String ownerName = "accountFrom";
        Account accountFrom = new Account(ownerName, 1);
        Account accountTo = new Account("accountTo", 1);
        saveAccounts(entityManagerFactory, accountFrom, accountTo);
        double requestedAmount = 100;

        //expect:
        thrown.expect(NotEnoughFundsException.class);
        thrown.expectMessage("Account \"" + ownerName + "\" has not enough funds to complete operation, requsted amount: " + requestedAmount + ", available: " + accountFrom.getBalance());

        //when:
        new TransferServiceImpl(entityManagerFactory).transfer(accountFrom.getOwner(), accountTo.getOwner(), requestedAmount);
    }

    @Test
    public void successfulTransfer() throws InvalidAmountForTransferException, InvalidAccountException, NotEnoughFundsException, SameAccountProhibitedOperationException {
        //given:
        Account accountFrom = new Account("ownerFrom", 100);
        Account accountTo = new Account("ownerTo", 0);
        saveAccounts(entityManagerFactory, accountFrom, accountTo);

        //when:
        new TransferServiceImpl(entityManagerFactory).transfer(accountFrom.getOwner(), accountTo.getOwner(), 100);

        //then:
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        assertEquals(0, Double.compare(0, entityManager.find(Account.class, accountFrom.getOwner()).getBalance()));
        assertEquals(0, Double.compare(100, entityManager.find(Account.class, accountTo.getOwner()).getBalance()));
    }

    @Test
    public void concurrentWithdrawalTest() throws InterruptedException, ExecutionException {
        //given:
        int maxUnits = 1000;
        Account accountFrom = new Account("accountFromOwnerName", maxUnits);
        Account accountTo = new Account("accountToOwnerName", 0);
        saveAccounts(entityManagerFactory, accountFrom, accountTo);

        //when:
        performParallelWithdraws(maxUnits, accountFrom, accountTo);

        //then:
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        accountFrom = entityManager.find(Account.class, accountFrom.getOwner());
        accountTo = entityManager.find(Account.class, accountTo.getOwner());
        System.out.println("accountFrom = " + accountFrom);
        System.out.println("accountTo = " + accountTo);
        assertEquals(0, Double.compare(0, accountFrom.getBalance()));
        assertEquals(0, Double.compare(maxUnits, accountTo.getBalance()));
        entityManager.close();
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
                        int amount = 1;
                        service.transfer(accountFrom.getOwner(), accountTo.getOwner(), amount);
                        System.out.println(
                                MessageFormat.format(
                                        "Successfully transferred {0} unit(s), from \"{1}\", "
                                                + "to \"{2}\". ({3})",
                                        amount, accountFrom.getOwner(), accountTo.getOwner(),
                                        Thread.currentThread().getName()
                                )
                        );
                    } catch (InvalidAmountForTransferException
                            | InvalidAccountException
                            | NotEnoughFundsException
                            | SameAccountProhibitedOperationException e) {
                        System.err.println(e.getMessage());
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
