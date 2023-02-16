package com.springsecurity.oauth.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.springsecurity.oauth.httpclient.GoogleLogin;
import com.springsecurity.oauth.entity.Member;
import com.springsecurity.oauth.service.SignUpOAuthService;
import com.springsecurity.oauth.service.SignUpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
        // 2. 1에서 받아온 아이디를 서비스에 전달하다.
        signUpService.loadUserByUsername(emailId);
        // 로그인 성공 및 실패 후 이동 페이지는 Spring Security가 각 핸들러를 통해 잡고 있기에 여기서 굳이 잡아줄 필요가 없다.
        // 메인 페이지로 이동
        // return "Main";
    }

    // 회원가입 페이지
    @GetMapping("/joinform")
    public String joinform(Model model) {
        // 0. 회원가입에 사용할 DTO를 바인딩한다.
        model.addAttribute("memberDTO", new Member.rqJoinMember());
        // 회원가입 페이지로 이동
        return "SignUp/JoinForm";
    }

    // 회원가입 진행 URL
    @PostMapping("/joinform/join")
    public String join(Member.rqJoinMember rqJoinMember, Model model) { // 1. 파라미터로 form에서 넘어온 DTO를 받아온다.
        // 2. 1에서 파라미터로 넘어온 DTO와 비밀번호 암호화 메소드를 같이 서비스에 전달한다.
        Member.rpJoinMember member = signUpService.joinMember(rqJoinMember, passwordEncoder);
        // 11. 2에서 반환받은 DTO를 바인딩한다.
        model.addAttribute("member", member);
        // 환영 페이지로 이동
        return "SignUp/Welcome";
    }
