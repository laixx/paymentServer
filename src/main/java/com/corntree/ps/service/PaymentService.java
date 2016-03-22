package com.corntree.ps.service;

import java.util.HashMap;
import java.util.Map;

import org.jboss.netty.channel.Channel;
import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;

import com.corntree.ps.domain.thirdparty.IosPayResponse;
import com.corntree.ps.dao.PaymentDao;
import com.corntree.ps.dao.PaymentReceiptDao;
import com.corntree.ps.domain.Payment;
import com.corntree.ps.domain.PaymentReceipt;
import com.corntree.ps.domain.Session;

@Service
public class PaymentService {
    public static final Logger logger = LoggerFactory.getLogger(PaymentService.class);


    private Map<String, Float> moneryMap = new HashMap<String, Float>();
    {
        moneryMap.put("com.ct.dxggj.qcplay.648", 648f);
        moneryMap.put("com.ct.dxggj.qcplay.328", 328f);
        moneryMap.put("com.ct.dxggj.qcplay.198", 198f);
        moneryMap.put("com.ct.dxggj.qcplay.98", 98f);
        moneryMap.put("com.ct.dxggj.qcplay.30", 30f);
        moneryMap.put("com.ct.dxggj.qcplay.12", 12f);
        moneryMap.put("com.ct.dxggj.qcplay2.60", 60f);
        moneryMap.put("com.ct.dxggj.qcplay.25", 25f);
        moneryMap.put("com.ct.dxggj.qcplay.6", 6f);
    }
    private Map<String, Float> moneryMap2 = new HashMap<String, Float>();
    {
        moneryMap2.put("com.ct.dxback1.qcplay.648", 648f);
        moneryMap2.put("com.ct.dxback1.qcplay.328", 328f);
        moneryMap2.put("com.ct.dxback1.qcplay.198", 198f);
        moneryMap2.put("com.ct.dxback1.qcplay.98", 98f);
        moneryMap2.put("com.ct.dxback1.qcplay.30", 30f);
        moneryMap2.put("com.ct.dxback1.qcplay.12", 12f);
        moneryMap2.put("com.ct.dxback1.qcplay.60", 60f);
        moneryMap2.put("com.ct.dxback1.qcplay.25", 25f);
        moneryMap2.put("com.ct.dxback1.qcplay.6", 6f);
    }
    
    private static int transcationCount = 0;

    @Value("${discount.alipay}")
    public float discountAlipay;
    
    @Value("${supportAppStore}")
    public int supportAppStore;
    
	@Autowired
	public PaymentDao paymentDao;
    @Autowired
    public PaymentReceiptDao paymentReceiptDao;
    
    @Autowired
    private GsService gsService;
    

    private Gson gson = new Gson();
    
    private RestTemplate restTemplate = new RestTemplate();
    {
        SimpleClientHttpRequestFactory requestFactory = (SimpleClientHttpRequestFactory) restTemplate
                .getRequestFactory();
        requestFactory.setConnectTimeout(30 * 1000);
        requestFactory.setReadTimeout(30 * 1000);
    }

	public Payment generatePayment(int serverId, long accountId, 
			long playerId, int ingots) {
		String code = String.format("%03d%d%d%d%02d", serverId, accountId, playerId, System.currentTimeMillis(), transcationCount++);
        if (transcationCount >= 100) {
            transcationCount = 0;
        }
        logger.warn("Save transaction:" + code + ",serverId:" + serverId + ",acountId:" + accountId +
                ",playerId:" + playerId + ",ingots:" + ingots);
        Payment payment = new Payment(serverId, accountId, playerId, ingots, code);
        paymentDao.save(payment);
        return payment;
	}

    public int updatePaymentAsPaid(String transactionCode, Float fee, String appendMsg) {
        return updatePaymentAsPaid(transactionCode, fee, appendMsg, Payment.RECHARGE_CHANNEL_3RD);
    }
    
    public int updatePaymentAsPaid(String transactionCode, Float fee, String appendMsg, short rechargeChannel) {
        return updatePaymentAsPaid(transactionCode, fee, appendMsg, Payment.RECHARGE_CHANNEL_3RD, 1.0f);
    }
    
    public int updatePaymentAsPaid(String transactionCode, Float fee, String appendMsg, short rechargeChannel, float discount) {
        return updatePaymentAsPaid(transactionCode, fee, appendMsg, Payment.RECHARGE_CHANNEL_3RD, discount, false);
    }
    
