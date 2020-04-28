// (C) Copyright IBM Corporation 2019
// U.S. Government Users Restricted Rights:  Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.

package org.opengroup.osdu.legal.ibm.countries;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;

import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.ibm.auth.ServiceCredentials;
import org.opengroup.osdu.core.ibm.cloudant.IBMCloudantClientFactory;
import org.opengroup.osdu.legal.provider.interfaces.IStorageReader;
import org.opengroup.osdu.legal.provider.interfaces.IStorageReaderFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

/**
 * @author mbayser
 *
 */
@Component
public class StorageReaderFactoryImpl implements IStorageReaderFactory {
	
	private ConcurrentHashMap<String, IStorageReader> readers = new ConcurrentHashMap<>();
	
	@Value("${ibm.legal.db.url}") 
	private String dbUrl;
	@Value("${ibm.legal.db.apikey:#{null}}")
	private String apiKey;
	@Value("${ibm.legal.db.user:#{null}}")
	private String dbUser;
	@Value("${ibm.legal.db.password:#{null}}")
	private String dbPassword;
	@Value("${ibm.env.prefix:local-dev}")
	private String dbNamePrefix;
	
	@Value("${ibm.legal.db.credentials:#{null}}")
	private String credentialsJSON;
	
	@Value("${ibm.countries.db.name:countries}")
	private String dbName;
	
	@Inject
	private ResourceLoader resourceLoader;
	
	@Inject
	private JaxRsDpsLog logger;
	
	
	/* (non-Javadoc)
	 * @see org.opengroup.osdu.legal.countries.StorageReaderFactory#getReader(org.opengroup.osdu.core.multitenancy.TenantInfo, java.lang.String)
	 */
	@Override
	
	public IStorageReader getReader(TenantInfo tenant, String projectRegion) {
		
			ServiceCredentials creds = null;
			
			if (dbUrl != null && apiKey != null) {
				creds = new ServiceCredentials(dbUrl, apiKey);
			} else if (dbUrl != null && dbUser != null) {
				creds = new ServiceCredentials(dbUrl, dbUser, dbPassword);
			} else {
				try {
					creds = new ServiceCredentials(new InputStreamReader(resourceLoader.getResource(credentialsJSON).getInputStream()));
				} catch (IOException e) {
					logger.error(" 500, Malformed URL Invalid cloudant URL", e);
					throw new AppException(500, "Malformed URL", "Invalid cloudant URL", e);
				}
			} 
				
			IBMCloudantClientFactory cloudantFactory = new IBMCloudantClientFactory(creds);

			return getReader(cloudantFactory, tenant, projectRegion, dbNamePrefix, dbName);		
	}
	
	public IStorageReader getReader(IBMCloudantClientFactory cloudantFactory, TenantInfo tenant, String projectRegion, String dbNamePrefix, String dbName) {

		final String key = tenant+":"+projectRegion;
		readers.computeIfAbsent(key, s -> {
			try {		
				return new StorageReaderImpl(tenant, projectRegion, cloudantFactory, dbNamePrefix, dbName);
			} catch (MalformedURLException | FileNotFoundException e) {
				logger.error("Error creating a Storage Reader", e);
				return null;
			}
		});
		return readers.get(key);		
	}
}