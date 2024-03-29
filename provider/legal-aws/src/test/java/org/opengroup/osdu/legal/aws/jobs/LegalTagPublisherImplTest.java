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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.aws.sns.AmazonSNSConfig;
import org.opengroup.osdu.core.aws.sns.PublishRequestBuilder;
import org.opengroup.osdu.core.aws.ssm.K8sLocalParameterProvider;
import org.opengroup.osdu.core.aws.ssm.K8sParameterNotFoundException;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.legal.StatusChangedTag;
import org.opengroup.osdu.core.common.model.legal.StatusChangedTags;
import org.springframework.test.util.ReflectionTestUtils;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.PublishRequest;

@ExtendWith(MockitoExtension.class)
class LegalTagPublisherImplTest {
    
    @InjectMocks
    private LegalTagPublisherImpl legalTagPublisherImpl;

    @Mock
    private AmazonSNSConfig snsConfig;

    @Mock
    private K8sLocalParameterProvider k8sLocalParameterProvider;

    @Mock
    private AmazonSNS snsClient;

    @Mock
    private DpsHeaders headers;

    @Mock
    private PublishRequestBuilder<AwsStatusChangedTag> publishRequestBuilder;

    @Mock
    private StatusChangedTags tags;

    @Mock
    private StatusChangedTag tag;

    @Mock
    private JaxRsDpsLog log;

    private final String testTopic = "testTopic";
    private final String testRegion = "testRegion";
    
    @BeforeEach
    void setup() throws K8sParameterNotFoundException {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testInit() throws K8sParameterNotFoundException{
        
        when(k8sLocalParameterProvider.getParameterAsString("legal-sns-topic-arn"))
                .thenReturn(testTopic);
        ReflectionTestUtils.setField(legalTagPublisherImpl, "amazonSNSRegion", testRegion);
        assertDoesNotThrow(() -> legalTagPublisherImpl.init());
    } 

    @Test
    public void testPubish(){

        try (MockedConstruction<PublishRequestBuilder> k8sParameterProvider = Mockito.mockConstruction(PublishRequestBuilder.class,
                                                                                                           (mock, context) -> {
                                                                                                               when(mock.generatePublishRequest(anyString(),anyString(), any())).thenReturn(new PublishRequest());
                                                                                                           })) {
            
            List<StatusChangedTag> tagList = new ArrayList<StatusChangedTag>();
            tagList.add(tag);
            when(tags.getStatusChangedTags()).thenReturn(tagList);
            legalTagPublisherImpl.publish("projectId", headers, tags);
            verify(snsClient, times(1)).publish(any());
        }
    }     

}

