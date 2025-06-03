package com.miempresa.erp.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class SubscriptionPlanTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static SubscriptionPlan getSubscriptionPlanSample1() {
        return new SubscriptionPlan().id(1L).name("name1").description("description1").status("status1");
    }

    public static SubscriptionPlan getSubscriptionPlanSample2() {
        return new SubscriptionPlan().id(2L).name("name2").description("description2").status("status2");
    }

    public static SubscriptionPlan getSubscriptionPlanRandomSampleGenerator() {
        return new SubscriptionPlan()
            .id(longCount.incrementAndGet())
            .name(UUID.randomUUID().toString())
            .description(UUID.randomUUID().toString())
            .status(UUID.randomUUID().toString());
    }
}
