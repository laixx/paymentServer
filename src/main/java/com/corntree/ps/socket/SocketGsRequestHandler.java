package com.corntree.ps.socket;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

import org.apache.log4j.NDC;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ChildChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.timeout.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.corntree.ps.domain.Session;
import com.corntree.ps.service.GsService;
import com.corntree.ps.socket.handler.BaseHandler;
import com.corntree.ps.socket.netPackage.BasePackage;


@Component
public class SocketGsRequestHandler extends SimpleChannelUpstreamHandler {

	public static final Logger logger = LoggerFactory.getLogger(SocketGsRequestHandler.class);
	private static final Logger connectlogger = LoggerFactory.getLogger(SocketGsRequestHandler.class);
	
    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private GsService gsService;

	@Autowired
	private TcpServer server;

	public SocketGsRequestHandler() {
	}

	@Override
	public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e)
			throws Exception {
		Channel channel = e.getChannel();
		NDC.push(new StringBuilder("channelId:").append(channel.getId()).toString());
		
		super.handleUpstream(ctx, e);
		
		if (e instanceof ChildChannelStateEvent) {
            ChildChannelStateEvent evt = (ChildChannelStateEvent) e;
            if (evt.getChildChannel().isOpen()) {
				connectlogger.info("[Open_Connect] IP:" + channel.getRemoteAddress());
            } else {
				connectlogger.info("[Close_Connect] IP:" + channel.getRemoteAddress());
            }
        } else if (e instanceof ChannelStateEvent) {
            ChannelStateEvent evt = (ChannelStateEvent) e;
            switch (evt.getState()) {
            case OPEN:
                if (Boolean.TRUE.equals(evt.getValue())) {
    				connectlogger.info("[Open_Connect] IP:" + channel.getRemoteAddress());
                } else {
    				connectlogger.info("[Close_Connect] IP:" + channel.getRemoteAddress());
                }
                break;
			default:
				break;
			}
		}
		NDC.remove();
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		if (server.isShuttingDown()) {
			// just swallow messages while server is shutting down.
			return;
		}
		BasePackage buffer = (BasePackage) e.getMessage();
		Channel channel = e.getChannel();
	    Session session = getOrCreateSession(channel);
	    
	    BaseHandler handler = getPackageHandle(buffer.getMessageID());
		if ( handler != null ) {
			handler.handleRequestData(buffer.getData(), channel);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {
		if (e.getCause() instanceof TimeoutException) {
			logger.warn("operation timeout...", e.getCause());
		} else {
			logger.warn("Unexpected exception from downstream...", e.getCause());
		}
		e.getChannel().close();
	}

	@Override
	public void channelDisconnected(ChannelHandlerContext ctx,
			ChannelStateEvent e) throws Exception {
		e.getChannel().close();
	}

	@Override
	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {
		removeSession(e.getChannel());
		super.channelClosed(ctx, e);
	}

	@Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) 
    		throws Exception {
        super.channelConnected(ctx, e);        
    }
	
	private void removeSession(Channel channel) {
		Object attachment = channel.getAttachment();
		if (attachment != null) {
			Session session = (Session) attachment;
			gsService.removeSession(session.getServerId());
			logger.warn("Disconnect with server id:" + session.getServerId());
		}
	}
	
    private Session getOrCreateSession(Channel channel) {
        Object attachment = channel.getAttachment();
        if (attachment == null) {
            Session session = new Session(channel);
            channel.setAttachment(session);
            return session;
        } else {
            return (Session) attachment;
        }
    }
    
    private BaseHandler getPackageHandle(short messageID) {
    	if (messageID == BasePackage.MessageID.MSG_GS_LOGIN.ordinal())
    	{
    		BaseHandler handler = applicationContext.getBean("REGIST_SERVER_REQUEST", BaseHandler.class);
    		return handler;
    	}
    	if (messageID ==  BasePackage.MessageID.MSG_GS_TIMELAPSE.ordinal())
    	{
    		BaseHandler handler = applicationContext.getBean("SERVER_DUMMY_REQUEST", BaseHandler.class);
    		return handler;
    	}
    	if (messageID ==  BasePackage.MessageID.MSG_GS_PAYMENT.ordinal())
    	{
    		BaseHandler handler = applicationContext.getBean("PAYMENT_CONFIRM", BaseHandler.class);
    		return handler;
    	}
    	if (messageID ==  BasePackage.MessageID.MSG_GS_PUSH.ordinal())
    	{
    		BaseHandler handler = applicationContext.getBean("PUSH_INFO", BaseHandler.class);
    		return handler;
    	}
    	return null;
    }
}
