package io.github.dyslabs.conquerors.ui;

import com.jme3.math.Vector2f;

import tonegod.gui.controls.windows.Window;
import tonegod.gui.core.Element;

/**
 * Allows chaining multiple calls
 * @author sn
 *
 */
public class WindowWrapper {
	private Window window;
	protected WindowWrapper(Window window) {
		this.window=window;
	}
	
	public WindowWrapper setTitle(String title) {
		window.setWindowTitle(title);
		return this;
	}
	
	public WindowWrapper resize(Vector2f size) {
		window.setDimensions(size);
		return this;
	}
	
	public WindowWrapper add(Element e) {
		window.addChild(e);
		return this;
	}
	
	public WindowWrapper move(Vector2f newpos) {
		window.setPosition(newpos);
		return this;
	}
	
	public Window window() {
		return window;
	}
}
