package io.github.dyslabs.conquerors;

public class RspHandler {
	private byte[] rsp = null;
	
	public synchronized boolean handleResponse(byte[] rsp) {
		this.rsp = rsp;
		this.notify();
		return true;
	}
	
	public synchronized void waitForResponse() {
		while(this.rsp == null) {
			try {
				this.wait();
			} catch (InterruptedException e) {
			}
		}
		
		Main.out.info("Approximate data: "+new String(this.rsp));
	}
}
