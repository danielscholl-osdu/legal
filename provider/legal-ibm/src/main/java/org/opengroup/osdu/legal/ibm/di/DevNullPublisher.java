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

package org.opengroup.osdu.legal.ibm.di;


import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.legal.StatusChangedTags;
import org.opengroup.osdu.legal.provider.interfaces.ILegalTagPublisher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * @author mbayser
 *
 */
@ConditionalOnProperty(
	    value="ibm.legal.publisher.devnull", 
	    havingValue = "true", 
	    matchIfMissing = false)
@Service
public class DevNullPublisher implements ILegalTagPublisher {


	/* (non-Javadoc)
	 * @see org.opengroup.osdu.legal.provider.interfaces.LegalTagPublisher#publish(java.lang.String, org.opengroup.osdu.core.api.DpsHeaders, org.opengroup.osdu.legal.jobs.StatusChangedTags)
	 */
	@Override
	public void publish(String projectId, DpsHeaders headers, StatusChangedTags tags) throws Exception {
		// TODO Auto-generated method stub

	}

}
