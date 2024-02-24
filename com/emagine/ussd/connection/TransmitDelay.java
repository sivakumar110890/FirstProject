package com.emagine.ussd.connection;

import java.math.BigDecimal;

public class TransmitDelay {

    private static final int MAX_BLOCK_NUMBER = 100;
    private final int messagesPerSecond;
    private final long startTime = System.currentTimeMillis();
    private final Offset[] offsetIndex = new Offset[1000];
    private final long[] times;

    private int curr = 0;
    private int remaining = 0;
    private long nextTime = 0L;

    private int counter = 0;

    public TransmitDelay(int messagesPerSecond) {
        this.messagesPerSecond = messagesPerSecond;
        if (messagesPerSecond > 0) {
            int numberOfBlocks = (messagesPerSecond > MAX_BLOCK_NUMBER) ? MAX_BLOCK_NUMBER : messagesPerSecond;
            int[] offset = new int[numberOfBlocks];
            int[] capacity = new int[numberOfBlocks];
            double value = 1000;
            for (int i = 0; i < numberOfBlocks; i++) {
                BigDecimal valueForRound = BigDecimal.valueOf((long) i + 1).multiply(BigDecimal.valueOf(value)).divide(BigDecimal.valueOf(numberOfBlocks));
                offset[i] = valueForRound.intValue();
                capacity[i] = 0;
            }
            for (int i = 0, j = 0; i < messagesPerSecond; i++) {
                int pos = BigDecimal.valueOf((long) i + 1).multiply(BigDecimal.valueOf(value)).divide(BigDecimal.valueOf(messagesPerSecond)).intValue();
                while (pos > offset[j]) {
                    j++;
                }
                capacity[j]++;
            }
            for (int i = 0, j = 0; i < 1000; j++) {
                Offset o = new Offset(offset[j], capacity[j]);
                while (i < offset[j]) {
                    offsetIndex[i++] = o;
                }

            }
            times = new long[messagesPerSecond];
        } else {
            times = null;
        }
    }

    public long pause(int count) throws InterruptedException {
        if (messagesPerSecond == 0) {
            return 0L;
        }
        long beginTime = System.currentTimeMillis();
        long endTime = 0L;
        for (int i = 0; i < count; i++) {
            while ((endTime = sleep()) < beginTime) {
                if (endTime == 0) {
                    endTime = 1;
                }
                Thread.sleep(endTime);
                counter++;
            }
            curr = (curr + 1) % messagesPerSecond;
        }
        return endTime - beginTime;
    }

    public int getCounter() {
        return counter;
    }

    private long sleep() {
        long currentTime = System.currentTimeMillis();

        if (currentTime <= times[curr] + 1000) {
            return (times[curr] + 1000 - currentTime) / 2;
        }

        if (currentTime > nextTime) {
            long diff = currentTime - startTime;
            int millis = (int) (diff % 1000);
            long secondOffset = diff - millis;
            Offset offset = offsetIndex[millis];
            nextTime = secondOffset + offset.getValue();

            remaining = offset.getCapacity();
        }
        if (remaining == 0) {
            return (nextTime - currentTime) / 2;
        }
        remaining--;
        times[curr] = currentTime;
        return currentTime;
    }

    public void print() {
        for (int i = 0; i < offsetIndex.length; i++) {
            System.out.println("offsetIndex : " + i + " - " + offsetIndex[i].getValue() + " - " + offsetIndex[i].getCapacity());
        }
    }

    private static class Offset {
        private final int value;
        private final int capacity;

        public Offset(int value, int capacity) {
            this.value = value;
            this.capacity = capacity;
        }

        public int getValue() {
            return value;
        }

        public int getCapacity() {
            return capacity;
        }
    }
}
