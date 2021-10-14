package org.opengroup.osdu.legal.aws.tags.dataaccess;


import org.apache.commons.lang3.StringUtils;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.common.provider.interfaces.ITenantFactory;
import org.opengroup.osdu.legal.provider.interfaces.ILegalTagRepository;
import org.opengroup.osdu.legal.provider.interfaces.ILegalTagRepositoryFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
@Primary
public class LegalTagRepositoryFactoryAwsImpl implements ILegalTagRepositoryFactory {


	private TenantInfo tenantInfo;
	private ITenantFactory tenantFactory;

	@Inject
	LegalTagRepositoryImpl repoImpl;

	public LegalTagRepositoryFactoryAwsImpl(TenantInfo tenantInfo, ITenantFactory tenantFactory) {
		this.tenantInfo = tenantInfo;
		this.tenantFactory = tenantFactory;
	}

	@Override
	public ILegalTagRepository get(String tenantName) {
		if (StringUtils.isBlank(tenantName)) {
			throw invalidTenantGivenException(tenantName);
		}
		TenantInfo tenantInfo = tenantFactory.getTenantInfo(tenantName);
		repoImpl.setTenantInfo(tenantInfo);

		return repoImpl;
	}

	AppException invalidTenantGivenException(String tenantName) {
		return new AppException(403, "Forbidden",
				String.format("You do not have access to the %s value given %s",
						DpsHeaders.ACCOUNT_ID, tenantName));
	}
}
