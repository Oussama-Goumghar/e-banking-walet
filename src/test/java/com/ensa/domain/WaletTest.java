package com.ensa.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.ensa.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class WaletTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Walet.class);
        Walet walet1 = new Walet();
        walet1.setId(1L);
        Walet walet2 = new Walet();
        walet2.setId(walet1.getId());
        assertThat(walet1).isEqualTo(walet2);
        walet2.setId(2L);
        assertThat(walet1).isNotEqualTo(walet2);
        walet1.setId(null);
        assertThat(walet1).isNotEqualTo(walet2);
    }
}
