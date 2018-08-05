package com.selbovi;

import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import org.hamcrest.Matchers;
import org.junit.Test;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class ControllerTest {

    @Test
    public void testSuccess() {
        given()
                .param("fromAccount", "1")
                .param("toAccount", "1")
                .param("amount", 100)
                .when().get("transfer")
                .then().statusCode(200);
    }
}
