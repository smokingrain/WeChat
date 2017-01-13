package com.xk.ui.main;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Shell;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.BaseTSD.LONG_PTR;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HDC;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.RECT;
import com.sun.jna.platform.win32.WinUser.WNDENUMPROC;

/**
 * 用途：屏幕截图画布
 *
 * @author xiaokui
 * @date 2017年1月13日
 */
public class ScreenCanvas extends Canvas implements PaintListener{

	private Shell parent;
	private Image base;
	private STATUS status;
	private Rectangle selection;
	private List<Rectangle> windows = new ArrayList<Rectangle>();
	
	public ScreenCanvas(Shell parent, Image base) {
		super(parent, SWT.DOUBLE_BUFFERED);
		this.parent = parent;
		this.base = base;
		fatchAllWindows();
		initEvent();
		this.status = STATUS.INITED;
		reset();
	}
	
	private void reset() {
		
	}
	
	private void fatchAllWindows() {
		final User32 u32 = User32.INSTANCE;
		System.out.println("shell handle : " + parent.handle);
		WNDENUMPROC proc = new WNDENUMPROC(){

			@Override
			public boolean callback(HWND hwnd, Pointer pointer) {
				RECT rect = new RECT();
				boolean hasrect = u32.GetWindowRect(hwnd, rect);
				LONG_PTR handle = u32.GetWindowLongPtr(hwnd, User32.GWL_HINSTANCE);
				boolean visiable =u32.IsWindowVisible(hwnd);
				if(hasrect && visiable) {
					System.out.println("handle : " + handle.longValue());
					Rectangle rec = new Rectangle(rect.left, rect.top, rect.right - rect.left, rect.bottom - rect.top);
					windows.add(rec);
					System.out.println(rec);
				}
				
				return true;
			}
			
		};
		u32.EnumWindows(proc, Pointer.createConstant(0));
	}
	
	private void initEvent(){
		
	}

	
	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
	
	private enum STATUS{
		INITED, //刚初始化
		DRAWING, //正在选取区域
		DRAWED, //区域选取完
		MOVING, //移动选区
	}

	@Override
	public void paintControl(PaintEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}
