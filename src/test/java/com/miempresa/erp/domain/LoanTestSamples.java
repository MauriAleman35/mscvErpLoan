package com.miempresa.erp.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class LoanTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + (2 * Short.MAX_VALUE));

    public static Loan getLoanSample1() {
        return new Loan().id(1L).hashBlockchain("hashBlockchain1").currentStatus("currentStatus1").latePaymentCount(1);
    }

    public static Loan getLoanSample2() {
        return new Loan().id(2L).hashBlockchain("hashBlockchain2").currentStatus("currentStatus2").latePaymentCount(2);
    }

    public static Loan getLoanRandomSampleGenerator() {
        return new Loan()
            .id(longCount.incrementAndGet())
            .hashBlockchain(UUID.randomUUID().toString())
            .currentStatus(UUID.randomUUID().toString())
            .latePaymentCount(intCount.incrementAndGet());
    }
}
