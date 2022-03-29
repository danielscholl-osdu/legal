package org.opengroup.osdu.legal.aws.api.mongo.tags.dataaccess;

import com.google.common.primitives.Longs;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.legal.LegalTag;
import org.opengroup.osdu.core.common.model.legal.ListLegalTagArgs;
import org.opengroup.osdu.core.common.model.legal.Properties;
import org.opengroup.osdu.legal.aws.api.mongo.config.LegalTestConfig;
import org.opengroup.osdu.legal.aws.api.mongo.util.ParentUtil;
import org.opengroup.osdu.legal.aws.tags.dataaccess.mongodb.config.MultiClusteredConfigReaderLegal;
import org.opengroup.osdu.legal.aws.tags.dataaccess.mongodb.repository.LegalTagRepositoryMongoDBImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.Date;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.opengroup.osdu.legal.aws.api.mongo.util.LegalTagGenerator.generateLegalTag;
import static org.opengroup.osdu.legal.aws.api.mongo.util.LegalTagGenerator.generateLegalTags;


@DataMongoTest
@RunWith(SpringRunner.class)
@EnableAutoConfiguration
@SpringJUnitConfig(classes = {LegalTestConfig.class})
@Import({MultiClusteredConfigReaderLegal.class})
public class LegalTagRepositoryImplTest extends ParentUtil {

    @Autowired
    private LegalTagRepositoryMongoDBImpl legalTagRepository ;

    @Test
    public void create() {
        //given
        LegalTag legalTag = generateLegalTag(null);

        //when
        Long legalId = this.legalTagRepository.create(legalTag);

        //then

        assertEquals(legalTag.getId(), legalId);

        LegalTag legalTagFromDB = this.mongoTemplateHelper.findById(legalId, ParentUtil.DATA_PARTITION);

        assertNotNull(legalTagFromDB);
        assertEquals(legalTag.getName(), legalTagFromDB.getName());
        assertEquals(legalTag.getId(), legalTagFromDB.getId());

    }

    @Test(expected = AppException.class)
    public void createDuplicate() {
        //given
        LegalTag legalTag = generateLegalTag(null);
        LegalTag legalTagInDb = this.mongoTemplateHelper.insert(legalTag);

        assertNotNull(legalTagInDb);

        //when
        this.legalTagRepository.create(legalTag);

        //then
        // throws an AppException
    }

    @Test(expected = AppException.class)
    public void createInvalidId() {
        //given
        // ID is null
        LegalTag legalTag = new LegalTag();
        legalTag.setName(LEGAL_TAG_NAME);
        //when
        this.legalTagRepository.create(legalTag);

        //then
        // throws an AppException
    }

    @Test
    public void get() {
        //given
        List<String> legalTagNames = Arrays.asList("legalTag1", "LegalTag2", "legalTag3");
        List<LegalTag> legalTags = generateLegalTags(legalTagNames);

        this.mongoTemplateHelper.insert(legalTags);

        legalTags.add(generateLegalTag("legalTag4"));
        LegalTag legalTag = generateLegalTag("legalTag5");
        legalTags.add(legalTag);
        String otherDataPartition = "otherDataPartition";

        this.mongoTemplateHelper.insert(legalTag, otherDataPartition);

        List<LegalTag> legalTagsFromDb = this.mongoTemplateHelper.findAll(DATA_PARTITION);
        legalTagsFromDb.add(this.mongoTemplateHelper.findById(legalTag.getId(), otherDataPartition));

        assertEquals(legalTags.size() - 1, legalTagsFromDb.size());

        long[] ids = Longs.toArray(legalTags.stream().map(LegalTag::getId).collect(Collectors.toList()));

        //when
        Collection<LegalTag> legalTagsFromRepoByDefaultDP = this.legalTagRepository.get(ids);
        //change DpsHeaders's DataPartition
        this.setHeaderDataPartition(otherDataPartition);
        Collection<LegalTag> legalTagsFromRepoByOtherDP = this.legalTagRepository.get(ids);

        //then
        assertEquals(3, legalTagsFromRepoByDefaultDP.size());
        assertTrue(legalTags.containsAll(legalTagsFromRepoByDefaultDP));

        assertEquals(1, legalTagsFromRepoByOtherDP.size());
        assertTrue(legalTags.containsAll(legalTagsFromRepoByOtherDP));
    }

