package com.imovel.api.request;

import lombok.Data;

@Data
public class UserLoginRequest {
    private String email;
    private String password;
}