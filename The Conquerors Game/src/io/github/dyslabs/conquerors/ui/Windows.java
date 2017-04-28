package io.github.dyslabs.conquerors.ui;

import java.util.Random;

import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector4f;

import io.github.dyslabs.conquerors.JavaFunction;
import io.github.dyslabs.conquerors.Main;
import tonegod.gui.controls.buttons.Button;
import tonegod.gui.controls.buttons.ButtonAdapter;
import tonegod.gui.controls.scrolling.ScrollArea;
import tonegod.gui.controls.text.Label;
import tonegod.gui.controls.text.LabelElement;
import tonegod.gui.controls.windows.Window;
import tonegod.gui.core.ElementManager;
import tonegod.gui.core.Screen;

public class Windows {
	private static Random random=new Random(System.nanoTime());
	private static Screen screen;
	public static WindowWrapper newWindow(String id,String title,Vector2f pos) {
		Window win=new Window(screen,id(id),pos);
		win.setWindowTitle(title);
		return new WindowWrapper(win);
	}
	
	public static void init(Screen screen) {
		Windows.screen=screen;
	}
	
	public static WindowWrapper newChatWindow(String classi,Vector2f pos) {
		return new ChatWindow(Windows.newWindow("chatwindow", "Chat: "+classi, pos).window(),classi);
	}
	
	public static WindowWrapper newConfirmDialog(String msg,Vector2f pos,JavaFunction yes,JavaFunction no) {
		WindowWrapper w=Windows.newWindow(id("confirm"), "Please confirm", pos);
		ButtonAdapter yesbtn=new ButtonAdapter(screen){
			@Override
			public void onButtonMouseLeftUp(MouseButtonEvent evt, boolean toggled) {
				yes.run();
			}
		};
		ButtonAdapter nobtn=new ButtonAdapter(screen){
			@Override
			public void onButtonMouseLeftUp(MouseButtonEvent evt, boolean toggled) {
				no.run();
			}
		};
		Label text=new Label(screen, id("label"),new Vector2f(20,60),new Vector2f(w.window().getDimensions().add(new Vector2f(-20,-20))),new Vector4f(0,0,0,0),null);
		text.setText(msg);
		yesbtn.setText("Yes");
		nobtn.setText("No");
		yesbtn.setPosition(new Vector2f(20,30));
		nobtn.setPosition(new Vector2f(w.window().getDimensions().x-(20+nobtn.getDimensions().x),30));
		
		w.add(yesbtn);
		w.add(nobtn);
		w.add(text);
		return w;
	}
	
	public static String id(String id) {
		//Main.out.info(id);
		return id+"["+random.nextLong()+"]";
	}
}
