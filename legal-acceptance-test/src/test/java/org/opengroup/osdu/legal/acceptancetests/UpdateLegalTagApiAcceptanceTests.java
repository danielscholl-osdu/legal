package org.opengroup.osdu.legal.acceptancetests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.opengroup.osdu.legal.util.Constants.DATA_PARTITION_ID;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opengroup.osdu.legal.util.AcceptanceBaseTest;
import org.opengroup.osdu.legal.util.LegalTagUtils;
import org.opengroup.osdu.legal.util.TestUtils;

import com.sun.jersey.api.client.ClientResponse;

public final class UpdateLegalTagApiAcceptanceTests extends AcceptanceBaseTest {

    static private String defaultName = LegalTagUtils.createRandomNameTenant();
    private String name;
    private String expDate;

    @BeforeEach
    @Override
    public void setup() throws Exception {
        this.legalTagUtils = new LegalTagUtils();
        super.setup();
        legalTagUtils.create(defaultName);
        name = defaultName;
        expDate = "2199-12-25";
    }

    @AfterEach
    @Override
    public void teardown() throws Exception{
        legalTagUtils.delete(defaultName);
        super.teardown();
        this.legalTagUtils = null;
    }

    @Test
    public void should_returnOk_and_updateProperties_when_userUpdatesExistingLegalTags() throws Exception{
        Map<String, String> headers = new HashMap<>();
        headers.put(DATA_PARTITION_ID, TestUtils.getMyDataPartition());
        ClientResponse response = legalTagUtils.send(this.getApi(), this.getHttpMethod(), legalTagUtils.accessToken(), getBody(), getQuery(), headers);
        LegalTagUtils.ReadableLegalTag result = legalTagUtils.getResult(response, 200, LegalTagUtils.ReadableLegalTag.class);

        assertEquals("B1234", result.properties.contractId);
        assertEquals("2199-12-25", result.properties.expirationDate);
        assertEquals(name, result.name);
    }
    @Test
    public void should_return400_when_userHasGivenInvalidExpDate() throws Exception{
        expDate = "2010-12-31"; //set expired date
        ClientResponse response = legalTagUtils.send(this.getApi(), this.getHttpMethod(), legalTagUtils.accessToken(), getBody(), getQuery());
        String error = legalTagUtils.getResult(response, 400, String.class);
        assertEquals("{\"code\":400,\"reason\":\"Validation error.\",\"message\":\"{\\\"errors\\\":[\\\"Expiration date must be a value in the future. Given 2010-12-31\\\"]}\"}", error);
    }

    @Test
    public void should_return400_when_givenEmptyBody() throws Exception{
        ClientResponse response = legalTagUtils.send(this.getApi(), this.getHttpMethod(), legalTagUtils.accessToken(), "{}", getQuery());
        assertEquals(400, response.getStatus());
    }

    @Test
    public void should_return404_when_givenLegalTagToUpdateThatDoesNotExist() throws Exception{
        name = LegalTagUtils.createRandomNameTenant();
        ClientResponse response = legalTagUtils.send(this.getApi(), this.getHttpMethod(), legalTagUtils.accessToken(), getBody(), getQuery());
        assertEquals(404, response.getStatus());
    }

    @Override
    protected String getBody(){
        return LegalTagUtils.updateBody(name, expDate);
    }

    @Override
    protected String getApi() {
        return "legaltags";
    }

    @Override
    protected String getHttpMethod() {
        return "PUT";
    }
}
