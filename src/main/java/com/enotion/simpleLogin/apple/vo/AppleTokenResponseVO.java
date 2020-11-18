package com.enotion.simpleLogin.apple.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
public class AppleTokenResponseVO {

    private String access_token;
    private Long expires_in;
    private String id_token;
    private String refresh_token;
    private String token_type;

}
