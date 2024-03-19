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

package org.opengroup.osdu.legal.aws.jobs;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opengroup.osdu.core.aws.sns.AmazonSNSConfig;
import org.opengroup.osdu.core.aws.sns.PublishRequestBuilder;
import org.opengroup.osdu.core.aws.ssm.K8sLocalParameterProvider;
import org.opengroup.osdu.core.aws.ssm.K8sParameterNotFoundException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;

import org.springframework.test.util.ReflectionTestUtils;

import com.amazonaws.services.sns.AmazonSNS;

class AboutToExpireLegalTagPublisherImplTest {
    
    @InjectMocks
    private AboutToExpireLegalTagPublisherImpl aboutToExpireLegalTagPublisherImpl;

    @Mock
    private AmazonSNSConfig snsConfig;

    @Mock
    private K8sLocalParameterProvider k8sLocalParameterProvider;

    @Mock
    private AmazonSNS snsClient;

    @Mock
    private DpsHeaders headers;

    @Mock
    private PublishRequestBuilder<AwsAboutToExpireLegalTags> publishRequestBuilder;


    private final String testTopic = "testTopic";


    private final String testRegion = "testRegion";
    private static final String DATA_PARTITION_ID = "testDataPartitionId";
    private static final String CORRELATION_ID = "testCorrelationId";
    private static final String AUTHORIZATION = "testAuthorization";
    
    @BeforeEach
    void setup() throws K8sParameterNotFoundException {
        MockitoAnnotations.openMocks(this);
        when(snsConfig.AmazonSNS()).thenReturn(snsClient);
        when(k8sLocalParameterProvider.getParameterAsString("legal-sns-topic-arn"))
                .thenReturn(testTopic);
        ReflectionTestUtils.setField(aboutToExpireLegalTagPublisherImpl, "amazonSNSRegion", testRegion);
        when(headers.getPartitionIdWithFallbackToAccountId()).thenReturn(DATA_PARTITION_ID);
        doNothing().when(headers).addCorrelationIdIfMissing();
        when(headers.getCorrelationId()).thenReturn(CORRELATION_ID);
        when(headers.getAuthorization()).thenReturn(AUTHORIZATION);
        
        aboutToExpireLegalTagPublisherImpl.init();

    }

    @Test
    void testInit() throws K8sParameterNotFoundException{
        
        // Assert
        assertNotNull(aboutToExpireLegalTagPublisherImpl);
    }       

}

