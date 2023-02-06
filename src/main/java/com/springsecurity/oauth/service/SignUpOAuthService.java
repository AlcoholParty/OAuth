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

    public void join(Member.oauthGoogle oauthGoogle) {
        Member member = oauthGoogle.toEntity();
        memberRepository.save(member);
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration()
                                           .getRegistrationId(); // 플랫폼 이름값
        String userNameAttributeName = userRequest.getClientRegistration()
                                                  .getProviderDetails()
                                                  .getUserInfoEndpoint()
                                                  .getUserNameAttributeName(); // oAuth2User에서 sub값에 해당하는 키값
        String emailId = "email"; // oAuth2User에서 email값에 해당하는 키값

        Member.oauthGoogle oauthGoogle = Member.oauthGoogle.of(registrationId, emailId, oAuth2User.getAttributes());

        Member entityMember = oauthGoogle.toEntity();
        Member member = memberRepository.findByEmailId(entityMember.getEmailId());

        return new DefaultOAuth2User(Collections.singleton(new SimpleGrantedAuthority(member.getRoleName())),
                                                                                      oauthGoogle.getAttributes(),
                                                                                      oauthGoogle.getNameAttributeKey());
    }
}