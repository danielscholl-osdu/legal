/*
 * Copyright 2021 Google LLC
 * Copyright 2021 EPAM Systems, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengroup.osdu.legal.tags.dataaccess;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.opengroup.osdu.legal.tags.LegalTestUtils.createValidLegalTag;
import static org.powermock.api.mockito.PowerMockito.when;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.legal.LegalTag;
import org.opengroup.osdu.core.common.model.legal.ListLegalTagArgs;
import org.opengroup.osdu.legal.config.MongoDBConfigProperties;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

@Ignore
@RunWith(SpringRunner.class)
@PrepareForTest({MongoOperations.class, MongoClientProvider.class, MongoClient.class,
    MongoLegalTagRepository.class})
public class MongoLegalTagRepositoryTest {

  private static final String DB_NAME = "test";

  @MockBean
  private MongoClient client;

  @MockBean
  private MongoOperations ops;

  @MockBean
  private MongoClientProvider clientProvider;

  @MockBean
  private MongoDBConfigProperties mongoDBConfigProperties;

  @InjectMocks
  private MongoLegalTagRepository repo;

  @Before
  public void setup() throws Exception {
    final String connectionString = String.format("%s%s:%s@%s/?%s",
        "mongodb://",
        "admin",
        "gonrg422",
        "localhost:27017",
        "retryWrites=true&w=majority");
    ConnectionString connString = new ConnectionString(connectionString);
    MongoClientSettings settings = MongoClientSettings.builder()
        .applyConnectionString(connString)
        .retryWrites(true)
        .build();

    client = MongoClients.create(settings);
    ops = new MongoTemplate(client, DB_NAME);
    repo = new MongoLegalTagRepository(mongoDBConfigProperties, clientProvider);

    ReflectionTestUtils.setField(repo, "dbName", DB_NAME);
    ReflectionTestUtils.setField(clientProvider, "mongoClient", client);

    initMocks(this);
  }

  @After
  public void teardown() throws Exception {
    client.getDatabase(DB_NAME).drop();
  }

  @Test
  public void testCreate() throws Exception {
    when(clientProvider.getOps(anyString())).thenReturn(ops);
    LegalTag tag1 = createValidLegalTag("tag1");
    Long id1 = tag1.getId();

    Long saved_id1 = repo.create(tag1);

    assertEquals(id1, saved_id1);

    try {
      LegalTag nullIdtag = createValidLegalTag("nulltag");
      nullIdtag.setId(null);
      repo.create(nullIdtag);
      fail("Expected exception");
    } catch (NullPointerException e) {
    }

    LegalTag tag2 = createValidLegalTag("tag2");
    tag2.setIsValid(true);
    Long id2 = tag2.getId();

    Long saved_id2 = repo.create(tag2);

    assertEquals(id2, saved_id2);

  }

  @Test
  public void testGet() throws Exception {
    when(clientProvider.getOps(anyString())).thenReturn(ops);
    LegalTag tag1 = createValidLegalTag("tag1");
    Long id1 = tag1.getId();

    Long saved_id1 = repo.create(tag1);
    assertEquals(id1, saved_id1);

    LegalTag tag2 = createValidLegalTag("tag2");
    tag2.setIsValid(true);
    Long id2 = tag2.getId();

    Long saved_id2 = repo.create(tag2);

    assertEquals(id2, saved_id2);

    Collection<LegalTag> retrv1 = repo.get(new long[]{id1});
    assertEquals(retrv1.size(), 1);

    LegalTag retrv1_tag1 = retrv1.iterator().next();
    assertEquals(tag1, retrv1_tag1);
  }

  @Test
  public void testNone() throws Exception {
    when(clientProvider.getOps(anyString())).thenReturn(ops);
    LegalTag tag1 = createValidLegalTag("tag1");
    Long id1 = tag1.getId();

    Long saved_id1 = repo.create(tag1);
    assertEquals(id1, saved_id1);

    LegalTag tag2 = createValidLegalTag("tag2");
    tag2.setIsValid(true);
    Long id2 = tag2.getId();

    Long saved_id2 = repo.create(tag2);

    assertEquals(id2, saved_id2);

    Collection<LegalTag> retrv1 = repo.get(new long[]{4324L});
    assertEquals(retrv1.size(), 0);
  }


  @Test
  public void testGetMultiple() throws Exception {
    when(clientProvider.getOps(anyString())).thenReturn(ops);
    LegalTag tag1 = createValidLegalTag("tag1");
    Long id1 = tag1.getId();

    Long saved_id1 = repo.create(tag1);
    assertEquals(id1, saved_id1);

    LegalTag tag2 = createValidLegalTag("tag2");
    tag2.setIsValid(true);
    Long id2 = tag2.getId();

    Long saved_id2 = repo.create(tag2);

    assertEquals(id2, saved_id2);

    Collection<LegalTag> retrv1 = repo.get(new long[]{id1, id2});
    assertEquals(retrv1.size(), 2);

    Iterator<LegalTag> it = retrv1.iterator();

    LegalTag retrv1_tag1 = it.next();
    assertEquals(tag1, retrv1_tag1);
    LegalTag retrv1_tag2 = it.next();
    assertEquals(tag2, retrv1_tag2);
  }

  @Test
  public void testUpdate() throws Exception {
    when(clientProvider.getOps(anyString())).thenReturn(ops);
    LegalTag tag1 = createValidLegalTag("tag1");
    Long id1 = tag1.getId();
    tag1.setDescription("description1");

    try {
      repo.update(tag1);
      fail("expected exception when updating object not present in the database");
    } catch (AppException ex) {
    }

    Long saved_id1 = repo.create(tag1);
    assertEquals(id1, saved_id1);

    Collection<LegalTag> retrv1 = repo.get(new long[]{id1});
    assertEquals(retrv1.size(), 1);

    Iterator<LegalTag> retrv1_it = retrv1.iterator();

    LegalTag retrv1_tag1 = retrv1_it.next();
    assertEquals(tag1, retrv1_tag1);

    retrv1_tag1.setDescription("description2");

    // This update will work without extra logic because retrv1_tag1 is
    // actually a CloudantBackedLegalTag with a valid _rev
    repo.update(retrv1_tag1);

    Collection<LegalTag> retrv2 = repo.get(new long[]{id1});
    assertEquals(retrv2.size(), 1);

    Iterator<LegalTag> retrv2_it = retrv2.iterator();

    LegalTag retrv2_tag1 = retrv2_it.next();

    assertNotEquals(tag1, retrv2_tag1);
    assertEquals(retrv1_tag1, retrv2_tag1);

    LegalTag tag1_equivalent = createValidLegalTag("tag1");
    tag1.setDescription("description3");
    assertEquals(id1, tag1_equivalent.getId());

    // This update would fail it the update method didn't handle the missing _rev
    repo.update(tag1_equivalent);

    // This update would fail it the update method didn't handle the outdated _rev
    repo.update(retrv2_tag1);

  }

  @Test
  public void testDelete() throws Exception {
    when(clientProvider.getOps(anyString())).thenReturn(ops);
    LegalTag tag1 = createValidLegalTag("tag1");
    Long id1 = tag1.getId();
    tag1.setDescription("description1");

    assertTrue(repo.delete(tag1));

    Long saved_id1 = repo.create(tag1);
    assertEquals(id1, saved_id1);

    Collection<LegalTag> retrv1 = repo.get(new long[]{id1});
    assertEquals(retrv1.size(), 1);

    assertTrue(repo.delete(tag1));

    Collection<LegalTag> retrv2 = repo.get(new long[]{id1});
    assertEquals(retrv2.size(), 0);

    repo.create(tag1);

    Collection<LegalTag> retrv3 = repo.get(new long[]{id1});
    assertEquals(retrv3.size(), 1);

    Iterator<LegalTag> retrv3_it = retrv3.iterator();

    LegalTag retrv3_tag1 = retrv3_it.next();
    assertEquals(tag1, retrv3_tag1);

    retrv3_tag1.setDescription("description2");
    assertNotEquals(tag1, retrv3_tag1);

    repo.update(retrv3_tag1);

    Collection<LegalTag> retrv4 = repo.get(new long[]{id1});
    assertEquals(retrv4.size(), 1);

    // This would fail without the retry on conflict logic
    assertTrue(repo.delete(retrv3_tag1));

    Collection<LegalTag> retrv5 = repo.get(new long[]{id1});
    assertEquals(retrv5.size(), 0);

  }

  @Test
  public void testList() throws Exception {
    when(clientProvider.getOps(anyString())).thenReturn(ops);
    LegalTag tag1 = createValidLegalTag("tag1");
    tag1.setIsValid(false);
    repo.create(tag1);
    LegalTag tag2 = createValidLegalTag("tag2");
    tag2.setIsValid(true);
    repo.create(tag2);
    LegalTag tag3 = createValidLegalTag("tag3");
    tag3.setIsValid(false);
    repo.create(tag3);
    LegalTag tag4 = createValidLegalTag("tag4");
    tag4.setIsValid(true);
    repo.create(tag4);
    LegalTag tag5 = createValidLegalTag("tag5");
    tag5.setIsValid(false);
    repo.create(tag5);
    LegalTag tag6 = createValidLegalTag("tag6");
    tag6.setIsValid(true);
    repo.create(tag6);

    ListLegalTagArgs allTagsNoPaging = new ListLegalTagArgs();
    allTagsNoPaging.setIsValid(null);
    allTagsNoPaging.setLimit(0);

    Set<String> allNames = repo.list(allTagsNoPaging).stream().map(t -> t.getName())
        .collect(Collectors.toSet());

    assertEquals(6, allNames.size());
    assertTrue(allNames.contains("tag1"));
    assertTrue(allNames.contains("tag2"));
    assertTrue(allNames.contains("tag3"));
    assertTrue(allNames.contains("tag4"));
    assertTrue(allNames.contains("tag5"));
    assertTrue(allNames.contains("tag6"));

    ListLegalTagArgs validTagsNoPaging = new ListLegalTagArgs();
    validTagsNoPaging.setIsValid(true);
    validTagsNoPaging.setLimit(0);

    Set<String> validNames = repo.list(validTagsNoPaging).stream().map(t -> t.getName())
        .collect(Collectors.toSet());

    assertEquals(3, validNames.size());
    assertTrue(validNames.contains("tag2"));
    assertTrue(validNames.contains("tag4"));
    assertTrue(validNames.contains("tag6"));
  }
}