package org.opengroup.osdu.legal.api;

import com.google.gson.Gson;
import java.util.Collections;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.opengroup.osdu.core.common.model.http.AppError;
import org.opengroup.osdu.legal.countries.LegalTagCountriesService;
import org.opengroup.osdu.legal.logging.AuditLogger;
import org.opengroup.osdu.legal.tags.LegalTagService;
import org.opengroup.osdu.legal.tags.dto.*;
import org.opengroup.osdu.core.common.model.legal.AllowedLegaltagPropertyValues;
import org.opengroup.osdu.core.common.model.legal.validation.ValidName;
import org.opengroup.osdu.core.common.model.http.RequestInfo;
import org.opengroup.osdu.core.common.model.legal.ServiceConfig;

import javax.validation.Valid;
import javax.validation.ValidationException;
import javax.validation.constraints.NotNull;
import javax.inject.Inject;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(path= "/")
@Validated
@Tag(name = "legaltag", description = "LegalTags related endpoints")
public class LegalTagApi {

    private RequestInfo requestInfo;
    @Inject //injectMock only works on setter DI
    public void setRequestInfo(RequestInfo requestInfo)
    {
        this.requestInfo = requestInfo;
    }

    private LegalTagService legalTagService;
    @Inject //injectMock only works on setter DI
    public void setLegalTagService(LegalTagService legalTagService)
    {
        this.legalTagService = legalTagService;
    }

    @Inject
    private AllowedLegaltagPropertyValues allowedLegaltagPropertyValues;

    @Inject
    private AuditLogger auditLogger;
    
    @Inject
    private LegalTagCountriesService legalTagCountriesService;

    @Operation(summary = "${legalTagApi.createLegalTag.summary}", description = "${legalTagApi.createLegalTag.description}",
            security = {@SecurityRequirement(name = "Authorization")}, tags = { "legaltag" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created LegalTag successfully.", content = { @Content(schema = @Schema(implementation = LegalTagDto.class)) }),
            @ApiResponse(responseCode = "400", description = "Bad Request",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "403", description = "User not authorized to perform the action.",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "404", description = "Not Found",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "409", description = "A LegalTag with the given name already exists.",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "502", description = "Bad Gateway",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "503", description = "Service Unavailable",  content = {@Content(schema = @Schema(implementation = AppError.class ))})
    })
    @PreAuthorize("@authorizationFilter.hasPermission('" + ServiceConfig.LEGAL_EDITOR + "', '" + ServiceConfig.LEGAL_ADMIN + "')")
    @PostMapping("/legaltags")
    public ResponseEntity<LegalTagDto> createLegalTag(@NotNull @RequestBody LegalTagDto legalTag) {
        LegalTagDto output = legalTagService.create(legalTag, requestInfo.getTenantInfo().getName());
        return new ResponseEntity<LegalTagDto>(output, HttpStatus.CREATED);
    }

    @Operation(summary = "${legalTagApi.updateLegalTag.summary}", description = "${legalTagApi.updateLegalTag.description}",
            security = {@SecurityRequirement(name = "Authorization")}, tags = { "legaltag" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated LegalTag successfully.", content = { @Content(schema = @Schema(implementation = LegalTagDto.class)) }),
            @ApiResponse(responseCode = "400", description = "Bad Request",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "403", description = "User not authorized to perform the action.",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "404", description = "Requested LegalTag to update was not found.",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "409", description = "A LegalTag with the given name already exists.",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "502", description = "Bad Gateway",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "503", description = "Service Unavailable",  content = {@Content(schema = @Schema(implementation = AppError.class ))})
    })
    @PreAuthorize("@authorizationFilter.hasPermission('" + ServiceConfig.LEGAL_EDITOR + "', '" + ServiceConfig.LEGAL_ADMIN + "')")
    @PutMapping("/legaltags")
    public ResponseEntity<LegalTagDto> updateLegalTag(@Valid @NotNull @RequestBody UpdateLegalTag legalTag) {
        LegalTagDto output = legalTagService.update(legalTag, requestInfo.getTenantInfo().getName());
        return new ResponseEntity<LegalTagDto>(output, HttpStatus.OK);
    }

