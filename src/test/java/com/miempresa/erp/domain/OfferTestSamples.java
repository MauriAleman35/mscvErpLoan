package com.miempresa.erp.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class OfferTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + (2 * Short.MAX_VALUE));

    public static Offer getOfferSample1() {
        return new Offer().id(1L).partnerId(1).loanTerm(1).status("status1");
    }

    public static Offer getOfferSample2() {
        return new Offer().id(2L).partnerId(2).loanTerm(2).status("status2");
    }

    public static Offer getOfferRandomSampleGenerator() {
        return new Offer()
            .id(longCount.incrementAndGet())
            .partnerId(intCount.incrementAndGet())
            .loanTerm(intCount.incrementAndGet())
            .status(UUID.randomUUID().toString());
    }
}
