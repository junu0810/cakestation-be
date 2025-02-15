package com.cakestation.backend.user.service;

import com.cakestation.backend.auth.exception.InvalidTokenException;
import com.cakestation.backend.common.exception.ErrorType;
import com.cakestation.backend.config.JwtProperties;
import com.cakestation.backend.config.KakaoConfig;
import com.cakestation.backend.user.exception.InvalidUserException;
import com.cakestation.backend.user.service.dto.response.KakaoUserDto;
import com.cakestation.backend.user.service.dto.response.CheckDto;
import com.cakestation.backend.user.service.dto.response.TokenDto;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;

import static com.cakestation.backend.config.KakaoConfig.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class KakaoService {

    private final KakaoConfig kakaoConfig;

    public TokenDto getKaKaoAccessToken(String code) {
        TokenDto tokenDto = null;

        try {
            String getTokenURL = kakaoConfig.GET_TOKEN_URL + "&code=" + code;
            URL url = new URL(getTokenURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            //POST 요청을 위해 기본값이 false인 setDoOutput을 true로
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            //POST 요청에 필요로 요구하는 파라미터 스트림을 통해 전송
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
            StringBuilder sb = new StringBuilder();
            bw.write(sb.toString());

            bw.flush();

            //결과 코드가 200이라면 성공
            int responseCode = conn.getResponseCode();

            //요청을 통해 얻은 JSON타입의 Response 메세지 읽어오기
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = "";
            StringBuilder result = new StringBuilder();

            while ((line = br.readLine()) != null) {
                result.append(line);
            }

            //Gson 라이브러리에 포함된 클래스로 JSON파싱 객체 생성
            JsonElement element = JsonParser.parseString(result.toString());

            tokenDto = TokenDto.builder()
                    .accessToken(element.getAsJsonObject().get("access_token").getAsString())
                    .refreshToken(element.getAsJsonObject().get("refresh_token").getAsString())
                    .accessExpires(element.getAsJsonObject().get("expires_in").getAsInt())
                    .refreshExpires(element.getAsJsonObject().get("refresh_token_expires_in").getAsInt())
                    .build();

            br.close();
            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return tokenDto;
    }

    //토큰 유효성 검사를 위한 클래스
    public CheckDto checkAccessToken(String access_Token) {
        CheckDto tokenUser = null;
        try {
            URL url = new URL(CHECK_TOKEN);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            String Token = access_Token.replace(JwtProperties.TOKEN_PREFIX, "");

            // 요청에 필요한 Header에 포함될 내용
            conn.setRequestProperty(JwtProperties.HEADER_STRING, JwtProperties.TOKEN_PREFIX + Token);

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            String line = "";
            StringBuilder result = new StringBuilder();

            while ((line = br.readLine()) != null) {
                result.append(line);
            }

            JsonElement element = JsonParser.parseString(result.toString());

            int userId = element.getAsJsonObject().get("id").getAsInt();
            tokenUser = CheckDto.builder()
                    .userUid(userId)
                    .build();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return tokenUser;
    }

    // refresh를 통한 토큰 최신화
    public TokenDto refreshAccessToken(String refreshToken) {
        String refreshURL = kakaoConfig.REFRESH_ACCESS + refreshToken;
        TokenDto tokenDto = null;

        try {
            URL url = new URL(refreshURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            //POST 요청을 위해 기본값이 false인 setDoOutput을 true로
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            //POST 요청에 필요로 요구하는 파라미터 스트림을 통해 전송
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
            bw.write("");
            bw.flush();

            //결과 코드가 200이라면 성공
            int responseCode = conn.getResponseCode();

            //요청을 통해 얻은 JSON타입의 Response 메세지 읽어오기
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = "";
            String result = "";

            while ((line = br.readLine()) != null) {
                result += line;
            }

            //Gson 라이브러리에 포함된 클래스로 JSON파싱 객체 생성
            JsonElement element = JsonParser.parseString(result);

            tokenDto = TokenDto.builder()
                    .accessToken(element.getAsJsonObject().get("access_token").getAsString())
                    .refreshToken(element.getAsJsonObject().get("refresh_token").getAsString())
                    .accessExpires(element.getAsJsonObject().get("expires_in").getAsInt())
                    .refreshExpires(element.getAsJsonObject().get("refresh_token_expires_in").getAsInt())
                    .build();

            br.close();
            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return tokenDto;
    }

    //로그아웃을 통한 발행된 토큰 무효화 작업(로그아웃)
    public void LogoutToken(String access_Token) throws InvalidTokenException {

        try {
            URL url = new URL(LOGOUT_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            String Token = access_Token.replace(JwtProperties.TOKEN_PREFIX, "");
            // 요청에 필요한 Header에 포함될 내용
            conn.setRequestProperty(JwtProperties.HEADER_STRING, JwtProperties.TOKEN_PREFIX + Token);

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            String line;
            StringBuilder result = new StringBuilder();

            while ((line = br.readLine()) != null) {
                result.append(line);
            }

            JsonElement element = JsonParser.parseString(result.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //회원탈퇴작업을 통해 나의 카카오에 등록된 유저에서 삭제
    public void deleteUser(String accessToken) {
        try {
            URL url = new URL(WITH_DRAWL_USER);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty(JwtProperties.HEADER_STRING, JwtProperties.TOKEN_PREFIX + accessToken);

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            String line = "";
            String result = "";

            while ((line = br.readLine()) != null) {
                result += line;
            }

            JsonElement element = JsonParser.parseString(result);

            System.out.println("Element" + element);
            int userId = element.getAsJsonObject().get("id").getAsInt();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public KakaoUserDto getUserInfo(String access_Token) {

        KakaoUserDto kakaoUserDto = null;

        try {
            URL url = new URL(GET_USER_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            System.out.println(access_Token);
            // 요청에 필요한 Header에 포함될 내용
            conn.setRequestProperty(JwtProperties.HEADER_STRING, JwtProperties.TOKEN_PREFIX + access_Token);


            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = "";
            StringBuilder result = new StringBuilder();

            while ((line = br.readLine()) != null) {
                result.append(line);
            }
            Optional<JsonElement> element = Optional.ofNullable(JsonParser.parseString(result.toString()));
            element.orElseThrow(() -> new InvalidUserException(ErrorType.NOT_FOUND_USER));

            JsonObject account = element.get().getAsJsonObject().get("kakao_account").getAsJsonObject();
            String username = account.getAsJsonObject().get("profile").getAsJsonObject().get("nickname").getAsString();
            String email = account.getAsJsonObject().get("email").getAsString();

            kakaoUserDto = KakaoUserDto.builder()
                    .username(username)
                    .email(email)
                    .build();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return kakaoUserDto;
    }
}
