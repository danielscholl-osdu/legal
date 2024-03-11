//  Copyright © Microsoft Corporation
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.

package org.opengroup.osdu.legal.azure;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.legal.azure.jobs.AboutToExpireLegalTagPublisherImpl;
import org.opengroup.osdu.legal.jobs.models.AboutToExpireLegalTags;

@RunWith(MockitoJUnitRunner.class)
public class AboutToExpireLegalTagPublisherImplTest {

    @Mock
    private DpsHeaders headers;

    @InjectMocks
    private AboutToExpireLegalTagPublisherImpl sut;

    @Test
    public void shouldPublishToServiceBus() throws Exception {
        AboutToExpireLegalTags aboutToExpireLegalTags = new AboutToExpireLegalTags();
        sut.publish("project-id", headers, aboutToExpireLegalTags);
    }

}
