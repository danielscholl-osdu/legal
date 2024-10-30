package org.opengroup.osdu.legal.acceptancetests;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opengroup.osdu.legal.util.AcceptanceBaseTest;
import org.opengroup.osdu.legal.util.LegalTagUtils;
import org.opengroup.osdu.legal.util.TokenLegalTagUtils;

public final class DeleteLegalTagApiAcceptanceTests extends AcceptanceBaseTest {

    private String name;
    
    @BeforeEach
    @Override
    public void setup() throws Exception {
        this.legalTagUtils = new TokenLegalTagUtils();
        super.setup();
    }

    @AfterEach
    @Override
    public void teardown() throws Exception {
        super.teardown();
        this.legalTagUtils = null;
    }

    @Test
    public void should_return204_when_deletingAContractThatDoesNotExist() throws Exception{
        name = LegalTagUtils.createRandomNameTenant();
        validateAccess(204);
    }

    @Test
    public void should_return204_when_deletingAContractThatDoesExist() throws Exception{
        name = LegalTagUtils.createRandomNameTenant();
        legalTagUtils.getResult(legalTagUtils.create(name), 201, String.class );
        validateAccess(204);
    }

    @Test
    public void should_return400_when_deletingAContractWithAnInvalidName() throws Exception{
        name = "invalid*name";
        send("", 400);
    }

    @Override
    protected String getApi() {
        return "legaltags/" + name;
    }

    @Override
    protected String getHttpMethod() {
        return "DELETE";
    }
}
