package com.corntree.ps.api;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;


@RequestMapping("/login")
@RestController
public class AnySdkController {
	public static final Logger logger = LoggerFactory.getLogger(AnySdkController.class);

    /**
     * anysdk统一登录地址
     */
    private String loginCheckUrl = "http://oauth.anysdk.com/api/User/LoginOauth/";

    /**
     * connect time out
     * 
     * @var int
     */
    private int connectTimeOut = 5 * 1000;

    /**
     * time out second
     * 
     * @var int
     */
    private int timeOut = 5 * 1000;

    /**
     * user agent
     * 
     * @var string
     */
    private static final String userAgent = "px v1.0";

    /**
     * 检查登录合法性及返回sdk返回的用户id或部分用户信息
     * @param request
     * @param response
     * @return 验证合法 返回true 不合法返回 false
     */
    
    @RequestMapping(value = "AnySdk/OauthLogin", produces = "text/html")
    @ResponseBody
    public ResponseEntity<String> OauthLogin(HttpServletRequest request, HttpServletResponse response ) {
        try {
            String appMsg = "/AnySdk/OauthLogin " + request.getRemoteAddr();
            logger.info(appMsg);
            Map<String,String[]> params = request.getParameterMap();
            
            StringBuilder sb = new StringBuilder();
            for (Iterator iter = params.keySet().iterator(); iter.hasNext();) {
				String name = (String) iter.next();
				String[] values = (String[]) params.get(name);
				String valueStr = "";
				for (int i = 0; i < values.length; i++) {
				    valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
				}
				sb.append(name).append(":").append(valueStr);
            }
            logger.info(sb.toString());
            //检测必要参数
            if(parametersIsset( params )) {
                return createPlainResponseEntity("parameter not complete");
            }
            
            String queryString = getQueryString( request );
            logger.info("loginCheckUrl = " + loginCheckUrl + ";  param = " + queryString);
            URL url = new URL(loginCheckUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty( "User-Agent", userAgent );
            conn.setReadTimeout(timeOut);
            conn.setConnectTimeout(connectTimeOut);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            
            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter( new OutputStreamWriter(os, "UTF-8") );
            writer.write( queryString );
            writer.flush();
            tryClose( writer );
            tryClose( os );
            conn.connect();
            
            InputStream is = conn.getInputStream();
            String result = stream2String( is ); 
            return createPlainResponseEntity(result);
        } catch (Exception e) {
            logger.warn(" failed.", e);
            return createPlainResponseEntity("unkonw error");
        }
    }
    
    public void setLoginCheckUrl(String loginCheckUrl) {
        this.loginCheckUrl = loginCheckUrl;
        logger.info("setLoginCheckUrl() loginCheckUrl = " + loginCheckUrl);
    }

    /**
     * 设置连接超时
     * @param connectTimeOut
     */
    public void setConnectTimeOut(int connectTimeOut) {
        this.connectTimeOut = connectTimeOut;
        logger.info("setConnectTimeOut() connectTimeOut = " + connectTimeOut);
    }

    /**
     * 设置超时时间
     * @param timeOut
     */
    public void setTimeOut(int timeOut) {
        this.timeOut = timeOut;
        logger.info("setTimeOut() timeOut = " + timeOut);
    }


    /**
     * check needed parameters isset 检查必须的参数 channel
     * uapi_key：渠道提供给应用的app_id或app_key（标识应用的id）
     * uapi_secret：渠道提供给应用的app_key或app_secret（支付签名使用的密钥）
     * 
     * @param params
     * @return boolean
     */
    private boolean parametersIsset(Map<String, String[]> params) {
        return !(params.containsKey("channel") && params.containsKey("uapi_key")
                && params.containsKey("uapi_secret"));
    }

    /**
     * 获取查询字符串
     * @param request
     * @return
     */
    private String getQueryString( HttpServletRequest request )  {
        Map<String, String[]> params = request.getParameterMap();
        String queryString = "";
        for (String key : params.keySet()) {
            String[] values = params.get(key);
            for (int i = 0; i < values.length; i++) {
                String value = values[i];
                
                if ("accountSign".equals(key))
                {
                    value = java.net.URLEncoder.encode(value);
                }
                queryString += key + "=" + value + "&";
            }
        }
        queryString = queryString.substring(0, queryString.length() - 1);
        return queryString;
    }

    /**
     * 获取流中的字符串
     * @param is
     * @return
     */
    private String stream2String( InputStream is ) {
        BufferedReader br = null;
        try{
            br = new BufferedReader( new java.io.InputStreamReader( is ));  
            String line = "";
            StringBuilder sb = new StringBuilder();
            while( ( line = br.readLine() ) != null ) {
                sb.append( line );
            }
            return sb.toString();
        } catch( Exception e ) {
            e.printStackTrace();
        } finally {
            tryClose( br );
        }
        return "";
    }
    
    /**
     * 向客户端应答结果
     * @param response
     * @param content
     */
    private void sendToClient( HttpServletResponse response, String content ) {
        response.setContentType( "text/plain;charset=utf-8");
        try{
            PrintWriter writer = response.getWriter();
//            content = "[\""+content+"\"]";
            writer.write(content);
            writer.flush();
        } catch( Exception e ) {
            e.printStackTrace();
        }
        logger.info("sendToClient() content = " + content);
    }
    /**
     * 关闭输出流
     * @param os
     */
    private void tryClose( OutputStream os ) {
        try{
            if( null != os ) {
                os.close();
                os = null;
            }
        } catch( Exception e ) {
            e.printStackTrace();
        }
    }
    
    /**
     * 关闭writer
     * @param writer
     */
    private void tryClose( java.io.Writer writer ) {
        try{
            if( null != writer ) {
                writer.close();
                writer = null;
            }
        } catch( Exception e ) {
            e.printStackTrace();
        }
    }
    
    /**
     * 关闭Reader
     * @param reader
     */
    private void tryClose( java.io.Reader reader ) {
        try{
            if( null != reader ) {
                reader.close();
                reader = null;
            }
        } catch( Exception e ) {
            e.printStackTrace();
        }
    }
    
    private ResponseEntity<String> createPlainResponseEntity(String response) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        return new ResponseEntity<String>(response, headers, HttpStatus.OK);
    }
}
