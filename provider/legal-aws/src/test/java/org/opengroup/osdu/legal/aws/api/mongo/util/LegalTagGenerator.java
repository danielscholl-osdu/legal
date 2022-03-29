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
