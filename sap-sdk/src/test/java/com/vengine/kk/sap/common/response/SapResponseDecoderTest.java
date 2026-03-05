package com.vengine.kk.sap.common.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vengine.kk.sap.common.exception.SapClientException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class SapResponseDecoderTest {

    private SapResponseDecoder decoder;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class TestEntity {
        private String id;
        private String name;
    }

    @BeforeEach
    void setUp() {
        decoder = new SapResponseDecoder(new ObjectMapper());
    }

    // ── decode (single entity) ──────────────────────────────────────────────

    @Test
    void decodeSingleEntity() {
        String json = """
                {"d": {"id": "123", "name": "Test"}}
                """;
        TestEntity result = decoder.decode(json, TestEntity.class);
        assertThat(result.getId()).isEqualTo("123");
        assertThat(result.getName()).isEqualTo("Test");
    }

    @Test
    void decodeMissingDNodeThrows() {
        String json = """
                {"results": {"id": "123"}}
                """;
        assertThatThrownBy(() -> decoder.decode(json, TestEntity.class))
                .isInstanceOf(SapClientException.class)
                .hasMessageContaining("missing 'd' envelope");
    }

    @Test
    void decodeMalformedJsonThrows() {
        assertThatThrownBy(() -> decoder.decode("{not valid json", TestEntity.class))
                .isInstanceOf(SapClientException.class)
                .hasMessageContaining("Failed to decode SAP response");
    }

    // ── decodeList ──────────────────────────────────────────────────────────

    @Test
    void decodeListStandardArray() {
        String json = """
                {"d": {"results": [{"id": "1", "name": "A"}, {"id": "2", "name": "B"}]}}
                """;
        List<TestEntity> result = decoder.decodeList(json, TestEntity.class);
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo("1");
        assertThat(result.get(1).getName()).isEqualTo("B");
    }

    @Test
    void decodeListArrayConverterQuirkObjectInsteadOfArray() {
        String json = """
                {"d": {"results": {"id": "42", "name": "Quirk"}}}
                """;
        List<TestEntity> result = decoder.decodeList(json, TestEntity.class);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("42");
        assertThat(result.get(0).getName()).isEqualTo("Quirk");
    }

    @Test
    void decodeListMissingResultsThrows() {
        String json = """
                {"d": {"items": []}}
                """;
        assertThatThrownBy(() -> decoder.decodeList(json, TestEntity.class))
                .isInstanceOf(SapClientException.class)
                .hasMessageContaining("missing 'd.results' envelope");
    }

    @Test
    void decodeListEmptyArray() {
        String json = """
                {"d": {"results": []}}
                """;
        List<TestEntity> result = decoder.decodeList(json, TestEntity.class);
        assertThat(result).isEmpty();
    }

    @Test
    void decodeListMalformedJsonThrows() {
        assertThatThrownBy(() -> decoder.decodeList("{{bad", TestEntity.class))
                .isInstanceOf(SapClientException.class)
                .hasMessageContaining("Failed to decode SAP response");
    }
}
