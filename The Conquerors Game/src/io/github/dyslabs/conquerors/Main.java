package io.github.dyslabs.conquerors;

import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.control.Control;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.builder.ControlBuilder;
import de.lessvoid.nifty.builder.LayerBuilder;
import de.lessvoid.nifty.builder.PanelBuilder;
import de.lessvoid.nifty.builder.ScreenBuilder;
import de.lessvoid.nifty.controls.Chat;
import de.lessvoid.nifty.controls.NiftyControl;
import de.lessvoid.nifty.controls.button.builder.ButtonBuilder;
import de.lessvoid.nifty.controls.chatcontrol.ChatControl;
import de.lessvoid.nifty.controls.chatcontrol.builder.ChatBuilder;
import de.lessvoid.nifty.controls.label.builder.LabelBuilder;
import de.lessvoid.nifty.controls.scrollpanel.builder.ScrollPanelBuilder;
import de.lessvoid.nifty.controls.textfield.builder.TextFieldBuilder;
import de.lessvoid.nifty.controls.window.builder.WindowBuilder;
import de.lessvoid.nifty.screen.DefaultScreenController;
import io.github.dyslabs.conquerors.ui.ChatWindow;
import io.github.dyslabs.conquerors.ui.ChatWindow.ChatMessage;
import io.github.dyslabs.conquerors.ui.Windows;
import p.Packet;
import tonegod.gui.controls.windows.Window;
import tonegod.gui.core.Screen;

public class Main extends SimpleApplication {
	public static Logger out=Logger.getLogger(Main.class.getName());
	public static Main APP;

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
        btn.addActionListener(new java.awt.event.ActionListener(){
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
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        PrintStream real_out=System.out;
        System.setOut(new PrintStream(new ByteArrayOutputStream()));
        while (true) {
        	System.out.println(canRun[0]+"=canRun");
        	if (canRun[0]) break;
        }
        System.setOut(real_out);
        app.username=JOptionPane.showInputDialog("Enter a username: ");
        Main.user=app.username;
        JOptionPane.showMessageDialog(null, "Sometimes, the OpenAL library cannot be found. If that is the case, simply restart this program and it *should* correct itself."
        		+ "\nIf the issue persists, try updating your sound card drivers.\n"
        		+ "If that fails, install OpenAL manually.");
        JOptionPane.showMessageDialog(null, "WASD: Movement\nLeft-mouse hold and drag: Look\n"
        		+ "E: Switch between global and ally chat\n"
        		+ "Jump: Space"
        		+ "", "Controls", JOptionPane.INFORMATION_MESSAGE);
        Main.start(app);
    }
    
    private String username;
    public static String user;
    private ByteArrayOutputStream bo=new ByteArrayOutputStream();
    private PacketOutputStream pout;
    
    public static void start(Main app) throws IllegalArgumentException, IllegalAccessException, NoSuchMethodException, SecurityException, InvocationTargetException, InstantiationException, NoSuchFieldException, IOException {
    	app.pout=new PacketOutputStream(app.bo);
    	app.writePacket(Packet.c(0, app.username,0));
    	app.start();
    }
    
    private Selector selector;
    private SocketChannel channel;
    private Screen defaultScreen;
    private Window exitScreen;
    private ChatWindow global,ally;
    private boolean ally_mode=false;
    
    public static boolean chkModel(String model) {
    	return new File(model).exists();
    }

    @Override
    public void simpleInitApp() {
    	AppSettings as=this.getContext().getSettings();
    	as.setAudioRenderer(AppSettings.LWJGL_OPENAL);
    	Main.APP=this;
    	setDisplayFps(false);
    	setDisplayStatView(false);
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
    	
    	guiNode.detachAllChildren();
    	flyCam.setDragToRotate(true);
    	
        defaultScreen=new Screen(this);
        guiNode.addControl(defaultScreen);
        Windows.init(defaultScreen);
        global=(ChatWindow) Windows.newChatWindow("Global",new Vector2f(35,35));
        ally=(ChatWindow)Windows.newChatWindow("Ally", new Vector2f(35,35));
        defaultScreen.addElement(global.window());
        
        //exitScreen=new Screen(this);
        //Windows.init(exitScreen);
        exitScreen=Windows.newConfirmDialog("Are you sure you want to exit the game?", new Vector2f(35,35),new JavaFunction(){
			@Override
			public <T> T run(Object... params) {//yes
				try {
					writePacket(Packet.c(16,new Object[]{}));
				} catch (IllegalArgumentException | IllegalAccessException | NoSuchMethodException | SecurityException
						| InvocationTargetException | InstantiationException | NoSuchFieldException e) {
					System.exit(1);
				}
				System.exit(0);
				return null;
			}},new JavaFunction(){//no
				@Override
				public <T> T run(Object... params) {
					Main.APP.defaultScreen.getElements().stream().forEach(e -> {
						e.show();
					});
					exitScreen.hide();
					return null;
				}}).window();
        
        defaultScreen.addElement(exitScreen);
        exitScreen.hide();
        
        Box b = new Box(1, 1, 1);
        Geometry geom = new Geometry("Box", b);

        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Blue);
        geom.setMaterial(mat);

        rootNode.attachChild(geom);
    	
    	setupKeys();
    	
    }
    
