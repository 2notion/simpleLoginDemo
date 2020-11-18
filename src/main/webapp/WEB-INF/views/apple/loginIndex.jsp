<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<form id="certFrm" name="certFrm" action="">
<input type="hidden" name="client_id" value="${client_id }"/>	<!-- 변경불가-->
<input type="hidden" name="redirect_uri" value="${redirect_uri}">	<!-- 회원사코드 -->
<input type="hidden" name="nonce" value="${nonce}">	<!-- 토큰 -->
<input type="hidden" name="response_type" value="code id_token">
<input type="hidden" name="scope" value="name email">
<input type="hidden" name="response_mode" value="form_post">
</form>

<script>
window.onload = function () {
	request();
}

function request(){
	document.certFrm.action = "https://appleid.apple.com/auth/authorize";
	document.certFrm.method = "get";
	document.certFrm.submit();
}

</script>