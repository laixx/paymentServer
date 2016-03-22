package com.corntree.ps.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;

import com.alipay.util.AlipayNotify;
import com.corntree.ps.domain.Payment;
import com.corntree.ps.domain.PaymentReceipt;
import com.corntree.ps.domain.thirdparty.UcPayResponse;
import com.corntree.ps.service.PaymentService;

@RestController
@RequestMapping("/payment")
public class PaymentController {
    public static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    //private static final String UC_APP_ID = "21263";
    //private static final String UC_APP_KEY = "f7f1aba065806ef7d91ebf3348b2db9d";

    @Value("${UC_CP_ID}")
    public String UC_CP_ID;
    @Value("${UC_APP_KEY}")
    public String UC_APP_KEY;
    
    @Autowired
    PaymentService paymentService;

    private Gson gson = new Gson();
    
    private RestTemplate restTemplate = new RestTemplate();
    {
        SimpleClientHttpRequestFactory requestFactory = (SimpleClientHttpRequestFactory) restTemplate
                .getRequestFactory();
        requestFactory.setConnectTimeout(30 * 1000);
        requestFactory.setReadTimeout(30 * 1000);
    }
    
    private ResponseEntity<String> createPlainResponseEntity(String response) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/text; charset=utf-8");
        return new ResponseEntity<String>(response, headers, HttpStatus.OK);
    }
    
    @RequestMapping(value = "test")
	@ResponseBody
	public String test() {
    	//paymentService.paymentDao.updatePaymentFinished("00110010000145535615855402");

        //PaymentReceipt temp = new PaymentReceipt("11111", "dddddddd");
        //paymentService.paymentReceiptDao.save(temp);
        /*PaymentReceipt paymentReceipt = paymentService.paymentReceiptDao.findByReceipt("dddddddda");
        if (paymentReceipt != null) {
            return "find";
        }*/
    	//paymentService.updatePaymentAsPaid("00210012345145550241596400", 200f, "testaaaa");
    	return "success";
    }
    
	@RequestMapping(value = "request/transaction")
	@ResponseBody
	public String transactionReq(
            @RequestParam(value = "serverId") int serverId,
            @RequestParam(value = "accountId") long accountId, 
            @RequestParam(value = "playerId") long playerId,
            @RequestParam(value = "ingots") int ingots, 
            HttpServletRequest httpRequest
			) {
        String appMsg = "/request/transaction " + httpRequest.getRemoteAddr() + " serverId:" + serverId + 
                ",accountId:" + accountId + ",playerId:" + playerId + ",ingots:" + ingots;
        logger.info(appMsg);
        
        Payment payment = paymentService.generatePayment(serverId, accountId, playerId, ingots);
		
		return payment.getTransactionCode();
	}
	
	//----------------------------------------------------------

    @RequestMapping(value = "/request/verify")      //客户端请求校验订单
    @ResponseBody
    public String VerifyReceipt(
            @RequestParam(value = "transactionCode") String transactionCode,
            @RequestParam(value = "transactionReceipt") String transactionReceipt, 
            HttpServletRequest httpRequest) {

        if (paymentService.supportAppStore == 0) {
            logger.info("/request/verify do not support appstore");
            return "" + 3;
        }
        String appMsg = "/request/verify " + httpRequest.getRemoteAddr() + 
                ",transactionCode:" + transactionCode + ",transactionReceipt:" + transactionReceipt;
        logger.info(appMsg);
        try {
            if ("".equals(transactionCode) || "".equals(transactionReceipt)) {
                logger.warn("TransactionCode or transactionReceipt is null!");
                return "" + 2;
            }
            int state = 0;
            logger.info("begin  paymentService.verifyReceipt");
            state = paymentService.verifyReceipt(transactionCode, transactionReceipt);
            logger.info("end  paymentService.verifyReceipt");
            return "" + state;
        } catch (Exception e) {
            logger.warn("TransactionCode verify error!", e);
            return "" + 2;
        }
    }

    @RequestMapping(value = "/notify/alipay3", method = RequestMethod.POST)
    @ResponseBody
    public String alipay3Notify(
            HttpServletRequest httpRequest) {
                
        StringBuilder appMsg = new StringBuilder();
        appMsg.append("/api/notify/alipay3 ").append(httpRequest.getRemoteAddr());

        //获取支付宝POST过来反馈信息
        Map<String,String> params = new HashMap<String,String>();
        Map requestParams = httpRequest.getParameterMap();
        for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            params.put(name, valueStr);
            appMsg.append(name).append(":").append(valueStr).append(" ");
        }
        
        logger.info(appMsg.toString());
        
        try {
            //获取支付宝的通知返回参数，可参考技术文档中页面跳转同步通知参数列表(以下仅供参考)//
            //商户订单号
            String out_trade_no = httpRequest.getParameter("out_trade_no");
            //支付宝交易号
            String total_fee = httpRequest.getParameter("total_fee");
            //交易状态
            String trade_status = httpRequest.getParameter("trade_status");
            //获取支付宝的通知返回参数，可参考技术文档中页面跳转同步通知参数列表(以上仅供参考)//
            if (AlipayNotify.verify(params, "RSA")) {//验证成功
                if (trade_status.equals("TRADE_FINISHED")) {
                if (paymentService.updatePaymentAsPaid(out_trade_no, new Float(total_fee), appMsg.toString(), Payment.RECHARGE_CHANNEL_ALIPAY, paymentService.discountAlipay) < 2) {
                    return ("success");
                } else {
                    logger.warn("alipay3 payment process failed.");
                    return ("fail");
                }
              }
              else {
                return ("success");
              }
            } else {
                logger.warn("alipay3 payment verify failed.");
                return ("fail");
            }
        } catch (Exception e) {
            logger.warn("alipay3 payment process failed.", e);
            return ("fail");
        }
    }

    @RequestMapping(value = "/notify/uc", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<String> ucNotify(@RequestBody String json, HttpServletRequest httpRequest) {
        String appMsg = "/api/notify/uc " + httpRequest.getRemoteAddr() + " json:" + json;
        logger.info(appMsg);
        UcPayResponse notify = gson.fromJson(json, UcPayResponse.class);
        try {
            if (!"S".equals(notify.getData().getOrderStatus())) {
                logger.warn("uc payment OrderStatu error");
                return createPlainResponseEntity("SUCCESS");
            }

            String signSource = "accountId=" + notify.getData().getAccountId() + 
                          "amount=" + notify.getData().getAmount() + 
                          "callbackInfo=" + notify.getData().getCallbackInfo() + 
                          "cpOrderId=" + notify.getData().getCpOrderId() + 
                          "creator=" + notify.getData().getCreator() +
                          "failedDesc=" + notify.getData().getFailedDesc() + 
                          "gameId=" + notify.getData().getGameId() +
                          "orderId=" + notify.getData().getOrderId() + 
                          "orderStatus=" + notify.getData().getOrderStatus() +
                          "payWay=" + notify.getData().getPayWay() + 
                          UC_APP_KEY;

            /*String signSource = UC_CP_ID + 
            		"amount=" + notify.getData().getAmount() + 
            		"callbackInfo=" + notify.getData().getCallbackInfo();
            if (notify.getData().getCpOrderId() != null) {
            	signSource += "cpOrderId=" + notify.getData().getCpOrderId();
            }
            signSource += "failedDesc=" + notify.getData().getFailedDesc() + 
            		"gameId=" + notify.getData().getGameId() + 
            		"orderId=" + notify.getData().getOrderId() + 
            		"orderStatus=" + notify.getData().getOrderStatus() + 
            		"payWay=" + notify.getData().getPayWay() + 
            		"serverId=" + notify.getData().getServerId() + 
            		"ucid=" + notify.getData().getUcid() + 
            		UC_APP_KEY;*/
            String sign = DigestUtils.md5Hex(signSource.getBytes());
            if (!sign.equals(notify.getSign())) {
                logger.warn("uc payment sign error, signSource=" + signSource + ",sign=" + sign);
                return createPlainResponseEntity("FAILURE");    
            }
            logger.warn("uc payment sign success, signSource=" + signSource + ",sign=" + sign);
            int status;
            status = paymentService.updatePaymentAsPaid(notify.getData().getCpOrderId(), new Float(notify.getData().getAmount()), appMsg);
            if (status < 2)
                return createPlainResponseEntity("SUCCESS");
            else{
                logger.warn("uc payment process failed. status = " + status);
                return createPlainResponseEntity("FAILURE");    
            }
                
        } catch (Exception e) {
            logger.warn("uc payment process failed.", e);
            return createPlainResponseEntity("FAILURE");
        }
    }
    
    @RequestMapping(value = "/notify/anySdk", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<String> asNotify(HttpServletRequest httpRequest) {
        try {
            logger.info("******************flag1*************");
            String appMsg = "/api/notify/anySdk " + httpRequest.getRemoteAddr() + " / " + httpRequest.getParameter("private_data");
            logger.info(appMsg);
            String md5Values = getSignForAnyValid(httpRequest);
            String sign = httpRequest.getParameter("sign");
            if (!sign.equals(md5Values)) {
                logger.warn("sign error   sign = " + sign + "; md5Values = " + md5Values);
                return createPlainResponseEntity("ok");
            }
            paymentService.updatePaymentAsPaid(httpRequest.getParameter("private_data"), new Float(httpRequest.getParameter("amount")), appMsg);
            return createPlainResponseEntity("ok");
        } catch (Exception e) {
            logger.warn("payment process failed.", e);
            return createPlainResponseEntity("ok");
        }
    }    
 
    //获得anysdk支付通知 sign,将该函数返回的值与any传过来的sign进行比较验证
    public String getSignForAnyValid(HttpServletRequest request){
        Enumeration<String> requestParams = request.getParameterNames();//获得所有的参数名
        List<String> params=new ArrayList<String>();
        while (requestParams.hasMoreElements()) {
            params.add((String) requestParams.nextElement());
        }
        sortParamNames(params);// 将参数名从小到大排序，结果如：adfd,bcdr,bff,zx

        String paramValues="";
        for (String param : params) {//拼接参数值
            if (param.equals("sign")) {
                continue;
            }
            String paramValue=request.getParameter(param);
            if (paramValue!=null) {
                paramValues+=paramValue;
            }
        }
        logger.info("paramValues = " + paramValues);
        String md5Values = DigestUtils.md5Hex(paramValues);
        md5Values = DigestUtils.md5Hex(md5Values.toLowerCase()+"C283EBC369A1903220A76328D3A49E52").toLowerCase();
        return md5Values;
    }
    
    //将参数名从小到大排序，结果如：adfd,bcdr,bff,zx
    public static void sortParamNames(List<String> paramNames) {
     
          Collections.sort(paramNames, new Comparator<String>() {
               public int compare(String str1,String str2) {
                return str1.compareTo(str2);
               }
           });
    }
}
