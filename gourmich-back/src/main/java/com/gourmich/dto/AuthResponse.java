package com.gourmich.dto;

import lombok.Getter;

@Getter
public class AuthResponse {
    public String token;

    public AuthResponse(String token){
        this.token = token;
    }

}
