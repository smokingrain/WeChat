package com.xk.hook;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.platform.win32.WinUser.MSG;

public class HotKeys {

	private User32 lib = User32.INSTANCE;
	private boolean registed = false;
	private List<HotKeyListener> listeners = new ArrayList<HotKeyListener>();
	
	private static class HotKeysHolder {
		private static HotKeys INSTANCE = new HotKeys();
	}
	
	public static HotKeys getInstance() {
		return HotKeysHolder.INSTANCE;
	}
	
	private HotKeys() {
		
	}
	
	
	
	public boolean add(HotKeyListener e) {
		return listeners.add(e);
	}

	public boolean remove(Object o) {
		return listeners.remove(o);
	}

	/**
	 * 注册系统热键
	 * 作者 ：肖逵
	 * 时间 ：2018年8月31日 下午12:46:56
	 * @return
	 */
	public boolean registerHotKey() {
		if(registed) {
			return true;
		}
    	final ReentrantLock lock = new ReentrantLock();
    	final Condition cond = lock.newCondition();
		Runnable r = new Runnable() {
			
			@Override
			public void run() {
				System.out.println("reging!");
				registed = lib.RegisterHotKey(null, 0xAAAA, User32.MOD_CONTROL, KeyEvent.VK_J);
				lock.lock();
				try {
					cond.signal();
				} finally {
					lock.unlock();
				}
				WinUser.MSG msg = new WinUser.MSG();
		    	while(registed) {
		    		while(0 != lib.GetMessage(msg, null, 0, 0)) {
		    			if(msg.message == User32.WM_HOTKEY) {
		    				for(HotKeyListener listener : listeners) {
		    					listener.notify(msg);
		    				}
		    			}
		    		}
		    		
		    	}
				
			}
		};
		new Thread(r).start();
		lock.lock();
		try {
			cond.await(5000, TimeUnit.MILLISECONDS);
			System.out.println("notify!!");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			lock.unlock();
		}
    	System.out.println("registed : " + registed);
		return registed;
    	
    }
	
	public boolean unregister() {
		if(registed) {
			return lib.UnregisterHotKey(null, 0xAAAA);
		}
		registed = false;
		return false;
	}
	
	public static void main(String[] args) {
		HotKeys hot = HotKeys.getInstance();
		hot.registerHotKey();
		hot.add(new HotKeyListener() {
			
			@Override
			public void notify(MSG msg) {
				System.out.println(msg);
				
			}
		});
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		hot.unregister();
	}
}
