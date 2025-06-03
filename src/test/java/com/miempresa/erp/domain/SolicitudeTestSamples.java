package com.miempresa.erp.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class SolicitudeTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static Solicitude getSolicitudeSample1() {
        return new Solicitude().id(1L).status("status1");
    }

    public static Solicitude getSolicitudeSample2() {
        return new Solicitude().id(2L).status("status2");
    }

    public static Solicitude getSolicitudeRandomSampleGenerator() {
        return new Solicitude().id(longCount.incrementAndGet()).status(UUID.randomUUID().toString());
    }
}