////////////////////////////// 소셜 로그인 API //////////////////////////////
    // 구글 로그인 토큰 발급 및 유저 정보 조회
    @GetMapping("/loginform/googletoken")
    public String googleAuthentication(String code, Model model) { // 4. 파라미터로 구글 로그인 URL을 통해 가져온 code를 받아온다.
        // 5. 4에서 파라미터로 받아온 code로 이번엔 access_token을 받아와야 하기에 구글 서버와 통신하는 메소드로 다시 전달한다.
        JsonNode jsonToken = GoogleLogin.getAccessToken(code);
        // 5에서 받환받은 값에 어떤것들이 들어있는지 조회 및 체크
        // System.out.println(jsonToken);
        // 18. 5에서 반환받은 값에서 필요한 access_token을 가져온다.
        // asText vs toString -
        String accessToken = jsonToken.get("access_token").asText();

        // 19. 18에서 받아온 access_token으로 이번엔 구글 로그인 유저 정보를 받아와야 하기에 구글 서버와 통신하는 메소드로 다시 전달한다.
        JsonNode userInfo = GoogleLogin.getGoogleUserInfo(accessToken);
        // 19에서 받환받은 값에 어떤것들이 들어있는지 조회 및 체크
        // System.out.println(userInfo);
        // 30. 19에서 반환받은 값에서 필요한 유저 정보인 이메일과 이름을 가져온다.
        // 30-1. 30에서 가져온 이메일을 아이디 변수에 전달한다.
        String emailId = userInfo.get("email").asText();
        // 30-2. 30에서 가져온 이름을 이름 변수에 전달한다.
        String name = userInfo.get("name").asText();

        // 31. 18에서 받아온 access_token으로 이번엔 구글 로그인 유저의 추가 정보를 받아와야 하기에 구글 서버와 통신하는 메소드로 다시 전달한다.
        JsonNode people = GoogleLogin.getGooglePeople(accessToken);
        // 31에서 받환받은 값에 어떤것들이 들어있는지 조회 및 체크
        // System.out.println(userInfo);
        // 41. 31에서 반환받은 값에서 필요한 추가 유저 정보인 생년월일과 성별을 가져온다.
        // 41-1. 41에서 가져온 생년월일이 년, 월, 일로 따로따로 분리되서 나오기에 먼저 각각 변수로 받은뒤 그 다음 하나로 합쳐서 생일 변수에 전달한다.
        String year = people.get("birthdays").get(0).get("date").get("year").asText();
        String month = people.get("birthdays").get(0).get("date").get("month").asText();
        String day = people.get("birthdays").get(0).get("date").get("day").asText();
        String birthday = year + "-" + month + "-" + day;
        // 41-2. 41에서 가져온 성별이 male, female로 나오기에 if문을 통해서 DB 규칙에 맞게 각각 M과 F로 만들어서 다시 성별 변수에 전달한다.
        String gender = people.get("genders").get(0).get("value").asText();
        // 41-2-1. gender가 male일 경우
        if ( gender.equals("male") ) {
            // 41-2-1-1. 성별 변수에 M을 전달한다.
            gender = "M";
        // 41-2-2. gender가 female일 경우
        } else {
            // 41-2-2-1. 성별 변수에 F를 전달한다.
            gender = "F";
        }

        // 42. 30-1에서 전달받은 아이디를 서비스에 전달한다.
        Member.rpJoinSocialMember rpJoinSocialMember = signUpOAuthService.findByJoinGoogleMember(emailId);
        // 48. 42에서 반환받은 DTO가 있는지 체크한다.
        // 48-1. 반환받은 DTO가 없는 경우 - 비가입자로 여기서 받아온 구글 유저 정보들을 들고 구글 회원가입 추가입력 페이지로 이동한다.
        if ( rpJoinSocialMember == null ) { // 회원가입
            // 48-1-1. 30-1에서 전달받은 아이디를 바인딩한다.
            model.addAttribute("emailId", emailId);
            // 48-1-2. 30-2에서 전달받은 이름을 바인딩한다.
            model.addAttribute("name", name);
            // 48-1-3. 41-1에서 전달받은 생년월일을 바인딩한다.
            model.addAttribute("birthday", birthday);
            // 48-1-4. 41-2에서 전달받은 성별을 바인딩한다.
            model.addAttribute("gender", gender);
            // 48-1-5. 구글 회원가입에 사용할 DTO를 바인딩한다.
            model.addAttribute("memberDTO", new Member.rqJoinSocialMember());
            // 48-1-6. 구글 회원가입 추가입력 페이지로 이동
            return "SignUp/GoogleJoinForm";
        // 48-2. 반환받은 DTO가 있는 경우 - 구글 가입자 or 타 플랫폼 가입자
        } else {
            // 49. 받환받은 DTO 값 중 Idx를 체크한다.
            // 49-1. idx가 0일 경우 - 구글 이메일을 사용하여 다른 플랫폼으로 가입한 유저
            if ( rpJoinSocialMember.getIdx() == 0 ) { // 에러
                try {
                    // 49-1-1. 에러 메시지를 UTF-8 형식으로 인코딩하여 로그인 페이지로 리다이렉트한다.
                    return "redirect:/loginform?loginErrMsg=" + URLEncoder.encode(rpJoinSocialMember.getErrMsg(), "UTF-8");
                } catch (UnsupportedEncodingException e) { // 지원되지 않는 인코딩 예외
                    throw new RuntimeException(e);
                }
            // 49-2. idx가 0이 아닐 경우 - 구글로 가입한 유저
            } else { // 로그인
                // 49-2-1. Spring Security가 관리하고 있는 OAuth2를 통해 OAuth2UserService로 리다이렉트한다.
                return "redirect:/oauth2/authorization/google";
            }
        }
    }

    // 구글 회원가입 URL
    @PostMapping("/loginform/googlejoin")
    public String googleJoin(Member.rqJoinSocialMember rqJoinSocialMember) { // 1. 파라미터로 form에서 넘어온 DTO를 받아온다.
        // 2. 1에서 파라미터로 받아온 DTO를 서비스에 전달한다.
        signUpOAuthService.socialJoin(rqJoinSocialMember);
        // 6. 로그인 페이지로 리다이렉트 한다.
        return "redirect:/loginform";
    }

    // 네이버 콜백 페이지
    @GetMapping("/loginform/navercallback")
    public String naverCallback(Model model) {
        // 0. 네이버 유저 정보를 가져올때 사용할 DTO를 바인딩한다.
        model.addAttribute("memberDTO", new Member.rqJoinSocialMember());
        // 1. 네이버 콜백 페이지로 이동
        return "SignUp/NaverCallback";
    }

    // 네이버 로그인 인증
    @PostMapping("/loginform/naverauthentication")
    @ResponseBody
    public String naverAuthentication(Member.rqJoinSocialMember rqJoinSocialMember) { // 1. 파라미터로 네이버 콜백 페에지에서 넘어온 DTO를 받아온다.
        // 2. 1에서 파라미터로 받아온 DTO를 서비스에 전달한다.
        Member.rpJoinSocialMember rpJoinSocialMember = signUpOAuthService.findByJoinNaverMember(rqJoinSocialMember);
        // 9. 반환받은 DTO가 있는지 체크한다.
        // 9-1. 반환받은 DTO가 없는 경우 - 비가입자
        if ( rpJoinSocialMember != null ) { // 회원가입
            // 9-1-1. "0"을 반환한다. - 콜백 메소드에서 다음 네이버 회원가입 페이지로 이동시킨다.
            return "0";
        // 9-2. 반환받은 DTO가 있는 경우 - 네이버 가입자 or 타 플랫폼 가입자
        } else {
            // 10. 받환받은 DTO 값 중 Idx를 체크한다.
            // 10-1. idx가 0일 경우 - 네이버 이메일을 사용하여 다른 플랫폼으로 가입한 유저
            if ( rpJoinSocialMember.getIdx() == 0 ) { // 에러
                // 10-1-1. 에러메세지를 반환한다. - 콜백 메소드에서 에러메세지를 띄운뒤 로그인 페이지로 이동시킨다.
                return rpJoinSocialMember.getErrMsg();
            // 10-2. idx가 0이 아닐 경우 - 네이버로 가입한 유저
            } else { // 로그인
                // 10-2-1. "1"을 반환한다. - 콜백 메소드에서 Spring Security가 관리하고 있는 OAuth2를 통해 OAuth2UserService로 이동시킨다.
                return "1";
            }
        }
    }

    // 네이버 회원가입 추가입력 페이지
    @PostMapping("/loginform/naverjoinform")
    public String naverJoinForm(Member.rqJoinSocialMember rqJoinSocialMember, Model model) { // 1. 파라미터로 form에서 넘어온 DTO를 받아온다.
        // 2. 1에서 파라미터로 받아온 DTO에 들어있는 값들을 하나씩 꺼내서 각각 바인딩한다.
        // 2-1. 1에서 파라미터로 받아온 DTO 값 중 아이디를 바인딩한다.
        model.addAttribute("emailId", rqJoinSocialMember.getEmailId());
        // 2-2. 1에서 파라미터로 받아온 DTO 값 중 이름을 바인딩한다.
        model.addAttribute("name", rqJoinSocialMember.getName());
        // 2-3. 1에서 파라미터로 받아온 DTO 값 중 휴대폰 번호를 바인딩한다.
        model.addAttribute("phoneNumber", rqJoinSocialMember.getPhoneNumber());
        // 2-4. 1에서 파라미터로 받아온 DTO 값 중 성별을 바인딩한다.
        model.addAttribute("gender", rqJoinSocialMember.getGender());
        // 2-5. 1에서 파라미터로 받아온 DTO 값 중 생일을 바인딩한다.
        model.addAttribute("birthday", rqJoinSocialMember.getBirthday());
        // 2-6. 네이버 회원가입에 사용할 DTO를 바인딩한다.
        model.addAttribute("memberDTO", new Member.rqJoinSocialMember());
        // 2-7. 네이버 회원가입 추가입력 페이지로 이동
        return "SignUp/NaverJoinForm";
    }

    // 네이버 회원가입 URL
    @PostMapping("/loginform/naverjoin")
    public String naverJoin(Member.rqJoinSocialMember rqJoinSocialMember) { // 1. 파라미터로 form에서 넘어온 DTO를 받아온다.
        // 2. 1에서 파라미터로 받아온 DTO를 서비스에 전달한다.
        signUpOAuthService.socialJoin(rqJoinSocialMember);
        // 6. 로그인 페이지로 리다이렉트 한다.
        return "redirect:/loginform";
    }
}
