package com.corntree.ps.socket.netPackage;

public class PaymentConfirm  extends AbstractPackage{
	private String transactionCode;
	
	private int retCode;

	public String getTransactionCode() {
		return transactionCode;
	}

	public void setTransactionCode(String transactionCode) {
		this.transactionCode = transactionCode;
	}

	public int getRetCode() {
		return retCode;
	}

	public void setRetCode(int retCode) {
		this.retCode = retCode;
	}

}
