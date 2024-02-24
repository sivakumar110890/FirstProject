/**
 *
 */
package com.emagine.ussd.connection;

/**
 * @author udaykapavarapu
 *
 */
public class UssdMessageException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public UssdMessageException(String message) {
        super(message);
    }

    public UssdMessageException(String message, Throwable e) {
        super(message, e);
    }

}
