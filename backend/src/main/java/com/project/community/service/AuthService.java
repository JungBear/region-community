package com.project.community.service;

import com.project.community.common.AuthenticationUtils;
import com.project.community.common.JwtProvider;
import com.project.community.common.ResponseCode;
import com.project.community.dto.auth.SignupDto;
import com.project.community.dto.auth.TokenDto;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtProvider jwtProvider;
    private final RedisService redisService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private final String SERVER = "Server";

    public void signup(SignupDto signupDto){
        String encodePwd = bCryptPasswordEncoder.encode(signupDto.getPwd());
    }

    /**
     * AT가 만료일자만 초과한 유효한 토큰인지 검사
     *
     * @param requestAccessTokenInHeader
     * @return
     */
    public boolean validate(String requestAccessTokenInHeader) {
        String requestAccessToken = resolveToken(requestAccessTokenInHeader);
        return jwtProvider.validateAccessTokenOnlyExpired(requestAccessToken); // true = 재발급
    }


    @Transactional
    public TokenDto reissue(String requestAccessTokenInHeader, String requestRefreshToken) {
        String requestAccessToken = resolveToken(requestAccessTokenInHeader);

        Authentication authentication = jwtProvider.getAuthentication(requestAccessToken);
        String principal = getPrincipal(requestAccessToken);

        String refreshTokenInRedis = redisService.getValues("RT(" + SERVER + "):" + principal);
        if (refreshTokenInRedis == null) { // Redis에 저장되어 있는 RT가 없을 경우
            return null; // -> 재로그인 요청
        }

        // 요청된 RT의 유효성 검사 & Redis에 저장되어 있는 RT와 같은지 비교
        if(!jwtProvider.validateRefreshToken(requestRefreshToken) || !refreshTokenInRedis.equals(requestRefreshToken)) {
            redisService.deleteValues("RT(" + SERVER + "):" + principal); // 탈취 가능성 -> 삭제
            return null; // -> 재로그인 요청
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String authorities = getAuthorities(authentication);
        int idx = AuthenticationUtils.getUserIdx(authentication);
        String id = AuthenticationUtils.getUserId(authentication);

        // 토큰 재발급 및 Redis 업데이트
        redisService.deleteValues("RT(" + SERVER + "):" + principal); // 기존 RT 삭제
        TokenDto tokenDto = jwtProvider.createToken(idx, authorities, id);
        saveRefreshToken(SERVER, principal, tokenDto.getRefreshToken());
        return tokenDto;
    }
    /**
     * 토큰 발급
     * Redis에 기존의 RT가 이미 있을 경우, 삭제한다. AT와 RT를 생성하고, Redis에 새로 발급한 RT를 저장한다.
     *
     * @param provider
     * @param email
     * @param authorities
     * @return
     */
    @Transactional
    public TokenDto generateToken(int idx, String provider, String email, String authorities) {
        // RT가 이미 있을 경우
        if(redisService.getValues("RT(" + provider + "):" + email) != null) {
            redisService.deleteValues("RT(" + provider + "):" + email); // 삭제
        }

        // AT, RT 생성 및 Redis에 RT 저장
        TokenDto tokenDto = jwtProvider.createToken(idx, authorities, email);
        saveRefreshToken(provider, email, tokenDto.getRefreshToken());
        return tokenDto;
    }

    /**
     * RT를 Redis에 저장
     *
     * @param provider
     * @param principal
     * @param refreshToken
     */
    @Transactional
    public void saveRefreshToken(String provider, String principal, String refreshToken) {
        redisService.setValuesWithTimeout("RT(" + provider + "):" + principal, // key
                refreshToken, // value
                jwtProvider.getTokenExpirationTime(refreshToken)); // timeout(milliseconds)
    }

    /**
     * AT로부터 principal 추출
     *
     * @param requestAccessToken
     * @return
     */
    public String getPrincipal(String requestAccessToken) {
        return jwtProvider.getAuthentication(requestAccessToken).getName();
    }

    /**
     * "Bearer {AT}"에서 {AT} 추출
     *
     * @param requestAccessTokenInHeader
     * @return
     */

    public String resolveToken(String requestAccessTokenInHeader) {
        if (requestAccessTokenInHeader != null && requestAccessTokenInHeader.startsWith("Bearer ")) {
            return requestAccessTokenInHeader.substring(7);
        }
        return null;
    }

    /**
     * 역할 가져오기
     *
     * @param authentication
     * @return
     */
    public String getAuthorities(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
    }



}
