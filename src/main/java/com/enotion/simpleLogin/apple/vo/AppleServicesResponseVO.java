package com.enotion.simpleLogin.apple.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
public class AppleServicesResponseVO {

    private String state;
    private String code;
    private String id_token;
    private String user;

}
