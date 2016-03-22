package com.corntree.ps.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.springframework.format.annotation.DateTimeFormat;

import com.corntree.ps.socket.netPackage.AbstractPackage;

@Entity
@Table(name = "payment_receipt")
public class PaymentReceipt extends AbstractPackage {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id", columnDefinition = "int(11)")
	private Long id;

	@Column(unique = true, columnDefinition = "varchar(50)")
	private String transactionCode;

	@Column(unique = true, columnDefinition = "varchar(4096)")
	private String receipt;

	@Temporal(TemporalType.TIMESTAMP)
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date tradeTime;

	public PaymentReceipt() {
		super();
	}

	public PaymentReceipt(String argTransactionCode, String argReceipt) {
		super();
		this.transactionCode = argTransactionCode;
		this.receipt = argReceipt;
		this.tradeTime = new Date();
	}

	public String getReceipt() {
		return receipt;
	}

	public void setReceipt(String argReceipt) {
		this.receipt = argReceipt;
	}
}
