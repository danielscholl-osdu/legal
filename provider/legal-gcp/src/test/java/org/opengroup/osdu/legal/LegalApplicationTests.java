package org.opengroup.osdu.legal;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.opengroup.osdu.core.common.provider.interfaces.ITenantFactory;
import org.opengroup.osdu.legal.di.TenantFactoryService;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class LegalApplicationTests {

	@MockBean
	TenantFactoryService tenantFactoryService;

	@MockBean
	ITenantFactory tenantFactory;

	@Test
	public void contextLoads() throws Exception {
		Mockito.when(tenantFactoryService.getObject()).thenReturn(tenantFactory);
	}

}
