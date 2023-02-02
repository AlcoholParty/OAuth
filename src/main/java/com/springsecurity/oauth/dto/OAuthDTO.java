package com.springsecurity.oauth.dto;

import com.springsecurity.oauth.entity.Member;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
public class OAuthDTO {
    private Map<String, Object> attributes;
    private String nameAttributeKey;
    private String name;
    private String email;

    @Builder
    public OAuthDTO(Map<String, Object> attributes, String nameAttributeKey, String name, String email, String picture) {
        this.attributes = attributes;
        this.nameAttributeKey = nameAttributeKey;
        this.name = name;
        this.email = email;
    }

    public static OAuthDTO of(String registrationId, String userNameAttributeName, Map<String, Object> oAuth2User) {
        return OAuthDTO.builder()
                .name((String) oAuth2User.get("name"))
                .email((String) oAuth2User.get("email"))
                .attributes(oAuth2User)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    public Member toEntity() {
        return Member.builder()
                .name(name)
                .emailId(email)
                .roleName("USER")
                .build();
    }
}
