package com.enotion.simpleLogin.apple.web;


import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.enotion.simpleLogin.apple.service.AppleLoginService;
import com.enotion.simpleLogin.apple.vo.AppleAppsResponseVO;
import com.enotion.simpleLogin.apple.vo.ApplePayloadVO;
import com.enotion.simpleLogin.apple.vo.AppleServicesResponseVO;
import com.enotion.simpleLogin.apple.vo.AppleTokenResponseVO;
import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class AppleLoginController {

    @Autowired
    AppleLoginService nAppleLoginService;

    /**
     * Sign in with Apple - JS Page (index.html)
     *
     * @param model
     * @return
     */
	@RequestMapping(value="/login")
    public String appleLoginPage(Model model) {
		Map<String, String> metaInfo = nAppleLoginService.getLoginMetaInfo();

		System.out.println("*******************************");
		System.out.println(metaInfo.get("CLIENT_ID"));
		System.out.println(metaInfo.get("REDIRECT_URI"));
		System.out.println(metaInfo.get("NONCE"));
		System.out.println("*******************************");

        model.addAttribute("client_id", metaInfo.get("CLIENT_ID"));
        model.addAttribute("redirect_uri", metaInfo.get("REDIRECT_URI"));
        model.addAttribute("nonce", metaInfo.get("NONCE"));

        return "/apple/loginIndex";
    }

//    /**
//     * Apple login page Controller (SSL - https)
//     *
//     * @param model
//     * @return
//     */
//    @RequestMapping(value="/COMN000200.kb")
//    public String appleLogin(Model model) {
//
//        Map<String, String> metaInfo = nAppleLoginService.getLoginMetaInfo();
//
//        model.addAttribute("client_id", metaInfo.get("CLIENT_ID"));
//        model.addAttribute("redirect_uri", metaInfo.get("REDIRECT_URI"));
//        model.addAttribute("nonce", metaInfo.get("NONCE"));
//        model.addAttribute("response_type", "code id_token");
//        model.addAttribute("scope", "name email");
//        model.addAttribute("response_mode", "form_post");
//
//        return "redirect:https://appleid.apple.com/auth/authorize";
//    }

    /**
     * Apple Login 유저 정보를 받은 후 권한 생성
     *
     * @param serviceResponse
     * @return
     */
    @RequestMapping(value="/callback")
    public String servicesRedirect(Model model, AppleServicesResponseVO serviceResponse) {

    	boolean isResult = true;

        try {

        	 if (serviceResponse == null) {
        		 isResult = false;
             }

        	 if(isResult) {

	        	String code = serviceResponse.getCode();
	        	ApplePayloadVO applePayloadVO = null;
	            String client_secret = nAppleLoginService.getAppleClientSecret(serviceResponse.getId_token());

	            System.out.println("================================");
	            System.out.println("code ‣ " + code);
	            System.out.println("id_token ‣ " + serviceResponse.getId_token());
	            System.out.println("payload ‣ " + nAppleLoginService.getPayload(serviceResponse.getId_token()).toString());
	            System.out.println("client_secret ‣ " + client_secret);
	            System.out.println("================================");

	            applePayloadVO = nAppleLoginService.getPayload(serviceResponse.getId_token());

	            ApplePayloadVO returnVO = new ApplePayloadVO();

		        if(applePayloadVO != null) {

		            returnVO.setSub(StringUtils.trimToEmpty(applePayloadVO.getSub()));
		            returnVO.setIs_private_email(StringUtils.trimToEmpty(applePayloadVO.getIs_private_email()));
		            returnVO.setEmail(StringUtils.trimToEmpty(applePayloadVO.getEmail()));
		            returnVO.setNonce_supported(applePayloadVO.isNonce_supported() );

		            nAppleLoginService.requestCodeValidations(client_secret, code, null);

		            model.addAttribute("payload", new Gson().toJson(returnVO));

	            }else {

	            	isResult = false;

	            }

        	 }

        }catch(Exception e) {
        	isResult = false;
        }

		model.addAttribute("SUCC_YN",	isResult);

        return "/apple/loginCallback";
    }

    /**
     * refresh_token 유효성 검사
     *
     * @param client_secret
     * @param refresh_token
     * @return
     */
    @RequestMapping(value = "/refresh")
    @ResponseBody
    public AppleTokenResponseVO refreshRedirect(@RequestParam String client_secret, @RequestParam String refresh_token) {
        return nAppleLoginService.requestCodeValidations(client_secret, null, refresh_token);
    }

    /**
     * Apple 유저의 이메일 변경, 서비스 해지, 계정 탈퇴에 대한 Notifications을 받는 Controller (SSL - https (default: 443))
     *
     * @param appsResponse
     */
    @RequestMapping(value="/endpoint")
    @ResponseBody
    public void appsToEndpoint(@RequestBody AppleAppsResponseVO appsResponse) {
    	log.debug("[/path/to/endpoint] RequestBody ‣ " + appsResponse.getPayload());
    }

}
