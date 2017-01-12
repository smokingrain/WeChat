package com.xk.ui.main;

import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.internal.win32.OS;
import org.eclipse.swt.internal.win32.POINT;
import org.eclipse.swt.internal.win32.RECT;
import org.eclipse.swt.widgets.Shell;

import com.xk.uiLib.ICallable;
import com.xk.uiLib.ICallback;
import com.xk.utils.SWTTools;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;


/**
 * 用途：悬浮窗口
 *
 * @author xiaokui
 * @date 2017年1月5日
 */
public class FloatWindow implements ICallback{

	public Shell shell;
	private Object result;
	
	private Long timeOut = 2000L;//鼠标两秒不在焦点就消失
	private Timer timer;//失去焦点计时器

	private static class WindowHolder{
		private static FloatWindow instance = new FloatWindow();
	}
	
	public static FloatWindow getInstance() {
		return WindowHolder.instance;
	}
	
	private FloatWindow() {
		
	}
	
	public void init() {
		kill();
		createContents();
	}
	
	
	/**
	 * Open the window.
	 */
	public Object open(int x, int y) {
		shell.open();
		shell.layout();
		if(x >= 0 && y >= 0) {
			shell.setLocation(x, y);
		}
		SWTTools.topWindow(shell);
		if(timeOut > 0){
			timer = new Timer();
			timer.schedule(new DisposeTask(), timeOut, timeOut);
		}
		Display display = Display.getDefault();
		while (null != shell && !shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}
	
	public void setSize(int width, int height) {
		shell.setSize(width, height);
	}

	public void setTimeOut(Long timeOut) {
		this.timeOut = timeOut;
		if(timeOut < 0 && null != timer) {
			timer.cancel();
			timer = null;
		}
	}
	
	
	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shell = new Shell(SWT.FILL_WINDING);
		StackLayout stackLayout=new StackLayout();
		shell.setLayout(stackLayout);
	}
	
	/**
	 * 用途：设置窗体内部显示面板
	 * @date 2017年1月5日
	 * @param comp
	 */
	public void add(Control comp) {
		StackLayout lo = (StackLayout)(shell.getLayout());
		if(null != lo.topControl && !lo.topControl.isDisposed()) {
			lo.topControl.dispose();
		}
		lo.topControl = comp;
		if(comp instanceof ICallable) {
			ICallable ic = (ICallable) comp;
			ic.setCallBack(this);
		}
	}

	/**
	 * 关闭悬浮窗
	 * 用途：
	 * @date 2017年1月5日
	 */
	public void kill() {
		if(shell != null && !shell.isDisposed()) {
			shell.dispose();
			shell = null;
		}
		if(null != timer) {
			timer.cancel();
		}
	}
	
	@Override
	public Object callback(Object obj) {
		this.result = obj;
		kill();
		return obj;
	}

	/**
	 * 用途：计时器关闭悬浮窗
	 *
	 * @author xiaokui
	 * @date 2017年1月5日
	 */
	private class DisposeTask extends TimerTask {

		@Override
		public void run() {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					POINT pt = new POINT();
					OS.GetCursorPos(pt);//获取鼠标位置
					RECT rect = new RECT();
					OS.GetClientRect (shell.handle, rect);//获取窗口范围
					OS.MapWindowPoints (shell.handle, 0, rect, 2);
					if(!OS.PtInRect (rect, pt)){//鼠标不在窗口内，关闭窗口
						kill();
					}
				}
			});
			
		}
		
	}
	
}
