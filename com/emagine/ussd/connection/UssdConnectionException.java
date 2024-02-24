package com.emagine.ussd.connection;

public class UssdConnectionException extends Exception {

    private static final long serialVersionUID = 1L;

    public UssdConnectionException(String message) {
        super(message);
    }

    public UssdConnectionException(String message, Exception e) {
        super(message, e);
    }

}
