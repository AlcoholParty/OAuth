package com.springsecurity.oauth.config.google;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class GoogleLogin {
    public static JsonNode getAccessToken(String code) { // 6. 파라미터로 컨트롤러에서 넘어온 code를 받아온다.
        // 7. 6에서 받아온 code를 가지고 access_token을 받아오는 구글 서버 URL을 작성한다.
        final String requestUrl = "https://www.googleapis.com/oauth2/v4/token";

        // 8. POST방식은 데이터를 URL에 직접적으로 담아서 가져갈 수 없기에 따로 List를 만들어 가져갈 수 있도록 만든다.
        final List<NameValuePair> postParams = new ArrayList<NameValuePair>();
        // 9. 8에서 만든 List에 URL을 통해 같이 가져가야할 데이터들을 name / value 쌍으로 담아준다.
        postParams.add(new BasicNameValuePair("grant_type", "authorization_code")); // 권한 타입을 인증 코드로 설정
        postParams.add(new BasicNameValuePair("client_id", "346535144521-qthfl467cl8jrdcdc2g8l3lvpurqsk1h.apps.googleusercontent.com")); // 클라이언트 ID
        postParams.add(new BasicNameValuePair("client_secret", "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")); // 클라이언트 비밀키
        postParams.add(new BasicNameValuePair("redirect_uri", "http://localhost:8888/loginform/googletoken")); // 리다이렉트 URI
        postParams.add(new BasicNameValuePair("code", code)); // 6에서 파라미터로 받아온 code

        // 10. 외부 서버와 통신을 맡아줄 HttpClient를 생성한다.
        final HttpClient client = HttpClientBuilder.create().build();
        // 11. 외부 서버와 통신할 때 사용할 HttpMethod를 생성하고, 파라미터에 7에서 작성한 URL을 전달한다.
        final HttpPost post = new HttpPost(requestUrl); // POST 방식

        // 12. 해당 메소드의 반환 값으로 사용할 변수를 미리 만들어둔다.
        JsonNode returnNode = null;

        // 외부 서버와 통신 시작
        try {
            // 13. 8과 9에서 만든 데이터 List를 HttpMethod에 setter를 통해 전달한다. - POST 방식에만 적용
            post.setEntity(new UrlEncodedFormEntity(postParams));
            // 14. 10에서 생성한 HttpClent에 11에서 생성한 HttpMethod를 전달하여 실행하고, HttpResponse로 반환되는 값을 받아온다.
            final HttpResponse response = client.execute(post);
            // 응답 상태 코드 얻기
            final int responseCode = response.getStatusLine().getStatusCode();

            System.out.println("\nSending 'POST' request to URL : " + requestUrl);
            System.out.println("Post parameters : " + postParams);
            System.out.println("Response Code : " + responseCode);

            // 15. 매퍼를 생성하여 14에서 받아온 JSON 형식의 반환 값을 처리하고, 12에서 미리 만든 해당 메소드의 반환 값으로 전달한다.
            ObjectMapper mapper = new ObjectMapper();
            returnNode = mapper.readTree(response.getEntity().getContent());


        } catch (UnsupportedEncodingException e) { // 지원되지 않는 인코딩 예외
            e.printStackTrace();
        } catch (ClientProtocolException e) { // 클라이언트 프로토콜 예외
            e.printStackTrace();
        } catch (IOException e) { // I/O 예외
            e.printStackTrace();
        } finally {
            // 자원을 비운다.
        }

        // 16. 15에서 전달받은 값을 반환한다.
        return returnNode;
    }

    public static JsonNode getGoogleUserInfo(String authorizeCode) {
        final String requestUrl = "https://www.googleapis.com/oauth2/v2/userinfo";

        final HttpClient client = HttpClientBuilder.create().build();
        final HttpGet get = new HttpGet(requestUrl);

        JsonNode returnNode = null;

        // add header
        get.addHeader("Authorization", "Bearer " + authorizeCode);

        try {
            final HttpResponse response = client.execute(get);
            final int responseCode = response.getStatusLine().getStatusCode();

            ObjectMapper mapper = new ObjectMapper();
            returnNode = mapper.readTree(response.getEntity().getContent());

            System.out.println("\nSending 'GET' request to URL : " + requestUrl);
            System.out.println("Response Code : " + responseCode);


        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // clear resources
        }
        return returnNode;
    }

    public static JsonNode getGooglePeople(String authorizeCode) {
        final String requestUrl = "https://people.googleapis.com/v1/people/me?" +
                                  "personFields=birthdays" +
                                  "&personFields=genders" +
                                  "&personFields=phoneNumbers" +
                                  "&key=AIzaSyDr3XNA_3hT9py0zIaHxQwIBVibhuIC_3E" +
                                  "&access_token=" + authorizeCode;

        final HttpClient client = HttpClientBuilder.create().build();
        final HttpGet get = new HttpGet(requestUrl);

        JsonNode returnNode = null;
        // https://people.googleapis.com/v1/people/me?personFields=birthdays&personFields=genders&personFields=phoneNumbers&access_token=ya29.a0AVvZVsqBkrHEOI9uIzC767YL7To3MyzIkSweDmdRAgJzBjW2_3tjPWT1-Y1JwYcr4IdPxLtt89tJrfE-ROTe9a7bUNtlVJttFUbT9jAZ6ODTzx0dVRz1CfaTnRC0tmq6rsESgHdideu8-VclOQsgiy4BK7h32gaCgYKAeUSARMSFQGbdwaIVUNOAgRiT0dAoQ-ZPedG8w0165
        // add header
        //get.setHeader("personFields", "Bearer " + "birthdays"); // 첫 번째 항목에 대해 다음과 같이 HttpGet 개체에서 setHeader() 메서드를 사용한다.
        //get.addHeader("personFields", "Bearer " + "genders"); // 그런 다음 두 번째 헤더에 대해 다음과 같이 HttpGet 개체에서 addHeader() 메서드를 사용한다.
        //get.addHeader("key", "Bearer " + "AIzaSyDr3XNA_3hT9py0zIaHxQwIBVibhuIC_3E");
        //get.addHeader("access_token", "Bearer " + authorizeCode);

        try {
            final HttpResponse response = client.execute(get);
            final int responseCode = response.getStatusLine().getStatusCode();

            ObjectMapper mapper = new ObjectMapper();
            returnNode = mapper.readTree(response.getEntity().getContent());

            System.out.println("\nSending 'GET' request to URL : " + requestUrl);
            System.out.println("Response Code : " + responseCode);


        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // clear resources
        }
        return returnNode;
    }
}
