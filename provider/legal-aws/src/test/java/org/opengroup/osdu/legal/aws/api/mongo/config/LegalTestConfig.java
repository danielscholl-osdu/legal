/**
* Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
*      http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

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
import org.springframework.context.annotation.Primary;
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
    @Primary
    public ITenantFactory tenantFactory() {
        return Mockito.mock(ITenantFactory.class);
    }

    @Bean
    public MongoTemplate createMongoTemplate(MongoDBSimpleFactory dbSimpleFactory) {
        return dbSimpleFactory.mongoTemplate(properties);
    }
}