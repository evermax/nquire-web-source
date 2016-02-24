package org.greengin.nquireit.logic.tincan;

import gov.adlnet.xapi.client.StatementClient;
import gov.adlnet.xapi.model.Statement;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.ArrayList;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.logging.log4j.LogManager;
import org.springframework.http.MediaType;

/**
 * Provide a client to be able to request token for an user on his personal LRS
 * The method used is Authorization Code Grant: http://tools.ietf.org/html/rfc6749#section-4.1
 * So it needs to be provided with the authorization code.
 * This client wrap the postStatement method from StatementClient to use Oauth2 tokens
 * instead of basic authentication.
 * @author Maxime Lasserre <maxlasserre@free.fr>
 */
public class OAuth2Client extends StatementClient {
    private static final String GRANT_TYPE = "grant_type";
    private static final String CLIENT_ID = "client_id";
    private static final String CLIENT_SECRET = "client_secret";
    private static final String CODE = "code";
    private static final String REFRESH_TOKEN = "refresh_token";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";

    private final String clientId;
    private final String clientSecret;
    private final String accessTokenUri;
    private final String authorizationCode;
    private final String refreshToken;
    
    private HttpClient httpClient;
    
    public OAuth2Client (String uri, String clientId, String clientSecret,
            String accessTokenUri, String authorizationCode, String refreshToken) throws MalformedURLException {
        super(uri, "", "");
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.accessTokenUri = accessTokenUri;
        this.authorizationCode = authorizationCode;
        this.refreshToken = refreshToken;
    }
    
    public String PostStatementWithToken(String token, Statement statement) throws IOException {
        this.authString = "Bearer " + token;
        try {
            return this.postStatement(statement);
        } catch (IOException e) {
            if (e.getMessage().contains("401")) {
                TokenResponse tokenResponse = this.refreshToken();
                if (token != null) {
                    // Persist token
                    this.authString = "Bearer " + tokenResponse.getAccessToken();
                    return this.postStatement(statement);
                }
            }
            throw(e);
        }
    }
    
    public TokenResponse requestToken() {
        TokenResponse response;
        
        HttpPost request = new HttpPost(accessTokenUri);
        HttpEntity entity;
        
        List<NameValuePair> params = new ArrayList<NameValuePair>(4);
        params.add(new BasicNameValuePair(CLIENT_ID, this.clientId));
        params.add(new BasicNameValuePair(CLIENT_SECRET, this.clientSecret));
        params.add(new BasicNameValuePair(GRANT_TYPE, GrantType.AUTHORIZATION_CODE.getName()));
        params.add(new BasicNameValuePair(CODE, this.authorizationCode));
        try {
            entity = new UrlEncodedFormEntity(params);
        } catch (UnsupportedEncodingException e) {
            LogManager.getLogger(OAuth2Client.class).error("An error occured while requesting auth token for LRS", e);
            return null;
        }
        
        request.setEntity(entity);
        request.setHeader(CONTENT_TYPE_HEADER, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        try {
            response = this.doRequest(request, 200);
        } catch (IOException e) {
            LogManager.getLogger(OAuth2Client.class).error("An error occured while making the request", e);
            return null;
        }
        return response;
    }
    
    private TokenResponse refreshToken() {
        TokenResponse response;
        
        HttpPost request = new HttpPost(accessTokenUri);
        HttpEntity entity;
        
        List<NameValuePair> params = new ArrayList<NameValuePair>(4);
        params.add(new BasicNameValuePair(CLIENT_ID, this.clientId));
        params.add(new BasicNameValuePair(CLIENT_SECRET, this.clientSecret));
        params.add(new BasicNameValuePair(GRANT_TYPE, GrantType.REFRESH_TOKEN.getName()));
        params.add(new BasicNameValuePair(REFRESH_TOKEN, this.refreshToken));
        try {
            entity = new UrlEncodedFormEntity(params);
        } catch (UnsupportedEncodingException e) {
            LogManager.getLogger(OAuth2Client.class).error("Encoding problem for refresh token request", e);
            return null;
        }
        
        request.setEntity(entity);
        request.setHeader(CONTENT_TYPE_HEADER, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        try {
            response = this.doRequest(request, 200);
        } catch (IOException e) {
            LogManager.getLogger(OAuth2Client.class).error("Request error while refreshing token", e);
            return null;
        }
        return response;
    }

    private TokenResponse doRequest( HttpUriRequest request, final int expectedResponseStatus ) throws IOException {
        httpClient = HttpClients.createDefault();

        ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
            @Override
            public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
                int status = response.getStatusLine().getStatusCode();
                if ( status == expectedResponseStatus ) {
                    HttpEntity entity = response.getEntity();
                    return entity != null ? EntityUtils.toString(entity) : null;
                } else {
                    throw new ClientProtocolException("Unexpected response status: " + status + "\n"
                            + IOUtils.toString(response.getEntity().getContent()));
                }
            }
        };

        String responseBody = httpClient.execute( request, responseHandler );
        return new ObjectMapper().readValue( responseBody, TokenResponse.class );
    }
    
    public enum GrantType {
        REFRESH_TOKEN("refresh_token"), AUTHORIZATION_CODE("authorization_code");
        private final String name;
        
        GrantType(String name) {
            this.name = name;
        }
        
        public String getName() {
            return name;
        }
    }
}
