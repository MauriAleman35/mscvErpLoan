package com.miempresa.erp.domain;

import static com.miempresa.erp.domain.SolicitudeTestSamples.*;
import static com.miempresa.erp.domain.UserTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.miempresa.erp.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class SolicitudeTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Solicitude.class);
        Solicitude solicitude1 = getSolicitudeSample1();
        Solicitude solicitude2 = new Solicitude();
        assertThat(solicitude1).isNotEqualTo(solicitude2);

        solicitude2.setId(solicitude1.getId());
        assertThat(solicitude1).isEqualTo(solicitude2);

        solicitude2 = getSolicitudeSample2();
        assertThat(solicitude1).isNotEqualTo(solicitude2);
    }

    @Test
    void borrowerTest() {
        Solicitude solicitude = getSolicitudeRandomSampleGenerator();
        User userBack = getUserRandomSampleGenerator();

        solicitude.setBorrower(userBack);
        assertThat(solicitude.getBorrower()).isEqualTo(userBack);

        solicitude.borrower(null);
        assertThat(solicitude.getBorrower()).isNull();
    }
}
