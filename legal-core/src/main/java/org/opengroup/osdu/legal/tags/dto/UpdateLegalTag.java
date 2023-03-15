package org.opengroup.osdu.legal.tags.dto;

import  java.sql.Date;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import org.opengroup.osdu.core.common.model.legal.validation.ValidDescription;
import org.opengroup.osdu.core.common.model.legal.validation.ValidName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * If any class variable changed here,
 * need to update the corresponding doc model class in SwaggerHelper.java
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "The model to update an existing LegalTag")
public class UpdateLegalTag {

    @Schema(description = "The name of the LegalTag", example = "OSDU-Private-EHCData")
    @ValidName
    private String name = "";

    @Schema(description = "The Id of the physical contract associated with the data being ingested.", example = "No Contract Related")
    private String contractId = "";

    @Schema(description = "The optional description if the LegalTag to allow for easier discoverability of Legaltags overtime.")
    @ValidDescription
    private String description = "";

    @Schema(description = "The optional expiration date of the contract in the format YYYY-MM-DD", example = "2025-12-25")
    private Date expirationDate;

    @Schema(description = "The optional object field to attach any company specific attributes.")
    private Map<String, Object> extensionProperties;
}
