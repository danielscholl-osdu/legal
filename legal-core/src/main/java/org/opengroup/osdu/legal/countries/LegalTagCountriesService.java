package org.opengroup.osdu.legal.countries;

import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.springframework.stereotype.Service;
import org.opengroup.osdu.core.common.model.http.RequestInfo;
import org.opengroup.osdu.core.common.model.legal.ServiceConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;

@Service
public class LegalTagCountriesService {

    @Inject
    private LegalTagCountriesTenantRepositories repositories;

    @Inject
    private RequestInfo requestInfo;

    @Inject
    private ServiceConfig serviceConfig;

    @Inject
    private DefaultCountriesRepository defaultCountriesRepository;

    public Map<String, String> getValidCOOs() {
        Map<String,String> coos = new HashMap<>();

        TenantInfo tenant = requestInfo.getTenantInfo();
        LegalTagCountriesRepository legalTagCountriesRepository = repositories.get(tenant, serviceConfig.getRegion());

        generateCOOsFromRepository(coos, new DefaultCountriesRepository(), legalTagCountriesRepository, "none");
        return coos;
    }

    public Map<String, String> getValidCOOs(String dataType) {
        Map<String,String> coos = new HashMap<>();

        TenantInfo tenant = requestInfo.getTenantInfo();
        LegalTagCountriesRepository legalTagCountriesRepository = repositories.get(tenant, serviceConfig.getRegion());

        generateCOOsFromRepository(coos, defaultCountriesRepository, legalTagCountriesRepository, dataType);
        return coos;
    }

    public Map<String, String> getValidORDCs() {
        Map<String,String> ordcs = new HashMap<>();

        TenantInfo tenant = requestInfo.getTenantInfo();
        LegalTagCountriesRepository legalTagCountriesRepository = repositories.get(tenant, serviceConfig.getRegion());

        generateORDCsFromRepository(ordcs, defaultCountriesRepository);
        generateORDCsFromRepository(ordcs, legalTagCountriesRepository);
        return ordcs;
    }

    private void generateCOOsFromRepository(Map<String, String> coos, LegalTagCountriesRepository defaultCountriesRepository, LegalTagCountriesRepository cloudCountriesRepository, String dataType) {
        List<Country> cloudStorageCountries = this.mergeCountriesRepositories(defaultCountriesRepository, cloudCountriesRepository);
        for (Country country : cloudStorageCountries) {
            if (country.getResidencyRisk() != null &&
                    (country.getResidencyRisk().equals(Country.RESIDENCY_RISK.NO_RESTRICTION) ||
                            country.getResidencyRisk().equals(Country.RESIDENCY_RISK.NOT_ASSIGNED) ||
                            country.getResidencyRisk().equals(Country.RESIDENCY_RISK.CLIENT_CONSENT_REQUIRED))) {
                coos.put(country.getAlpha2(), country.getName());
            } else if (country.getTypesNotApplyDataResidency().contains(dataType)) {
                coos.put(country.getAlpha2(), country.getName());
            }
        }
    }

    private List<Country> mergeCountriesRepositories(LegalTagCountriesRepository defaultRepository, LegalTagCountriesRepository cloudRepository) {
        List<Country> sourceCountries = defaultRepository.read();
        List<Country> tenantCountries = cloudRepository.read();
        for (Country tenantCountry : tenantCountries) {
            for (int i = 0; i < sourceCountries.size(); i++) {
                if (sourceCountries.get(i).isMatchByAlpha2(tenantCountry)) {
                    sourceCountries.set(i, tenantCountry);
                }
            }
        }
        return sourceCountries;
    }


    private void generateORDCsFromRepository(Map<String, String> coos, LegalTagCountriesRepository legalTagCountriesRepository) {
        List<Country> cloudStorageCountries = legalTagCountriesRepository.read();
        for (Country country : cloudStorageCountries) {
            if (country.getResidencyRisk() != null && !country.getResidencyRisk().equals(Country.RESIDENCY_RISK.EMBARGOED)) {
                coos.put(country.getAlpha2(), country.getName());
            } else {
                coos.remove(country.getAlpha2());
            }
        }
    }
}