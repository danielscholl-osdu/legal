package org.opengroup.osdu.legal.aws.tags.dataaccess;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.opengroup.osdu.core.common.model.legal.Properties;

class PropertiesTypeConverterTest {

    private PropertiesTypeConverter converter = new PropertiesTypeConverter();

    private static MockedConstruction<Properties> propertiesConstructor;

    private Properties properties = new Properties();

    private String jsonString = "{\"originator\":\"John\",\"contractId\":\"ABC123\"}";

    @BeforeAll
    static void setUp() {
        propertiesConstructor = Mockito.mockConstruction(Properties.class, (mock, context) -> {
            when(mock.getOriginator()).thenReturn("John");
            when(mock.getContractId()).thenReturn("ABC123");
        });
    }

    @Test
    void testConvert() throws JsonProcessingException {
        try (MockedConstruction<ObjectMapper> mocked = Mockito.mockConstruction(ObjectMapper.class, (mock, context) -> {
           when(mock.writeValueAsString(Mockito.any())).thenReturn(jsonString);
        })) {
            String convertedString = converter.convert(properties);
            assertEquals(jsonString, convertedString);
        }
    }

    @Test
    void testConvertThrowsException() throws JsonProcessingException {
        try (MockedConstruction<ObjectMapper> mocked = Mockito.mockConstruction(ObjectMapper.class, (mock, context) -> {
           when(mock.writeValueAsString(properties)).thenThrow(JsonProcessingException.class);
        })) {
            String convertedString = converter.convert(properties);
            assertEquals("", convertedString);
        }
    }

    @Test
    void testUnconvert() throws JsonMappingException, JsonProcessingException {
        try (MockedConstruction<ObjectMapper> mocked = Mockito.mockConstruction(ObjectMapper.class, (mock, context) -> {
           when(mock.readValue(eq(jsonString), any(TypeReference.class))).thenReturn(properties);
        })) {
            Properties unconvertedProperties = converter.unconvert(jsonString);
            assertEquals(properties, unconvertedProperties);
        }
    }

    @Test
    void testUnconvertThrowsJsonParseException() throws JsonParseException, JsonProcessingException {
        try (MockedConstruction<ObjectMapper> mocked = Mockito.mockConstruction(ObjectMapper.class, (mock, context) -> {
           when(mock.readValue(eq(jsonString), any(TypeReference.class))).thenThrow(JsonParseException.class);
        })) {
            Properties unconvertedProperties = converter.unconvert(jsonString);
            assertEquals("John", unconvertedProperties.getOriginator());
            assertEquals("ABC123", unconvertedProperties.getContractId());   
        }
    }
    
    @AfterAll
    static void tearDown() {
        propertiesConstructor.close();
    }

}
