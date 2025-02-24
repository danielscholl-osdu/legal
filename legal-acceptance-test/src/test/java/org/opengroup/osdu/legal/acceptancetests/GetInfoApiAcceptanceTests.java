package org.opengroup.osdu.legal.acceptancetests;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opengroup.osdu.legal.util.AcceptanceBaseTest;
import org.opengroup.osdu.legal.util.LegalTagUtils;
import org.opengroup.osdu.legal.util.VersionInfoUtils;

import com.sun.jersey.api.client.ClientResponse;

public final class GetInfoApiAcceptanceTests extends AcceptanceBaseTest {

  private static final VersionInfoUtils VERSION_INFO_UTILS = new VersionInfoUtils();

  @BeforeEach
  @Override
  public void setup() throws Exception {
      this.legalTagUtils = new LegalTagUtils();
      super.setup();
  }

  @AfterEach
  @Override
  public void teardown() throws Exception {
      super.teardown();
      this.legalTagUtils = null;
  }
  
  @Test
  public void should_returnInfo() throws Exception {
    ClientResponse response = send(StringUtils.EMPTY, HttpStatus.SC_OK);
    VersionInfoUtils.VersionInfo responseObject =
        VERSION_INFO_UTILS.getVersionInfoFromResponse(response);

    assertNotNull(responseObject.groupId);
    assertNotNull(responseObject.artifactId);
    assertNotNull(responseObject.version);
    assertNotNull(responseObject.buildTime);
    assertNotNull(responseObject.branch);
    assertNotNull(responseObject.commitId);
    assertNotNull(responseObject.commitMessage);
  }

  @Override
  protected String getApi() {
    return "info";
  }

  @Override
  protected String getHttpMethod() {
    return "GET";
  }

  @Override
  public void should_return307_when_makingHttpRequest() throws Exception {
    // not actual for this endpoint
  }

  @Override
  public void should_return401_when_makingHttpRequestWithoutToken() throws Exception {
    // not actual for this endpoint
  }
}
