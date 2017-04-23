package io.github.dyslabs.conquerors;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;

import p.Packet;

public class Main extends SimpleApplication {
	public static Logger out=Logger.getLogger(Main.class.getName());

    public static void main(String[] args) throws SecurityException, IOException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException, NoSuchFieldException {
    	final Date d = new Date();
		final SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHms");
		final SimpleDateFormat df2 = new SimpleDateFormat("dd MMMMM yyyy h:m:s a");
		final Formatter txtFormat = new Formatter() {
			@Override
			public String format(final LogRecord record) {
				final String timestamp = df2.format(new Date());
				if (record.getThrown() == null) {
					return timestamp + "\r\n" + record.getSourceClassName() + " " + record.getSourceMethodName()
							+ "\r\n" + record.getLevel() + ": " + record.getMessage() + "\r\n\r\n";
				} else {
					final Throwable t = record.getThrown();
					final StringBuilder sb = new StringBuilder(record.getMessage() + "\n");
					final StackTraceElement[] stackTrace = t.getStackTrace();
					for (final StackTraceElement element : stackTrace) {
						sb.append("\t" + element).append("\r\n");
					}
					if (t.getCause() != null) {
						sb.append("Caused by: " + t.getCause().toString()).append("\r\n");
						final StackTraceElement[] stackTrace1 = t.getCause().getStackTrace();
						for (final StackTraceElement element : stackTrace1) {
							sb.append("\t" + element).append("\r\n");
						}
					}
					return timestamp + "\r\n" + record.getSourceClassName() + " " + record.getSourceMethodName()
							+ " \r\n" + t.toString() + "\r\n" + sb.toString() + "\r\n\r\n";
				}
			}
		};
		final FileHandler fileTxt = new FileHandler(df.format(d) + ".txt");
		fileTxt.setFormatter(txtFormat);
		out.addHandler(fileTxt);
		
		
        Main app = new Main();
        JFrame frame=new JFrame("Server");
        JPanel pane=new JPanel();
        JLabel label=(JLabel) pane.add(new JLabel("<server address>:<server port>"));
        JTextField addr_info=(JTextField) pane.add(new JTextField("localhost:22"));
        JButton btn=(JButton) pane.add(new JButton("Connect"));
        boolean[] canRun=new boolean[]{false};
        btn.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				frame.setVisible(false);
				try {
					String[] addr=addr_info.getText().split(":");
					String host=addr[0];
					int port=Integer.parseInt(addr[1]);
					if (!Net.connect(host, port)) throw new IOException("Could not connect to the server");
					canRun[0]=true;
				} catch (Exception e) {
					e.printStackTrace();
					label.setText("An error occured. Make sure that the server is online, and the address is entered correctly.");
					frame.pack();
					frame.setVisible(true);
					return;
				}
				frame.dispose();
			}});
        frame.add(pane);
        frame.pack();
        frame.setVisible(true);
        PrintStream real_out=System.out;
        System.setOut(new PrintStream(new ByteArrayOutputStream()));
        while (true) {
        	System.out.println(canRun[0]+"=canRun");
        	if (canRun[0]) break;
        }
        System.setOut(real_out);
        Main.start(app);
    }
    
    private ByteArrayOutputStream bo=new ByteArrayOutputStream();
    private PacketOutputStream pout;
    
    public static void start(Main app) throws IllegalArgumentException, IllegalAccessException, NoSuchMethodException, SecurityException, InvocationTargetException, InstantiationException, NoSuchFieldException, IOException {
    	app.pout=new PacketOutputStream(app.bo);
    	app.writePacket(Packet.c(0, "dan",0));
    	app.start();
    }
    
    private Selector selector;
    private SocketChannel channel;

    @Override
    public void simpleInitApp() {
    	try {
			selector=Selector.open();
			channel=SocketChannel.open();
			channel.configureBlocking(false);
			channel.connect(Net.addr);
			Main.out.info("Connected to "+channel.getRemoteAddress());
			channel.register(selector, SelectionKey.OP_CONNECT);
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
        Box b = new Box(1, 1, 1);
        Geometry geom = new Geometry("Box", b);

        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Blue);
        geom.setMaterial(mat);

        rootNode.attachChild(geom);
    	
    	
    }
    
    public void writePacket(Packet p) throws IllegalArgumentException, IllegalAccessException, NoSuchMethodException, SecurityException, InvocationTargetException {
    	pout.writePacket(p);
    }
    
    public void write(SelectionKey key) throws IOException {
    	write((SocketChannel)key.channel());
    }
    
    public void write(SocketChannel ch) throws IOException {
    	ByteBuffer buffer=ByteBuffer.wrap(bo.toByteArray());
    	ch.write(buffer);
    	bo.reset();
    }
    
    private boolean connected=false;

    @Override
    public void simpleUpdate(float tpf) {
    	if (bo.size()>0 && connected)
			try {
				write(channel);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
    //Main.out.info("tpf="+tpf);
       try {
		if (selector.selectNow()==0) return;
		Main.out.info("selected");
		Iterator<SelectionKey> keys=selector.selectedKeys().iterator();
		while (keys.hasNext()) {
			SelectionKey key=keys.next();
			keys.remove();
			//Main.out.info(sb.toString().substring(0, sb.toString().lastIndexOf(", "))+"]");
			if (!key.isValid()) continue;
			if (key.isConnectable()) {
				((SocketChannel)key.channel()).finishConnect();
				key.interestOps(SelectionKey.OP_READ);
				connected=true;
			}
			if (key.isReadable()) {
				read(key);
			}
		}
	} catch (IOException e) {
		e.printStackTrace();
	} catch (InstantiationException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IllegalAccessException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IllegalArgumentException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (InvocationTargetException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (NoSuchMethodException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (SecurityException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (ClassNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (NoSuchFieldException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    }
    
    private void read(SelectionKey key) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException, NoSuchFieldException, IOException {
    	SocketChannel channel=(SocketChannel) key.channel();
    	ByteBuffer buffer=ByteBuffer.allocate(1024);
    	int numRead=-1;
    	try {
    		numRead=channel.read(buffer);
    	} catch (IOException e) {
    		Main.out.info("Server closed connection");
    		throw new RuntimeException("Server closed connection");
    	}
    	if (numRead==-1) {
    		throw new RuntimeException("Server closed connection");
    	}
    	if (numRead==0) return;
    	Main.out.info(numRead+" bytes read");
    	byte[] data=new byte[numRead];
    	System.arraycopy(buffer.array(), 0, data, 0, numRead);
    	//Main.out.info("Approximate data: "+new String(data));
    	ByteArrayInputStream bin=new ByteArrayInputStream(data);
    	PacketInputStream pin=new PacketInputStream(bin);
    	while (bin.available()>0) {
    		this.handlePacket(pin.readPacket());
    	}
    }
    
    public void handlePacket(Packet p) throws IllegalArgumentException, IllegalAccessException {
    	int pid=p.getPacketID();
    	if (pid==1) {
    		
    	}
    }

    @Override
    public void simpleRender(RenderManager rm) {
        
    }
    
    @Override
    public void destroy() {
    	out.warning("Suprise shutdown !!");
    	for (Handler h:out.getHandlers()) {
    		h.close();
    	}
    	
    	super.destroy();
    }
}
