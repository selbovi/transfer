package com.selbovi;

import com.selbovi.model.Account;
import org.hamcrest.core.StringContains;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import static com.selbovi.util.SaveHelper.saveAccounts;
import static io.restassured.RestAssured.given;

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

    //TODO body
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
}
