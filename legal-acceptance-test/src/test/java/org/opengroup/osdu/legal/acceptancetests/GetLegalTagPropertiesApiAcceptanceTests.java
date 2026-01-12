package org.opengroup.osdu.legal.acceptancetests;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opengroup.osdu.legal.util.AcceptanceBaseTest;
import org.opengroup.osdu.legal.util.LegalTagUtils;

import com.sun.jersey.api.client.ClientResponse;

public final class GetLegalTagPropertiesApiAcceptanceTests extends AcceptanceBaseTest {

	@BeforeEach
    @Override
    public void setup() throws Exception {
        this.legalTagUtils = new LegalTagUtils();
        super.setup();
    }

    @AfterEach
    @Override
    public void teardown() throws Exception {
        super.teardown();
        this.legalTagUtils = null;
    }
    
    @Test
    public void should_returnAllLegalTagProperties_when_getPropertiesApi() throws Exception {
        ClientResponse response = send("", 200);
        LegalTagUtils.ReadablePropertyValues result = legalTagUtils.getResult(response, 200,
                LegalTagUtils.ReadablePropertyValues.class);

        System.out.println(result);
        assertTrue(result.countriesOfOrigin.size() > 0);
        assertTrue(result.countriesOfOrigin.containsKey("US"));

        assertTrue(result.otherRelevantDataCountries.size() > 0);
        assertTrue(result.otherRelevantDataCountries.containsKey("FR"));

        assertTrue(result.personalDataTypes.size() > 0);
        assertTrue(result.personalDataTypes.contains("No Personal Data"));

        assertTrue(result.securityClassifications.size() > 0);
        assertTrue(result.securityClassifications.contains("Private"));

        assertTrue(result.exportClassificationControlNumbers.size() > 0);
        assertTrue(result.exportClassificationControlNumbers.contains("EAR99"));

        assertTrue(result.dataTypes.size() > 0);
        assertTrue(result.dataTypes.contains("First Party Data"));
    }

    @Override
    protected String getBody(){
        return "";
    }
    @Override
    protected String getApi() {
        return "legaltags:properties";
    }
    @Override
    protected String getHttpMethod() {
        return "GET";
    }
}
