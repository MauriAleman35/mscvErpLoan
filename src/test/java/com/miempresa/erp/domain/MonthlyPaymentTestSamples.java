package com.miempresa.erp.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class MonthlyPaymentTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + (2 * Short.MAX_VALUE));

    public static MonthlyPayment getMonthlyPaymentSample1() {
        return new MonthlyPayment().id(1L).comprobantFile("comprobantFile1").daysLate(1).paymentStatus("paymentStatus1");
    }

    public static MonthlyPayment getMonthlyPaymentSample2() {
        return new MonthlyPayment().id(2L).comprobantFile("comprobantFile2").daysLate(2).paymentStatus("paymentStatus2");
    }

    public static MonthlyPayment getMonthlyPaymentRandomSampleGenerator() {
        return new MonthlyPayment()
            .id(longCount.incrementAndGet())
            .comprobantFile(UUID.randomUUID().toString())
            .daysLate(intCount.incrementAndGet())
            .paymentStatus(UUID.randomUUID().toString());
    }
}
