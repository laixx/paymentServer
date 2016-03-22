package com.corntree.ps.socket;

import static org.jboss.netty.channel.Channels.pipeline;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.execution.ExecutionHandler;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;
import org.jboss.netty.handler.logging.LoggingHandler;
import org.jboss.netty.handler.timeout.ReadTimeoutHandler;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SocketGsPipelineFactory  implements ChannelPipelineFactory {

	private static Timer timer = new HashedWheelTimer();
	private static OrderedMemoryAwareThreadPoolExecutor e = new OrderedMemoryAwareThreadPoolExecutor(16, 0, 0);
	
	@Autowired
	private SocketGsRequestHandler requestHandler;
	
	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline p = pipeline();
		p.addLast("executor", new ExecutionHandler(e));
        p.addLast("log", new LoggingHandler());
		p.addLast("timeoutHandler", new ReadTimeoutHandler(timer, 1000));
		p.addLast("encoder", new PSServerEncoder());
		p.addLast("decoder", new PSServerDecoder());
		p.addLast("handler", requestHandler);
		return p;
	}
}
