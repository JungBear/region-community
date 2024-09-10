package com.project.community.service;

import com.project.community.dto.auth.MemberDetails;
import com.project.community.model.Member;
import com.project.community.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberDetailsServiceImpl implements UserDetailsService {

    private final MemberRepository memberRepository;
    /**
     * Locates the user based on the username. In the actual implementation, the search
     * may possibly be case sensitive, or case insensitive depending on how the
     * implementation instance is configured. In this case, the <code>UserDetails</code>
     * object that comes back may have a username that is of a different case than what
     * was actually requested..
     *
     * @param id the username identifying the user whose data is required.
     * @return a fully populated user record (never <code>null</code>)
     * @throws UsernameNotFoundException if the user could not be found or the user has no
     *                                   GrantedAuthority
     */
    @Override
    public MemberDetails loadUserByUsername(String id) throws UsernameNotFoundException {
        log.info("id : " + id);

        Member findUser = memberRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Can't find user with this email. -> " + id));

        if(findUser != null){
            return new MemberDetails(findUser);
        }

        return null;
    }
}
