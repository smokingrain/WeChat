package com.xk.ui.main;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.BaseTSD.LONG_PTR;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HDC;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.RECT;
import com.sun.jna.platform.win32.WinUser.WNDENUMPROC;
import com.xk.uiLib.ICallback;

/**
 * 用途：屏幕截图画布
 *
 * @author xiaokui
 * @date 2017年1月13日
 */
public class ScreenCanvas extends Canvas implements PaintListener{

	private Shell parent;
	private Image base;
	private STATUS status = STATUS.NONE;
	private Rectangle selection;
	private Rectangle selecting;
	private Point downLoc;
	private List<Rectangle> windows = new ArrayList<Rectangle>();
	private Rectangle nowWindow;
	
	private ICallback callBack;
	
	public ScreenCanvas(Shell parent, Image base, ICallback callBack) {
		super(parent, SWT.DOUBLE_BUFFERED);
		this.parent = parent;
		this.base = base;
		this.callBack = callBack;
		fatchAllWindows();
		initEvent();
		reset();
	}
	
	
	/**
	 * 重置
	 */
	private void reset() {
		this.status = STATUS.INITED;
		selection = new Rectangle(0, 0, 0, 0);
		selecting = new Rectangle(0, 0, 0, 0);
		downLoc = new Point(0, 0);
	}
	
	/**
	 * 抓去所有窗口的坐标，大小
	 */
	private void fatchAllWindows() {
		final User32 u32 = User32.INSTANCE;
		System.out.println("shell handle : " + parent.handle);
		WNDENUMPROC proc = new WNDENUMPROC(){

			@Override
			public boolean callback(HWND hwnd, Pointer pointer) {
				RECT rect = new RECT();
				boolean hasrect = u32.GetWindowRect(hwnd, rect);
				int handle =  u32.GetWindowLong(hwnd, User32.GWL_HINSTANCE);
//				LONG_PTR handle = u32.GetWindowLongPtr(hwnd, User32.GWL_HINSTANCE);
				boolean visiable =u32.IsWindowVisible(hwnd);
				if(hasrect && visiable && handle > 0) {
					System.out.println("handle : " + handle);
					Rectangle rec = new Rectangle(rect.left, rect.top, rect.right - rect.left, rect.bottom - rect.top);
					windows.add(rec);
					System.out.println(rec);
				}
				
				return true;
			}
			
		};
		u32.EnumWindows(proc, Pointer.createConstant(0));
	}
	
	/**
	 * 初始化各种事件
	 */
	private void initEvent(){
		addMouseMoveListener(new MouseMoveListener() {
			
			@Override
			public void mouseMove(MouseEvent e) {
				if(STATUS.INITED.equals(status)) {
					moveOnInit(e);
				}else if(STATUS.CLICKING.equals(status)) {
					status = STATUS.DRAWING;
				}else if(STATUS.DRAWING.equals(status)) {
					int width = e.x - downLoc.x;
					int height = e.y - downLoc.y;
					selecting = new Rectangle(downLoc.x, downLoc.y, Math.abs(width), Math.abs(height));
					if(width < 0) {
						selecting.x = downLoc.x;
					}
					if(height < 0) {
						selecting.y = downLoc.y;
					}
					redraw();
				}
				
				
			}
		});
		addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseUp(MouseEvent e) {
				if(e.button != 1) {
					callBack.callback(null);
					return;
				}
				Point clickLoc = new Point(e.x, e.y);
				if(STATUS.CLICKING.equals(status)) {
					status = STATUS.DRAWED;
					selection = nowWindow;
				}else if(STATUS.DRAWING.equals(status)) {
					int width = clickLoc.x - selection.x;
					int height = clickLoc.y - selection.y;
					selection.width = Math.abs(width);
					selection.height = Math.abs(height);
					if(width < 0){
						selection.x = clickLoc.x;
					}else if(width == 0) {
						reset();
						return;
					}
					if(height < 0) {
						selection.y = clickLoc.y;
					}else if(height == 0) {
						reset();
						return;
					}
					status = STATUS.DRAWED;
				}
				redraw();
				
			}
			
			@Override
			public void mouseDown(MouseEvent e) {
				if(e.button != 1) {
					return;
				}
				if(STATUS.INITED.equals(status)) {
					Point clickLoc = new Point(e.x, e.y);
					if(nowWindow.contains(clickLoc)) {
						status = STATUS.CLICKING;
					}else {
						status = STATUS.DRAWING;
					}
					downLoc.x = clickLoc.x;
					downLoc.y = clickLoc.y;
					selection.x = clickLoc.x;
					selection.y = clickLoc.y;
				}
				
			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {
				if(e.button != 1) {
					return;
				}
				if(STATUS.DRAWED.equals(status)) {
					Point loc = new Point(e.x, e.y);
					if(selection.contains(loc)) {
						Image img = new Image(null, selection.width, selection.height);
						GC gc = new GC(img);
						gc.drawImage(base, selection.x, selection.y, selection.width, selection.height, 0, 0, selection.width, selection.height);
						gc.dispose();
						if(null != callBack) {
							callBack.callback(img);
						}
					}
				}
			}
			
			
		});
		
