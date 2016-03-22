package com.corntree.ps.socket.handler;

import java.util.concurrent.TimeUnit;

import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.corntree.ps.domain.Session;
import com.corntree.ps.service.GsService;
import com.corntree.ps.socket.netPackage.AbstractPackage;
import com.corntree.ps.socket.netPackage.LoginRequest;


@Component("REGIST_SERVER_REQUEST")
public class RegistServerHandler extends BaseHandler {
	
    private static final Logger logger = LoggerFactory.getLogger(RegistServerHandler.class.getName());

    @Autowired
    GsService gsService;
    
	@Override
	public void handle(AbstractPackage packetData, Channel channel)
			throws RuntimeException {
		
		LoginRequest server = (LoginRequest)packetData;
		
		Session s = getSession(channel);
		s.setServerId(server.getServerID());
		if (gsService.addSession(s)) {

			server.setResponse((byte) 0);
			channel.write(server);
			logger.info("Server id(" + server.getServerID() + ") regist success.");
		} else {
			server.setResponse((byte)7);
			channel.write(server);
			logger.warn("Server id(" + server.getServerID() + ") regist fail, already exist !!!");
			channel.close();
		}
	}

    protected Session getSession(Channel channel) {
        return (Session) channel.getAttachment();
    }
}