    @Operation(summary = "${legalTagApi.listLegalTags.summary}", description = "${legalTagApi.listLegalTags.description}",
            security = {@SecurityRequirement(name = "Authorization")}, tags = { "legaltag" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Retrieved LegalTags successfully.", content = { @Content(schema = @Schema(implementation = LegalTagDtos.class)) }),
            @ApiResponse(responseCode = "400", description = "Bad Request",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "403", description = "User not authorized to perform the action.",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "404", description = "Requested LegalTag to update was not found.",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "502", description = "Bad Gateway",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "503", description = "Service Unavailable",  content = {@Content(schema = @Schema(implementation = AppError.class ))})
    })
    @PreAuthorize("@authorizationFilter.hasPermission('" + ServiceConfig.LEGAL_USER + "', '" + ServiceConfig.LEGAL_EDITOR + "', '" + ServiceConfig.LEGAL_ADMIN + "')")
    @GetMapping("/legaltags")
    public ResponseEntity<LegalTagDtos> listLegalTags(@Parameter(description = "If true returns only valid LegalTags, if false returns only invalid LegalTags.  Default value is true.")
                                       @RequestParam(name = "valid", required = false, defaultValue = "true") boolean valid) {
        if (requestInfo.getTenantInfo() == null) {
            throw new ValidationException("No tenant supplied");
        }
        LegalTagDtos output = legalTagService.list(valid, requestInfo.getTenantInfo().getName());
        return new ResponseEntity<LegalTagDtos>(output, HttpStatus.OK);
    }

    @Operation(summary = "${legalTagApi.getLegalTag.summary}", description = "${legalTagApi.getLegalTag.description}",
            security = {@SecurityRequirement(name = "Authorization")}, tags = { "legaltag" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Retrieved LegalTag successfully.", content = { @Content(schema = @Schema(implementation = LegalTagDto.class)) }),
            @ApiResponse(responseCode = "400", description = "Bad Request",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "403", description = "User not authorized to perform the action.",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "404", description = "Requested LegalTag was not found.",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "502", description = "Bad Gateway",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "503", description = "Service Unavailable",  content = {@Content(schema = @Schema(implementation = AppError.class ))})
    })
    @PreAuthorize("@authorizationFilter.hasPermission('" + ServiceConfig.LEGAL_USER + "', '" + ServiceConfig.LEGAL_EDITOR + "', '" + ServiceConfig.LEGAL_ADMIN + "')")
    @GetMapping("/legaltags/{name}")
    public ResponseEntity getLegalTag(@Parameter(description = "Name of the LegalTag", example = "OSDU-Private-USA-EHC")
                                          @PathVariable("name") @ValidName String name) {
        LegalTagDto output = legalTagService.get(name, requestInfo.getTenantInfo().getName());

        if (output == null)
            return new ResponseEntity<>(createNotFoundBody(), HttpStatus.NOT_FOUND);
        else {
            return new ResponseEntity<LegalTagDto>(output, HttpStatus.OK);
        }
    }

