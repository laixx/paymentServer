package com.corntree.ps.socket.handler;

import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.corntree.ps.service.GsService;
import com.corntree.ps.socket.netPackage.AbstractPackage;
import com.corntree.ps.socket.netPackage.TimeLapse;


@Component("SERVER_DUMMY_REQUEST")
public class ServerDummyHandler extends BaseHandler {
	
	private static final Logger logger = LoggerFactory.getLogger(ServerDummyHandler.class.getName());
	
	@Autowired
	GsService gsService;
	
	@Override
	public void handle(AbstractPackage packetData, Channel channel)
			throws RuntimeException {
		
		TimeLapse tl = (TimeLapse)packetData;
		
		if (gsService.getSession(tl.getServerID()) == null) {
			logger.warn("Server:"+ tl.getServerID() + ", not regiested!");
			channel.close();
			return;
		}
		
		channel.write(tl);
	}

}
