package com.miempresa.erp.domain;

import static com.miempresa.erp.domain.DocumentTestSamples.*;
import static com.miempresa.erp.domain.UserTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.miempresa.erp.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class DocumentTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Document.class);
        Document document1 = getDocumentSample1();
        Document document2 = new Document();
        assertThat(document1).isNotEqualTo(document2);

        document2.setId(document1.getId());
        assertThat(document1).isEqualTo(document2);

        document2 = getDocumentSample2();
        assertThat(document1).isNotEqualTo(document2);
    }

    @Test
    void userTest() {
        Document document = getDocumentRandomSampleGenerator();
        User userBack = getUserRandomSampleGenerator();

        document.setUser(userBack);
        assertThat(document.getUser()).isEqualTo(userBack);

        document.user(null);
        assertThat(document.getUser()).isNull();
    }
}
