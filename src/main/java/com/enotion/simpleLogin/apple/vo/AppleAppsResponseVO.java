package com.enotion.simpleLogin.apple.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
public class AppleAppsResponseVO {

    private String payload;

}
