package com.miempresa.erp.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class UserSubscriptionTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static UserSubscription getUserSubscriptionSample1() {
        return new UserSubscription().id(1L).status("status1");
    }

    public static UserSubscription getUserSubscriptionSample2() {
        return new UserSubscription().id(2L).status("status2");
    }

    public static UserSubscription getUserSubscriptionRandomSampleGenerator() {
        return new UserSubscription().id(longCount.incrementAndGet()).status(UUID.randomUUID().toString());
    }
}
