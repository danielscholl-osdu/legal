package org.opengroup.osdu.legal.aws.api.mongo.config;

import org.mockito.Mockito;
import org.opengroup.osdu.core.aws.mongodb.MongoDBSimpleFactory;
import org.opengroup.osdu.core.aws.mongodb.MultiClusteredConfigReader;
import org.opengroup.osdu.core.aws.mongodb.config.MongoProperties;
import org.opengroup.osdu.core.common.provider.interfaces.ITenantFactory;
import org.opengroup.osdu.legal.aws.cache.GroupCache;
import org.opengroup.osdu.legal.aws.jobs.LegalTagPublisherImpl;
import org.opengroup.osdu.legal.aws.tags.dataaccess.mongodb.config.MultiClusteredConfigReaderLegal;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoTemplate;

import static org.mockito.ArgumentMatchers.any;

@TestConfiguration
public class LegalTestConfig {
    private final MongoProperties properties = MongoProperties.builder().
            endpoint("localhost:27019").
            databaseName("osdu_test")
            .build();

    @Bean
    public LegalTagPublisherImpl legalTagPublisherImpl() {
        return Mockito.mock(LegalTagPublisherImpl.class);
    }

    @Bean
    public MultiClusteredConfigReader configReader() {
        MultiClusteredConfigReaderLegal multiClusteredConfigReaderLegal = Mockito.mock(MultiClusteredConfigReaderLegal.class);
        Mockito.doReturn(properties).when(multiClusteredConfigReaderLegal).readProperties(any());
        return multiClusteredConfigReaderLegal;
    }

    @Bean
    public GroupCache groupCache() {
        return Mockito.mock(GroupCache.class);
    }

    @Bean
    public ITenantFactory tenantFactory() {
        return Mockito.mock(ITenantFactory.class);
    }

    @Bean
    public MongoTemplate createMongoTemplate(MongoDBSimpleFactory dbSimpleFactory) {
        return dbSimpleFactory.mongoTemplate(properties);
    }
}