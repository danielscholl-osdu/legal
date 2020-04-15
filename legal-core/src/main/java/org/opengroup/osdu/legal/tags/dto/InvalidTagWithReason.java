package org.opengroup.osdu.legal.tags.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvalidTagWithReason {
    private String name;

    private String reason;
}
