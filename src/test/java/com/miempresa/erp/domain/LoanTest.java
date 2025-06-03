package com.miempresa.erp.domain;

import static com.miempresa.erp.domain.LoanTestSamples.*;
import static com.miempresa.erp.domain.OfferTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.miempresa.erp.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class LoanTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Loan.class);
        Loan loan1 = getLoanSample1();
        Loan loan2 = new Loan();
        assertThat(loan1).isNotEqualTo(loan2);

        loan2.setId(loan1.getId());
        assertThat(loan1).isEqualTo(loan2);

        loan2 = getLoanSample2();
        assertThat(loan1).isNotEqualTo(loan2);
    }

    @Test
    void offerTest() {
        Loan loan = getLoanRandomSampleGenerator();
        Offer offerBack = getOfferRandomSampleGenerator();

        loan.setOffer(offerBack);
        assertThat(loan.getOffer()).isEqualTo(offerBack);

        loan.offer(null);
        assertThat(loan.getOffer()).isNull();
    }
}
