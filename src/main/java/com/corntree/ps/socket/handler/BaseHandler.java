package com.corntree.ps.socket.handler;


import org.jboss.netty.channel.Channel;

import com.corntree.ps.socket.netPackage.AbstractPackage;

public abstract class BaseHandler {

    public abstract void handle(AbstractPackage packetData, Channel channel) throws RuntimeException;
    // 统一处理服务器异常,并向上层抛出api层异常.
    public final void handleRequestData(AbstractPackage packetData, Channel channel) throws RuntimeException {
    	try {
	        handle(packetData, channel);
	    } catch (RuntimeException e) {
	        // 这里不记录日志.由api层记录
	        throw new RuntimeException(e.getMessage());
	    }
    }
}
