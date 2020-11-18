package com.enotion.simpleLogin.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.InvalidKeyException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.enotion.simpleLogin.apple.vo.AppleKeyVO;
import com.enotion.simpleLogin.apple.vo.AppleKeysVO;
import com.enotion.simpleLogin.apple.vo.ApplePayloadVO;
import com.enotion.simpleLogin.apple.vo.AppleTokenResponseVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import lombok.extern.slf4j.Slf4j;
import sun.security.ec.ECPrivateKeyImpl;

@Slf4j
@Component
public class AppleLoginUtils {

	@Value("${APPLE.PUBLICKEY.URL}")
    private String APPLE_PUBLIC_KEYS_URL;

    @Value("${APPLE.ISS}")
    private String ISS;

    @Value("${APPLE.AUD}")
    private String AUD;

    @Value("${APPLE.TEAM.ID}")
    private String TEAM_ID;

    @Value("${APPLE.KEY.ID}")
    private String KEY_ID;

    @Value("${APPLE.KEY.PATH}")
    private String KEY_PATH;

    @Value("${APPLE.AUTH.TOKEN.URL}")
    private String AUTH_TOKEN_URL;

    @Value("${APPLE.WEBSITE.URL}")
    private String APPLE_WEBSITE_URL;

    /**
     * User가 Sign in with Apple 요청(https://appleid.apple.com/auth/authorize)으로 전달받은 id_token을 이용한 최초 검증
     * Apple Document URL ‣ https://developer.apple.com/documentation/sign_in_with_apple/sign_in_with_apple_rest_api/verifying_a_user
     *
     * @param id_token
     * @return boolean
     */
    public boolean verifyIdentityToken(String id_token) {

        try {
            SignedJWT signedJWT = SignedJWT.parse(id_token);
            JWTClaimsSet payload = signedJWT.getJWTClaimsSet();

            // EXP
            Date currentTime = new Date(System.currentTimeMillis());
            if (!currentTime.before(payload.getExpirationTime())) {
                return false;
            }

            // NONCE(Test value), ISS, AUD
            if (!"20B20D-0S8-1K8".equals(payload.getClaim("nonce")) || !ISS.equals(payload.getIssuer()) || !AUD.equals(payload.getAudience().get(0))) {
                return false;
            }

            // RSA
            if (verifyPublicKey(signedJWT)) {
                return true;
            }
        } catch (ParseException e) {
        	log.error("parse Error", e);
        }

        return false;
    }

    /**
     * Apple Server에서 공개 키를 받아서 서명 확인
     *
     * @param signedJWT
     * @return
     */
    private boolean verifyPublicKey(SignedJWT signedJWT) {

        try {

        	String publicKeys = UrlConnectionUtils.sslConnection(APPLE_PUBLIC_KEYS_URL, "", "GET");

            ObjectMapper objectMapper = new ObjectMapper();
            AppleKeysVO keys = objectMapper.readValue(publicKeys, AppleKeysVO.class);
            for (AppleKeyVO key : keys.getKeys()) {
                RSAKey rsaKey = (RSAKey) JWK.parse(objectMapper.writeValueAsString(key));
                RSAPublicKey publicKey = rsaKey.toRSAPublicKey();
                JWSVerifier verifier = new RSASSAVerifier(publicKey);

                if (signedJWT.verify(verifier)) {
                    return true;
                }
            }
        } catch (Exception e) {
        	log.error("verifyPublicKey Error", e);
        }

        return false;
    }

    /**
     * client_secret 생성
     * Apple Document URL ‣ https://developer.apple.com/documentation/sign_in_with_apple/generate_and_validate_tokens
     *
     * @return client_secret(jwt)
     */
    public String createClientSecret() {

    	Date now = new Date();

    	JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
	       	     .issuer(TEAM_ID)
	       	     .issueTime(now)
	       	     .expirationTime(new Date(now.getTime() + 3600000))
	       	     .audience(ISS)
	       	     .subject(AUD)
	       	     .build();

    	SignedJWT jwt = new SignedJWT(
	        		new JWSHeader.Builder(JWSAlgorithm.ES256).keyID(KEY_ID).build(),
	        	    claimsSet);
        try {

 	        ECPrivateKey ecPrivateKey = new ECPrivateKeyImpl(readPrivateKey());

 	        JWSSigner signer = new ECDSASigner(ecPrivateKey) ;

 	        jwt.sign(signer);

        } catch (InvalidKeyException e) {
        	log.error("InvalidKey Error", e);
        } catch (JOSEException e) {
        	log.error("JOSE Error", e);
        }

        return jwt.serialize();
    }

