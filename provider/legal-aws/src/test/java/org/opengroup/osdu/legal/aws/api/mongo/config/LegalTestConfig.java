package org.opengroup.osdu.legal.aws.api.mongo.config;

import org.mockito.Mockito;
import org.opengroup.osdu.core.aws.mongodb.MongoDBSimpleFactory;
import org.opengroup.osdu.core.aws.mongodb.config.MongoProperties;
import org.opengroup.osdu.core.aws.multitenancy.TenantFactory;
import org.opengroup.osdu.core.common.provider.interfaces.ITenantFactory;
import org.opengroup.osdu.legal.aws.cache.GroupCache;
import org.opengroup.osdu.legal.aws.jobs.LegalTagPublisherImpl;
import org.opengroup.osdu.legal.aws.tags.dataaccess.mongodb.config.MongoPropertiesReader;
import org.opengroup.osdu.legal.aws.tags.dataaccess.mongodb.config.MongoPropertiesReaderImpl;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.MongoTemplate;

@TestConfiguration
@ComponentScan(
        basePackages = {"org.opengroup.osdu"},
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = MongoPropertiesReaderImpl.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = LegalTagPublisherImpl.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = TenantFactory.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = GroupCache.class)
        }
)
public class LegalTestConfig {

    @Bean
    @Primary
    public MongoPropertiesReader mongoPropertiesReader() {
        MongoPropertiesReader propertiesReader = Mockito.mock(MongoPropertiesReader.class);
        MongoProperties properties = MongoProperties.builder().
                endpoint("localhost:27019").
                databaseName("osdu_test")
                .build();
        Mockito.doReturn(properties).when(propertiesReader).getProperties();
        return propertiesReader;
    }

    @Bean
    public LegalTagPublisherImpl legalTagPublisherImpl() {
        return Mockito.mock(LegalTagPublisherImpl.class);
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
    public MongoTemplate createMongoTemplate(MongoDBSimpleFactory dbSimpleFactory, MongoPropertiesReader propertiesReader) {
        return dbSimpleFactory.mongoTemplate(propertiesReader.getProperties());
    }
}