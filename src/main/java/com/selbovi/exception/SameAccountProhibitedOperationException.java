package com.selbovi.exception;

/**
 * Exception to be thrown in case if operation couldn't be performed on the same account.
 */
public class SameAccountProhibitedOperationException extends Exception {
    /**
     * Template message.
     */
    public static final String OPERATION_MSG = "Operation can't be performed with same account: ";

    /**
     * Constructor of exception.
     *
     * @param accountName {@link com.selbovi.model.Account#getOwner()}
     */
    public SameAccountProhibitedOperationException(final String accountName) {
        super(OPERATION_MSG + accountName);
    }
}
