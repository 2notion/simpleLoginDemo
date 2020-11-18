package com.enotion.simpleLogin.apple.vo;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
public class AppleKeysVO {

    private List<AppleKeyVO> keys;

}
