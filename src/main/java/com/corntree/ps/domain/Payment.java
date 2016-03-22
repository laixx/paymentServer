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
@Table(name = "payment")
public class Payment extends AbstractPackage {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id", columnDefinition = "int(11)")
	private Long id;
	
	@Column(unique = true, columnDefinition = "varchar(50)")
	private String transactionCode;
	
	@Column(columnDefinition = "int(11)")
	private Long accountId;
	
	@Column(columnDefinition = "bigint(21)")
	private Long playerId;
	

	@Column(columnDefinition = "mediumint default '0'")
	private Integer ingots;

	@Column(columnDefinition = "tinyint(3) default 0")
	private boolean finished;

	@Column(columnDefinition = "float default '0'")
	private float fee;// 单位:元

	@Temporal(TemporalType.TIMESTAMP)
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date tradeTime;

	@Column(columnDefinition = "tinyint default '1'")
	private Integer serverId;

	@Column(columnDefinition = "varchar(512) default ''")
	private String serverName;
	
	@Column(columnDefinition = "varchar(1024) default 'TEST'")
	private String appendMsg;
	
	// 充值渠道:1->支付宝, 2->appstore, 3->第三方渠道
	public static final short RECHARGE_CHANNEL_ALIPAY = 1;
	public static final short RECHARGE_CHANNEL_APPSTORE = 2;
	public static final short RECHARGE_CHANNEL_3RD = 3;
	
	@Column(columnDefinition = "tinyint default '3'")
	private short rechargeChannel;
	
	@Column(columnDefinition = "float default '1.0'")
	private float discount;
	
	@Column(columnDefinition = "tinyint(3) default 0")
	private Integer sandbox;
	

	public Payment() {
		super();
	}

	public Payment(String transactionCode, Long playerId, Integer ingots) {
		super();
		this.rechargeChannel = RECHARGE_CHANNEL_3RD;
		this.transactionCode = transactionCode;
		this.playerId = playerId;
		this.ingots = ingots;
		this.rechargeChannel = RECHARGE_CHANNEL_3RD;
		this.discount = 1.0f;
		this.sandbox = 0;
		this.tradeTime = new Date();
	}
	
	public Payment(Integer serverId, Long accountId, Long playerId, Integer ingots, String transactionCode) {
		super();
		this.rechargeChannel = RECHARGE_CHANNEL_3RD;
		this.transactionCode = transactionCode;
		this.serverId = serverId;
		this.accountId = accountId;
		this.playerId = playerId;
		this.ingots = ingots;
		this.rechargeChannel = RECHARGE_CHANNEL_3RD;
		this.discount = 1.0f;
		this.sandbox = 0;
		this.tradeTime = new Date();
	}
	
	public Long getId() {
		return id;
	}
	
	public float getFee() {
		return fee;
	}
	
	public void setFee(float fee) {
		this.fee = fee;
	}

	public String getAppendMsg() {
		return appendMsg;
	}

	public void setAppendMsg(String appendMsg) {
		this.appendMsg = appendMsg;
	}
	
    public boolean isFinished() {
        return this.finished;
    }
	
	public String getTransactionCode() {
		return transactionCode;
	}

    public short getRechargeChannel() {
        return rechargeChannel;
    }
    
    public void setRechargeChannel(short channel) {
        rechargeChannel = channel;
    }
    
    public float getDiscount() {
        return discount;
    }
    
    public void setDiscount(float value) {
        discount = value;
    }
    
    public int getSandbox() {
        return sandbox;
    }
    
    public void setSandbox() {
        sandbox = 1;
    }
    
    public int getIngots() {
    	return this.ingots;
    }
    
    public int getServerId() {
    	return this.serverId;
    }
    
    public Long getAccountId() {
    	return this.accountId;
    }
    
    public Long getPlayerId() {
    	return this.playerId;
    }
}
