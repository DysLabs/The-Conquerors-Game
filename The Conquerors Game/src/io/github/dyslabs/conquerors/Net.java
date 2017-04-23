package io.github.dyslabs.conquerors;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import p.Packet;

public class Net {
	private static SocketChannel client;
	private static ByteArrayOutputStream bo=new ByteArrayOutputStream();
	private static PacketInputStream in;
	private static PacketOutputStream out;
	private static Selector selector;
	private static boolean hasData=false;
	public static ArrayList<Packet> jobs=new ArrayList<>();
	public static InetSocketAddress addr;
	public static boolean canRead(){
		return hasData;
	}
	public static boolean connect(String addr,int port) {
		Net.addr=new InetSocketAddress(addr,port);
		
		
		Main.out.info("Connected to "+addr);
		return true;
	}
	
	public static void update() throws IOException {
		selector.select();
		Iterator<SelectionKey> keys=selector.selectedKeys().iterator();
		while (keys.hasNext()) {
			SelectionKey key=keys.next();
			keys.remove();
			
			if (!key.isValid()) continue;
			if (key.isReadable()) {
				hasData=true;
				read(key);
			}
			if (key.isWritable()) {
				send(bo.toByteArray());
			}
		}
	}
	
	private static void read(SelectionKey key) throws IOException {
		SocketChannel channel=(SocketChannel) key.channel();
		ByteBuffer buffer=ByteBuffer.allocate(64*100);
		int numRead=-1;
		try {
			numRead=channel.read(buffer);
		} catch (IOException e) {
			key.cancel();
			channel.close();
			return;
		}
		if (numRead==-1) {
			key.channel().close();
			key.cancel();
			return;
		}
		byte[] data=new byte[numRead];
		System.arraycopy(buffer.array(), 0, data, 0, numRead);
		Main.out.info("Approximate data: "+new String(data));
		
	}
	
	private static void send(byte[]data) throws IOException {
		ByteBuffer buffer=ByteBuffer.wrap(data);
		client.write(buffer);
		bo.reset();//if the byte array is not reset, all previous packets will be resent
	}
	
	public static void close() throws IOException {
		client.close();
		out.close();
		in.close();
	}
	
	public static void writePacket(Packet p) throws IllegalArgumentException, IllegalAccessException, NoSuchMethodException, SecurityException, InvocationTargetException, IOException {
		out.writePacket(p);//writes the packet data to a byte array
	}
	
	private static Packet readPacket() throws NoSuchFieldException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException, IOException {
		return in.readPacket();
	}
	
	/**
	 * THIS WILL RETURN NULL IF THERE IS NO DATA TO BE READ -- CHECK !!!
	 * @return
	 * @throws IOException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws ClassNotFoundException
	 * @throws NoSuchFieldException
	 */
	public static Packet safeReadPacket() throws IOException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException, NoSuchFieldException {
		return null;
	}
}
