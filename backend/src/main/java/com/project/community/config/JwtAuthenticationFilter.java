package com.project.community.config;

import com.project.community.common.JwtProvider;
import io.jsonwebtoken.IncorrectClaimException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;


    /**
     * Same contract as for {@code doFilter}, but guaranteed to be
     * just invoked once per request within a single request thread.
     * See {@link #shouldNotFilterAsyncDispatch()} for details.
     * <p>Provides HttpServletRequest and HttpServletResponse arguments instead of the
     * default ServletRequest and ServletResponse ones.
     *
     * @param request
     * @param response
     * @param filterChain
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // Accest Toekn 추출
        String accessToken = resolveToken(request);
        // 토큰 검사
        try{
            if(accessToken != null && jwtProvider.validateAccessToken(accessToken)){
                Authentication authentication = jwtProvider.getAuthentication(accessToken);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("ContextHolder에 authentication 저장");
            }
        }catch (IncorrectClaimException e){
            SecurityContextHolder.clearContext();
            log.debug("유요하지 않은 토큰입니다.");
            response.sendError(403);
        }catch (UsernameNotFoundException e){
            SecurityContextHolder.clearContext();
            log.debug("사용자를 찾을 수 없습니다.");
            response.sendError(403);
        }
        filterChain.doFilter(request, response);
    }

    // 토큰 추출하기
    public String resolveToken(HttpServletRequest httpServletRequest){
        String bearerToken = httpServletRequest.getHeader("Authorization");
        // bearerToken이 비어있지 않거나 Bearar 로 시작하면 Bearer 부분을 제외한 토큰을 반환
        if(bearerToken != null && bearerToken.startsWith("Bearer ")){
            return bearerToken.substring(7);
        }
        return null;
    }

}
