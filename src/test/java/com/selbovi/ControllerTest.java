package com.selbovi;

import org.hamcrest.core.StringContains;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static io.restassured.RestAssured.given;

public class ControllerTest {

    @BeforeClass
    public static void setup() {
        AppInitializer.createAndRun();
    }

    @AfterClass
    public static void clear() {
        AppInitializer.shutdown();
    }

    @Test
    public void testSuccess() {
        given()
                .param("fromAccount", "1")
                .param("toAccount", "2")
                .param("amount", 100)
                .when().get("transfer")
                .then().statusCode(200).body(StringContains.containsString("account"));
    }
}
