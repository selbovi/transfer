package com.selbovi.exception;

public class InvalidAmountForTransferException extends Exception {

    public static final String AMOUNT_MSG = "Invalid amount for transfer specified, shoul be greate than 0 but was: ";

    public InvalidAmountForTransferException(double invalidAmount) {
        super(AMOUNT_MSG + invalidAmount);
    }
}
