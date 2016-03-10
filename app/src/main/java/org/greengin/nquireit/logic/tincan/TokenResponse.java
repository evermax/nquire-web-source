package org.greengin.nquireit.logic.tincan;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/**
 * POJO to parse the answer from the LRS
 * @author Maxime Lasserre <maxlasserre@free.fr>
 */
public class TokenResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @JsonProperty(value = "access_token")
    private String accessToken;
    @JsonProperty(value = "token_type")
    private String tokenType;
    @JsonProperty(value = "refresh_token")
    private String refreshToken;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
