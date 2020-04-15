package org.opengroup.osdu.legal.tags.dto;

import org.opengroup.osdu.legal.tags.LegalTestUtils;
import org.opengroup.osdu.core.common.model.legal.LegalTag;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LegalTagDtoTests {

    @Test
    public void should_appendWarningToDescription_when_clientConsentRequiredOnCoo(){
        LegalTag lt = LegalTestUtils.createValidLegalTag("abc");
        lt.getProperties().getCountryOfOrigin().add("MY");
        LegalTagDto dto = LegalTagDto.convertTo(lt);

        assertTrue(dto.getDescription().startsWith("One or more Country of Origin for these Legal tag require consent of client or a license."));
    }

    @Test
    public void should_notAppendWarningToDescription_when_clientConsentNotRequiredOnCoo(){
        LegalTag lt = LegalTestUtils.createValidLegalTag("abc");
        LegalTagDto dto = LegalTagDto.convertTo(lt);

        assertFalse(dto.getDescription().startsWith("One or more Country of Origin for these Legal tag require consent of client or a license."));
    }
}
