package org.opengroup.osdu.legal;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opengroup.osdu.core.common.provider.interfaces.ITenantFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class LegalApplicationTests {

    @MockBean
    ITenantFactory tenantFactory;

    @Test
    public void contextLoads() throws Exception {
    }

}
