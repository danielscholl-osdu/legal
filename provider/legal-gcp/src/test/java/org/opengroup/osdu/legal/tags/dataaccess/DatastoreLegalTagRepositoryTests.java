package org.opengroup.osdu.legal.tags.dataaccess;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;
import com.google.common.collect.Iterables;
import org.opengroup.osdu.core.common.model.legal.ListLegalTagArgs;
import org.opengroup.osdu.core.common.model.legal.PersistenceException;
import org.opengroup.osdu.legal.tags.LegalTestUtils;
import org.opengroup.osdu.core.common.model.legal.LegalTag;

import org.opengroup.osdu.core.common.model.http.AppException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Datastore.class, KeyFactory.class, Key.class})
public class DatastoreLegalTagRepositoryTests {
    @Mock
    private Datastore datastore;
    @Mock
    private KeyFactory keyFactory;

    static final Date now = new Date();
    Date modified;

    @Before
    public void setup() {
        modified = new Date();
        MockitoAnnotations.initMocks(this);

        when(this.datastore.newKeyFactory()).thenReturn(keyFactory);
    }

    @Test
    public void should_NotPersistContracts_When_GivenNullContract(){
        DatastoreLegalTagRepository sut = new DatastoreLegalTagRepository(datastore);

        Long id = sut.create(null);

        assertEquals(-1L, (long)id);
    }

    @Test
    public void should_PersistContracts_And_ReturnNewId_When_GivenValidContract(){
        DatastoreLegalTagRepository sut = new DatastoreLegalTagRepository(datastore);

        Key key = mockKey(1L);
        mockGet(false, key, now);//make sure a legalTag does not exist yet

        Key key1 = mockKey(10L);
        List<Key> mockKeys = new ArrayList<Key>(){{add(key1);}};
        Transaction txn = mockTransaction(mockKeys, false);

        LegalTag legalTag = LegalTestUtils.createValidLegalTag("name");
        when(this.keyFactory.setKind(anyString())).thenReturn(this.keyFactory);
        when(this.keyFactory.newKey(anyLong())).thenReturn(key1);
        Long id = sut.create(legalTag);

        verify(txn, times(1)).put(isA(FullEntity.class));
        verify(txn, times(1)).commit();
        assertEquals(10L, (long)id);
    }

    @Test
    public void should_RollbackTransaction_When_SavingContractsThrowsAnException(){
        DatastoreLegalTagRepository sut = new DatastoreLegalTagRepository(datastore);

        LegalTag legalTag = LegalTestUtils.createValidLegalTag("1");
        Key key = mockKey(1L);
        mockGet(false, key, now);

        Key key1 = mockKey(1L);
        List<Key> mockKeys = new ArrayList<Key>(){{add(key1);}};
        Transaction txn = mockTransaction(mockKeys, true);

        doThrow(new DatastoreException(400, "", "")).when(txn).put(isA(FullEntity.class));

        try {
            sut.create(legalTag);
            fail("Expected an exception");
        }
        catch(PersistenceException ex){}

        verify(txn, times(1)).rollback();
    }

    @Test(expected = AppException.class)
    public void should_throwAppException_when_savingContractAndOneAlreadyExistsForGivenKind(){
        DatastoreLegalTagRepository sut = new DatastoreLegalTagRepository(datastore);

        Key key = mockKey(1L);
        mockGet(true, key, now);//make sure a legalTag does not exist yet

        Key key1 = mockKey(10L);
        List<Key> mockKeys = new ArrayList<Key>(){{add(key1);}};
        Transaction txn = mockTransaction(mockKeys, false);
        when(txn.get(key1)).thenReturn(Entity.newBuilder(key1).build());
        when(this.keyFactory.setKind(anyString())).thenReturn(this.keyFactory);
        when(this.keyFactory.newKey(anyLong())).thenReturn(key1);
        LegalTag legalTag = LegalTestUtils.createValidLegalTag("name");

        sut.create(legalTag);
    }

    @Test
    public void should_ReturnNull_When_GettingLegalTagThatDoesNotExist(){
        DatastoreLegalTagRepository sut = new DatastoreLegalTagRepository(datastore);
        Key key = mockKey(1L);
        mockGet(false, key, now);

        long[] input = new long[]{7L};
        Collection<LegalTag> legalTags = sut.get(input);

        assertEquals(0, legalTags.size());
    }

    @Test
    public void should_ReturnEmpty_When_GettingLegalTagAndInputIsEmpty(){
        DatastoreLegalTagRepository sut = new DatastoreLegalTagRepository(datastore);
        Key key = mockKey(1L);
        mockGet(true, key, now);

        long[] input = new long[0];
        Collection<LegalTag> legalTags = sut.get(input);

        assertEquals(0, legalTags.size());
    }