    /**
     * 파일에서 private key 획득
     *
     * @return Private Key
     */
    private byte[] readPrivateKey() {

    	File f = new File(KEY_PATH);
        byte[] content = null;

        try (	InputStream keyReader = new FileInputStream(f);
                PemReader pemReader = new PemReader(new InputStreamReader(keyReader))){

                PemObject pemObject = pemReader.readPemObject();
                content = pemObject.getContent();

        } catch (IOException e) {
        	log.error("readPrivateKey Error", e);
        	return content;
        }

        return content;
    }

    /**
     * 유효한 code 인지 Apple Server에 확인 요청
     * Apple Document URL ‣ https://developer.apple.com/documentation/sign_in_with_apple/generate_and_validate_tokens
     *
     * @return
     */
    public AppleTokenResponseVO validateAuthorizationGrantCode(String client_secret, String code) {

        StringBuffer tokenRequest = new StringBuffer();
    	tokenRequest.append("&").append("client_id").append("=").append(AUD);
    	tokenRequest.append("&").append("client_secret").append("=").append(client_secret);
    	tokenRequest.append("&").append("code").append("=").append(code);
    	tokenRequest.append("&").append("grant_type").append("=").append("authorization_code");
		tokenRequest.append("&").append("redirect_uri").append("=").append(APPLE_WEBSITE_URL);


        return getTokenResponse(tokenRequest.toString());
    }

    /**
     * 유효한 refresh_token 인지 Apple Server에 확인 요청
     * Apple Document URL ‣ https://developer.apple.com/documentation/sign_in_with_apple/generate_and_validate_tokens
     *
     * @param client_secret
     * @param refresh_token
     * @return
     */
    public AppleTokenResponseVO validateAnExistingRefreshToken(String client_secret, String refresh_token) {

    	StringBuffer tokenRequest = new StringBuffer();
    	tokenRequest.append("&").append("client_id").append("=").append(AUD);
    	tokenRequest.append("&").append("client_secret").append("=").append(client_secret);
    	tokenRequest.append("&").append("grant_type").append("=").append("refresh_token");
    	tokenRequest.append("&").append("refresh_token").append("=").append(refresh_token);

        return getTokenResponse(tokenRequest.toString());
    }

    /**
     * POST https://appleid.apple.com/auth/token
     *
     * @param tokenRequest
     * @return
     */
    private AppleTokenResponseVO getTokenResponse(String tokenRequest) {

        try {

            String response = null;
			try {
				response = UrlConnectionUtils.sslConnection(AUTH_TOKEN_URL, tokenRequest, "POST");

			} catch (Exception e) {
				// TODO Auto-generated catch block
				return null;

			}

            ObjectMapper objectMapper = new ObjectMapper();
            AppleTokenResponseVO tokenResponse = objectMapper.readValue (response, AppleTokenResponseVO.class);

            if (tokenRequest != null) {
                return tokenResponse;
            }
        } catch (JsonProcessingException e) {
        	log.error("JsonProcessing Error", e);

        } catch (IOException e) {
        	log.error("IO Error", e);
		}

        return null;
    }

    /**
     * Apple Meta Value
     *
     * @return
     */
    public Map<String, String> getMetaInfo() {

        Map<String, String> metaInfo = new HashMap<>();

        metaInfo.put("CLIENT_ID", AUD);
        metaInfo.put("NONCE", "20B20D-0S8-1K8"); // Test value
    	metaInfo.put("REDIRECT_URI", APPLE_WEBSITE_URL);

        return metaInfo;
    }

    /**
     * id_token을 decode해서 payload 값 가져오기
     *
     * @param id_token
     * @return
     */
    public ApplePayloadVO decodeFromIdToken(String id_token) {

        try {
            SignedJWT signedJWT = SignedJWT.parse(id_token);
            JWTClaimsSet getPayload = signedJWT.getJWTClaimsSet();

            ObjectMapper objectMapper = new ObjectMapper();
            ApplePayloadVO payload = objectMapper.readValue(getPayload.toString(), ApplePayloadVO.class);

            if (payload != null) {
                return payload;
            }
        } catch (Exception e) {
        	log.error("decodeFromIdToken Error", e);
        }

        return null;
    }

}
