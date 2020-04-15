package org.opengroup.osdu.legal.tags.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Set;

@Data
@NoArgsConstructor
public class ReadablePropertyValues {
    private Map<String, String> countriesOfOrigin;
    private Map<String, String> otherRelevantDataCountries;
    private Set<String> securityClassifications;
    private Set<String> exportClassificationControlNumbers;
    private Set<String> personalDataTypes;
    private Set<String> dataTypes;
}