    @Test
    public void should_returnCompleteLegalTag_When_gettingLegalTagAndKindAndItExists(){
        DatastoreLegalTagRepository sut = new DatastoreLegalTagRepository(datastore);

        Key key = mockKey(123L);
        mockGet(true, key, now);
        when(this.keyFactory.setKind(anyString())).thenReturn(this.keyFactory);
        when(this.keyFactory.newKey(anyLong())).thenReturn(key);
        long[] input = new long[]{1L};
        Collection<LegalTag> legalTags = sut.get(input);
        assertEquals(1, legalTags.size());

        LegalTag legalTag = Iterables.get(legalTags, 0);
        assertEquals(123L, (long) legalTag.getId());
        assertEquals("name", legalTag.getName());
        assertTrue(legalTag.getIsValid());
        assertEquals("B", legalTag.getProperties().getOriginator());
        assertEquals("A", legalTag.getProperties().getContractId());
        assertEquals(2, legalTag.getProperties().getCountryOfOrigin().size());
        assertEquals("1", legalTag.getProperties().getCountryOfOrigin().get(0));
        assertEquals("C", legalTag.getProperties().getDataType());
        assertEquals("E", legalTag.getProperties().getExportClassification());
        assertEquals("F", legalTag.getProperties().getPersonalData());
        assertEquals("D", legalTag.getProperties().getSecurityClassification());
        assertEquals(new java.sql.Date(Timestamp.of(now).toSqlTimestamp().getTime()), legalTag.getProperties().getExpirationDate());
    }

    @Test
    public void should_returnLegalTagWithDescription_when_descriptionExists(){
        DatastoreLegalTagRepository sut = new DatastoreLegalTagRepository(datastore);

        Key key = mockKey(123L);
        mockGet(true, createEntityWithDesc(key, now));

        long[] input = new long[]{1L};
        Collection<LegalTag> legalTags = sut.get(input);
        assertEquals(1, legalTags.size());

        LegalTag legalTag = Iterables.get(legalTags, 0);

        assertEquals("desc", legalTag.getDescription());
    }

    @Test
    public void should_rollback_when_deletingLegalTagThrowsAnException(){
        DatastoreLegalTagRepository sut = new DatastoreLegalTagRepository(datastore);

        Key key1 = mockKey(1L);
        Transaction txn = mockTransaction(null, true);
        when(txn.get(key1)).thenReturn(createEntity(key1, now));
        when(this.keyFactory.setKind(anyString())).thenReturn(this.keyFactory);
        when(this.keyFactory.newKey(anyLong())).thenReturn(key1);
        doThrow(new DatastoreException(400, "", "")).when(txn).delete(anyVararg());

        try {
            sut.delete(new LegalTag());
            fail("Expected an exception");
        }
        catch(PersistenceException ex){}

        verify(txn, times(1)).rollback();
    }
    @Test
    public void should_returnTrue_when_deletingLegalTagIsSuccessful(){
        DatastoreLegalTagRepository sut = new DatastoreLegalTagRepository(datastore);

        Key key1 = mockKey(1L);
        Transaction txn = mockTransaction(null, false);
        when(txn.get(key1)).thenReturn(createEntity(key1, now));
        when(this.keyFactory.setKind(anyString())).thenReturn(this.keyFactory);
        when(this.keyFactory.newKey(anyLong())).thenReturn(key1);
        Boolean result = sut.delete(new LegalTag());

        verify(txn, times(1)).commit();
        assertTrue(result);
    }
    @Test
    public void should_deleteLegalTagAndMakeBackup_when_deletingExistingLegalTag(){
        DatastoreLegalTagRepository sut = new DatastoreLegalTagRepository(datastore);
        LegalTag input = LegalTestUtils.createValidLegalTag("name");

        Key key1 = mockKey(1L);
        Transaction txn = mockTransaction(null, false);
        when(txn.get(key1)).thenReturn(createEntity(key1, now));
        when(this.keyFactory.setKind(anyString())).thenReturn(this.keyFactory);
        when(this.keyFactory.newKey(anyLong())).thenReturn(key1);
        Boolean result = sut.delete(new LegalTag());


        verify(txn, times(1)).delete(key1);
        verify(txn, times(1)).commit();
    }
    @Test
    public void should_notDeleteLegalTagAndMakeBackup_when_deletingNonExistingLegalTag_butShouldReturnTrue(){
        DatastoreLegalTagRepository sut = new DatastoreLegalTagRepository(datastore);

        Key key1 = mockKey(1L);
        Transaction txn = mockTransaction(null, false);
        when(txn.get(key1)).thenReturn(null);
        Boolean result = sut.delete(new LegalTag());

        verify(txn, times(0)).delete(key1);
        verify(txn, times(0)).commit();
        assertTrue(result);
    }


