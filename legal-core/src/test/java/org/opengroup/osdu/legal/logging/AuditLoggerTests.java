package org.opengroup.osdu.legal.logging;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;

@RunWith(MockitoJUnitRunner.class)
public class AuditLoggerTests {

    private static final String TEST_USER = "test@example.com";
    private static final String TEST_IP = "192.168.1.100";
    private static final String TEST_USER_AGENT = "TestAgent/1.0";
    private static final String TEST_AUTHORIZED_GROUP = "users.datalake.viewers";
    private static final List<String> TEST_REQUIRED_GROUPS = Arrays.asList("users.datalake.editors", "service.legal.admin");

    @Mock
    private JaxRsDpsLog log;

    @Mock
    private DpsHeaders headers;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private AuditLogger sut;

    private List<String> resources;
    private AuditEvents auditEvents;

    @Before
    public void setup() {
        resources = Collections.singletonList("1");

        // Create reference AuditEvents with same values mocks will produce
        auditEvents = new AuditEvents(TEST_USER, TEST_IP, TEST_USER_AGENT, TEST_AUTHORIZED_GROUP);

        when(headers.getUserEmail()).thenReturn(TEST_USER);
        when(headers.getUserAuthorizedGroupName()).thenReturn(TEST_AUTHORIZED_GROUP);
        when(httpServletRequest.getRemoteAddr()).thenReturn(TEST_IP);
        when(httpServletRequest.getHeader("User-Agent")).thenReturn(TEST_USER_AGENT);
    }

    @Test
    public void should_writeLegaltagCreatedEventSuccess() {
        sut.createdLegalTagSuccess(resources, TEST_REQUIRED_GROUPS);

        verify(log).audit(auditEvents.getCreateLegalTagEventSuccess(resources, TEST_REQUIRED_GROUPS));
    }

    @Test
    public void should_writeLegaltagDeleteSuccessEvent() {
        sut.deletedLegalTagSuccess(resources, TEST_REQUIRED_GROUPS);

        verify(log).audit(auditEvents.getDeleteLegalTagEventSuccess(resources, TEST_REQUIRED_GROUPS));
    }

    @Test
    public void should_writeLegaltagJobRunSuccessEvent() {
        sut.legalTagJobRanSuccess(resources, TEST_REQUIRED_GROUPS);

        verify(log).audit(auditEvents.getLegalTagStatusJobEventSuccess(resources, TEST_REQUIRED_GROUPS));
    }

    @Test
    public void should_writeLegalTagJobRunFailEvent() {
        sut.legalTagJobRanFail(resources, TEST_REQUIRED_GROUPS);

        verify(log).audit(auditEvents.getLegalTagStatusJobEventFail(resources, TEST_REQUIRED_GROUPS));
    }

    @Test
    public void should_writeLegaltagPublishedStatusSuccessEvent() {
        sut.publishedStatusChangeSuccess(resources, TEST_REQUIRED_GROUPS);

        verify(log).audit(auditEvents.getPublishStatusEventSuccess(resources, TEST_REQUIRED_GROUPS));
    }

    @Test
    public void should_writeLegalTagReadPropertiesSuccessEvent() {
        sut.readLegalPropertiesSuccess(resources, TEST_REQUIRED_GROUPS);

        verify(log).audit(auditEvents.getReadLegalPropertiesEventSuccess(resources, TEST_REQUIRED_GROUPS));
    }

    @Test
    public void should_writeLegalTagReadPropertiesFailEvent() {
        sut.readLegalPropertiesFail(resources, TEST_REQUIRED_GROUPS);

        verify(log).audit(auditEvents.getReadLegalPropertiesEventFail(resources, TEST_REQUIRED_GROUPS));
    }

    @Test
    public void should_writeLegalTagUpdateSuccessEvent() {
        sut.updatedLegalTagSuccess(resources, TEST_REQUIRED_GROUPS);

        verify(log).audit(auditEvents.getUpdateLegalTagEventSuccess(resources, TEST_REQUIRED_GROUPS));
    }

    @Test
    public void should_writeLegalTagUpdateFailEvent() {
        sut.updatedLegalTagFail(resources, TEST_REQUIRED_GROUPS);

        verify(log).audit(auditEvents.getUpdateLegalTagEventFail(resources, TEST_REQUIRED_GROUPS));
    }

    @Test
    public void should_writeLegalTagReadSuccessEvent() {
        sut.readLegalTagSuccess(resources, TEST_REQUIRED_GROUPS);

        verify(log).audit(auditEvents.getReadLegalTagEventSuccess(resources, TEST_REQUIRED_GROUPS));
    }

    @Test
    public void should_writeLegalTagReadFailEvent() {
        sut.readLegalTagFail(resources, TEST_REQUIRED_GROUPS);

        verify(log).audit(auditEvents.getReadLegalTagEventFail(resources, TEST_REQUIRED_GROUPS));
    }

    @Test
    public void should_writeLegalTagValidateSuccessEvent() {
        sut.validateLegalTagSuccess(TEST_REQUIRED_GROUPS);

        verify(log).audit(auditEvents.getValidateLegalTagEventSuccess(TEST_REQUIRED_GROUPS));
    }

    @Test
    public void should_writeLegalTagValidateFailEvent() {
        sut.validateLegalTagFail(TEST_REQUIRED_GROUPS);

        verify(log).audit(auditEvents.getValidateLegalTagEventFail(TEST_REQUIRED_GROUPS));
    }

    @Test
    public void should_writeLegalTag_whenLegalTagsBackup() {
        sut.legalTagsBackup("tenant", TEST_REQUIRED_GROUPS);

        verify(log).audit(auditEvents.getLegalTagBackupEvent("tenant", TEST_REQUIRED_GROUPS));
    }

    @Test
    public void should_writeLegalTag_whenLegalTagRestored() {
        sut.legalTagRestored("tenant", TEST_REQUIRED_GROUPS);

        verify(log).audit(auditEvents.getLegalTagRestoreEvent("tenant", TEST_REQUIRED_GROUPS));
    }

    @Test
    public void should_useUnknownFallback_whenUserIsEmpty() {
        when(headers.getUserEmail()).thenReturn("");

        // Should not throw - uses "unknown" fallback for user
        sut.validateLegalTagSuccess(TEST_REQUIRED_GROUPS);

        // Verify audit was logged with fallback values
        AuditEvents fallbackEvents = new AuditEvents("", TEST_IP, TEST_USER_AGENT, TEST_AUTHORIZED_GROUP);
        verify(log).audit(fallbackEvents.getValidateLegalTagEventSuccess(TEST_REQUIRED_GROUPS));
    }
}
