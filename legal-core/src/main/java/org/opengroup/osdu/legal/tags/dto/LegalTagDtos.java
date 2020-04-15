package org.opengroup.osdu.legal.tags.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;

/*
    Read only collection of LegalTag model that are sent to the user
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LegalTagDtos {
    private Collection<LegalTagDto> legalTags = new ArrayList<>();
}
