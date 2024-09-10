package com.project.community.common;

import com.project.community.dto.auth.MemberDetails;
import com.project.community.model.Member;
import com.project.community.service.MemberDetailsServiceImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

public class AuthenticationUtils {

    /**
     * Authentication 객체에서 Member 객체를 추출합니다.
     *
     * @param authentication 인증 객체
     * @return Member 객체
     * @throws AuthenticationException 인증 정보가 올바르지 않을 경우
     */
    public static Member getMember(Authentication authentication) {
        MemberDetails memberDetails = getMemberDetails(authentication);
        return memberDetails.getMember();
    }

    /**
     * Authentication 객체에서 사용자의 ID를 추출합니다.
     *
     * @param authentication 인증 객체
     * @return 사용자의 ID
     * @throws AuthenticationException 인증 정보가 올바르지 않을 경우
     */
    public static String getUserId(Authentication authentication) {
        return getMember(authentication).getId();
    }
    /**
     * Authentication 객체에서 사용자의 ID를 추출합니다.
     *
     * @param authentication 인증 객체
     * @return 사용자의 ID
     * @throws AuthenticationException 인증 정보가 올바르지 않을 경우
     */
    public static int getUserIdx(Authentication authentication) {
        return getMember(authentication).getIdx();
    }

    /**
     * Authentication 객체에서 사용자의 역할(Role)을 추출합니다.
     *
     * @param authentication 인증 객체
     * @return 사용자의 역할
     * @throws AuthenticationException 인증 정보가 올바르지 않을 경우
     */
    public static String getUserRole(Authentication authentication) {
        return getMember(authentication).getRole().name();
    }

    /**
     * Authentication 객체에서 MemberDetails 객체를 추출합니다.
     *
     * @param authentication 인증 객체
     * @return MemberDetails 객체
     * @throws AuthenticationException 인증 정보가 올바르지 않을 경우
     */
    private static MemberDetails getMemberDetails(Authentication authentication) {
        if (authentication == null) {
            throw new AuthenticationException("Authentication object is null") {};
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof MemberDetails)) {
            throw new AuthenticationException("Principal is not of type MemberDetails") {};
        }
        return (MemberDetails) principal;
    }
}