    @Test
    public void delete() {
        //given
        LegalTag legalTag = generateLegalTag(null);
        LegalTag legalTagInDb = this.mongoTemplateHelper.insert(legalTag);

        assertNotNull(legalTagInDb);

        //when
        boolean isDeleted = this.legalTagRepository.delete(legalTag);

        //then
        assertTrue(isDeleted);
        LegalTag legalTagFromDb = this.mongoTemplateHelper.findById(legalTag.getId(), DATA_PARTITION);
        assertNull(legalTagFromDb);
    }

    @Test
    public void update() {
        //given
        LegalTag legalTag = generateLegalTag(null);
        LegalTag legalTagInDb = this.mongoTemplateHelper.insert(legalTag);

        assertNotNull(legalTagInDb);
        String changedLegalTagName = "Changed legal tag";
        legalTag.setName(changedLegalTagName);
        Properties properties = new Properties();
        String contractId = "contractId";
        properties.setContractId(contractId);
        Date expirationDate = new Date(System.currentTimeMillis());
        properties.setExpirationDate(expirationDate);
        legalTag.setProperties(properties);

        //when
        LegalTag updated = this.legalTagRepository.update(legalTag);

        //then
        assertEquals(legalTag, updated);
        LegalTag updatedDB = mongoTemplateHelper.findById(legalTag.getId(), DATA_PARTITION);
        assertEquals(legalTag, updatedDB);
    }

    @Test(expected = AppException.class)
    public void updateNotExists() {
        //given
        LegalTag legalTag = generateLegalTag(null);

        //when
        this.legalTagRepository.update(legalTag);

        //then
        // throws an AppException
    }

    @Test
    public void list() {
        //given
        List<String> legalTagNamesValid = Arrays.asList("legalTag1", "LegalTag2", "legalTag3", "legalTag4", "legalTag5", "legalTag6");
        List<LegalTag> legalTagsValidDefaultDP = generateLegalTags(legalTagNamesValid, true);

        this.mongoTemplateHelper.insert(legalTagsValidDefaultDP);// default partition

        List<String> legalTagNamesInvalid = Arrays.asList("legalTag8", "LegalTag9", "legalTag10", "legalTag11", "legalTag12", "legalTag13", "legalTag14");
        List<LegalTag> legalTagsInValidDefaultDP = generateLegalTags(legalTagNamesInvalid, false);

        this.mongoTemplateHelper.insert(legalTagsInValidDefaultDP);// default partition

        List<LegalTag> allInDefaultDP = this.mongoTemplateHelper.findAll(DATA_PARTITION);
        assertEquals(legalTagNamesValid.size() + legalTagNamesInvalid.size(), allInDefaultDP.size());

        List<String> legalTagNamesInvalidOtherDataPartition = Arrays.asList("legalTag1", "LegalTag2", "legalTag3", "legalTag4",
                "legalTag5", "legalTag6", "legalTag7");
        List<LegalTag> legalTagNameInvalidOtherDP = generateLegalTags(legalTagNamesInvalidOtherDataPartition, false);

        String otherDataPartition = "otherDataPartition";

        this.mongoTemplateHelper.insert(legalTagNameInvalidOtherDP, otherDataPartition);

        List<LegalTag> allInOtherDP = this.mongoTemplateHelper.findAll(otherDataPartition);

        assertEquals(legalTagNamesInvalidOtherDataPartition.size(), allInOtherDP.size());


        ListLegalTagArgs listLegalTagArgs = new ListLegalTagArgs();
        listLegalTagArgs.setIsValid(true);

        //when
        Collection<LegalTag> legalTagsFromRepoByDefaultDP = this.legalTagRepository.list(listLegalTagArgs);
        //change DpsHeaders's DataPartition
        this.setHeaderDataPartition(otherDataPartition);

        listLegalTagArgs = new ListLegalTagArgs();
        listLegalTagArgs.setIsValid(false);

        Collection<LegalTag> legalTagsFromRepoByOtherDP = this.legalTagRepository.list(listLegalTagArgs);

        //then
        assertEquals(6, legalTagsFromRepoByDefaultDP.size());
        assertTrue(legalTagsValidDefaultDP.containsAll(legalTagsFromRepoByDefaultDP));

        assertEquals(7, legalTagsFromRepoByOtherDP.size());
        assertTrue(legalTagNameInvalidOtherDP.containsAll(legalTagsFromRepoByOtherDP));
    }
}