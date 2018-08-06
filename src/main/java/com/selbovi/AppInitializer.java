package com.selbovi;

import com.selbovi.impl.TransferServiceImpl;
import io.undertow.Undertow;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class AppInitializer {

    private static EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("com.selbovi.jpa");
    private static Undertow server;

    public static void main(String[] args) {
        createAndRun();
    }

    public static void createAndRun() {
        TransferService transferService = new TransferServiceImpl(entityManagerFactory);
        Controller controller = new Controller(transferService);
        server = Undertow.builder().addHttpListener(8080, "localhost")
                .setHandler(controller).build();
        server.start();
    }

    public static void shutdown() {
        entityManagerFactory.close();
        server.stop();
    }

}
