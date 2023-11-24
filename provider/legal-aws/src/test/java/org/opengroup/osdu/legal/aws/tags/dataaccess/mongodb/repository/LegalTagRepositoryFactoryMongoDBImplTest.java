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

package org.opengroup.osdu.legal.aws.tags.dataaccess.mongodb.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opengroup.osdu.legal.provider.interfaces.ILegalTagRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LegalTagRepositoryFactoryMongoDBImplTest {

    @InjectMocks
    private LegalTagRepositoryFactoryMongoDBImpl factory;

    @Mock
    private LegalTagRepositoryMongoDBImpl repoImpl;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGet() {
        ILegalTagRepository result = factory.get("someTenantName");

        // Verifying that the get method returns the correct repoImpl
        assertEquals(repoImpl, result);
    }
}