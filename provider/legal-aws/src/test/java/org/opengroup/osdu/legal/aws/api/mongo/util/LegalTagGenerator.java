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

package org.opengroup.osdu.legal.aws.api.mongo.util;

import org.opengroup.osdu.core.common.model.legal.LegalTag;

import java.util.List;
import java.util.stream.Collectors;

public class LegalTagGenerator {

    public static LegalTag generateLegalTag(String legalTagName) {
        return generateLegalTag(legalTagName, true);
    }

    public static LegalTag generateLegalTag(String legalTagName, boolean isValid) {
        if (legalTagName == null) {
            legalTagName = ParentUtil.LEGAL_TAG_NAME;
        }
        LegalTag legalTag = new LegalTag();
        legalTag.setName(legalTagName);
        legalTag.setIsValid(isValid);
        legalTag.setDefaultId();
        return legalTag;
    }

    public static List<LegalTag> generateLegalTags(List<String> names) {
        return names.stream().map(LegalTagGenerator::generateLegalTag).collect(Collectors.toList());
    }

    public static List<LegalTag> generateLegalTags(List<String> names, boolean isValid) {
        return names.stream().map(s -> generateLegalTag(s, isValid)).collect(Collectors.toList());
    }
}
