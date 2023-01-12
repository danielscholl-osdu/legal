package org.opengroup.osdu.legal.util;

import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class GoogleServiceAccount {

    public static final String DEFAULT_TARGET_AUDIENCE = "osdu";
    public GoogleServiceAccount(String serviceAccountEncoded)throws IOException {
        this(Base64.getDecoder().decode(serviceAccountEncoded));
    }
    public GoogleServiceAccount(byte[] serviceAccountJson)throws IOException {
        try(InputStream inputStream = new ByteArrayInputStream(serviceAccountJson)) {

            serviceAccount = ServiceAccountCredentials.fromStream(inputStream);
        }
    }

    final ServiceAccountCredentials serviceAccount;

    public String getEmail(){
        return serviceAccount.getClientEmail();
    }
    public String getAuthToken() throws IOException {
        JwtBuilder jwtBuilder = Jwts.builder();

        Map<String, Object> header = new HashMap<>();
        header.put("type", "JWT");
        header.put("alg", "RS256");
        jwtBuilder.setHeader(header);

        Map<String, Object> claims = new HashMap<>();
        claims.put("target_audience", DEFAULT_TARGET_AUDIENCE);
        claims.put("exp", System.currentTimeMillis() / 1000 + 3600);
        claims.put("iat", System.currentTimeMillis() / 1000);
        claims.put("iss", getEmail());
        claims.put("aud", "https://www.googleapis.com/oauth2/v4/token");
        jwtBuilder.addClaims(claims);

        jwtBuilder.signWith(SignatureAlgorithm.RS256, serviceAccount.getPrivateKey());
        String jwt = jwtBuilder.compact();

        HttpPost httpPost = new HttpPost("https://www.googleapis.com/oauth2/v4/token");

        ArrayList<NameValuePair> postParameters = new ArrayList<>();
        postParameters.add(new BasicNameValuePair("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer"));
        postParameters.add(new BasicNameValuePair("assertion", jwt));

        HttpClient client = new DefaultHttpClient();

        httpPost.setEntity(new UrlEncodedFormEntity(postParameters, "UTF-8"));
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        HttpResponse response = client.execute(httpPost);

        String responseEntity = EntityUtils.toString(response.getEntity());
        JsonObject content = new JsonParser().parse(responseEntity).getAsJsonObject();
        return content.get("id_token").getAsString();
    }
}
