package com.enotion.simpleLogin.apple.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
public class AppleKeyVO {

    private String kty;
    private String kid;
    private String use;
    private String alg;
    private String n;
    private String e;

}
