package org.opengroup.osdu.legal.tags.validation;

import com.google.common.base.Strings;
import org.opengroup.osdu.legal.countries.LegalTagCountriesService;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;

public class OtherRelevantDataCountriesValidator implements ConstraintValidator<ValidOtherRelevantDataCountries, List<String>> {

    private LegalTagCountriesService legalTagCountriesService;

    public OtherRelevantDataCountriesValidator(LegalTagCountriesService legalTagCountriesService) {
        this.legalTagCountriesService = legalTagCountriesService;
    }

    @Override
    public void initialize(ValidOtherRelevantDataCountries constraintAnnotation) {
        //needed by interface - we don't use
    }

    @Override
    public boolean isValid(List<String> countries, ConstraintValidatorContext context) {
        if(countries == null)
            return true;

        for(String country : countries){
            if (Strings.isNullOrEmpty(country) || !legalTagCountriesService.getValidORDCs().containsKey(country))
                return false;
        }
        return true;
    }
}
