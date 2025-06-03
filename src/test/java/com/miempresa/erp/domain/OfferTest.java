package com.miempresa.erp.domain;

import static com.miempresa.erp.domain.OfferTestSamples.*;
import static com.miempresa.erp.domain.SolicitudeTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.miempresa.erp.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class OfferTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Offer.class);
        Offer offer1 = getOfferSample1();
        Offer offer2 = new Offer();
        assertThat(offer1).isNotEqualTo(offer2);

        offer2.setId(offer1.getId());
        assertThat(offer1).isEqualTo(offer2);

        offer2 = getOfferSample2();
        assertThat(offer1).isNotEqualTo(offer2);
    }

    @Test
    void solicitudeTest() {
        Offer offer = getOfferRandomSampleGenerator();
        Solicitude solicitudeBack = getSolicitudeRandomSampleGenerator();

        offer.setSolicitude(solicitudeBack);
        assertThat(offer.getSolicitude()).isEqualTo(solicitudeBack);

        offer.solicitude(null);
        assertThat(offer.getSolicitude()).isNull();
    }
}