		addPaintListener(this);
	}

	/**
	 * 选取窗口
	 * @param e
	 */
	private void moveOnInit(MouseEvent e) {
		Point now = new Point(e.x, e.y);
		for(Rectangle rect : windows) {
			if(rect.contains(now)) {
				if(null == nowWindow) {
					nowWindow = rect;
					redraw();
					System.out.println("find rect " + rect);
					break;
				} else if (!rect.equals(nowWindow)) {
					if(rect.equals(nowWindow.intersection(rect))) {
						nowWindow = rect;
						redraw();
						System.out.println("find sub rect " + rect);
						break;
					}else {
						if(rect.intersects(nowWindow) && nowWindow.contains(now)) {
							continue;
						}
						nowWindow = rect;
						redraw();
						System.out.println("find parent rect " + rect);
					}
				}
			}
		}
	}
	
	
	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
	
	@Override
	public void paintControl(PaintEvent e) {
		GC gc = e.gc;
		gc.drawImage(base, 0, 0);
		if(STATUS.INITED.equals(status)) {
			maskRect(gc, nowWindow, 2);
		}else if(STATUS.DRAWING.equals(status)) {
			maskRect(gc, selecting, 1);
		}else if(STATUS.DRAWED.equals(status)) {
			maskRect(gc, selection, 1);
		}
		gc.dispose();
		
	}
	
	/**
	 * 遮罩一个矩形
	 * @param gc
	 * @param rect
	 * @param width
	 */
	private void maskRect(GC gc, Rectangle rect, int width) {
		if(null != rect) {
			int alpha = gc.getAlpha();
			gc.setAlpha(200);
			Point size = getSize();
			if(rect.x > 0 && rect.x < size.x) {
				gc.fillRectangle(0, 0, rect.x, size.y);
			}
			if(rect.x + rect.width > 0 && rect.x + rect.width < size.x) {
				gc.fillRectangle(rect.x + rect.width, 0, size.x - (rect.x + rect.width), size.y);
			}
			if(rect.y > 0 && rect.y < size.y){
				gc.fillRectangle(rect.x, 0, rect.width, rect.y);
			}
			if(rect.y + rect.height > 0 && rect.y + rect.height < size.y) {
				gc.fillRectangle(rect.x, rect.y + rect.height, rect.width, size.y - (rect.y + rect.height));
			}
			gc.setAlpha(alpha);
			drawRect(gc, width, rect);
		}
	}
	
	
	/**
	 * 绘制矩形
	 * @param gc
	 * @param width
	 * @param rect
	 */
	private void drawRect(GC gc, int width, Rectangle rect) {
		Color back = SWTResourceManager.getColor(0, 174, 255);
		Color old = gc.getForeground();
		gc.setForeground(back);
		gc.setLineWidth(width);
		gc.drawRectangle(rect);
		gc.setForeground(old);
	}
	
	
	/**
	 * 截图状态
	 * @author xiaokui
	 */
	private enum STATUS{
		NONE,
		INITED, //刚初始化
		CLICKING,//鼠标按下，选取截图区域或者准备截取
		DRAWING, //正在选取区域
		DRAWED, //区域选取完
		MOVING, //移动选区
	}
}
