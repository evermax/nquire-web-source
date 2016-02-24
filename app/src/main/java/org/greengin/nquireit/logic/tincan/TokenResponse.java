package org.greengin.nquireit.logic.tincan;

import java.io.Serializable;

/**
 * POJO to parse the answer from the LRS
 * @author Maxime Lasserre <maxlasserre@free.fr>
 */
public class TokenResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String accessToken;
    private String tokenType;
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
