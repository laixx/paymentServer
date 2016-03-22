package com.corntree.ps.socket;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.buffer.LittleEndianHeapChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.corntree.ps.socket.netPackage.AbstractPackage;
import com.corntree.ps.socket.netPackage.BasePackage;
import com.corntree.ps.socket.netPackage.LoginRequest;
import com.corntree.ps.socket.netPackage.PaymentConfirm;
import com.corntree.ps.socket.netPackage.TimeLapse;


public class PSServerDecoder extends FrameDecoder {
	public static final Logger logger = LoggerFactory.getLogger(PSServerDecoder.class);
    
    @Override
    protected Object decode(ChannelHandlerContext context, Channel channel, ChannelBuffer buffer) throws Exception {
        buffer.markReaderIndex();
        LittleEndianHeapChannelBuffer leBuffer = new LittleEndianHeapChannelBuffer(buffer.array());
        short length = (leBuffer.readShort());
    	buffer.readerIndex(1);
        if (leBuffer.readableBytes() < length) {
        	logger.warn("error decode message.");
            return null;
        }
        int keep = leBuffer.readInt();
        short messageID = leBuffer.readShort();
        short dataLength = leBuffer.readShort();
        BasePackage request = new BasePackage();
        request.setLength(length);
        request.setKeep(keep);
        request.setMessageID(messageID);
        request.setDataLength(dataLength);
        if (leBuffer.readableBytes() < dataLength) {
        	return null;
        }
        LittleEndianHeapChannelBuffer dataBuffer = new LittleEndianHeapChannelBuffer(dataLength);
        leBuffer.readBytes(dataBuffer, dataLength);
        request.setData(decodedate(messageID, dataBuffer));
        buffer.readerIndex(length + 2);
        return request;
    }
    
    private AbstractPackage decodedate(short messageID, LittleEndianHeapChannelBuffer buffer)
    {
    	if (messageID == BasePackage.MessageID.MSG_GS_LOGIN.ordinal())
    	{
    		LoginRequest data = new LoginRequest();
    		data.setServerID(buffer.readShort());
    		data.setServerPort(buffer.readShort());
    		data.setMaxPlayer(buffer.readShort());
    		data.setServerType(buffer.readInt());
    		return data;
    	}
    	if (messageID == BasePackage.MessageID.MSG_GS_TIMELAPSE.ordinal())
    	{
    		TimeLapse data = new TimeLapse();
    		data.setServerID(buffer.readInt());
    		return data;
    	}
    	if (messageID == BasePackage.MessageID.MSG_GS_PAYMENT.ordinal())
    	{
    		PaymentConfirm data = new PaymentConfirm();
    		data.setRetCode(buffer.readInt());
    		short tranLength = buffer.readShort();
    		StringBuilder transactionCode = new StringBuilder();
    		for (short i = 0; i < tranLength; i++)
    		{
    			transactionCode.append((char)buffer.readByte());
    		}
    		data.setTransactionCode(transactionCode.toString());
    		return data;
    	}
    	return null;
    }
}