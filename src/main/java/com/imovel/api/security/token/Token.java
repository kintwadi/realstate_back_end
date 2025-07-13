package com.imovel.api.security.token;

import java.io.Serializable;

public class Token implements Serializable {
    private String accessToken;
    private String refreshToken;

    /**
     * Constructs a new Token instance with the provided access and refresh
     * tokens.
     *
     * @param accessToken The JWT access token used for authenticated requests.
     * @param refreshToken The refresh token used to obtain a new access token.
     */
    public Token( String accessToken,
                  String refreshToken )
    {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    // Getters
    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }


}