package com.corntree.ps.socket;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelDownstreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.corntree.ps.domain.Payment;
import com.corntree.ps.socket.netPackage.AbstractPackage;
import com.corntree.ps.socket.netPackage.BasePackage;
import com.corntree.ps.socket.netPackage.LoginRequest;
import com.corntree.ps.socket.netPackage.TimeLapse;



public class PSServerEncoder extends SimpleChannelDownstreamHandler {
	public static final Logger logger = LoggerFactory.getLogger(PSServerEncoder.class);
	
    private short PACKHEADER_LENGTH = 4;
    @Override
    public void writeRequested(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
    	AbstractPackage data = (AbstractPackage)e.getMessage();
    	short messageID = 0;
    	short dataLength = 0;
    	if (data instanceof LoginRequest)
    	{
    		messageID = (short)BasePackage.MessageID.MSG_GS_LOGIN.ordinal();
    		dataLength = 1;
    	}
    	if (data instanceof TimeLapse)
    	{
    		messageID = (short)BasePackage.MessageID.MSG_GS_TIMELAPSE.ordinal();
    		dataLength = 4; //UINT32
    	}
    	if (data instanceof Payment)
    	{
    		messageID = (short)BasePackage.MessageID.MSG_GS_PAYMENT.ordinal();
    		dataLength = 24; //UINT32 * 2 + UINT64 + 2 + 2 + 4
    		int length1 = ((Payment) data).getTransactionCode().length();
    		dataLength += length1;
    	}
        /**
         * 先组织报文头         
         * short 包长   不含自身
         * short 消息Id
         * short 数据部分长度
         */
        ByteBuffer headBuffer = ByteBuffer.allocate(PACKHEADER_LENGTH + dataLength + 2);
        headBuffer.order(ByteOrder.LITTLE_ENDIAN);
        headBuffer.putShort((short)(PACKHEADER_LENGTH + dataLength));  
        headBuffer.putShort(messageID);
        headBuffer.putShort(dataLength);
    	//数据组装
        if (data instanceof LoginRequest)
    	{
    		headBuffer.put(((LoginRequest)data).getResponse());
    	}
        if (data instanceof TimeLapse)
    	{
    		headBuffer.putInt(((TimeLapse)data).getServerID());
    	}
        if (data instanceof Payment)
    	{
    		//logger.info("discount:" + ((Payment)data).getDiscount());
    		headBuffer.putInt(((Payment)data).getServerId()); 
    		headBuffer.putLong(((Payment)data).getPlayerId());
    		headBuffer.putInt((int)(((Payment)data).getFee()));
    		short trcaLength = (short)((Payment) data).getTransactionCode().length();
    		headBuffer.putShort(trcaLength);
            ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
            buffer.writeBytes(((Payment)data).getTransactionCode().getBytes());
            for (short i = 0; i < trcaLength; i++)
            {
            	headBuffer.put(buffer.readByte());
            }
            headBuffer.putShort(((Payment)data).getRechargeChannel());
            headBuffer.putFloat(((Payment)data).getDiscount());
    	}
            
        /**
         * 非常重要
         * ByteBuffer需要手动flip()，ChannelBuffer不需要
         */
        headBuffer.flip();
        ChannelBuffer totalBuffer = ChannelBuffers.dynamicBuffer();
        totalBuffer.writeBytes(headBuffer);
        Channels.write(ctx, e.getFuture(), totalBuffer);
    } 
}
          