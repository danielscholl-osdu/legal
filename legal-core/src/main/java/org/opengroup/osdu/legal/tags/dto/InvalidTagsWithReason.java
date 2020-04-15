package org.opengroup.osdu.legal.tags.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvalidTagsWithReason {
    private Collection<InvalidTagWithReason> invalidLegalTags = new ArrayList<>();
}
