package com.miempresa.erp.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class UserTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + (2 * Short.MAX_VALUE));

    public static User getUserSample1() {
        return new User()
            .id(1L)
            .name("name1")
            .lastName("lastName1")
            .email("email1")
            .phone("phone1")
            .ci("ci1")
            .password("password1")
            .score(1)
            .status("status1")
            .userType("userType1");
    }

    public static User getUserSample2() {
        return new User()
            .id(2L)
            .name("name2")
            .lastName("lastName2")
            .email("email2")
            .phone("phone2")
            .ci("ci2")
            .password("password2")
            .score(2)
            .status("status2")
            .userType("userType2");
    }

    public static User getUserRandomSampleGenerator() {
        return new User()
            .id(longCount.incrementAndGet())
            .name(UUID.randomUUID().toString())
            .lastName(UUID.randomUUID().toString())
            .email(UUID.randomUUID().toString())
            .phone(UUID.randomUUID().toString())
            .ci(UUID.randomUUID().toString())
            .password(UUID.randomUUID().toString())
            .score(intCount.incrementAndGet())
            .status(UUID.randomUUID().toString())
            .userType(UUID.randomUUID().toString());
    }
}
