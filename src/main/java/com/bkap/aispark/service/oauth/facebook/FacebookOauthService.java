package com.bkap.aispark.service.oauth.facebook;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class FacebookOauthService {

    @Value("${facebook.app-id}")
    private String appId;

    @Value("${facebook.app-secret}")
    private String appSecret;

    @Value("${facebook.redirect-uri}")
    private String redirectUri;

    private final RestTemplate rest = new RestTemplate();


    // Tạo URL để chuyển hướng sang Facebook Login
    public String buildLoginUrl() {
        return "https://www.facebook.com/v19.0/dialog/oauth"
                + "?client_id=" + appId
                + "&redirect_uri=" + redirectUri
                + "&response_type=code"
                + "&scope=email," +
                "public_profile";
    }


    //  Access Token
    public String exchangeCodeForToken(String code) {

        String url =
                "https://graph.facebook.com/v19.0/oauth/access_token"
                        + "?client_id=" + appId
                        + "&client_secret=" + appSecret
                        + "&redirect_uri=" + redirectUri
                        + "&code=" + code;

        Map response = rest.getForObject(url, Map.class);

        return (String) response.get("access_token");
    }


    // Gọi Facebook API lấy thông tin user
    public Map getUserInfo(String accessToken) {

        String url =
                "https://graph.facebook.com/me"
                        + "?fields=id,name,email"
                        + "&access_token=" + accessToken;

        return rest.getForObject(url, Map.class);
    }
}