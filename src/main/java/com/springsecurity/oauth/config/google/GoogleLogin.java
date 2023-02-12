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
    public static JsonNode getAccessToken(String authorizeCode) {
        final String RequestUrl = "https://www.googleapis.com/oauth2/v4/token";

        final List<NameValuePair> postParams = new ArrayList<NameValuePair>();
        postParams.add(new BasicNameValuePair("grant_type", "authorization_code"));
        postParams.add(new BasicNameValuePair("client_id", "346535144521-qthfl467cl8jrdcdc2g8l3lvpurqsk1h.apps.googleusercontent.com"));
        postParams.add(new BasicNameValuePair("client_secret", "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"));
        postParams.add(new BasicNameValuePair("redirect_uri", "http://localhost:8888/loginform/googletoken")); // 리다이렉트 URI
        postParams.add(new BasicNameValuePair("code", authorizeCode)); // 로그인 과정중 얻은 code 값

        final HttpClient client = HttpClientBuilder.create().build();
        final HttpPost post = new HttpPost(RequestUrl);
        JsonNode returnNode = null;

        try {
            post.setEntity(new UrlEncodedFormEntity(postParams));
            final HttpResponse response = client.execute(post);
            final int responseCode = response.getStatusLine().getStatusCode();

            System.out.println("\nSending 'POST' request to URL : " + RequestUrl);
            System.out.println("Post parameters : " + postParams);
            System.out.println("Response Code : " + responseCode);

            // JSON 형태 반환값 처리
            ObjectMapper mapper = new ObjectMapper();
            returnNode = mapper.readTree(response.getEntity().getContent());


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

    public static JsonNode getGoogleUserInfo(String authorizeCode) {
        final String RequestUrl = "https://www.googleapis.com/oauth2/v2/userinfo";

        final HttpClient client = HttpClientBuilder.create().build();
        final HttpGet get = new HttpGet(RequestUrl);

        JsonNode returnNode = null;

        // add header
        get.addHeader("Authorization", "Bearer " + authorizeCode);

        try {
            final HttpResponse response = client.execute(get);
            final int responseCode = response.getStatusLine().getStatusCode();

            ObjectMapper mapper = new ObjectMapper();
            returnNode = mapper.readTree(response.getEntity().getContent());

            System.out.println("\nSending 'GET' request to URL : " + RequestUrl);
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
        final String RequestUrl = "https://people.googleapis.com/v1/people/me?" +
                                  "personFields=birthdays" +
                                  "&personFields=genders" +
                                  "&personFields=phoneNumbers" +
                                  "&key=AIzaSyDr3XNA_3hT9py0zIaHxQwIBVibhuIC_3E" +
                                  "&access_token=" + authorizeCode;

        final HttpClient client = HttpClientBuilder.create().build();
        final HttpGet get = new HttpGet(RequestUrl);

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

            System.out.println("\nSending 'GET' request to URL : " + RequestUrl);
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
