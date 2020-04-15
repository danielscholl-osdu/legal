package org.opengroup.osdu.legal.tags;

import org.opengroup.osdu.legal.logging.AuditLogger;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.legal.provider.interfaces.ILegalTagPublisher;
import org.opengroup.osdu.legal.provider.interfaces.ILegalTagRepository;
import org.opengroup.osdu.legal.provider.interfaces.ILegalTagRepositoryFactory;
import org.opengroup.osdu.legal.tags.util.PersistenceExceptionToAppExceptionMapper;
import org.opengroup.osdu.legal.tags.dto.*;
import org.opengroup.osdu.core.common.model.legal.LegalTag;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class LegalTagServiceTests {
    private PersistenceExceptionToAppExceptionMapper mapper = new PersistenceExceptionToAppExceptionMapper();
    @Mock
    private LegalTagConstraintValidator validator;
    @Mock
    private ILegalTagRepositoryFactory reposMock;
    @Mock
    private ILegalTagRepository legalTagRepositoryMock;
    @Mock
    private AuditLogger auditLogger;
    @Mock
    private ILegalTagPublisher messagePublisherMock;
    @Mock
    private JaxRsDpsLog log;

    @InjectMocks
    private LegalTagService sut;

    @Before
    public void setup() {
        sut.exceptionMapper = mapper;
        when(validator.getErrors(any())).thenReturn(null);
    }

    @Test
    public void should_notCreateLegalTag_when_givenNullContractArg() {
        sut = createSut();

        LegalTagDto result = sut.create(null, "tenant");

        assertEquals(null, result);
    }

    @Test
    public void should_createLegalTag_when_givenContractWithWriteAccess() {
        sut = createSut();

        LegalTagDto legalTag = LegalTestUtils.createValidLegalTagDto("test-1");

        LegalTagDto result = sut.create(legalTag, "test");

        assertEquals(legalTag.getName(), result.getName());
        verify(validator, times(2)).isValidThrows(any());
    }

    @Test
    public void should_logAuditLog_when_LegalTagIsCreated() {
        LegalTag output = new LegalTag();
        output.setName("kind2");
        LegalTagDto legalTagDto = LegalTestUtils.createValidLegalTagDto("account-1");
        LegalTagService testService = createSut(output);

        testService.create(legalTagDto,"tenant2");

        verify(auditLogger).createdLegalTagSuccess(any());
    }

    @Test
    public void should_assignVersionAndId_when_creatingLegalTag() {
        sut = createSut();
        LegalTagDto legalTagDto = LegalTestUtils.createValidLegalTagDto("1");

        legalTagDto = sut.create(legalTagDto,"tenant");
        LegalTag legalTag = LegalTagDto.convertFrom(legalTagDto);
        assertEquals(-1306694706L, (long) legalTag.getId());
    }

    @Test
    public void should_setTenantNamePrefix_when_creatingLegalTagAndItIsNotAlreadyPrefixed() {
        LegalTag legalTag = LegalTestUtils.createValidLegalTag("mylegaltag");
        sut = createSut(legalTag);

        LegalTagDto legalTagDto = LegalTagDto.convertTo(legalTag);
        legalTagDto = sut.create(legalTagDto,"tenant1");

        assertEquals("tenant1-mylegaltag", legalTagDto.getName());
        assertEquals((Long) LegalTag.getDefaultId("tenant1-mylegaltag"), LegalTagDto.convertFrom(legalTagDto).getId());
    }

    @Test
    public void should_useTenantNameAsPrefix_and_ignoreAccountId() {
        LegalTag legalTag = LegalTestUtils.createValidLegalTag("mylegaltag");
        sut = createSut(legalTag);

        LegalTagDto legalTagDto = LegalTagDto.convertTo(legalTag);
        legalTagDto = sut.create(legalTagDto, "tenant1");

        assertEquals("tenant1-mylegaltag", legalTagDto.getName());
    }

    @Test
    public void should_setTenantNamePrefix_when_creatingLegalTagAndItIsAlreadyPrefixedButInDifferentCase() {
        sut = createSut();
        LegalTagDto legalTagDto = LegalTestUtils.createValidLegalTagDto("mylegaltag");
        legalTagDto.setName("Tenant1-" + legalTagDto.getName());

        legalTagDto = sut.create(legalTagDto,"tenant1");

        assertEquals("tenant1-Tenant1-mylegaltag", legalTagDto.getName());
        //verify isvalidthrows was called
        verify(validator, times(2)).isValidThrows(any());
    }

    @Test
    public void should_notSetTenantNamePrefix_when_creatingLegalTagAndItIsAlreadyPrefixed() {
        LegalTag legalTag = LegalTestUtils.createValidLegalTag("mylegaltag");
        legalTag.setName("tenant2-" + legalTag.getName());
        sut = createSut(legalTag);

        LegalTagDto dto = LegalTagDto.convertTo(legalTag);
        dto = sut.create(dto,"tenant2");

        assertEquals("tenant2-mylegaltag", dto.getName());
        assertEquals((Long) LegalTag.getDefaultId("tenant2-mylegaltag"), LegalTagDto.convertFrom(dto).getId());
    }

    @Test
    public void should_ReturnNull_When_GivenNullName() {
        sut = createSut();

        LegalTagDto result = sut.get(null, "tenant2");

        assertEquals(null, result);
    }

    @Test
    public void should_returnLegalTag_when_givenExistingName() {
        sut = createSut();

        LegalTagDto result = sut.get("mykind", "tenant2");

        assertEquals("kind2", result.getName());
    }

    @Test
    public void should_ReturnNull_When_GivenNameThatDoesNotExist() {
        sut = createSut(null);

        LegalTagDto result = sut.get("mykind","tenant1");

        assertEquals(null, result);
    }

    @Test
    public void should_returnFalse_When_GivenNullNameToDelete() {
        sut = createSut();

        Boolean result = sut.delete("project1", null, DpsHeaders.createFromMap(new HashMap<>()), "tenant1");

        assertFalse(result);
    }

    @Test
    public void should_returnFalse_When_GivenNullHeadersInDelete() {
        sut = createSut();

        Boolean result = sut.delete("project1", "name", null, "tenant");

        assertFalse(result);
    }

    @Test
    public void should_returnTrue_When_DeletingWithWriteAccessAndNoMatchingRecords() {
        sut = createSut();

        Boolean result = sut.delete("project1", "name", DpsHeaders.createFromMap(new HashMap<>()), "tenant2");

        assertTrue(result);
    }

    @Test
    public void should_returnTrue_When_DeletingLegalTagWhichDoesNotExist() {
        sut = createSut(null);

        Boolean result = sut.delete("project1", "name", DpsHeaders.createFromMap(new HashMap<>()), "tenant2");

        assertTrue(result);
    }

    @Test
    public void should_returnNull_when_updateIsGivenNullLegalTagOrHeaders() {
        sut = createSut();

        LegalTagDto result = sut.update(null, "tenant1");
        assertNull(result);

        result = sut.update(new UpdateLegalTag(), null);
        assertNull(result);
    }

    @Test
    public void should_returnNull_when_updateStatusIsGivenNullLegalTagOrHeaders() {
        sut = createSut();

        LegalTagDto result = sut.updateStatus(null, true,"tenant1");
        assertNull(result);

        result = sut.updateStatus("hi", true, null);
        assertNull(result);
    }

    @Test
    public void should_throwAppException400_when_legalTagDoesNotAlreadyExistOnUpdate() {
        sut = createSut(null);

        try {
            sut.update(LegalTestUtils.createUpdateLegalTag("name"), "tenant2");
            fail("Expected error");
        } catch (AppException ex) {
            assertEquals(404, ex.getError().getCode());
        }
    }

    @Test
    public void should_returnNullOnUpdate_when_repositoryReturnsNull() {
        UpdateLegalTag updateTag = LegalTestUtils.createUpdateLegalTag("name");
        LegalTag input = LegalTestUtils.createValidLegalTag("name");
        sut = createSut(input);
        when(legalTagRepositoryMock.update(input)).thenReturn(null);

        LegalTagDto result = sut.update(updateTag, "tenant2");
        assertNull(result);
    }

    @Test
    public void should_returnReadableLegalTag_when_updateSucceeds() {
        UpdateLegalTag updateTag = LegalTestUtils.createUpdateLegalTag("name");
        LegalTag input = LegalTestUtils.createValidLegalTag("name");
        sut = createSut(input);
        when(legalTagRepositoryMock.update(input)).thenReturn(input);

        LegalTagDto result = sut.update(updateTag, "tenant2");

        assertEquals(result.getName(), updateTag.getName());
        assertEquals(result.getProperties().getContractId(), updateTag.getContractId());
        assertEquals(result.getProperties().getExpirationDate(), updateTag.getExpirationDate());
        assertEquals(result.getDescription(), updateTag.getDescription());
        verify(validator, times(1)).isValidThrows(any());
    }

    @Test
    public void should_returnReadableLegalTag_when_updateStatusSucceeds() {
        LegalTag input = LegalTestUtils.createValidLegalTag("name");
        sut = createSut(input);
        when(legalTagRepositoryMock.update(input)).thenReturn(input);

        assertFalse(input.getIsValid());

        LegalTagDto result = sut.updateStatus("name", true, "tenant1");

        assertEquals("name", result.getName());
        assertTrue(input.getIsValid());
    }

    @Test
    public void should_logAuditLog_when_updateStatusSucceeds() {
        LegalTag input = LegalTestUtils.createValidLegalTag("name");
        DpsHeaders headers = DpsHeaders.createFromMap(new HashMap<>());
        headers.put("Correlation-Id", "legal.123");
        sut = createSut(input);

        when(legalTagRepositoryMock.update(input)).thenReturn(input);

        sut.updateStatus("name", true, "tenant2");

        verify(auditLogger).updatedLegalTagSuccess(any());
    }

    @Test
    public void should_returnNullCollection_when_givenNonExistingNames() {
        sut = createSut();
        String[] input = new String[]{"abc", "123"};

        when(legalTagRepositoryMock.get(any())).thenReturn(new ArrayList<>());
        LegalTagDtos result = sut.getBatch(input, "tenant1");
        assertEquals(0, result.getLegalTags().size());
    }

    @Test
    public void should_returnNullCollection_when_givenNullNames() {
        sut = createSut();
        LegalTagDtos result = sut.getBatch(null, "tenant2");
        assertEquals(null, result);
    }

    @Test
    public void should_returnLegalTagCollection_when_givenExistingLegalTags() {
        sut = createSut();

        String[] input = new String[]{"abc", "123"};
        LegalTagDtos result = sut.getBatch(input,"tenant2");
        assertEquals(2, result.getLegalTags().size());
        verify(auditLogger).readLegalTagSuccess(any());
    }

    @Test
    public void should_returnLegalTags_when_givenLegalTagsIsCurrentlyInvalid() {
        sut = createSut();
        //when(validator.getErrors(any())).thenReturn("Not valid");

        String[] input = new String[]{"abc", "123"};
        LegalTagDtos result = sut.getBatch(input, "tenant1");

        assertEquals(2, result.getLegalTags().size());
        for (LegalTagDto lt : result.getLegalTags()) {
            assertEquals("kind2", lt.getName());
        }
        verify(auditLogger).readLegalTagSuccess(any());
    }

    @Test
    public void should_returnLegalTag_when_legalTagsIsNotCurrentlyValid() {
        sut = createSut();
        //when(validator.getErrors(any())).thenReturn("Not valid");

        LegalTagDto result = sut.get("abc", "tenant1");

        assertEquals("kind2", result.getName());
    }

    @Test
    public void should_returnNull_when_givenNullNames() {
        sut = createSut();
        InvalidTagsWithReason result = sut.validate(null, "tenant1");
        assertEquals(0, result.getInvalidLegalTags().size());
    }

    @Test
    public void should_returnEmptyCollection_when_givenAllExistingAndValidNames() {
        sut = createSut();
        String[] input = new String[]{"kind2", "kind2"};
        InvalidTagsWithReason result = sut.validate(input, "tenant1");

        assertEquals(0, result.getInvalidLegalTags().size());
        verify(auditLogger).validateLegalTagSuccess();
    }

    @Test
    public void should_returnInvalidTagsWithNotFoundReason_when_givenNonexistingLegalTagNames() {
        sut = createSut();

        String[] input = new String[]{"kind2", "123"};
        InvalidTagsWithReason result = sut.validate(input, "tenant1");

        assertEquals(1, result.getInvalidLegalTags().size());
        for (InvalidTagWithReason invalidTagWithReason : result.getInvalidLegalTags()) {
            assertEquals("LegalTag not found", invalidTagWithReason.getReason());
        }
    }

    @Test
    public void should_returnAllNamesWithNotFound_when_givenAllNonexistingLegalTagNames() {
        sut = createSut();

        String[] input = new String[]{"abc", "123"};

        when(legalTagRepositoryMock.get(any())).thenReturn(new ArrayList<>());
        InvalidTagsWithReason result = sut.validate(input, "tenant1");

        assertEquals(2, result.getInvalidLegalTags().size());
        for (InvalidTagWithReason invalidTagWithReason : result.getInvalidLegalTags()) {
            assertEquals("LegalTag not found", invalidTagWithReason.getReason());
        }
    }

    @Test
    public void should_returnInvalidTagsWithNotValidReason_when_givenExistingLegalTagNames_which_areNotValid() {
        when(validator.getErrors(any())).thenReturn("Not valid");
        sut = createSut();

        String[] input = new String[]{"kind2", "kind2"};
        InvalidTagsWithReason result = sut.validate(input, "tenant1");

        assertEquals(2, result.getInvalidLegalTags().size());
        for (InvalidTagWithReason invalidTagWithReason : result.getInvalidLegalTags()) {
            assertEquals("Not valid", invalidTagWithReason.getReason());
        }
    }

    @Test
    public void should_returnValidTagsOnly_when_requestingOnlyValidTags() {
        sut = createSut();
        //when(validator.getErrors(any())).thenReturn("");
        LegalTagDtos result = sut.list(true, "tenant1");

        assertEquals(2, result.getLegalTags().size());
        verify(auditLogger).readLegalTagSuccess(any());
    }

    @Test
    public void should_sendDeletedLegalTagNameAndIncompliantStatusWithCorrelationIdToPubSub_when_legalTagIsDeleted() throws Exception {
        sut = createSut();

        Map<String, String> headers = new HashMap<>();
        headers.put(DpsHeaders.USER_EMAIL, "ash");
        headers.put(DpsHeaders.CORRELATION_ID, "123");
        headers.put(DpsHeaders.ACCOUNT_ID, "SIS-INTERNAL-HQ");

        DpsHeaders standardHeaders = DpsHeaders.createFromMap(headers);

        sut.delete("project1", "name", standardHeaders, "tenant1");
        verify(messagePublisherMock, times(1)).publish(any(), any(), any());
        verify(auditLogger).deletedLegalTagSuccess(any());
    }

    @Test
    public void should_notSendMessageToPubSub_when_deletedLegalTagDoesNotExist() throws Exception {
        sut = createSut(null);
        sut.delete("project1", "name", DpsHeaders.createFromMap(new HashMap<>()), "tenant1");

        verify(messagePublisherMock, never()).publish(any(), any(), any());
    }

    @Test
    public void should_notSendMessageToPubSub_when_GivenNullNameToDelete() throws Exception {
        sut = createSut();
        sut.delete("project1", null, DpsHeaders.createFromMap(new HashMap<>()), "tenant1");

        verify(messagePublisherMock, never()).publish(any(), any(), any());
    }

    private LegalTagService createSut() {
        LegalTag output = new LegalTag();
        output.setName("kind2");
        return createSut(output);
    }

    private LegalTagService createSut(LegalTag getOutput) {
        Answer<Long> answer = invocation -> getOutput.getId();
        when(legalTagRepositoryMock.create(any())).thenAnswer(answer);
        when(legalTagRepositoryMock.get(any())).thenReturn(Arrays.asList(getOutput, getOutput));
        when(legalTagRepositoryMock.list(any())).thenReturn(Arrays.asList(getOutput, getOutput));
        when(legalTagRepositoryMock.delete(any())).thenReturn(true);
        when(reposMock.get(any())).thenReturn(legalTagRepositoryMock);

        return sut;
    }
}
