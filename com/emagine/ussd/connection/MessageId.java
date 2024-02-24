package com.emagine.ussd.connection;

import java.util.concurrent.atomic.AtomicLong;

public class MessageId {

    private static final AtomicLong SEQ = new AtomicLong(System.currentTimeMillis() << 12);

    private MessageId() {
    }

    public static long nextMessageId() {
        return SEQ.incrementAndGet();
    }

    public static long nextMessageId(final int steps) {
        return SEQ.addAndGet(steps) - steps + 1;
    }
}
