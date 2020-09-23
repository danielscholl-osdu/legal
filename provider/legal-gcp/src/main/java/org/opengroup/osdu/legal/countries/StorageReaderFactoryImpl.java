package org.opengroup.osdu.legal.countries;

import org.opengroup.osdu.core.common.model.tenant.TenantInfo;

import org.opengroup.osdu.legal.provider.interfaces.IStorageReader;
import org.opengroup.osdu.legal.provider.interfaces.IStorageReaderFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class StorageReaderFactoryImpl implements IStorageReaderFactory {

  @Value("#{new Boolean('${enable.full.bucket.name}')}")
  private Boolean isFullBucketName;

	@Override
    public IStorageReader getReader(TenantInfo tenant, String projectRegion) {
        return new StorageReaderImpl(tenant, projectRegion, isFullBucketName);
    }
}