    @Test
    public void should_returnNull_when_updateIsGivenANullLegalTag(){
        DatastoreLegalTagRepository sut = new DatastoreLegalTagRepository(datastore);
        LegalTag result = sut.update(null);
        assertEquals(null, result);
    }
    @Test
    public void should_rollbackTransaction_when_updateThrowsAnError(){
        DatastoreLegalTagRepository sut = new DatastoreLegalTagRepository(datastore);
        LegalTag input = LegalTestUtils.createValidLegalTag("name");

        Key key1 = mockKey(1L);

        Transaction txn = mockTransaction(null, true);
        when(txn.get(key1)).thenReturn(Entity.newBuilder(key1).build());
        doThrow(new DatastoreException(400, "", "")).when(txn).commit();
        when(this.keyFactory.setKind(anyString())).thenReturn(this.keyFactory);
        when(this.keyFactory.newKey(anyLong())).thenReturn(key1);
        try {
            sut.update(input);
            fail("Expected an exception");
        }
        catch(PersistenceException ex){}

        verify(txn, times(1)).rollback();
    }
    @Test(expected = AppException.class)
    public void should_throw400Error_when_updatingALegalTagThatDoesNotExist(){
        DatastoreLegalTagRepository sut = new DatastoreLegalTagRepository(datastore);
        mockKey(1L);
        mockTransaction(null, true);
        LegalTag input = LegalTestUtils.createValidLegalTag("name");
        sut.update(input);
    }
    @Test
    public void should_updateLegaltag_and_makeBackupOfPreviousLegalTag_when_updateGivenExistingLegaltag(){
        DatastoreLegalTagRepository sut = new DatastoreLegalTagRepository(datastore);
        LegalTag input = LegalTestUtils.createValidLegalTag("name");

        Key key1 = mockKey(1L);

        Transaction txn = mockTransaction(null, true);
        when(txn.get(key1)).thenReturn(createEntity(key1, now));
        when(this.keyFactory.setKind(anyString())).thenReturn(this.keyFactory);
        when(this.keyFactory.newKey(anyLong())).thenReturn(key1);
        LegalTag result = sut.update(input);

        verify(txn, times(1)).put(any(FullEntity.class));
        verify(txn, times(1)).putWithDeferredIdAllocation(anyObject());
        verify(txn, times(1)).commit();
        assertEquals(input, result);
    }
    @Test
    public void should_returnCollection_when_requestingList(){
        DatastoreLegalTagRepository sut = new DatastoreLegalTagRepository(datastore);

        Key key = mockKey(123L);
        mockGet(true, key, now);

        ListLegalTagArgs args = new ListLegalTagArgs();
        args.setIsValid(true);
        Collection<LegalTag> result = sut.list(args);

        assertEquals(1, result.size());
    }

    Key mockKey(Long id){
        Key key = mock(Key.class);
        when(key.getKind()).thenReturn("key-name");
        when(key.getId()).thenReturn(id);
        when(keyFactory.setKind(any())).thenReturn(keyFactory);
        when(keyFactory.newKey(anyInt())).thenReturn(key);
        return key;
    }
    Transaction mockTransaction(List<Key> keys, Boolean isActive) {
        Transaction txn = mock(Transaction.class);
        when(this.datastore.newTransaction()).thenReturn(txn);
        Transaction.Response rsp = mock(Transaction.Response.class);
        when(txn.commit()).thenReturn(rsp);
        when(txn.isActive()).thenReturn(isActive);
        when(rsp.getGeneratedKeys()).thenReturn(keys);
        return txn;
    }

    @SuppressWarnings("unchecked")
    void mockGet(boolean hasNext, Key key, Date now){
        mockGet(hasNext, createEntity(key, now));
    }
    @SuppressWarnings("unchecked")
    void mockGet(boolean hasNext, Entity entity){
        QueryResults<Object> results = mock(QueryResults.class);
        when(results.hasNext()).thenReturn(hasNext).thenReturn(false);

        when(results.next()).thenReturn(entity);
        when(datastore.get(isA(List.class))).thenReturn(results);
        when(datastore.run(any())).thenReturn(results);
    }

    private Entity createEntityWithDesc(Key key, Date now) {
        return getEntityBuilder(key, now)
                    .set(DatastoreLegalTagRepository.DESCRIPTION, "desc")
                    .build();
    }
    private Entity createEntity(Key key, Date now) {
        return getEntityBuilder(key, now)
                .build();
    }

    private Entity.Builder getEntityBuilder(Key key, Date now) {
        return Entity.newBuilder(key)
                .set(DatastoreLegalTagRepository.COUNTRY_OF_ORIGIN, new ArrayList<StringValue>(){{ add(new StringValue("1")); add(new StringValue("2")); }})
                .set(DatastoreLegalTagRepository.CONTRACT_ID, "A")
                .set(DatastoreLegalTagRepository.EXPIRATION_DATE, Timestamp.of(now))
                .set(DatastoreLegalTagRepository.CREATION_DT, Timestamp.of(now))
                .set(DatastoreLegalTagRepository.ORIGINATOR, "B")
                .set(DatastoreLegalTagRepository.DATA_TYPE, "C")
                .set(DatastoreLegalTagRepository.SECURITY_CLASSIFICATION, "D")
                .set(DatastoreLegalTagRepository.EXPORT_CLASSIFICATION, "E")
                .set(DatastoreLegalTagRepository.PERSONAL_DATA, "F")
                .set(DatastoreLegalTagRepository.NAME, "name")
                .set(DatastoreLegalTagRepository.IS_VALID, true);
    }
}
