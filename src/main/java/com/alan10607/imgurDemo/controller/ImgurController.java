package com.alan10607.imgurDemo.controller;

import com.alan10607.imgurDemo.config.ImgurConfig;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequestMapping(path = "/imgur")
@AllArgsConstructor
@Slf4j
public class ImgurController {
    private final ImgurConfig config;
    private RestTemplate restTemplate;

    @GetMapping("/auth")
    public RedirectView auth(){
        StringBuffer url = new StringBuffer()
                .append(config.getAuthorizeUrl())
                .append("?client_id=").append(config.getClientId())
                .append("&response_type=token");

        return new RedirectView(url.toString());
    }

    @GetMapping("/redirect")
    public String redirect(HttpServletRequest request) {
        //Set imgur redirect as http://localhost:8080/imgur/redirect
        return "redirect.html";
    }

    @PostMapping("/saveToken")
    @ResponseBody
    public ResponseEntity saveToken(@RequestBody Map<String,String> data){
        config.setAccessToken(data.get("access_token"));
        config.setRefreshToken(data.get("refresh_token"));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/upload")
    @ResponseBody
    public ResponseEntity upload(@RequestParam(name = "image") MultipartFile image) throws IOException {
        String accessToken = config.getAccessToken();
        if(Strings.isBlank(accessToken))
            throw new RuntimeException("Access token not found, need admin auth");

        String base64Img = Base64.getEncoder().encodeToString(image.getBytes());

        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.MULTIPART_FORM_DATA);
        header.add("Authorization", "Bearer " + accessToken);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("title", "your title");
        body.add("image", base64Img);
        body.add("description", "your desc");
        body.add("type", "base64");
        body.add("album", config.getAlbumId());

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, header);
        ResponseEntity<Map> response = restTemplate.postForEntity(config.getUploadUrl(), request, Map.class);
        LinkedHashMap<String, Object> resBody = (LinkedHashMap<String, Object>) response.getBody();
        LinkedHashMap<String, Object> res = (LinkedHashMap<String, Object>) resBody.get("data");

        return ResponseEntity.ok().body(res);
    }

    @PostMapping("/refreshToken")
    @ResponseBody
    public ResponseEntity refreshToken() {
        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("refresh_token", config.getRefreshToken());
        body.add("client_id", config.getClientId());
        body.add("client_secret", config.getClientSecret());
        body.add("grant_type", "refresh_token");

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, header);
        ResponseEntity<Map> response = restTemplate.postForEntity(config.getAccessTokenUrl(), request, Map.class);

        LinkedHashMap<String, Object> resBody = (LinkedHashMap<String, Object>) response.getBody();
        String newAccessToken = (String) resBody.get("access_token");
        String newRefreshToken = (String) resBody.get("refresh_token");
        config.setAccessToken(newAccessToken);
        config.setRefreshToken(newRefreshToken);
        return ResponseEntity.ok().build();
    }
}