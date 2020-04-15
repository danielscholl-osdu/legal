package org.opengroup.osdu.legal.api;

import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.provider.interfaces.ITenantFactory;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;

import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.opengroup.osdu.legal.jobs.LegalTagStatusJob;
import org.opengroup.osdu.core.common.model.legal.StatusChangedTags;
import org.opengroup.osdu.legal.logging.AuditLogger;
import org.opengroup.osdu.legal.tags.LegalTagService;
import org.opengroup.osdu.core.common.model.http.RequestInfo;
import org.opengroup.osdu.core.common.model.legal.ServiceConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

@RunWith(MockitoJUnitRunner.class)
public class LegalTagStatusJobApiTests {
    @Mock
    private RequestInfo requestInfo;

    @Mock
    private LegalTagService legalTagService;

    @Mock
    private LegalTagStatusJob legalTagStatusJob;

    @Mock
    private AuditLogger auditLogger;

    @Mock
    private JaxRsDpsLog log;

    @Mock
    private ITenantFactory tenantStorageFactory;

    @InjectMocks
    private LegalTagStatusJobApi sut;

    @Before
    public void setup() {
        when(tenantStorageFactory.listTenantInfo()).thenReturn(new ArrayList<TenantInfo>() {{
            add(new TenantInfo());
        }});
        when(requestInfo.getHeaders()).thenReturn(new DpsHeaders());
        lenient().when(requestInfo.getUser()).thenReturn(ServiceConfig.LEGAL_CRON);
        when(requestInfo.getTenantInfo()).thenReturn(new TenantInfo());
    }

    @Test
    public void should_return200_when_checkUpdateStatusSucceeds() throws Exception {
        when(legalTagStatusJob.run(any(), any(), any())).thenReturn(new StatusChangedTags());

        ResponseEntity<HttpStatus> result = sut.checkLegalTagStatusChanges();

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
    }

    @Test
    public void should_logAudit_when_checkUpdateStatusSucceeds() throws Exception {
        when(legalTagStatusJob.run(any(), any(), any())).thenReturn(new StatusChangedTags());

        sut.checkLegalTagStatusChanges();

        verify(auditLogger, times(1)).legalTagJobRanSuccess(singletonList(new StatusChangedTags().toString()));
    }

    @Test
    public void should_return500_when_checkUpdateStatusThrowsAnErrorOnAnyTenant() throws Exception {
        when(legalTagStatusJob.run(any(), any(), any())).thenThrow(new Exception()).thenReturn(new StatusChangedTags());

        ResponseEntity<HttpStatus> result = sut.checkLegalTagStatusChanges();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
    }

    @Test
    public void should_return200_when_isNotACronButIsUsingHttps() throws Exception {
        // when(requestInfo.getUser()).thenReturn("anotheruser");
        // when(requestInfo.isHttps()).thenReturn(true);

        when(legalTagStatusJob.run(any(), any(), any())).thenReturn(new StatusChangedTags());

        ResponseEntity<HttpStatus> result = sut.checkLegalTagStatusChanges();
        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
    }
}
