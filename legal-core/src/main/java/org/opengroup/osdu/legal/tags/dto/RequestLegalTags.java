package org.opengroup.osdu.legal.tags.dto;

import org.opengroup.osdu.core.common.model.legal.validation.ValidName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestLegalTags {

    @NotNull
    @Size(min=1, max=25)
    @Valid
    List<@ValidName String> names = new ArrayList<>();
}
