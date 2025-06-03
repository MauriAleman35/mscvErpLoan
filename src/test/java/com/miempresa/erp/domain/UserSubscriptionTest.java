package com.miempresa.erp.domain;

import static com.miempresa.erp.domain.SubscriptionPlanTestSamples.*;
import static com.miempresa.erp.domain.UserSubscriptionTestSamples.*;
import static com.miempresa.erp.domain.UserTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.miempresa.erp.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class UserSubscriptionTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(UserSubscription.class);
        UserSubscription userSubscription1 = getUserSubscriptionSample1();
        UserSubscription userSubscription2 = new UserSubscription();
        assertThat(userSubscription1).isNotEqualTo(userSubscription2);

        userSubscription2.setId(userSubscription1.getId());
        assertThat(userSubscription1).isEqualTo(userSubscription2);

        userSubscription2 = getUserSubscriptionSample2();
        assertThat(userSubscription1).isNotEqualTo(userSubscription2);
    }

    @Test
    void userTest() {
        UserSubscription userSubscription = getUserSubscriptionRandomSampleGenerator();
        User userBack = getUserRandomSampleGenerator();

        userSubscription.setUser(userBack);
        assertThat(userSubscription.getUser()).isEqualTo(userBack);

        userSubscription.user(null);
        assertThat(userSubscription.getUser()).isNull();
    }

    @Test
    void subscriptionPlanTest() {
        UserSubscription userSubscription = getUserSubscriptionRandomSampleGenerator();
        SubscriptionPlan subscriptionPlanBack = getSubscriptionPlanRandomSampleGenerator();

        userSubscription.setSubscriptionPlan(subscriptionPlanBack);
        assertThat(userSubscription.getSubscriptionPlan()).isEqualTo(subscriptionPlanBack);

        userSubscription.subscriptionPlan(null);
        assertThat(userSubscription.getSubscriptionPlan()).isNull();
    }
}
