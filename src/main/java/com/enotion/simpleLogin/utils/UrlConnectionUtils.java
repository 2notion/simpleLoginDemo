package com.enotion.simpleLogin.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class UrlConnectionUtils {

	public static <E> String sslConnection(String pURL, String param, String method) throws Exception {

		String myResult = "";

        URL url = null;
        HttpsURLConnection http = null;
        BufferedReader reader = null;

        try {

        	TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
        		public X509Certificate[] getAcceptedIssuers() {
        			return null;
        		}
        		public void checkClientTrusted(X509Certificate[] certs, String authType) {
        		}
        		public void checkServerTrusted(X509Certificate[] certs, String authType) {
        		}
        	}};

        	System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");

        	SSLContext sc = SSLContext.getInstance("TLSv1.2");
        	sc.init(null, trustAllCerts, new SecureRandom());
        	HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

//        	StringBuffer sb = new StringBuffer();

//        	if (pList != null) {
//
//                Set key = pList.keySet();
//
//                for (Iterator<E> iterator = key.iterator(); iterator.hasNext();) {
//                    String keyName = (String) iterator.next();
//                    String valueName = pList.get(keyName);
//                    sb.append("&").append(keyName).append("=").append(valueName);
//                }
//            }
//
//        	pURL += sb.toString();

        	System.out.println(pURL);



            //   URL 설정하고 접속하기
            url = new URL(pURL); // URL 설정
            http = (HttpsURLConnection) url.openConnection(); // 접속

            //--------------------------
            //   전송 모드 설정 - 기본적인 설정
            //--------------------------
            http.setDefaultUseCaches(false);
            http.setDoInput(true); // 서버에서 읽기 모드 지정

            http.setDoOutput(true); // 서버로 쓰기 모드 지정

            //--------------------------
            // 헤더 세팅
            //--------------------------
            // 서버에게 웹에서 <Form>으로 값이 넘어온 것과 같은 방식으로 처리하라는 걸 알려준다
            http.setRequestProperty("content-type", "application/x-www-form-urlencoded");

            if("POST".equals(method)) {
            	http.setRequestMethod("POST"); // 전송 방식은 POST

            //--------------------------
            //   서버로 값 전송
            //--------------------------
            StringBuffer buffer = new StringBuffer();

            //HashMap으로 전달받은 파라미터가 null이 아닌경우 버퍼에 넣어준다
//            if (pList != null) {
//
//                Set key = pList.keySet();
//
//                for (Iterator<E> iterator = key.iterator(); iterator.hasNext();) {
//                    String keyName = (String) iterator.next();
//                    String valueName = pList.get(keyName);
//                    buffer.append("&").append(keyName).append("=").append(valueName);
//                }
//            }

            //--------------------------
            //   전송 예제1
            //--------------------------

//			  OutputStreamWriter outStream = new OutputStreamWriter(http.getOutputStream(),
//			  "UTF-8"); PrintWriter writer = new PrintWriter(outStream);
//			  writer.write(buffer.toString()); writer.flush();


            //--------------------------
            //   전송 예제2
            //--------------------------

            buffer.append(param);
            OutputStreamWriter outStream = new OutputStreamWriter(http.getOutputStream(),
      			  "UTF-8"); PrintWriter writer = new PrintWriter(outStream);
      			  writer.write(buffer.toString()); writer.flush();
            }

            //--------------------------
            //   서버에서 전송받기
            //--------------------------
            if(http.getResponseCode() == HttpsURLConnection.HTTP_OK) {
            	reader = new BufferedReader(new InputStreamReader(http.getInputStream(), "UTF-8"));
                StringBuffer strData = new StringBuffer();

                String str;
                while ((str = reader.readLine()) != null) {
                	strData.append(str);
                }

                http.disconnect();
                reader.close();

                myResult = strData.toString();

            }else {
            	System.out.println("응답코드 : " + http.getResponseCode());
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException | IOException | KeyManagementException e) {
        	e.printStackTrace();
        } finally {
        	if (reader != null) {
                try {
                    reader.close();
                } catch (IOException exp) {
                	log.error("IOError", exp);
                }
            }
        }
        return myResult;
	}
}
