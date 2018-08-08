package com.selbovi;

import com.selbovi.impl.TransferRequestHandler;
import com.selbovi.impl.TransferServiceImpl;
import io.undertow.Undertow;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Maintains logic for starting/finishing the service.
 */
public class AppInitializer {

    private static final int PORT = 8080;
    private static final String HOST = "localhost";

    private static EntityManagerFactory entityManagerFactory = Persistence
            .createEntityManagerFactory("com.selbovi.jpa");
    private static Undertow server;

    /**
     * Apps entrypoint.
     * @param args arguments
     */
    public static void main(String[] args) {
        createAndRun();
    }

    /**
     * Start all.
     */
    public static void createAndRun() {
        TransferService transferService = new TransferServiceImpl(entityManagerFactory);
        TransferRequestHandler controller = new TransferRequestHandler(transferService);
        server = Undertow.builder().addHttpListener(PORT, HOST)
                .setHandler(controller).build();
        server.start();
    }

    /**
     * Gracefully shutdown.
     */
    public static void shutdown() {
        entityManagerFactory.close();
        server.stop();
    }

}