    private void set(String name,int key) {
    	inputManager.addMapping(name, new KeyTrigger(key));
    }
    
    private void setupKeys() {
    	inputManager.clearMappings();
    	set("exit",KeyInput.KEY_ESCAPE);
    	set("up",KeyInput.KEY_W);
    	set("left",KeyInput.KEY_A);
    	set("down",KeyInput.KEY_S);
    	set("right",KeyInput.KEY_D);
    	set("jump",KeyInput.KEY_SPACE);
    	set("chat",KeyInput.KEY_E);
    	
    	set("test-packets",KeyInput.KEY_F12);
    	
    	inputManager.addListener(new ActionListener(){
			@Override
			public void onAction(String name, boolean keyPressed, float tpf) {
				if (!keyPressed) {
					if (ally_mode) {
						defaultScreen.removeElement(ally.window());
						defaultScreen.addElement(global.window());
					} else {
						defaultScreen.removeElement(global.window());
						defaultScreen.addElement(ally.window());
					}
					ally_mode=!ally_mode;
				}
			}}, "chat");
    	
    	inputManager.addListener(new ActionListener(){
			@Override
			public void onAction(String name, boolean press, float tpf) {
				if (!press) {
				defaultScreen.getElements().stream().forEach(e -> {
					e.hide();
				});
				exitScreen.show();
				}
			}}, "exit");
    	
    	inputManager.addListener(new ActionListener(){
			@Override
			public void onAction(String arg0, boolean arg1, float arg2) {
				if (arg1) return;
				Main.global("Testing packet delivery");
				try {
					Packet p=Packet.c(11, 0,0,0);
					writePacket(p);
				} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
						| IllegalArgumentException | InvocationTargetException | NoSuchFieldException e) {
					throw new RuntimeException(e);
				}
			}}, "test-packets");
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
		//Main.out.info("selected");
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
    
    public static void global(String msg) {
    	Main.APP.global.addChat(new ChatWindow.ChatMessage("", msg));
    }
    
    public String spatialID;
    
    public void handlePacket(Packet p) throws IllegalArgumentException, IllegalAccessException, NoSuchMethodException, SecurityException, InvocationTargetException, InstantiationException, NoSuchFieldException {
    	int pid=p.getPacketID();
    	if (pid==1) {//login success
    		spatialID=p.getField("spatialID");
    		global("You have successfully connected");
    		global("Username: "+username);
    		global("Spatial ID: "+spatialID);
    	} else if (pid==2) {
    		try {
    		this.stop();
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
    		JOptionPane.showMessageDialog(null, "<html>Your login attempt was unseccessful<br>"+p.getField("reason"));
    	} else if (pid==6) {//Check Model
    		String model=p.getField("model");
    		if (!chkModel(model)) {
    			out.warning(model+": model does not exist");
    			writePacket(Packet.c(4, model));
    		}
    	}
    	else if (pid==18) {//Chat
    		boolean ally=p.getField("ally");
    		String msg=p.getField("message");
    		String sender=p.getField("sender");
    		if (msg.indexOf(": ")!=-1) {
    			sender=msg.substring(0, msg.indexOf(": "));
    			msg=msg.substring(msg.indexOf(": ")+2,msg.lastIndexOf(" ("));
    		}
    		ChatWindow.ChatMessage chat=new ChatWindow.ChatMessage(sender, msg);
    		if (ally) {
    			this.ally.addChat(chat);
    		} else {
    			global.addChat(chat);
    		}
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
    
    public void chatMsg(ChatMessage msg,boolean ally) throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException {
    	Packet p=Packet.c(17, ally, msg.toString());
    	writePacket(p);
    }
    
    public void handleError(String msg,Throwable t) {
		final StringBuilder sb = new StringBuilder(msg + "\n");
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
		String e=t.toString() + "\r\n" + sb.toString() + "\r\n\r\n";
		JOptionPane.showMessageDialog(null, e+"The game will now exit", "An unhandled error was encountered", JOptionPane.ERROR_MESSAGE);
    }
}
