/*
 * SMPPSequenceNumberScheme.java
 *
 * Created on 8 May 2007, 02:02
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.emagine.ussd.connection;

import java.util.concurrent.atomic.AtomicLong;

import ie.omk.smpp.util.SequenceNumberScheme;

public class UssdSequenceNumberScheme implements SequenceNumberScheme {

    private final int id;
    private final AtomicLong sequence;

    public UssdSequenceNumberScheme(int id) {
        this.id = ((id & 0x7F) << 24);
        sequence = new AtomicLong((System.currentTimeMillis() & 0x7FFFF) << 4);
    }

    public void reset() {
        sequence.set((System.currentTimeMillis() & 0x7FFFF) << 4);
    }

    public synchronized int peek() {
        return (id | (int) ((sequence.get() + 1) & 0xFFFFFF));
    }

    public synchronized int nextNumber() {
        return (id | (int) (sequence.incrementAndGet() & 0xFFFFFF));
    }

    public synchronized int peek(int i) {
        return (id | (int) ((sequence.get() + i) & 0xFFFFFF));
    }

}
