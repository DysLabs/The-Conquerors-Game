package io.github.dyslabs.conquerors.ui;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import com.jme3.input.event.MouseButtonEvent;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector4f;

import io.github.dyslabs.conquerors.Main;
import tonegod.gui.controls.buttons.ButtonAdapter;
import tonegod.gui.controls.scrolling.ScrollArea;
import tonegod.gui.controls.scrolling.ScrollAreaAdapter;
import tonegod.gui.controls.scrolling.ScrollPanel;
import tonegod.gui.controls.text.Label;
import tonegod.gui.controls.text.TextField;
import tonegod.gui.controls.windows.Window;

//FIXME resizing windows screws it up -- move away from absolute numbers
public class ChatWindow extends WindowWrapper {
	public final ScrollAreaAdapter chat;
	private StringBuilder log=new StringBuilder();
	public ChatWindow(Window window,String type) {
		super(window);
		chat=new ScrollAreaAdapter(window.getScreen(), Windows.id("log"), new Vector2f(10,30));//screen,uid,relative pos,text only?
		chat.setDimensions(window.getDimensions().add(new Vector2f(-45,-80)));
		
		Vector2f textbox=new Vector2f(chat.getPosition().getX(), window.getDimensions().y-40);
		
		TextField textfield=new TextField(window.getScreen(),Windows.id("chat-text"),new Vector2f(textbox));
		Vector2f textbox_dim=new Vector2f(window.getDimensions().x*0.75f,textfield.getDimensions().getY());
		textfield.setDimensions(textbox_dim);
		
		Vector2f btn=new Vector2f(textbox.add(new Vector2f(textbox_dim.x,0)));
		
		ButtonAdapter send=new ButtonAdapter(window.getScreen(), Windows.id("send"), btn){
			public void onButtonMouseLeftUp(MouseButtonEvent evt,boolean toggled) {
				boolean ally=(type.equals("Ally"));
				String sender=Main.user;
				String mes=textfield.getText();
				ChatMessage msg=new ChatMessage(sender,mes);
				try {
				Main.APP.chatMsg(msg,ally);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
				textfield.setText("");
			}
		};
		send.setDimensions(new Vector2f(window.getDimensions().x*0.20f,textfield.getDimensions().y));
		send.setText(type);
		
		add(send);
		add(textfield);
		add(chat);
	}

	public void addChat(ChatMessage msg) {
		log.append(msg).append("\n");
		chat.setText(log.toString());
		chat.scrollToBottom();
	}
	
	public static class ChatMessage {
		private final SimpleDateFormat df=new SimpleDateFormat("h:m");
		protected String date;
		protected String sender,msg;
		public ChatMessage(String sender,String msg) {
			date=df.format(new Date());
			this.sender=sender;
			this.msg=msg;
		}
		
		public String toString() {
			if (sender.equals("")) {
				return msg+" ("+date+")";
			}
			return sender+": "+msg +" ("+date+")";
		}
	}
}
