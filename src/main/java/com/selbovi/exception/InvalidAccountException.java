package com.selbovi.exception;

/**
 * Exception to be throw in case if account doesnt exist in database.
 */
public class InvalidAccountException extends Exception {

    public static final String ATTEMPT_MSG = "Attemp to pass invalid owner name for the account: ";

    public InvalidAccountException(String accountsOwnerName) {
        super(ATTEMPT_MSG + accountsOwnerName);
    }
}
