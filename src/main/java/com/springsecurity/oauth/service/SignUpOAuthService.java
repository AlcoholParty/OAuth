package com.springsecurity.oauth.service;

import com.springsecurity.oauth.entity.Member;
import com.springsecurity.oauth.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.Collections;

@RequiredArgsConstructor
@Service
public class SignUpOAuthService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    HttpSession httpSession;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration()
                                           .getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration()
                                                  .getProviderDetails()
                                                  .getUserInfoEndpoint()
                                                  .getUserNameAttributeName();

        Member.oauthGoogle oauthGoogle = Member.oauthGoogle.of(registrationId, userNameAttributeName, oAuth2User.getAttributes());

        Member member = saveOrUpdate(oauthGoogle);

        httpSession.setAttribute("user", new Member.SessionUserDTO(oauthGoogle));

        return new DefaultOAuth2User(Collections.singleton(new SimpleGrantedAuthority(member.getRoleName())),
                                                                                      oauthGoogle.getAttributes(),
                                                                                      oauthGoogle.getNameAttributeKey());
    }

    private Member saveOrUpdate(Member.oauthGoogle oauthGoogle) {
        Member member = oauthGoogle.toEntity();
        return memberRepository.save(member);
    }
}