package com.springsecurity.oauth.entity;

import com.springsecurity.oauth.dto.OAuthDTO;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Map;

@Getter // getter 어노테이션
@Setter // setter 어노테이션
@NoArgsConstructor // 파라미터가 없는 기본 생성자 어노테이션
@AllArgsConstructor // 모든 필드 값을 파라미터로 받는 생성자 어노테이션
@Builder // 빌더 어노테이션 - 빌더를 통해 해당 객체의 필드 값을 재생성 한다.
@ToString // 객체를 불러올때 주솟값이 아닌 String 타입으로 변경해주는 어노테이션
@Entity(name = "Member") // Entity 어노테이션 - 괄호안에는 테이블명과 똑같이 작성한다.
public class Member {
    @Id // 기본키 어노테이션 - 기본키 설정 (PRIMARY KEY)
    @GeneratedValue(strategy = GenerationType.IDENTITY) // AUTO_INCREMENT - MySQL에서 시퀀스 역할을 담당한다.
    // @Column() // 컬럼 어노테이션 - 기본키 제외 나머지 컬럼 설정 - 기본키랑 같이 쓰이면 기본키의 설정값들을 잡아줄 수 있다.
    private Integer idx;

    @Column(length = 50)
    private String emailId;

    @Column(length = 255)
    private String pwd;

    @Column(length = 10)
    private String name;

    // length = 길이, unique = (기본값)false:유니크 해제 / true:유니크 설정, nullable = (기본값)true:눌값 허용 / false:눌값 불가
    @Column(length = 20)
    private String nickname;

    @Column(length = 10)
    private String birthday;

    @Column(length = 1)
    private String gender;

    @Column(length = 15)
    private String phoneNumber;

    @Column(length = 100)
    private String address;

    @Column(length = 10)
    private String studyType;

    @Column(length = 10)
    private String platform;

    @Column(length = 100)
    private String roleName; // Spring Security 권한 설정

    @Column(length = 100)
    private String profileImage;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // DTO 구역

    // 회원가입 자사 Request DTO
    @Getter // getter 어노테이션
    @Setter // setter 어노테이션
    @NoArgsConstructor // 파라미터가 없는 기본 생성자 어노테이션
    @AllArgsConstructor // 모든 필드 값을 파라미터로 받는 생성자 어노테이션
    @Builder // 빌더 사용 어노테이션
    @ToString // 객체를 불러올때 주솟값이 아닌 String 타입으로 변경해주는 어노테이션
    public static class rqJoinMember {
        private String emailId;
        private String pwd;
        private String name;
        private String nickname;
        private String birthday;
        private String gender;
        private String phoneNumber;
        private String address;
        private String studyType;

        // DTO를 Entity로 변환 (빌더 방식)
        public Member toEntity(PasswordEncoder passwordEncoder) { // 5. 파라미터로 서비스에서 넘어온 비밀번호 암호화 메소드를 받아온다.
            // 6. 비밀번호 암호화
            String enPassword = passwordEncoder.encode(pwd);
            // 7. 변환된 Entity를 반환한다.
            return Member.builder()
                    .idx(null)
                    .emailId(emailId)
                    .pwd(enPassword) // 암호화된 비밀번호 저장
                    .name(name)
                    .nickname(nickname)
                    .birthday(birthday)
                    .gender(gender)
                    .phoneNumber(phoneNumber)
                    .address(address)
                    .studyType(studyType)
                    .platform("soju") // 가입 플랫폼 설정
                    .roleName("USER") // Spring Security 권한에 USER로 설정
                    .profileImage("noImage.jpeg") // 회원가입할때 첫 사진은 아무것도 없는 공통 사진으로 설정
                    .build();
        }
    }

    // 회원가입 자사 Response DTO
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public static class rpJoinMember {
        private String nickname;
        private String roleName;

        // Entity를 DTO로 변환 (생성자 방식)
        public rpJoinMember(Member member) {
            this.nickname = member.getNickname();
            this.roleName = member.getRoleName();
        }
    }

