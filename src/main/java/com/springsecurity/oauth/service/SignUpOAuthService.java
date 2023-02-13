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

    // 구글 로그인 인증 - 비가입자, 가입자, 중복 가입자 비교
    public Member.rpJoinSocialMember findByJoinGoogleMember(String emailId) { // 43. 파라미터로 컨트롤러에서 넘어온 아이디를 받아온다.
        // 44. 43에서 파라미터로 받아온 아이디로 유저를 조회하고, 조회된 값을 받아온다.
        Member member = memberRepository.findByEmailId(emailId);
        // 45. 조회된 값이 있는지 체크한다.
        // 45-1. 조회된 값이 없는 경우 - 비가입자
        if ( member == null ) {
            // 45-1-1. 눌값을 반환한다.
            return null;
        // 45-2. 조회된 값이 있는 경우 - 구글 가입자 or 타 플랫폼 가입자
        } else {
            // 46. 43에서 파라미터로 받아온 아이디와 플랫폼 이름을 google로 지정해서 이에 해당하는 유저를 조회하고, 조회된 값을 받아온다.
            Member googleMember = memberRepository.findByGoogleMember(emailId, "google");
            // 47. 조회된 값이 있는지 체크한다.
            // 47-1. 조회된 값이 없는 경우 - 구글 이메일을 사용하여 다른 플랫폼으로 가입한 유저
            if (googleMember == null) {
                // 47-1-1. 에러 메시지를 작성해 DTO로 변환한다.
                Member.rpJoinSocialMember rpJoinSocialMember = new Member.rpJoinSocialMember("해당 유저는 다른 방식으로 가입한 이력이 있습니다.\n로그인 페이지로 이동합니다.");
                // 47-1-2. 변환된 DTO를 반환한다.
                return rpJoinSocialMember;
            // 47-2. 조회된 값이 있는 경우 - 구글로 가입한 유저
            } else {
                // 47-2-1. 46에서 조회하고 받아온 Entity를 DTO로 변환한다.
                Member.rpJoinSocialMember rpJoinSocialMember = new Member.rpJoinSocialMember(member);
                // 47-2-2. 변환된 DTO를 반환한다.
                return rpJoinSocialMember;
            }
        }
    }

    public Member.rpJoinSocialMember findByJoinNaverMember(Member.rqJoinSocialMember rqJoinSocialMember) {
        Member rqMember = rqJoinSocialMember.toEntity();

        Member member = memberRepository.findByJoinMember(rqMember.getName(), rqMember.getPhoneNumber());
        if ( member == null ) {
            return null;
        } else {

            Member naverMember = memberRepository.findByNaverMember(rqMember.getEmailId(), "naver");
            if (naverMember == null) {
                Member.rpJoinSocialMember rpJoinSocialMember = new Member.rpJoinSocialMember("해당 유저는 다른 방식으로 가입한 이력이 있습니다.\n로그인 페이지로 이동합니다.");
                return rpJoinSocialMember;
            } else {
                Member.rpJoinSocialMember rpJoinSocialMember = new Member.rpJoinSocialMember(naverMember);
                return rpJoinSocialMember;
            }
        }
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