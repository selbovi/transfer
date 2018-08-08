package com.selbovi.impl;

import com.selbovi.TransferService;
import com.selbovi.exception.InvalidAccountException;
import com.selbovi.exception.InvalidAmountForTransferException;
import com.selbovi.exception.NotEnoughFundsException;
import com.selbovi.exception.SameAccountProhibitedOperationException;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.PathHandler;

import java.text.MessageFormat;
import java.util.Deque;
import java.util.Map;

/**
 * Handler for processing "transfer" requests.
 */
public class TransferRequestHandler extends PathHandler {

    private TransferService transferService;

    /**
     * Constructor of a handler.
     *
     * @param transferService {@link TransferService}
     */
    public TransferRequestHandler(final TransferService transferService) {
        this.transferService = transferService;
    }

    /**
     * Process request that could be handled by this class.
     *
     * @param exchange represents request-response interaction.
     */
    @Override
    public void handleRequest(final HttpServerExchange exchange) {
        Map<String, Deque<String>> queryParameters =
                exchange.getQueryParameters();

        String fromAccount = queryParameters.get("fromAccount").getFirst();
        String toAccount = queryParameters.get("toAccount").getFirst();
        String amount = queryParameters.get("amount").getFirst();
        String result = MessageFormat.format(
                "Successfully transferred {0} unit(s), from \"{1}\", to \"{2}\".",
                amount, fromAccount, toAccount
        );

        try {
            transferService.transfer(fromAccount, toAccount, Double.parseDouble(amount));
        } catch (InvalidAmountForTransferException
                | InvalidAccountException
                | SameAccountProhibitedOperationException
                | NotEnoughFundsException e) {
            result = e.getMessage();
        }

        exchange.getResponseSender().send(result);
    }
}
