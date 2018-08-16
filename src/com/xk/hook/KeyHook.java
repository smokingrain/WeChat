package com.xk.hook;
import java.awt.event.KeyEvent;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HMODULE;
import com.sun.jna.platform.win32.WinDef.LRESULT;
import com.sun.jna.platform.win32.WinDef.WPARAM;
import com.sun.jna.platform.win32.WinDef.LPARAM;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.platform.win32.WinUser.HHOOK;
import com.sun.jna.platform.win32.WinUser.KBDLLHOOKSTRUCT;
import com.sun.jna.platform.win32.WinUser.LowLevelKeyboardProc;
import com.sun.jna.platform.win32.WinUser.MSG;

/**
 * 用途：全局键盘钩子，做快捷键用
 *
 * @author xiaokui
 * @date 2017年1月3日
 */
public class KeyHook {
	
    private HHOOK hhk;
    private User32 lib = User32.INSTANCE;
    private boolean installed = false;
    
    private static class HookHolder {
    	private static KeyHook INSTANCE = new KeyHook();
    }
    
    public static KeyHook getInstance() {
    	return HookHolder.INSTANCE;
    }
    
    
    public void install() {
    	if(installed) {
    		return;
    	}
    	installed = true;
    	Runnable r=new Runnable(){

			@Override
			public void run() {
				HMODULE hMod = Kernel32.INSTANCE.GetModuleHandle(null);
		    	LowLevelKeyboardProc keyboardHook = new LowLevelKeyboardProc() {
		            @Override
		            public LRESULT callback(int nCode, WPARAM wParam, KBDLLHOOKSTRUCT info) {
		                if (nCode >= 0) {
		                    switch(wParam.intValue()) {
		                    case WinUser.WM_KEYUP:
		                    case WinUser.WM_KEYDOWN:
		                    case WinUser.WM_SYSKEYUP:
		                    case WinUser.WM_SYSKEYDOWN:
		                    	
		                    	
		                        System.err.println("in callback, key = " + info.vkCode + ", scanCode = " + info.scanCode);
		                        
		                    }
		                }

		                Pointer ptr = info.getPointer();
		                long peer = Pointer.nativeValue(ptr);
		                return lib.CallNextHookEx(hhk, nCode, wParam, new LPARAM(peer));
		            }
		        };
		        hhk = lib.SetWindowsHookEx(WinUser.WH_KEYBOARD_LL, keyboardHook, hMod, 0);
		        int result;
		        MSG msg = new MSG();
		        while (installed && (result = lib.GetMessage(msg, null, 0, 0)) != 0) {
		            if (result == -1) {
		                System.err.println("error in get message");
		                break;
		            }
		            else {
		                System.err.println("got message" + msg);
		                lib.TranslateMessage(msg);
		                lib.DispatchMessage(msg);
		            }
		        }
		        uninstall();
			}
    	};
    	new Thread(r).start();
    	
    }
    
    
    public void uninstall() {
    	if(installed && null != hhk) {
    		System.out.println("uninstall");
    		lib.UnhookWindowsHookEx(hhk);
    		installed = false;
    	}
    }
    
    public void registerHotKey() {
    	boolean registed = lib.RegisterHotKey(null, 0xAAAA, User32.MOD_ALT, KeyEvent.VK_J);
    	System.out.println("registed:" + registed);
    	WinUser.MSG msg = new WinUser.MSG();
    	while(true) {
    		while(lib.PeekMessage(msg, null, 0, 0, 1)) {
    			System.out.println("before　: " + msg);
    			if(msg.message == User32.WM_HOTKEY) {
    				System.out.println(msg);
    			}
    		}
    		
    	}
    	
    }
    
    public static void main(String[] args) {
		KeyHook hook = KeyHook.getInstance();
//		hook.install();
		hook.registerHotKey();
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		hook.uninstall();
    	
    	
	}

}