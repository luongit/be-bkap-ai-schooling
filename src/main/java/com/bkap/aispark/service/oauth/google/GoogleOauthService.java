package com.bkap.aispark.service.oauth.google;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Service
public class GoogleOauthService {
    @Value("${google.client-id}")
    private String clientId;

    @Value("${google.client-secret}")
    private String clientSecret;

    @Value("${google.redirect-uri}")
    private String redirectUri;

    private final RestTemplate rest = new RestTemplate();

    // Tao URL de chuyen huong sang Google Login
    public String buildLoginUrl() {
        return "https://accounts.google.com/o/oauth2/v2/auth"
                + "?client_id=" + clientId
                + "&redirect_uri=" + redirectUri
                + "&response_type=code"
                + "&scope=openid%20email%20profile"
                + "&access_type=offline"
                + "&prompt=select_account%20consent";
    }


    // Doi code → Access Token
    public Map exchangeCodeForToken(String code) {

        Map<String, String> body = Map.of(
                "code", code,
                "client_id", clientId,
                "client_secret", clientSecret,
                "redirect_uri", redirectUri,
                "grant_type", "authorization_code"
        );

        return rest.postForObject(
                "https://oauth2.googleapis.com/token",
                body,
                Map.class
        );
    }

    // Goi Google API de lay thong tin user
    public Map getUserInfo(String accessToken) {

        return rest.getForObject(
                "https://www.googleapis.com/oauth2/v2/userinfo?access_token=" + accessToken,
                Map.class
        );
    }

}
