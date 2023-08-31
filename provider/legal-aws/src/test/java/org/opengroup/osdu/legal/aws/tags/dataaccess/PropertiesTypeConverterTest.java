package org.opengroup.osdu.legal.aws.tags.dataaccess;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import org.opengroup.osdu.core.common.model.legal.Properties;

class PropertiesTypeConverterTest {

    private PropertiesTypeConverter converter;

    private Properties properties;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        converter = new PropertiesTypeConverter();
        properties = new Properties();
        properties.setOriginator("John");
        properties.setContractId("ABC123");
        objectMapper = new ObjectMapper();
    }

    @Test
    void testConvert() throws JsonProcessingException {
        String converted = converter.convert(properties);
        String expectedString = objectMapper.writeValueAsString(properties);

        // Ensure the converted string is as expected.
        assertEquals(expectedString, converted);
    }

    @Test
    void testUnconvert() throws IOException {
        // Initialize other fields as needed
        String jsonString = objectMapper.writeValueAsString(properties);
        Properties unconvertedProperties = converter.unconvert(jsonString);

        // Ensure the unconverted Properties object is as expected
        assertEquals(properties, unconvertedProperties);
    }
}
