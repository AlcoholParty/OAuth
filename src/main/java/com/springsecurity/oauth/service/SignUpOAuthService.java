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

import java.util.Collections;

@RequiredArgsConstructor
@Service
public class SignUpOAuthService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    @Autowired
    MemberRepository memberRepository;

    public Member.rpJoinSocialMember findByJoinGoogleMember(String emailId) {
        Member member = memberRepository.findByEmailId(emailId);
        if ( member == null ) {
            return null;
        }

        Member googleMember = memberRepository.findByGoogleMember(emailId, "google");
        if ( googleMember == null ) {
            Member.rpJoinSocialMember rpJoinSocialMember = new Member.rpJoinSocialMember("해당 유저는 다른 방식으로 가입한 이력이 있습니다.\n로그인 페이지로 이동합니다.");
            return rpJoinSocialMember;
        }
        Member.rpJoinSocialMember rpJoinSocialMember = new Member.rpJoinSocialMember(member);
        return rpJoinSocialMember;
    }

    public Member.rpJoinSocialMember findByJoinNaverMember(Member.rqJoinSocialMember rqJoinSocialMember) {
        Member rqMember = rqJoinSocialMember.toEntity();

        Member member = memberRepository.findByJoinMember(rqMember.getName(), rqMember.getPhoneNumber());
        if ( member == null ) {
            return null;
        }

        Member naverMember = memberRepository.findByNaverMember(rqMember.getEmailId(), "naver");
        if ( naverMember == null ) {
            Member.rpJoinSocialMember rpJoinSocialMember = new Member.rpJoinSocialMember("해당 유저는 다른 방식으로 가입한 이력이 있습니다.\n로그인 페이지로 이동합니다.");
            return rpJoinSocialMember;
        }
        Member.rpJoinSocialMember rpJoinSocialMember = new Member.rpJoinSocialMember(naverMember);
        return rpJoinSocialMember;
    }

    public void socialJoin(Member.rqJoinSocialMember rqJoinSocial) {
        Member member = rqJoinSocial.toEntity();
        System.out.println(member);
        memberRepository.save(member);
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // DefaultOAuth2User 서비스를 통해 User 정보를 가져와야 하기 때문에 대리자 생성
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();

        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        // 네이버 로그인인지 구글로그인인지 서비스를 구분해주는 코드 - 플랫폼 이름값
        String registrationId = userRequest.getClientRegistration()
                                           .getRegistrationId();
//        // OAuth2 로그인 진행시 키가 되는 필드값 프라이머리키와 같은 값 네이버 카카오 지원 X
//        String userNameAttributeName = userRequest.getClientRegistration()
//                                                  .getProviderDetails()
//                                                  .getUserInfoEndpoint()
//                                                  .getUserNameAttributeName();
        // oAuth2User에서 email값에 해당하는 키값
        String userNameAttributeName = "email";

        // OAuth2UserService를 통해 가져온 데이터를 담을 클래스
        Member.oauthAttributes oauthGoogle = Member.oauthAttributes.of(registrationId, userNameAttributeName, oAuth2User.getAttributes());

        // 로그인 한 유저 정보
        Member entityMember = oauthGoogle.toEntity();
        Member member = memberRepository.findByEmailId(entityMember.getEmailId());

        // 로그인한 유저정보를 가지고 리턴
        return new DefaultOAuth2User(Collections.singleton(new SimpleGrantedAuthority(member.getRoleName())),
                                                                                      oauthGoogle.getAttributes(),
                                                                                      oauthGoogle.getNameAttributeKey());
    }
}