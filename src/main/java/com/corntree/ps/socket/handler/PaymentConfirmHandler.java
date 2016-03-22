package com.corntree.ps.socket.handler;

import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.corntree.ps.dao.PaymentDao;
import com.corntree.ps.socket.netPackage.AbstractPackage;
import com.corntree.ps.socket.netPackage.PaymentConfirm;


@Component("PAYMENT_CONFIRM")
public class PaymentConfirmHandler  extends BaseHandler{
	
	private static final Logger logger = LoggerFactory.getLogger(PaymentConfirmHandler.class.getName());

	@Autowired
	private PaymentDao paymentDao;
	
	@Override
	public void handle(AbstractPackage packetData, Channel channel)
			throws RuntimeException {
		
		PaymentConfirm confirm = (PaymentConfirm)packetData;
		String code = confirm.getTransactionCode();
		if (confirm.getRetCode() == 0) {
			paymentDao.updatePaymentFinished(code);
			logger.info("Finished transactionCode(" + code + "), update database.");
		} else {
			logger.warn("Process transactionCode(" + code + ") failed.");
		}
	}

}
