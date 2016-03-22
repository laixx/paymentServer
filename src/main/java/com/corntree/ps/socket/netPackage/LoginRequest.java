package com.corntree.ps.socket.netPackage;

public class LoginRequest extends AbstractPackage{
	private short serverID;
	private short serverPort;
	private short maxPlayer;
	private int serverType;
	private byte response;
	
	public short getServerID() {
		return serverID;
	}
	public void setServerID(short serverID) {
		this.serverID = serverID;
	}
	public short getServerPort() {
		return serverPort;
	}
	public void setServerPort(short serverPort) {
		this.serverPort = serverPort;
	}
	public short getMaxPlayer() {
		return maxPlayer;
	}
	public void setMaxPlayer(short maxPlayer) {
		this.maxPlayer = maxPlayer;
	}
	public int getServerType() {
		return serverType;
	}
	public void setServerType(int serverType) {
		this.serverType = serverType;
	}
	public byte getResponse() {
		return response;
	}
	public void setResponse(byte response) {
		this.response = response;
	}
}
