package com.selbovi.exception;

/**
 * Exception to be thrown in case if account doesnt exist in database.
 */
public class InvalidAccountException extends Exception {

    /**
     * Template message.
     */
    public static final String ATTEMPT_MSG = "Attemp to pass invalid owner name for the account: ";

    /**
     * Constructor of exception.
     *
     * @param accountsOwnerName {@link com.selbovi.model.Account#getOwner()}
     */
    public InvalidAccountException(final String accountsOwnerName) {
        super(ATTEMPT_MSG + accountsOwnerName);
    }
}
