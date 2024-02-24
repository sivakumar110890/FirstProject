package com.emagine.ussd.exception;

public class USSDPluginException extends Exception {

	private static final long serialVersionUID = 1L;
    private static final String DEFAULT_MESSAGE = "USSDPlugin Generic Exception";

    public USSDPluginException () {
        super(DEFAULT_MESSAGE);
    }

    public USSDPluginException (Exception e) {
        super(e);
    }

    public USSDPluginException (String msg) {
        super(msg);
    }

    public USSDPluginException (String msg, Throwable e) {
        super(msg, e);
    }

}
