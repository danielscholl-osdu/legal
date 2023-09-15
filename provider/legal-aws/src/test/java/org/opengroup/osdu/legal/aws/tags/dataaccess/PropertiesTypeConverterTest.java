// package org.opengroup.osdu.legal.aws.tags.dataaccess;

// import com.fasterxml.jackson.core.JsonParseException;
// import com.fasterxml.jackson.core.JsonProcessingException;
// import com.fasterxml.jackson.core.type.TypeReference;
// import com.fasterxml.jackson.databind.JsonMappingException;
// import com.fasterxml.jackson.databind.ObjectMapper;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;

// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.ArgumentMatchers.anyString;
// import static org.mockito.ArgumentMatchers.eq;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.when;
// import static org.mockito.MockitoAnnotations.openMocks;

// import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
// import org.opengroup.osdu.core.common.model.legal.Properties;

// class PropertiesTypeConverterTest {
//     @InjectMocks
//     private PropertiesTypeConverter converter;

//     @Mock
//     private Properties properties;

//     @Mock
//     private ObjectMapper objectMapper;

//     @Mock
//     private JaxRsDpsLog logger;

//     private String jsonString = "{\"originator\":\"John\",\"contractId\":\"ABC123\"}";

//     @BeforeEach
//     void setUp() {
//         openMocks(this);
//         when(properties.getOriginator()).thenReturn("John");
//         when(properties.getContractId()).thenReturn("ABC123");
//     }

//     @Test
//     void testConvert() throws JsonProcessingException {
//         when(objectMapper.writeValueAsString(properties)).thenReturn(jsonString);
//         String convertedString = converter.convert(properties);
//         assertEquals(jsonString, convertedString);
//     }

//     @Test
//     void testConvertThrowsException() throws JsonProcessingException {
//         when(objectMapper.writeValueAsString(properties)).thenThrow(JsonProcessingException.class);
//         String convertedString = converter.convert(properties);
//         verify(logger).error(anyString());
//         assertEquals("", convertedString);
//     }

//     @Test
//     void testUnconvert() throws JsonMappingException, JsonProcessingException {
//         when(objectMapper.readValue(eq(jsonString), any(TypeReference.class))).thenReturn(properties);
//         Properties unconvertedProperties = converter.unconvert(jsonString);
//         assertEquals(properties, unconvertedProperties);
//     }

//     @Test
//     void testUnconvertThrowsJsonParseException() throws JsonParseException, JsonProcessingException {
//         when(objectMapper.readValue(eq(jsonString), any(TypeReference.class))).thenThrow(JsonParseException.class);
//         Properties unconvertedProperties = converter.unconvert(jsonString);
//         verify(logger).error(anyString());
//         assertEquals(null, unconvertedProperties);
//     }

//     @Test
//     void testUnconvertThrowsJsonMappingException() throws JsonMappingException, JsonProcessingException {
//         when(objectMapper.readValue(eq(jsonString), any(TypeReference.class))).thenThrow(JsonMappingException.class);
//         Properties unconvertedProperties = converter.unconvert(jsonString);
//         verify(logger).error(anyString());
//         assertEquals(null, unconvertedProperties);
//     }
//     @Test
//     void testUnconvertThrowsJsonProcessingException() throws JsonMappingException, JsonProcessingException {
//         when(objectMapper.readValue(eq(jsonString), any(TypeReference.class))).thenThrow(JsonProcessingException.class);
//         Properties unconvertedProperties = converter.unconvert(jsonString);
//         verify(logger).error(anyString());
//         assertEquals(null, unconvertedProperties);
//     }
// }
