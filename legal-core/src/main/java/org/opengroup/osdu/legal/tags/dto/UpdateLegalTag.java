package org.opengroup.osdu.legal.tags.dto;

import  java.sql.Date;
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
public class UpdateLegalTag {
    @ValidName
    private String name = "";

    private String contractId = "";

    @ValidDescription
    private String description = "";

    private Date expirationDate;
}
