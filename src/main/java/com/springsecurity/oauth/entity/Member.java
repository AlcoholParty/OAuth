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
    private Long idx; // MySQL에서 AUTO_INCREMENT를 사용하면 null값이 들어가야 자동으로 숫자가 올라간다.
                      // 하지만 long 즉, 원시타입으로 작성하면 null값을 허용하지 않기 때문에 오류가 난다.
                      // 그래서 Long 즉, 참조타입으로 작성해야 null값을 허용해서 값이 제대로 들어가게 된다.

    // length = 길이, unique = (기본값)false:유니크 해제 / true:유니크 설정, nullable = (기본값)true:눌값 허용 / false:눌값 불가
    @Column(length = 50, unique = true, nullable = false)
    private String emailId;

    @Column(length = 255)
    private String pwd;

    @Column(length = 10, nullable = false)
    private String name;

    @Column(length = 20, unique = true, nullable = false)
    private String nickname;

    @Column(length = 10, nullable = false)
    private String birthday;

    @Column(length = 1, nullable = false)
    private String gender;

    @Column(length = 15, unique = true, nullable = false)
    private String phoneNumber;

    @Column(length = 100, nullable = false)
    private String address;

    @Column(length = 10, nullable = false)
    private String studyType;

    @Column(length = 10, nullable = false)
    private String platform;

    @Column(length = 100, nullable = false)
    private String roleName; // Spring Security 권한 설정
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // DTO 구역

    // 자사 회원가입 Request DTO
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
                    .build();
        }
    }

    // 자사 회원가입 Response DTO
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

    // Social 회원가입 Request DTO
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
                    .build();
        }
    }

    // Social 가입자 조회 Response DTO
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public static class rpJoinSocialMember {
        private long idx;
        private String errMsg; // 에러 메시지 변수

        // Entity를 DTO로 변환 (생성자 방식) - Social 가입자인 경우
        public rpJoinSocialMember(Member member) {
            this.idx = member.getIdx();
        }

        // Entity를 DTO로 변환 (생성자 방식) - 자사 플랫폼의 가입자인 경우
        public rpJoinSocialMember(String errMsg) { // 파라미터로 서비스에서 넘어온 에러 메시지를 받아온다.
            this.idx = 0; // idx는 0으로 고정해서 에러 체크값으로 사용한다.
            this.errMsg = errMsg; // errMsg는 받아온 에러 메시지를 저장한다.
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
                    .emailId(emailId)
                    .name(name)
                    .platform(platform)
                    .roleName("USER")
                    .build();
        }

        // 10. Entity를 DTO로 변환 (생성자 방식)
        @Builder
        public oauthAttributes(Map<String, Object> attributes,
                               String nameAttributeKey,
                               String emailId,
                               String name,
                               String birthyear,
                               String birthday,
                               String gender,
                               String phoneNumber,
                               String platform) { // 11. 파라미터로 9에서 넘어온 값들을 받아온다.
            // 12. 11에서 파라미터로 가져온 값중 플랫폼을 가져와 어느 플랫폼으로 로그인 진행중인지 체크한다.
            // 12-1. 네이버 로그인일 경우
            if ( "naver".equals(platform) ) {
                this.attributes = attributes; // 유저 정보 Map
                this.nameAttributeKey = nameAttributeKey; // 필드값
                this.emailId = emailId; // 이메일은 아이디로 전달한다.
                this.name = name; // 이름
                this.birthday = birthyear + "-" + birthday; // 생년월일은 DB 규칙에 맞게 생년과 생일을 합쳐서 전달한다.
                this.gender = gender; // 성별
                this.phoneNumber = phoneNumber; // 핸드폰 번호
                this.platform = platform; // 플랫폼
            // 12-2. 구글 로그인일 경우
            } else {
                this.attributes = attributes; // 유저 정보 Map
                this.nameAttributeKey = nameAttributeKey; // 필드값
                this.emailId = emailId; // 이메일은 아이디로 전달한다.
                this.name = name; // 이름
                this.platform = platform; // 플랫폼
            }
        }

        // 로그인한 플랫폼 구별용
        public static oauthAttributes of(String registrationId, String userNameAttributeName, Map<String, Object> attributes) { // 7. 파라미터로 서비스에서 넘어온 플랫폼과 필드값과 DTO를 받아온다.
            // 8. 7에서 파라미터로 가져온 값중 플랫폼을 가져와 어느 플랫폼으로 로그인 진행중인지 체크한다.
            // 8-1. 네이버 로그인일 경우
            if ( "naver".equals(registrationId) ) {
                // 8-1-1. 네이버 로그인 메소드로 7에서 받아온 값들을 모두 전달한다.
                return ofNaver(registrationId, userNameAttributeName, attributes);
            // 8-2. 구글 로그인일 경우
            } else {
                // 8-2-1. 구글 로그인 메소드로 7에서 받아온 값들을 모두 전달한다.
                return ofGoogle(registrationId, userNameAttributeName, attributes);
            }
        }

        // 9. 각 로그인 플랫폼에 해당하는 메소드로 이동한다.
        // 9-1. 네이버 로그인
        public static oauthAttributes ofNaver(String registrationId, String userNameAttributeName, Map<String, Object> attributes) { // 9-1-1. 파라미터로 8-1-1에서 넘어온 플랫폼과 필드값과 DTO를 받아온다.
            // 9-1-2. 네이버는 구글이랑 다르게 attributes가 유저 정보만 깔끔하게 들어오는게 아니기에 내부에서 유저 정보를 가지고있는 "response"를 가져와 Map으로 새로 만든다.
            Map<String, Object> response = (Map<String, Object>) attributes.get("response");
            // 9-1-3. Entity를 DTO로 변환하는 메소드로 빌드한다.
            return oauthAttributes.builder()
                    // (String) - 값을 받는쪽의 타입은 String인데, Map에서 키로 가져온 값의 타입은 Object이기 때문에, 이를 String 타입으로 변환한다.
                    .emailId((String) response.get("email")) // 여기서 이메일은 아이디가 된다.
                    .name((String) response.get("name")) // 이름
                    .birthyear((String) response.get("birthyear")) // 생년
                    .birthday((String) response.get("birthday")) // 생일
                    .gender((String) response.get("gender")) // 성별
                    .phoneNumber((String) response.get("mobile")) // 핸드폰 번호
                    .platform(registrationId) // 플랫폼
                    .attributes(response) // 57-1-2에서 새로 만든 유저 정보 Map
                    .nameAttributeKey(userNameAttributeName) // 필드값
                    .build();
        }
        // 9-2. 구글 로그인
        public static oauthAttributes ofGoogle(String registrationId, String userNameAttributeName, Map<String,Object> attributes){ // 9-2-1. 파라미터로 8-2-1에서 넘어온 플랫폼과 필드값과 DTO를 받아온다.
            // 9-2-2. Entity를 DTO로 변환하는 메소드로 빌드한다.
            return oauthAttributes.builder()
                    .emailId((String) attributes.get("email")) // 여기서 이메일은 아이디가 된다.
                    .name((String) attributes.get("name")) // 이름
                    .platform(registrationId) // 플랫폼
                    .attributes(attributes) // 유저 정보 Map
                    .nameAttributeKey(userNameAttributeName) // 필드값
                    .build();
        }
    }
}
