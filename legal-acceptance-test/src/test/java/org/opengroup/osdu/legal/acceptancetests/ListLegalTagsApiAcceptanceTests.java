package org.opengroup.osdu.legal.acceptancetests;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opengroup.osdu.legal.util.AcceptanceBaseTest;
import org.opengroup.osdu.legal.util.LegalTagUtils;
import org.opengroup.osdu.legal.util.TestUtils;

import com.google.common.base.Strings;
import com.sun.jersey.api.client.ClientResponse;

public final class ListLegalTagsApiAcceptanceTests extends AcceptanceBaseTest {

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
    public void should_return200_and_allValidLegalTags_when_sendingValidTrueParamter_And_notSendingValidParameter()throws Exception{
        ClientResponse response = send("", 200, "?valid=true", TestUtils.getMyDataPartition());
        LegalTagUtils.ReadableLegalTags result = legalTagUtils.getResult(response, 200, LegalTagUtils.ReadableLegalTags.class);
        System.out.println("number of lts:" + result.legalTags.length ) ;
        assertTrue(result.legalTags.length > 0);
        assertFalse(Strings.isNullOrEmpty(result.legalTags[0].name));
        assertFalse(Strings.isNullOrEmpty(result.legalTags[0].properties.countryOfOrigin[0]));

        ClientResponse response2 = send("", 200, "");
        LegalTagUtils.ReadableLegalTags result2 = legalTagUtils.getResult(response2, 200, LegalTagUtils.ReadableLegalTags.class);
        for(LegalTagUtils.ReadableLegalTag tag : result.legalTags){
            assertTrue(Arrays.stream(result2.legalTags).anyMatch(s -> tag.name.equals(s.name)));
        }
    }

    @Test
    public void should_returnDifferentResults_when_sendingValidParamterTrueOrFalse()throws Exception{
        ClientResponse response = send("", 200, "?valid=true");
        LegalTagUtils.ReadableLegalTags result = legalTagUtils.getResult(response, 200, LegalTagUtils.ReadableLegalTags.class);

        ClientResponse response2 = send("", 200, "?valid=false");
        LegalTagUtils.ReadableLegalTags result2 = legalTagUtils.getResult(response2, 200, LegalTagUtils.ReadableLegalTags.class);
        assertNotEquals(result.legalTags.length, result2.legalTags.length);
        for(LegalTagUtils.ReadableLegalTag tag : result.legalTags){
            assertFalse(Arrays.stream(result2.legalTags).anyMatch(s -> tag.name.equals(s.name)));
        }
    }

    @Override
    protected String getBody(){
        return "";
    }

    @Override
    protected String getApi() {
        return "legaltags";
    }

    @Override
    protected String getHttpMethod() {
        return "GET";
    }
}
