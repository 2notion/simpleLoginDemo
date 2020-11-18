<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<script src="/nbridge/js/libs/jquery-1.12.4.min.js"></script>

<script type="text/javascript">
//<![CDATA[
    $(function(){
    	var resultCode = ${SUCC_YN};
    	if(resultCode){
    		window.opener.returnData( ${payload} );
		}else{
			alert('본인인증이 실패했습니다.');
			self.close();
		}
    });
//>
</script>