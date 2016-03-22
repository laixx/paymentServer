package com.corntree.ps.socket;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.logging.InternalLoggerFactory;
import org.jboss.netty.logging.Log4JLoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.corntree.ps.common.ExecutorManager;

@Component
public class TcpServer {
	public static final Logger logger = LoggerFactory.getLogger(TcpServer.class);

    private ExecutorService bossThreadPool = ExecutorManager.newCachedThreadPool("Socket-Server-Boss");
    private ExecutorService workerThreadPool = ExecutorManager.newCachedThreadPool("Socket-Server-Worker");

    @Value("${ps.socket.port}")
    private int listenGsPort;
    
    @Autowired
    SocketGsPipelineFactory pipelineGsFactory;
    
    private boolean isShuttingDown = false;
    
    ServerBootstrap bootstrap;
    
    ChannelGroup allChannels = new DefaultChannelGroup("time-server");
    
    @PostConstruct
    public void start() {
    	listenToGS();
    }
    
    @PreDestroy
    public void shutdown() {
    	logger.info("server is shuting down...");
    	isShuttingDown = true;
    	
    	ChannelGroupFuture future = allChannels.close();
        future.awaitUninterruptibly();
    	bootstrap.releaseExternalResources();
    	
    	//System.exit(0);
    }
    
    private void listenToGS() {
        ChannelFactory channelFactory = new NioServerSocketChannelFactory(bossThreadPool, workerThreadPool);
        bootstrap = new ServerBootstrap(channelFactory);
        bootstrap.setPipelineFactory(pipelineGsFactory);
        Channel channel = bootstrap.bind(new InetSocketAddress(listenGsPort));
        allChannels.add(channel);
        logger.warn("server started, listening to game server :" + listenGsPort);
    }

    public boolean isShuttingDown() {
        return isShuttingDown;
    }
}
