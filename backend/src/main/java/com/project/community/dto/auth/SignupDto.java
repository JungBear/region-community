package com.project.community.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignupDto {
    
    @NotBlank(message = "아이디은 필수 입력 항목입니다.")
    @Pattern(regexp = "^[a-z]{4,15}$", message = "아이디는 영어 소문자 4~15자로 입력해주세요.")
    private String id;

    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?])(?=.{8,20}$)", message = "비밀번호는 영어 대문자, 소문자, 특수 문자와 숫자를 포함하여 8~20자이어야 합니다.")
    private String pwd;
    
    @NotBlank(message = "닉네임은 필수 입력 항목입니다.")
    @Pattern(regexp = "^[가-힣a-zA-Z0-9]{2,20}$", message = "닉네임은 한글 또는 숫자 또는 영어를 사용하여 2~20자로 입력해주세요.")
    private String nickname;

    @NotBlank(message = "전화번호는 필수 입력 항목입니다.")
    @Pattern(regexp = "^(010|011|016|017|018|019)-?\\d{3,4}-?\\d{4}$", message = "전화번호 형식이 아닙니다.")
    private String tel;

    @NotBlank(message = "주소는 필수 입력 항목입니다.")
    private String address;

    @Builder
    public SignupDto(String id, String pwd, String nickname, String tel, String address){
        this.id = id;
        this.pwd = pwd;
        this.nickname = nickname;
        this.tel = tel;
        this.address = address;
    }

    public static SignupDto encodePassword(SignupDto signupDto, String encodedPassword) {
        SignupDto newSignupDto = new SignupDto();
        newSignupDto.pwd = encodedPassword;
        return newSignupDto;
    }
}
