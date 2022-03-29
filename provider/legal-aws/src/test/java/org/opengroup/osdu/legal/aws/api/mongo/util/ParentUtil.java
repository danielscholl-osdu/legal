package org.opengroup.osdu.legal.aws.api.mongo.util;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExternalResource;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opengroup.osdu.core.aws.partition.PartitionServiceClientWithCache;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.MongoTemplate;

public abstract class ParentUtil {

    public static final String DATA_PARTITION = "osdu_legal_data_partition";
    public static final String LEGAL_TAG_NAME = "LEGAL_TAG_NAME";

    public MongoTemplateHelper mongoTemplateHelper;

    @MockBean
    private DpsHeaders headers;
    @MockBean
    private PartitionServiceClientWithCache partitionServiceClient;
    @MockBean
    private JaxRsDpsLog log;

    @Before
    public void setUpAnyTime() {
        MockitoAnnotations.openMocks(this);
        this.setHeaderDataPartition(DATA_PARTITION);
    }


    @Rule
    public ExternalResource resource = new ExternalResource() {
        @Override
        protected void before() {
            ParentUtil.this.mongoTemplateHelper.dropCollections();
        }

        @Override
        protected void after() {
            ParentUtil.this.mongoTemplateHelper.dropCollections();
        }
    };

    @Autowired
    public void set(MongoTemplate mongoTemplate) {
        this.mongoTemplateHelper = new MongoTemplateHelper(mongoTemplate);
    }


    public void setHeaderDataPartition(String dataPartition) {
        Mockito.when(this.headers.getPartitionId())
                .thenReturn(dataPartition);
    }

}