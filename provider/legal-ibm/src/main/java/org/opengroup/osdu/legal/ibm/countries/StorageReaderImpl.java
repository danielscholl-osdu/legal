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

import static com.cloudant.client.api.query.Expression.eq;
import static com.cloudant.client.api.query.Operation.and;

import java.io.FileNotFoundException;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.util.concurrent.atomic.AtomicLong;

import javax.inject.Inject;

import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.ibm.cloudant.IBMCloudantClientFactory;
import org.opengroup.osdu.legal.provider.interfaces.IStorageReader;

import com.cloudant.client.api.CloudantClient;
import com.cloudant.client.api.Database;
import com.cloudant.client.api.query.QueryBuilder;
import com.cloudant.client.api.query.QueryResult;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;


/**
 * @author mbayser
 *
 */
public class StorageReaderImpl implements IStorageReader {
	
    private final TenantInfo tenantInfo;
    private final String cloudRegion;
    private final String dbNamePrefix;
    private final String dbName;
    private final IBMCloudantClientFactory cloudantFactory;
    private final CloudantClient cloudant;
    private static final long CACHE_EXPIRATION_MS = 60000;
    private AtomicLong lastUpdate = new AtomicLong(0);
    private WeakReference<byte[]> cache = new WeakReference<byte[]>(null);
     

    public StorageReaderImpl(TenantInfo tenantInfo, String projectRegion, IBMCloudantClientFactory cloudantFactory, String dbNamePrefix, String dbName) throws MalformedURLException, FileNotFoundException {
        this.tenantInfo = tenantInfo;
        this.cloudRegion = projectRegion;
        this.dbNamePrefix =  dbNamePrefix;
        this.dbName = dbName;
        this.cloudantFactory = cloudantFactory;
        this.cloudant = cloudantFactory.getClient();
    }
    
    @Inject
	private JaxRsDpsLog logger;

	/* (non-Javadoc)
	 * @see org.opengroup.osdu.legal.countries.StorageReader#readAllBytes()
	 */
	@Override
	public byte[] readAllBytes() {
		 //There is a race condition here, but other than a few wasteful HTTP requests it does no harm.
		// A mutex here on the other hand could lead to increased latency.
		if ((lastUpdate.get() + CACHE_EXPIRATION_MS) > System.currentTimeMillis()) {
			byte[] c = cache.get();
			if (c != null) {
				return c;
			}
		}
		
		try {
			
			Database db = cloudantFactory.getDatabase(cloudant, dbNamePrefix, dbName);
	        
	        QueryResult<JsonObject> result = db.query(new QueryBuilder(and(eq("tenant", tenantInfo.getName()), eq("region", cloudRegion)))
	        		.fields("name", "alpha2", "numeric", "residencyRisk", "typesNotApplyDataResidency").build(), JsonObject.class);
	        
	        // The encapsulation of the Database class sucks in this case. If we could grab its internal
	        // import com.cloudant.client.org.lightcouch.CouchDbClient we could avoid de-serializing the
	        // response just to serialize it again.
	        JsonArray array = new JsonArray();
	        for (JsonObject s: result.getDocs()) { 
	        	array.add(s);
	        }
	        byte[] blob = cloudant.getGson().toJson(array).getBytes();
	        
	        cache = new WeakReference<byte[]>(blob);
	        lastUpdate.set(System.currentTimeMillis());
	        
	        return blob;
		} catch (MalformedURLException e) {
		    logger.error(" 500, Malformed URL Invalid cloudant URL", e);
			throw new AppException(500, "Malformed URL", "Invalid cloudant URL", e);
		}
	}

}
