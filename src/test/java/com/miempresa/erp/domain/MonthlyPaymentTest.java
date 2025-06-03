package com.miempresa.erp.domain;

import static com.miempresa.erp.domain.LoanTestSamples.*;
import static com.miempresa.erp.domain.MonthlyPaymentTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.miempresa.erp.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class MonthlyPaymentTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(MonthlyPayment.class);
        MonthlyPayment monthlyPayment1 = getMonthlyPaymentSample1();
        MonthlyPayment monthlyPayment2 = new MonthlyPayment();
        assertThat(monthlyPayment1).isNotEqualTo(monthlyPayment2);

        monthlyPayment2.setId(monthlyPayment1.getId());
        assertThat(monthlyPayment1).isEqualTo(monthlyPayment2);

        monthlyPayment2 = getMonthlyPaymentSample2();
        assertThat(monthlyPayment1).isNotEqualTo(monthlyPayment2);
    }

    @Test
    void loanTest() {
        MonthlyPayment monthlyPayment = getMonthlyPaymentRandomSampleGenerator();
        Loan loanBack = getLoanRandomSampleGenerator();

        monthlyPayment.setLoan(loanBack);
        assertThat(monthlyPayment.getLoan()).isEqualTo(loanBack);

        monthlyPayment.loan(null);
        assertThat(monthlyPayment.getLoan()).isNull();
    }
}
