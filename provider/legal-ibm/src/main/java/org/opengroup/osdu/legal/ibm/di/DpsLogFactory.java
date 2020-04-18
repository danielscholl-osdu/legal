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

import org.opengroup.osdu.core.common.logging.ILogger;
import org.opengroup.osdu.core.ibm.logging.logger.IBMLoggingProvider;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.stereotype.Component;

/**
 * @author mbayser
 *
 */
//@Component
public class DpsLogFactory extends AbstractFactoryBean<ILogger> {

	private IBMLoggingProvider ibmLoggingProvider = new IBMLoggingProvider();

	@Override
	protected ILogger createInstance() throws Exception {
		return ibmLoggingProvider.getLogger();
	}

	@Override
	public Class<?> getObjectType() {
		return ILogger.class;
	}
}