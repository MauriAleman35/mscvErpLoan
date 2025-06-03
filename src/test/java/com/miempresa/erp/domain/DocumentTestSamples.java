package com.miempresa.erp.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class DocumentTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static Document getDocumentSample1() {
        return new Document().id(1L).urlFile("urlFile1");
    }

    public static Document getDocumentSample2() {
        return new Document().id(2L).urlFile("urlFile2");
    }

    public static Document getDocumentRandomSampleGenerator() {
        return new Document().id(longCount.incrementAndGet()).urlFile(UUID.randomUUID().toString());
    }
}
