package com.enotion.simpleLogin.apple.service;

import java.util.Map;

import com.enotion.simpleLogin.apple.vo.ApplePayloadVO;
import com.enotion.simpleLogin.apple.vo.AppleTokenResponseVO;

public interface AppleLoginService {

	/**
     * 유효한 id_token인 경우 client_secret 생성
     *
     * @param id_token
     * @return
     */
    String getAppleClientSecret(String id_token);

    /**
     * code 또는 refresh_token가 유효한지 Apple Server에 검증 요청
     *
     * @param client_secret
     * @param code
     * @param refresh_token
     * @return
     */
    AppleTokenResponseVO requestCodeValidations(String client_secret, String code, String refresh_token);

    /**
     * Apple login page 호출을 위한 Meta 정보 가져오기
     *
     * @return
     */
    Map<String, String> getLoginMetaInfo();

    /**
     * id_token에서 payload 데이터 가져오기
     *
     * @return
     */
    ApplePayloadVO getPayload(String id_token);

}
