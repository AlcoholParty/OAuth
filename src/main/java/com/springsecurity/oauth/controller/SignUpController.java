package com.springsecurity.oauth.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.springsecurity.oauth.config.google.GoogleLogin;
import com.springsecurity.oauth.entity.Member;
import com.springsecurity.oauth.service.SignUpOAuthService;
import com.springsecurity.oauth.service.SignUpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@Controller
public class SignUpController {
    // 회원가입 및 로그인 인증 서비스
    @Autowired
    SignUpService signUpService;

    @Autowired
    SignUpOAuthService signUpOAuthService;

    // 비밀번호 암호화 메소드
    @Autowired
    PasswordEncoder passwordEncoder;

    // 로그인 진행 URL
    @PostMapping("/loginform/login")
    public void login(@RequestParam(value = "emailId") String emailId) { // 1. 파라미터로 로그인 할때 작성한 아이디를 받아온다.
        // 2. 서비스 파라미터로 로그인 할때 작성한 아이디를 넘겨준다.
        signUpService.loadUserByUsername(emailId);
        // 로그인 성공 및 실패 후 이동 페이지는 Spring Security가 각 핸들러를 통해 잡고 있기에 여기서 굳이 잡아줄 필요가 없다.
        // return "Main";
    }

    // 회원가입 페이지
    @GetMapping("/joinform")
    public String joinform(Model model) {
        // 0. Entity 대신 DTO를 사용하기 위해 사용할 DTO를 모델로 바인딩 한다.
        model.addAttribute("memberDto", new Member.rqJoinMember());
        return "SignUp/JoinForm";
    }

    // 회원가입 진행 URL
    @PostMapping("/joinform/join")
    public String join(Member.rqJoinMember rqJoinMember, Model model) { // 1. 파라미터로 form에서 넘어온 DTO를 받아온다.
        // 2. 서비스 파라미터로 MemberDTO와 비밀번호 암호화 메소드를 같이 넘겨준다.
        Member.rpJoinMember member = signUpService.joinMember(rqJoinMember, passwordEncoder);
        // 11. 반환된 DTO를 모델로 바인딩 한다.
        model.addAttribute("member", member);
        return "SignUp/Welcome";
    }
////////////////////////////// 소셜 로그인 API //////////////////////////////
    // 구글 로그인 토큰 발급 및 유저 정보 조회
    @GetMapping("/loginform/googletoken")
    public String googleAuthentication(@RequestParam(value = "code", required = false) String code, Model model) {
        JsonNode jsonToken = GoogleLogin.getAccessToken(code);
        String accessToken = jsonToken.get("access_token").asText();

        JsonNode userInfo = GoogleLogin.getGoogleUserInfo(accessToken);
        String emailId = userInfo.get("email").asText();
        String name = userInfo.get("name").asText();

        JsonNode people = GoogleLogin.getGooglePeople(accessToken);
        // 구글은 생년월일이 년, 월, 일로 따로따로 분리되서 나오기에 하나씩 받아서 하나로 합쳐준다.
        String year = people.get("birthdays").get(0).get("date").get("year").asText();
        String month = people.get("birthdays").get(0).get("date").get("month").asText();
        String day = people.get("birthdays").get(0).get("date").get("day").asText();
        String birthday = year + "-" + month + "-" + day;
        // 구글은 성별이 male, female로 나오기에 if문을 통해서 DB 규칙에 맞게 각각 M과 F로 만들어준다.
        String gender = people.get("genders").get(0).get("value").asText();
        if (gender.equals("male")) {
            gender = "M";
        } else {
            gender = "F";
        }

        Member.rpJoinSocialMember rpJoinSocialMember = signUpOAuthService.findByJoinGoogleMember(emailId);

        if ( rpJoinSocialMember != null ) {
            if ( rpJoinSocialMember.getIdx() != 0 ) {
                return "redirect:/oauth2/authorization/google";
            }
            try {
                return "redirect:/loginform?loginErrMsg=" + URLEncoder.encode(rpJoinSocialMember.getErrMsg(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }

        model.addAttribute("emailId", emailId);
        model.addAttribute("name", name);
        model.addAttribute("birthday", birthday);
        model.addAttribute("gender", gender);
        model.addAttribute("memberDTO", new Member.rqJoinSocialMember());
        // 구글 회원가입 추가입력 페이지로 이동
        return "SignUp/GoogleJoinForm";
    }

    // 구글 회원가입 URL
    @PostMapping("/loginform/googlejoin")
    public String googleJoin(Member.rqJoinSocialMember rqJoinSocialMember) {
        signUpOAuthService.socialJoin(rqJoinSocialMember);
        return "redirect:/loginform";
    }

    // 네이버 콜백 페이지
    @GetMapping("/loginform/navercallback")
    public String naverCallback(Model model) {
        model.addAttribute("memberDTO", new Member.rqJoinSocialMember());
        return "SignUp/NaverCallback";
    }

    @PostMapping("/loginform/naverauthentication")
    @ResponseBody
    public String naverAuthentication(Member.rqJoinSocialMember rqJoinSocialMember) {
        Member.rpJoinSocialMember rpJoinSocialMember = signUpOAuthService.findByJoinNaverMember(rqJoinSocialMember);

        if ( rpJoinSocialMember != null ) {
            if ( rpJoinSocialMember.getIdx() != 0 ) {
                return "1";
            }
            return rpJoinSocialMember.getErrMsg();
        }
        return "0";
    }

    @PostMapping("/loginform/naverjoinform")
    public String naverJoinForm(Member.rqJoinSocialMember rqJoinSocialMember, Model model) {
        model.addAttribute("emailId", rqJoinSocialMember.getEmailId());
        model.addAttribute("name", rqJoinSocialMember.getName());
        model.addAttribute("phoneNumber", rqJoinSocialMember.getPhoneNumber());
        model.addAttribute("gender", rqJoinSocialMember.getGender());
        model.addAttribute("birthday", rqJoinSocialMember.getBirthday());
        model.addAttribute("memberDTO", new Member.rqJoinSocialMember());
        return "SignUp/NaverJoinForm";
    }

    @PostMapping("/loginform/naverjoin")
    public String naverJoin(Member.rqJoinSocialMember rqJoinSocialMember) {
        signUpOAuthService.socialJoin(rqJoinSocialMember);
        return "redirect:/loginform";
    }
}
