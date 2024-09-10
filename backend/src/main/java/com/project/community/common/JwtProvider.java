package com.project.community.common;

import com.project.community.dto.auth.MemberDetails;
import com.project.community.dto.auth.TokenDto;
import com.project.community.service.MemberDetailsServiceImpl;
import com.project.community.service.RedisService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecureDigestAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@Transactional(readOnly = true)
public class JwtProvider implements InitializingBean {

    private final MemberDetailsServiceImpl memberDetailsService;
    private final RedisService redisService;

    private static final String AUTHORITIES_KEY = "role";
    private static final String IDX_KEY = "idx";
    private static final String ID_KEY = "id";
    private static final String url = "https://localhost:8080";

    private final String secretKey;
    private static Key signingkey;

    private final Long accessTokenValidityInMilliseconds;
    private final Long refreshTokenValidityInMilliseconds;

    /**
     * 생성자에서 @Value 어노테이션을 이용해 application.yml에서 미리 설정해둔 값을 가져와 사용한다.
     * application.yml에 적어둔 토큰들의 유효 기간 값의 단위가 seconds이기 때문에, 1000을 곱해 milliseconds로 변경해준다.
     * @param userDetailsService
     * @param redisService
     * @param secretKey
     * @param accessTokenValidityInMilliseconds
     * @param refreshTokenValidityInMilliseconds
     */
    public JwtProvider(
            MemberDetailsServiceImpl memberDetailsService,
            RedisService redisService,
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.access-token-validity-in-seconds}") Long accessTokenValidityInMilliseconds,
            @Value("${jwt.refresh-token-validity-in-seconds}") Long refreshTokenValidityInMilliseconds) {
        this.memberDetailsService = memberDetailsService;
        this.redisService = redisService;
        this.secretKey = secretKey;
        // seconds -> milliseconds
        this.accessTokenValidityInMilliseconds = accessTokenValidityInMilliseconds * 1000;
        this.refreshTokenValidityInMilliseconds = refreshTokenValidityInMilliseconds * 1000;
    }


    /**
     * Invoked by the containing {@code BeanFactory} after it has set all bean properties
     * and satisfied {@link BeanFactoryAware}, {@code ApplicationContextAware} etc.
     * <p>This method allows the bean instance to perform validation of its overall
     * configuration and final initialization when all bean properties have been set.
     *
     * @throws Exception in the event of misconfiguration (such as failure to set an
     *                   essential property) or if initialization fails for any other reason
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        byte[] secretKeyBytes = Decoders.BASE64.decode(secretKey);
        signingkey = Keys.hmacShaKeyFor(secretKeyBytes);
    }

    /**
     * 토큰 발급
     * @param idx
     * @param authorities
     * @param id
     * @return
     */
    @Transactional
    public TokenDto createToken(int idx, String authorities, String id){
        Long now = System.currentTimeMillis();
        Date accessTokenExpiresIn = new Date(now + accessTokenValidityInMilliseconds);
        Date refreshTokenExpiresIn = new Date(now + refreshTokenValidityInMilliseconds);

        Map<String, Object> claims = new HashMap<>();
        claims.put(AUTHORITIES_KEY, authorities);
        claims.put(IDX_KEY, idx);
        claims.put(ID_KEY, id);

        String accessToken = Jwts.builder()
                .header()
                    .add("typ", "JWT")
                    .add("alg", "HS512")
                .and()
                .issuer(url)
                .subject(String.valueOf(idx))
                .issuedAt(new Date(now))
                .expiration(accessTokenExpiresIn)
                .claims(claims)
                .signWith(signingkey)
                .compact();

        String refreshToken = Jwts.builder()
                .header()
                    .add("typ", "JWT")
                    .add("alg", "HS512")
                .and()
                .issuer(url)
                .subject(String.valueOf(idx))
                .issuedAt(new Date(now))
                .expiration(refreshTokenExpiresIn)
                .signWith(signingkey)
                .compact();

        return TokenDto.builder()
                .grantType("Bearer")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpiresIn(accessTokenExpiresIn.getTime())
                .build();
    }

    /**
     * 토큰 정보 읽기
     * @param token
     * @return
     */
    public Claims getClaims(String token){
        try{
            return Jwts.parser()
                    .verifyWith((SecretKey) signingkey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

        } catch (Exception  e) { // Access Token
            log.error("토큰 조회 실패", e);
            return null;
        }
    }

    public Authentication getAuthentication(String token) {
        Claims claims = getClaims(token);
        Integer idx = claims.get(IDX_KEY, Integer.class);
        String id = claims.get(ID_KEY, String.class);
        MemberDetails memberDetails = memberDetailsService.loadUserByUsername(id);

        log.info("토큰 인증 정보: idx={}, email={}", idx, id);
        return new UsernamePasswordAuthenticationToken(memberDetails, "", memberDetails.getAuthorities());
    }

    public long getTokenExpirationTime(String token) {
        return getClaims(token).getExpiration().getTime();
    }

    // == 토큰 검증 == //
    /**
     * 토큰을 검증한다. 각 예외별로 log를 남기고 false를 반환한다.
     */
    public boolean validateRefreshToken(String refreshToken){
        log.info("레디스 : " + redisService.getValues(refreshToken));
        try {

            if (redisService.getValues(refreshToken).equals("delete")) { // 회원 탈퇴했을 경우
                return false;
            }
            Jwts.parser()
                    .verifyWith((SecretKey)signingkey)
                    .build()
                    .parseSignedClaims(refreshToken);
            return true;
        } catch (SignatureException e) {
            log.error("Invalid JWT signature.");
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token.");
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token.");
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token.");
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty.");
        } catch (NullPointerException e){
            log.error("JWT Token is empty.");
        }
        return false;
    }

    // Filter에서 사용
    /**
     * Filter에서 AT 검증을 위해 쓰인다. 기간이 만료됐을 경우에도 true를 반환한다.
     * @param accessToken
     * @return
     */
    public boolean validateAccessToken(String accessToken) {
        try {
            if (redisService.getValues(accessToken) != null // NPE 방지
                    && redisService.getValues(accessToken).equals("logout")) { // 로그아웃 했을 경우
                return false;
            }
            Jwts.parser()
                    .verifyWith((SecretKey)signingkey)
                    .build()
                    .parseSignedClaims(accessToken);
            return true;
        } catch(ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // 재발급 검증 API에서 사용

    /**
     * 유효기간만 만료된 유효한 토큰일 경우 true를 반환한다.
     * @param accessToken
     * @return
     */
    public boolean validateAccessTokenOnlyExpired(String accessToken) {
        try {
            return getClaims(accessToken)
                    .getExpiration()
                    .before(new Date());
        } catch(ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