    @Operation(summary = "${legalTagApi.deleteLegalTag.summary}", description = "${legalTagApi.deleteLegalTag.description}",
            security = {@SecurityRequirement(name = "Authorization")}, tags = { "legaltag" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "LegalTag deleted successfully.", content = { @Content(schema = @Schema(implementation = HttpStatus.class)) }),
            @ApiResponse(responseCode = "400", description = "Bad Request",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "403", description = "User not authorized to perform the action.",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "404", description = "Requested LegalTag to delete was not found.",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "502", description = "Bad Gateway",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "503", description = "Service Unavailable",  content = {@Content(schema = @Schema(implementation = AppError.class ))})
    })
    @PreAuthorize("@authorizationFilter.hasPermission('" + ServiceConfig.LEGAL_ADMIN + "')")
    @DeleteMapping("/legaltags/{name}")
    public ResponseEntity<HttpStatus> deleteLegalTag(@Parameter(description = "Name of the LegalTag to delete", example = "OSDU-Private-USA-EHC")
                                                         @PathVariable("name") @ValidName String name) {
        if (legalTagService.delete(requestInfo.getTenantInfo().getProjectId(), name, requestInfo.getHeaders(), requestInfo.getTenantInfo().getName()))
            return new ResponseEntity<HttpStatus>(HttpStatus.NO_CONTENT);
        else
            return new ResponseEntity<HttpStatus>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Operation(summary = "${legalTagApi.getLegalTags.summary}", description = "${legalTagApi.getLegalTags.description}",
            security = {@SecurityRequirement(name = "Authorization")}, tags = { "legaltag" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Retrieved LegalTags successfully.", content = { @Content(schema = @Schema(implementation = LegalTagDtos.class)) }),
            @ApiResponse(responseCode = "400", description = "Bad Request",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "403", description = "User not authorized to perform the action.",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "404", description = "One or more requested LegalTags were not found.",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "502", description = "Bad Gateway",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "503", description = "Service Unavailable",  content = {@Content(schema = @Schema(implementation = AppError.class ))})
    })
    @PreAuthorize("@authorizationFilter.hasPermission('" + ServiceConfig.LEGAL_USER + "', '" + ServiceConfig.LEGAL_EDITOR + "', '" + ServiceConfig.LEGAL_ADMIN + "')")
    @PostMapping("/legaltags:batchRetrieve")
    public ResponseEntity<LegalTagDtos> getLegalTags(@Valid @NotNull @RequestBody RequestLegalTags requestedTags) {
        String[] names =  requestedTags.getNames().stream().toArray(String[]::new);
        LegalTagDtos result = legalTagService.getBatch(names, requestInfo.getTenantInfo().getName());

        if (result == null || result.getLegalTags().size() != requestedTags.getNames().size()) {
            return new ResponseEntity<LegalTagDtos>(result, HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<LegalTagDtos>(result, HttpStatus.OK);
        }
    }

    @Operation(summary = "${legalTagApi.validateLegalTags.summary}", description = "${legalTagApi.validateLegalTags.description}",
            security = {@SecurityRequirement(name = "Authorization")}, tags = { "legaltag" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Retrieved LegalTag names with reason successfully.", content = { @Content(schema = @Schema(implementation = InvalidTagsWithReason.class)) }),
            @ApiResponse(responseCode = "400", description = "Bad Request",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "403", description = "User not authorized to perform the action.",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "404", description = "LegalTag names were not found.",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "502", description = "Bad Gateway",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "503", description = "Service Unavailable",  content = {@Content(schema = @Schema(implementation = AppError.class ))})
    })
    @PreAuthorize("@authorizationFilter.hasPermission('" + ServiceConfig.LEGAL_USER + "', '" + ServiceConfig.LEGAL_EDITOR + "', '" + ServiceConfig.LEGAL_ADMIN + "')")
    @PostMapping("/legaltags:validate")
    public ResponseEntity<InvalidTagsWithReason> validateLegalTags(@Valid @NotNull @RequestBody RequestLegalTags requestedTags) {
        InvalidTagsWithReason result = legalTagService.validate(requestedTags.getNames().toArray(new String[0]),
                requestInfo.getTenantInfo().getName());

        return new ResponseEntity<InvalidTagsWithReason>(result, HttpStatus.OK);
    }

    @Operation(summary = "${legalTagApi.getLegalTagProperties.summary}", description = "${legalTagApi.getLegalTagProperties.description}",
            security = {@SecurityRequirement(name = "Authorization")}, tags = { "legaltag" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Retrieved LegalTag properties successfully.", content = { @Content(schema = @Schema(implementation = ReadablePropertyValues.class)) }),
            @ApiResponse(responseCode = "400", description = "Bad Request",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "403", description = "User not authorized to perform the action.",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "502", description = "Bad Gateway",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "503", description = "Service Unavailable",  content = {@Content(schema = @Schema(implementation = AppError.class ))})
    })
    @PreAuthorize("@authorizationFilter.hasPermission('" + ServiceConfig.LEGAL_USER + "', '" + ServiceConfig.LEGAL_EDITOR + "', '" + ServiceConfig.LEGAL_ADMIN + "')")
    @GetMapping("/legaltags:properties")
    public ResponseEntity<ReadablePropertyValues> getLegalTagProperties() {
        ReadablePropertyValues output = new ReadablePropertyValues();
        output.setCountriesOfOrigin(legalTagCountriesService.getValidCOOs());
        output.setOtherRelevantDataCountries(legalTagCountriesService.getValidORDCs());
        output.setExportClassificationControlNumbers(allowedLegaltagPropertyValues.getEccns());
        output.setPersonalDataTypes(allowedLegaltagPropertyValues.getPersonalDataType());
        output.setSecurityClassifications(allowedLegaltagPropertyValues.getSecurityClassifications());
        output.setDataTypes(allowedLegaltagPropertyValues.getDataTypes());
        auditLogger.readLegalPropertiesSuccess(Collections.singletonList(output.toString()));

        return new ResponseEntity<ReadablePropertyValues>(output, HttpStatus.OK);
    }

    private String createNotFoundBody() {
        final Map<String, String> body = new HashMap<>();
        body.put("error", "Not found.");
        return new Gson().toJson(body);
    }
}