    // 회원가입 Social Request DTO
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class rqJoinSocialMember {
        private String emailId;
        private String name;
        private String nickname;
        private String birthday;
        private String gender;
        private String phoneNumber;
        private String address;
        private String studyType;
        private String platform;

        // DTO를 Entity로 변환 (빌더 방식)
        public Member toEntity() {
            return Member.builder()
                    .idx(null)
                    .emailId(emailId)
                    .name(name)
                    .nickname(nickname)
                    .birthday(birthday)
                    .gender(gender)
                    .phoneNumber(phoneNumber)
                    .address(address)
                    .studyType(studyType)
                    .platform(platform)
                    .roleName("USER") // Spring Security 권한에 USER로 설정
                    .profileImage("noImage.jpeg") // 회원가입할때 첫 사진은 아무것도 없는 공통 사진으로 설정
                    .build();
        }
    }

    // Naver 가입자 조회 Response DTO
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public static class rpJoinSocialMember {
        private int idx;
        private String errMsg;

        // Entity를 DTO로 변환 (생성자 방식) - Naver 가입자인 경우
        public rpJoinSocialMember(Member member) {
            this.idx = member.getIdx();
        }

        // Entity를 DTO로 변환 (생성자 방식) - 다른 방식의 가입자인 경우
        public rpJoinSocialMember(String errMsg) {
            this.idx = 0;
            this.errMsg = errMsg;
        }
    }

    // OAuth - Social DTO
    @Getter
    @Setter
    @NoArgsConstructor
    @ToString
    public static class oauthAttributes {
        private Map<String, Object> attributes;
        private String nameAttributeKey;
        private String emailId;
        private String name;
        private String birthday;
        private String gender;
        private String phoneNumber;
        private String platform;

        // DTO를 Entity로 변환 (빌더 방식)
        public Member toEntity() {
            return Member.builder()
                    .name(name)
                    .emailId(emailId)
                    .platform(platform)
                    .roleName("USER")
                    .profileImage("noImage.jpeg")
                    .build();
        }

        // Entity를 DTO로 변환 (생성자 방식)
        @Builder
        public oauthAttributes(Map<String, Object> attributes, String nameAttributeKey, String emailId, String name, String birthyear, String birthday, String gender, String phoneNumber, String platform) {
            if ( "naver".equals(platform) ) {
                this.attributes = attributes;
                this.nameAttributeKey = nameAttributeKey;
                this.emailId = emailId;
                this.name = name;
                this.birthday = birthyear + "-" + birthday;
                this.gender = gender;
                this.phoneNumber = phoneNumber;
                this.platform = platform;
            } else {
                this.attributes = attributes;
                this.nameAttributeKey = nameAttributeKey;
                this.emailId = emailId;
                this.name = name;
                this.platform = platform;
            }
        }

        // 사용자 정보는 Map이기 때문에 변경해야함
        public static oauthAttributes of(String registrationId, String userNameAttributeName, Map<String, Object> attributes) {
            //네이버 로그인 인지 판단.
            if ( "naver".equals(registrationId) ) {
                return ofNaver(registrationId, userNameAttributeName, attributes);
            }
            return ofGoogle(registrationId, userNameAttributeName, attributes);
        }

        public static oauthAttributes ofNaver(String registrationId, String userNameAttributeName, Map<String, Object> attributes) {
            // 응답 받은 사용자의 정보를 Map형태로 변경.
            Map<String, Object> response = (Map<String, Object>) attributes.get("response");
            // 미리 정의한 속성으로 빌드.
            return oauthAttributes.builder()
                    .emailId((String) response.get("email"))
                    .name((String) response.get("name"))
                    .birthyear((String) response.get("birthyear"))
                    .birthday((String) response.get("birthday"))
                    .gender((String) response.get("gender"))
                    .phoneNumber((String) response.get("mobile"))
                    .platform(registrationId)
                    .attributes(response)
                    .nameAttributeKey(userNameAttributeName)
                    .build();
        }

        public static oauthAttributes ofGoogle(String registrationId, String userNameAttributeName, Map<String,Object> attributes){
            // 미리 정의한 속성으로 빌드.
            return oauthAttributes.builder()
                    .name((String) attributes.get("name"))
                    .emailId((String) attributes.get("email"))
                    .platform(registrationId)
                    .attributes(attributes)
                    .nameAttributeKey(userNameAttributeName)
                    .build();
        }
    }
}
