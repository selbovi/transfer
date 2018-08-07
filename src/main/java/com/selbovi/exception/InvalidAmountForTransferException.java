package com.selbovi.exception;

/**
 * Exception to be thrown in case amount eg is negative.
 */
public class InvalidAmountForTransferException extends Exception {


    /**
     * Template message.
     */
    public static final String AMOUNT_MSG = "Invalid amount for transfer specified, shoul be greate than 0 but was: ";

    /**
     * Constructor of exception.
     *
     * @param invalidAmount units attempted to be transferred
     */
    public InvalidAmountForTransferException(final double invalidAmount) {
        super(AMOUNT_MSG + invalidAmount);
    }
}
