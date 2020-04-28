package org.opengroup.osdu.legal.ibm.acceptanceTests.util;

import org.opengroup.osdu.legal.util.LegalTagUtils;

public class IBMLegalTagUtils extends LegalTagUtils {

	public IBMLegalTagUtils() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void uploadTenantTestingConfigFile() {
		// TODO Auto-generated method stub

	}

	@Override
	public String accessToken() throws Exception {
		return System.getenv("LEGAL_TEST_TOKEN");
	}
}
