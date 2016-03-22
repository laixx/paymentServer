package com.corntree.ps.socket.netPackage;


public class BasePackage {  

	public enum MessageID {
		  MSG_GS_NONE, MSG_GS_TIMELAPSE, MSG_GS_LOGIN, MSG_GS_LOGOUT,
		  MSG_GS_4, MSG_GS_5, MSG_GS_6,MSG_GS_7,
		  MSG_GS_8,MSG_GS_9,MSG_GS_10,MSG_GS_11,
		  MSG_GS_PAYMENT,MSG_GS_13,MSG_GS_14,MSG_GS_PUSH;

	    public static MessageID valueOf(int value) {
	        switch (value) {
	          case 0: return MSG_GS_NONE;
	          case 1: return MSG_GS_TIMELAPSE;
	          case 2: return MSG_GS_LOGIN;
	          case 3: return MSG_GS_LOGOUT;
	          case 12: return MSG_GS_PAYMENT;
	          case 15: return MSG_GS_PUSH;
	          default: return null;
	        }
	      }
	}
	private short length;
	private int keep;
	private short messageID;
	private short dataLength;
	private AbstractPackage data;
	
	public short getMessageID() {
		return messageID;
	}
	public void setMessageID(short messageID) {
		this.messageID = messageID;
	}
	public int getKeep() {
		return keep;
	}
	public void setKeep(int keep) {
		this.keep = keep;
	}
	public short getLength() {
		return length;
	}
	public void setLength(short length) {
		this.length = length;
	}
	public short getDataLength() {
		return dataLength;
	}
	public void setDataLength(short dataLength) {
		this.dataLength = dataLength;
	}
	public AbstractPackage getData() {
		return data;
	}
	public void setData(AbstractPackage data) {
		this.data = data;
	}

}
