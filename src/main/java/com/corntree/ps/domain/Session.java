package com.corntree.ps.domain;

import org.jboss.netty.channel.Channel;

public class Session {

	private int gsServerId;
	private Channel channel;
	
	public Session(Channel channel) {
		this.channel = channel;
	}
	
	public int getServerId() {
		return gsServerId;
	}
	
	public void setServerId(int serverId) {
		this.gsServerId = serverId;
	}
	
	public Channel getChannel() {
		return channel;
	}
	
	public void setChannel(Channel channel) {
		this.channel = channel;
	}
}
