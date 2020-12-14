package org.opengroup.osdu.legal.countries;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Strings;
import com.google.api.services.iam.v1.Iam;
import com.google.api.services.iam.v1.IamScopes;
import com.google.api.services.iam.v1.Iam.Projects.ServiceAccounts.SignJwt;
import com.google.api.services.iam.v1.model.SignJwtRequest;
import com.google.api.services.storage.StorageScopes;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.net.HttpHeaders;
import com.google.gson.JsonObject;

import com.google.gson.JsonParser;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.opengroup.osdu.core.common.http.HttpClient;
import org.opengroup.osdu.core.common.http.HttpRequest;
import org.opengroup.osdu.core.common.http.HttpResponse;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.common.model.legal.ServiceConfig;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.CompletionException;

import org.apache.commons.lang3.time.DateUtils;

public class CloudStorageCredential extends GoogleCredentials {
    private static final long serialVersionUID = -8461791038757192780L;
    private static final String JWT_AUDIENCE = "https://www.googleapis.com/oauth2/v4/token";
    private static final String SERVICE_ACCOUNT_NAME_FORMAT = "projects/-/serviceAccounts/%s";
    private static final JsonFactory JSON_FACTORY = new JacksonFactory();
    private transient Iam iam;

    private final transient TenantInfo tenant;
    private final transient HttpClient httpClient;
    static final transient JsonParser parser = new JsonParser();

    public CloudStorageCredential(TenantInfo tenant) {
        this.tenant = tenant;
        this.httpClient = new HttpClient();
    }

    @Override
    public AccessToken refreshAccessToken() {
        String signedJwt = this.signJwt();
        return this.exchangeForAccessToken(signedJwt);
    }

    private String signJwt() {
        String issuer = this.tenant.getServiceAccount();
        String subject = "";
        String signingServiceAccountEmail = this.tenant.getServiceAccount();

        try {
            SignJwtRequest signJwtRequest = new SignJwtRequest();
            signJwtRequest.setPayload(this.getPayload(issuer, subject));

            SignJwt signJwt = this.getIam().projects().serviceAccounts()
                    .signJwt(String.format(SERVICE_ACCOUNT_NAME_FORMAT, signingServiceAccountEmail), signJwtRequest);

            return signJwt.execute().getSignedJwt();

        } catch (Exception e) {
            throw new CompletionException("Error signing jwt. See inner exception for more details.", e);
        }
    }

    private AccessToken exchangeForAccessToken(String signedJwt) {
        HttpRequest request = HttpRequest.post().url(JWT_AUDIENCE)
                .headers(Collections.singletonMap(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded"))
                .body(String.format("%s=%s&%s=%s", "grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer",
                        "assertion", signedJwt))
                .build();
        HttpResponse response = this.httpClient.send(request);
        JsonObject jsonResult = StringUtils.isBlank(response.getBody()) ? null : parser.parse(response.getBody()).getAsJsonObject();

        if (!response.isSuccessCode() || Objects.isNull(jsonResult) ||!jsonResult.has("access_token")) {
            throw new CompletionException("Error retrieving refresh token from Google. " + response.getBody(),
                    response.getException());
        }

        return new AccessToken(jsonResult.get("access_token").getAsString(),
                DateUtils.addSeconds(new Date(), jsonResult.get("expires_in").getAsInt()));
    }

    private String getPayload(String issuer, String subject) {
        JsonObject payload = new JsonObject();

        if (!Strings.isNullOrEmpty(subject)) {
            payload.addProperty("sub", subject);
        }

        payload.addProperty("iss", issuer);
        payload.addProperty("scope", StorageScopes.DEVSTORAGE_FULL_CONTROL);
        payload.addProperty("aud", JWT_AUDIENCE);
        payload.addProperty("iat", System.currentTimeMillis() / 1000);

        return payload.toString();
    }

    protected void setIam(Iam iam) {
        this.iam = iam;
    }

    private Iam getIam() throws IOException, GeneralSecurityException {
		if (this.iam == null) {
			HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

			GoogleCredential credential = GoogleCredential.getApplicationDefault();
			if (credential.createScopedRequired()) {
				List<String> scopes = new ArrayList<>();
				scopes.add(IamScopes.CLOUD_PLATFORM);
				credential = credential.createScoped(scopes);
			}

			this.iam = new Iam.Builder(httpTransport, JSON_FACTORY, credential)
					.setApplicationName(ServiceConfig.Instance().getHostname())
					.build();
		}
        return this.iam;
    }
}
