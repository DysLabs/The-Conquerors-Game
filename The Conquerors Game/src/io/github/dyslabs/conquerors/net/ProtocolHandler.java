package io.github.dyslabs.conquerors.net;

import io.github.dyslabs.conquerors.Main;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ProtocolHandler extends ChannelInboundHandlerAdapter {
	@Override
	public void channelRead(ChannelHandlerContext ctx,Object msg) {
		ByteBuf data=(ByteBuf)msg;
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx,Throwable t) {
		Main.APP.handleError("An error occured while handling incoming data", t);
		ctx.close();
	}
}
