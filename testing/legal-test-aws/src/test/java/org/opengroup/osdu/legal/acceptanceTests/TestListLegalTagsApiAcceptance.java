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

package org.opengroup.osdu.legal.acceptanceTests;

import org.junit.After;
import org.junit.Before;
import org.opengroup.osdu.legal.util.AwsLegalTagUtils;

public class TestListLegalTagsApiAcceptance extends ListLegalTagsApiAcceptanceTests {

    private String testLegalTag1Name = String.format("%s%s", "int-test-legal-tag1-", String.valueOf(System.currentTimeMillis()));
    private String testLegalTag2Name = String.format("%s%s", "int-test-legal-tag2-", String.valueOf(System.currentTimeMillis()));

    @Before
    @Override
    public void setup() throws Exception  {
        this.legalTagUtils = new AwsLegalTagUtils();
        legalTagUtils.create(testLegalTag1Name);
        legalTagUtils.create(testLegalTag2Name);
        super.setup();
    }

    @After
    @Override
    public void teardown() throws Exception  {
        super.teardown();
        legalTagUtils.delete(testLegalTag1Name);
        legalTagUtils.delete(testLegalTag2Name);
        this.legalTagUtils = null;
    }
}
