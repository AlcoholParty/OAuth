package com.springsecurity.oauth.dto;

import lombok.Getter;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.io.Serializable;

@Getter
public class SessionUserDTO implements Serializable {
    private String name;
    private String email;

    public SessionUserDTO(OAuthDTO oAuthDTO) {
        this.name = oAuthDTO.getName();
        this.email = oAuthDTO.getEmail();
    }
}
