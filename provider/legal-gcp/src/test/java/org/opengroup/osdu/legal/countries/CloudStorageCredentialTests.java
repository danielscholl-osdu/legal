package org.opengroup.osdu.legal.countries;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.services.iam.v1.Iam;
import com.google.api.services.iam.v1.model.SignJwtRequest;
import com.google.api.services.iam.v1.model.SignJwtResponse;
import com.google.gson.JsonObject;
import org.opengroup.osdu.core.common.http.HttpClient;
import org.opengroup.osdu.core.common.http.HttpRequest;
import org.opengroup.osdu.core.common.http.HttpResponse;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ GoogleCredential.class, HttpRequest.class })
public class CloudStorageCredentialTests {
    private static final String SERVCE_ACCOUNT = "service@gserviceaccount.com";
    private static final String SIGNED_JWT_VALUE = "SIGNED JWT";
    private static final String TENANT_NAME = "MY_Tenant";

    private CloudStorageCredential sut;

    private TenantInfo tenant;

    @Mock
    private HttpClient httpClient;

    @Before
    public void setup() throws Exception {
        mockStatic(GoogleCredential.class);
        mockStatic(HttpRequest.class);

        when(GoogleCredential.getApplicationDefault()).thenReturn(mock(GoogleCredential.class));

        this.tenant = new TenantInfo();
        this.tenant.setName(TENANT_NAME);
        this.tenant.setServiceAccount(SERVCE_ACCOUNT);

        this.sut = new CloudStorageCredential(this.tenant);
    }

    @Test(expected = RuntimeException.class)
    public void should_returnRuntimeException_when_errorSigningJwt() throws Exception {
        SignJwtResponse signJwtResponse = new SignJwtResponse();
        signJwtResponse.setSignedJwt(SIGNED_JWT_VALUE);

        Iam.Projects.ServiceAccounts.SignJwt signJwt = mock(Iam.Projects.ServiceAccounts.SignJwt.class);
        when(signJwt.execute()).thenReturn(signJwtResponse);

        Iam.Projects.ServiceAccounts serviceAccounts = mock(Iam.Projects.ServiceAccounts.class);
        when(serviceAccounts.signJwt(any(), any())).thenReturn(signJwt);

        Iam.Projects projects = mock(Iam.Projects.class);
        when(projects.serviceAccounts()).thenReturn(serviceAccounts);

        this.sut.refreshAccessToken();
    }

    @Test(expected = RuntimeException.class)
    public void should_returnRuntimeException_when_requestToGoogleApiIsNot200() throws Exception {
        SignJwtResponse signJwtResponse = new SignJwtResponse();
        signJwtResponse.setSignedJwt(SIGNED_JWT_VALUE);

        Iam.Projects.ServiceAccounts.SignJwt signJwt = mock(Iam.Projects.ServiceAccounts.SignJwt.class);
        when(signJwt.execute()).thenReturn(signJwtResponse);

        ArgumentCaptor<String> serviceAccountCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<SignJwtRequest> payloadCaptor = ArgumentCaptor.forClass(SignJwtRequest.class);

        Iam.Projects.ServiceAccounts serviceAccounts = mock(Iam.Projects.ServiceAccounts.class);
        when(serviceAccounts.signJwt(serviceAccountCaptor.capture(), payloadCaptor.capture())).thenReturn(signJwt);

        Iam.Projects projects = mock(Iam.Projects.class);
        when(projects.serviceAccounts()).thenReturn(serviceAccounts);

        JsonObject jsonResult = new JsonObject();
        jsonResult.addProperty("access_token", "my access token");
        jsonResult.addProperty("expires_in", "3600");

        HttpResponse response = mock(HttpResponse.class);
        when(response.isSuccessCode()).thenReturn(false);
        when(this.httpClient.send(any())).thenReturn(response);

        Iam iam = mock(Iam.class);
        when(iam.projects()).thenReturn(projects);
        this.sut.setIam(iam);

        this.sut.refreshAccessToken();
    }
}