    public int updatePaymentAsPaid(String transactionCode, Float fee, String appendMsg, short rechargeChannel, float discount, boolean sandbox) {
    	logger.info("enter updatePaymentAsPaid");
        Payment payment = paymentDao.findByTransactionCode(transactionCode);
        if (payment == null) {
            logger.warn("Save transaction not find," + transactionCode);
            return 2;
        }
        if (payment.isFinished()) {
            logger.warn("transaction finished," + transactionCode);
            return 1;
        }
        payment.setFee(fee);
        payment.setAppendMsg(appendMsg);
        payment.setRechargeChannel(rechargeChannel);
        payment.setDiscount(discount);
        if (sandbox == true) {
            payment.setSandbox();
        }
        updatePaymentAsPaid(payment);
        logger.info("exit updatePaymentAsPaid");
        return 0;
    }
    
    private void updatePaymentAsPaid(Payment payment) {
        if (payment != null) {
            Session session = gsService.getSession(payment.getServerId()); 
            if (session != null) {
                Channel channel = session.getChannel();
                if (null == channel) {
                    logger.warn("null channel");
                    return;
                }
                paymentDao.updatePayment(payment);
                logger.warn("Payment notify, ServerId:" + payment.getServerId() +
                        ", AccountId:" + payment.getAccountId() + ", PlayerId:" + payment.getPlayerId() +
                        ", fee:" + payment.getFee() + ", discount:" + payment.getDiscount());
                channel.write(payment);
            } else {
                logger.warn("null session");
            }
        } else {   
            logger.warn("null payment");
        }
    }
    
    private String restResp(String serverUrl, HttpEntity<String> httpEntity) {
    	String result = "";
        int retryCount = 3;
        for (int i = 0; i < retryCount; i++) {
            try {
                result = restTemplate.postForObject(serverUrl, httpEntity, String.class);
                if (result.length() > 0) {
                    break;
                }
            } catch (Exception e) {
                if (i == retryCount - 1) {
                    logger.warn("Network error 1!", e);
                    return "";
                }
            }
        }
        
        logger.info("ios server response=" + result);

        return result;
    }

    public int verifyReceipt(String transactionCode, String transactionReceipt) {

        Base64 b = new Base64();
        String srcRequest = new String(b.decode(transactionReceipt));
        String keyPurchase = "purchase-info";
        int beginPos = srcRequest.indexOf(keyPurchase);
        if (beginPos < 0) {
            logger.warn("receipt invalid!");
            return 3;
        }

        beginPos += keyPurchase.length() + 1;
        beginPos = srcRequest.indexOf("\"", beginPos) + 1;
        int endPos = srcRequest.indexOf("\"", beginPos + 1);
        String purchaseInfo = srcRequest.substring(beginPos, endPos);
        
        PaymentReceipt paymentReceipt = paymentReceiptDao.findByReceipt(purchaseInfo);
        if (paymentReceipt != null) {
            logger.warn("receipt already exist 1!");
            return 1;
        }
        paymentReceipt = paymentReceiptDao.findByTransactionCode(transactionCode);
        if (paymentReceipt != null) {
            logger.warn("receipt already exist 2!");
            return 1;
        }
        int state = 0;

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("receipt-data", transactionReceipt);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> httpEntity = new HttpEntity<String>(gson.toJson(params),
                headers);

        String appstoreProductVerifyUrl = "https://buy.itunes.apple.com/verifyReceipt";
        String appstoreSandboxVerifyUrl = "https://sandbox.itunes.apple.com/verifyReceipt";
        
        String result = restResp(appstoreProductVerifyUrl, httpEntity);
        if (result.length() == 0) {
        	return 3;
        }

        IosPayResponse payRes = gson.fromJson(result, IosPayResponse.class);
        if ("21007".equals(payRes.getStatus())) {
        	logger.info("payRes.getStatus() ==  21007");
        	
        	result = restResp(appstoreSandboxVerifyUrl, httpEntity);
        	if (result.length() == 0) {
            	return 3;
            }
        	payRes = gson.fromJson(result, IosPayResponse.class);
        }
        if ("0".equals(payRes.getStatus())) {
            Float money = moneryMap.get(payRes.getReceipt().getProduct_id());
            if (money == null || money == 0) {
                money = moneryMap2.get(payRes.getReceipt().getProduct_id());
            }
            if (money == null || money == 0) {
                logger.warn("error product id " + payRes.getReceipt().getProduct_id());
                return 3;
            }
            logger.info("updatePaymentAsPaid: transactionCode:" + transactionCode + ",money:" + money);
            
        	int status = updatePaymentAsPaid(transactionCode, money, result, Payment.RECHARGE_CHANNEL_APPSTORE);
            if (status != 0) {
                logger.warn("ios payment process failed.  status=" + status);
            }
            else {
                PaymentReceipt temp = new PaymentReceipt(transactionCode, purchaseInfo);
                paymentReceiptDao.save(temp);
            }
            state = status;
            logger.info("state:" + state);
        } else {
            logger.warn("ios payment process failed." + payRes.getStatus());
            state = Integer.parseInt(payRes.getStatus());
            logger.info("state:" + state);
        }
        return state;
    }
}
