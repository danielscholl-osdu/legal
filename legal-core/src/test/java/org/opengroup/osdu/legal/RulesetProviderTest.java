package org.opengroup.osdu.legal;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.RequestInfo;
import org.opengroup.osdu.core.common.model.legal.validation.rules.Rule;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.legal.tags.validation.rules.DefaultRule;
import org.opengroup.osdu.legal.tags.validation.rules.ExpiredContractRule;
import org.opengroup.osdu.legal.tags.validation.rules.UnownedDataRule;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RulesetProviderTest {


    @Mock
    private ExpiredContractRule expiredContractRule;

    @Mock
    private UnownedDataRule unownedDataRule;

    @Mock
    private DefaultRule defaultRule;

    @Mock
    private RequestInfo requestInfo;

    @InjectMocks
    private RulesetProvider sut;

    @Test
    public void shouldThrowRunTimeException_whenTenantInfoIsNull() {
        when(requestInfo.getTenantInfo()).thenReturn(null);
        try {
            sut.get();
            fail("Expected error");
        } catch (RuntimeException runtimeException) {
            assertEquals("No tenant info", runtimeException.getMessage());
        }
    }

    @Test
    public void shouldReturnRuleSet_whenTenantInfoComplianceRuleSetIsSHARED() {
        TenantInfo tenantInfo = new TenantInfo();
        tenantInfo.setComplianceRuleSet(TenantInfo.ComplianceRuleSets.SHARED);
        when(requestInfo.getTenantInfo()).thenReturn(tenantInfo);

        List<Rule> ruleList = sut.get();
        assertNotNull(ruleList);
        assertEquals(3, ruleList.size());
    }

    @Test
    public void shouldThrowAppException500_whenTenantInfoComplianceRuleSetIsNotSHARED() {
        TenantInfo tenantInfo = new TenantInfo();
        tenantInfo.setComplianceRuleSet(TenantInfo.ComplianceRuleSets.CUSTOMER);
        when(requestInfo.getTenantInfo()).thenReturn(tenantInfo);
        String errorMessage = "Unexpected ruleset requested " + TenantInfo.ComplianceRuleSets.CUSTOMER;

        try {
            List<Rule> ruleList = sut.get();
        } catch (AppException appException) {
            assertEquals(500, appException.getError().getCode());
            assertEquals(errorMessage, appException.getError().getMessage());
        }
    }

}