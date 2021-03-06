package com.enotion.simpleLogin.apple.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.enotion.simpleLogin.apple.vo.ApplePayloadVO;
import com.enotion.simpleLogin.apple.vo.AppleTokenResponseVO;
import com.enotion.simpleLogin.utils.AppleLoginUtils;

@Service
public class AppleLoginServiceImpl implements AppleLoginService {

    @Autowired
    AppleLoginUtils appleUtils;

    /**
     * 유효한 id_token인 경우 client_secret 생성
     *
     * @param id_token
     * @return
     */
    @Override
    public String getAppleClientSecret(String id_token) {

        if (appleUtils.verifyIdentityToken(id_token)) {
            return appleUtils.createClientSecret();
        }

        return null;
    }

    /**
     * code 또는 refresh_token가 유효한지 Apple Server에 검증 요청
     *
     * @param client_secret
     * @param code
     * @param refresh_token
     * @return
     */
    @Override
    public AppleTokenResponseVO requestCodeValidations(String client_secret, String code, String refresh_token) {

        AppleTokenResponseVO tokenResponse = new AppleTokenResponseVO();

        if (client_secret != null && code != null && refresh_token == null) {
            tokenResponse = appleUtils.validateAuthorizationGrantCode(client_secret, code);
        } else if (client_secret != null && code == null && refresh_token != null) {
            tokenResponse = appleUtils.validateAnExistingRefreshToken(client_secret, refresh_token);
        }
        return tokenResponse;
    }

    /**
     * Apple login page 호출을 위한 Meta 정보 가져오기
     *
     * @return
     */
    @Override
    public Map<String, String> getLoginMetaInfo() {
        return appleUtils.getMetaInfo();
    }

    /**
     * id_token에서 payload 데이터 가져오기
     *
     * @return
     */
    @Override
    public ApplePayloadVO getPayload(String id_token) {
        return appleUtils.decodeFromIdToken(id_token);
    }
}
