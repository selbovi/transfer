package com.selbovi;

import com.selbovi.model.Account;
import org.hamcrest.core.StringContains;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.selbovi.util.SaveHelper.generateAndSaveAccounts;
import static com.selbovi.util.SaveHelper.saveAccounts;
import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;

public class ControllerTest {

    private static EntityManagerFactory entityManagerFactory;

    @BeforeClass
    public static void setup() {
        entityManagerFactory = Persistence.createEntityManagerFactory("com.selbovi.jpa");
        AppInitializer.createAndRun();
    }

    @AfterClass
    public static void clear() {
        entityManagerFactory.close();
        AppInitializer.shutdown();
    }

    @Test
    public void failInvalidAccount() {
        given()
                .param("fromAccount", "1")
                .param("toAccount", "2")
                .param("amount", 100)
                .when().get("transfer")
                .then()
                .statusCode(200)
                .body(StringContains.containsString("Attemp to pass invalid owner name for the account"));
    }

    @Test
    public void failSameAccount() {
        String sameAccountName = "sameAccount";

        given()
                .param("fromAccount", sameAccountName)
                .param("toAccount", sameAccountName)
                .param("amount", 100)
                .when().get("transfer")
                .then()
                .statusCode(200)
                .body(StringContains.containsString("Operation can't be performed with same account"));
    }

    @Test
    public void failLowBalance() {
        Account accountFrom = new Account("ownerFrom", 10);
        Account accountTo = new Account("ownerTo", 0);
        saveAccounts(entityManagerFactory, accountFrom, accountTo);

        given()
                .param("fromAccount", accountFrom.getOwner())
                .param("toAccount", accountTo.getOwner())
                .param("amount", 99)
                .when().get("transfer")
                .then()
                .statusCode(200)
                .body(StringContains.containsString("Account \"ownerFrom\" has not enough funds to complete operation"));
    }

    //TODO clear DB
    @Test
    public void successfulTransfer() {
        Account accountFrom = new Account("ownerFromAccount", 10);
        Account accountTo = new Account("ownerToAccount", 0);
        saveAccounts(entityManagerFactory, accountFrom, accountTo);

        given()
                .param("fromAccount", accountFrom.getOwner())
                .param("toAccount", accountTo.getOwner())
                .param("amount", 5)
                .when().get("transfer")
                .then()
                .statusCode(200)
                .body(StringContains.containsString("Successfully transferred"));
    }

    @Test
    public void gameOfDrunkTransferAttempts() throws ExecutionException, InterruptedException {
        //given:
        int totalAccountsNum = 20;
        int maxBalance = 200;
        List<Account> accounts = generateAndSaveAccounts(entityManagerFactory, totalAccountsNum, maxBalance);
        int balanceBeforeTransferOperations = new Double(accounts.stream().mapToDouble(a -> a.getBalance()).sum()).intValue();

        //when:
        parallelTransferWithBetweenRandomAccounts(totalAccountsNum, maxBalance, accounts);

        //then:
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        Query query = entityManager.createQuery("SELECT SUM(account.balance) FROM Account account");
        assertEquals(balanceBeforeTransferOperations, ((Double) query.getSingleResult()).intValue());
        entityManager.close();
    }

    private void parallelTransferWithBetweenRandomAccounts(
            int totalAccountsNum,
            int maxBalance,
            List<Account> accounts
    ) throws InterruptedException, ExecutionException {

        List<Callable<Object>> tasks = new ArrayList<Callable<Object>>();
        for (int i = 0; i < 1000; i++) {
            Callable<Object> callable = Executors.callable(new Thread() {
                @Override
                public void run() {
                    Account accountFrom = accounts.get(new Random().nextInt(totalAccountsNum));
                    Account accountTo = accounts.get(new Random().nextInt(totalAccountsNum));
                    int amount = new Random().nextInt(maxBalance + 1);

                    String response = given()
                            .param("fromAccount", accountFrom.getOwner())
                            .param("toAccount", accountTo.getOwner())
                            .param("amount", amount)
                            .when().get("transfer")
                            .getBody().asString();

                    System.out.println("response = " + response);
                }
            });
            tasks.add(callable);
        }

        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<Future<Object>> futures = executorService.invokeAll(tasks);
        for (Future<Object> future : futures) {
            future.get();
        }
    }
}
