package com.project.community.model;

import com.project.community.common.Role;
import com.project.community.dto.auth.SignupDto;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.IdGeneratorType;

@Entity(name = "member")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Member {

    @Id
    @Column(name = "idx")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idx;

    private String id;

    private String pwd;

    private String nickname;

    private String tel;

    private String address;

    @Enumerated(EnumType.STRING)
    private Role role;

    /**
     * 회원 가입 시 생성 메서드
     * @param signupDto
     * @return
     */
    public static Member registerMember(SignupDto signupDto){
        Member member = new Member();
        member.id = signupDto.getId();
        member.pwd = signupDto.getPwd();
        member.nickname = signupDto.getNickname();
        member.address = signupDto.getAddress();
        member.role = Role.USER;
        return member;
    }

    @Builder
    public Member(String id, String pwd, String nickname, String tel,
                  String address, Role role){
        this.id = id;
        this.pwd = pwd;
        this.nickname = nickname;
        this.tel = tel;
        this.address = address;
        this.role = role;
    }

    public void changePwd(String encodedPwd){
        this.pwd=encodedPwd;
    }
}
