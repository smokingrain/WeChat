package com.xk.ui.main;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.BaseTSD.LONG_PTR;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HDC;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.RECT;
import com.sun.jna.platform.win32.WinUser.WNDENUMPROC;
import com.xk.bean.Pointd;
import com.xk.uiLib.ICallback;
import com.xk.utils.BezierUtil;

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
	private List<Drawable> options = new CopyOnWriteArrayList<Drawable>();
	private ICallback callBack;
	private Drawable option;
	
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
				int handle =  u32.GetWindowLong(hwnd, User32.GWL_HINSTANCE);//32位系统用这个
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
				} else if(STATUS.OPTIONG_DOWN.equals(status) || STATUS.OPTIONG.equals(status)) {
					status = STATUS.OPTIONG;
					option.processEvent(e, 1);
					redraw();
				}
				
				
			}
		});
		addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseUp(MouseEvent e) {
				if(e.button != 1 && STATUS.INITED.equals(status)) {
					callBack.callback(null);
					return;
				}else if(e.button != 1 && STATUS.DRAWED.equals(status)) {
					if(selection.contains(e.x, e.y)) {
						createMenu(e);
						return;
					}else {
						callBack.callback(null);
						return;
					}
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
				} else if(STATUS.OPTIONG.equals(status)) {
					option.processEvent(e, 2);
					status = STATUS.DRAWED;
					options.add(option);
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
				} else if(STATUS.OPTIONG_SELE.equals(status)) {
					option.processEvent(e, 0);
					status = STATUS.OPTIONG_DOWN;
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

	private void createMenu(MouseEvent e) {
		Menu m=new Menu(getParent());
		Menu menu=getParent().getMenu();
		if (menu != null) {
			menu.dispose();
		}
		
		MenuItem bezier=new MenuItem(m, SWT.NONE);
		bezier.setText("涂鸦");
		bezier.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				option = new  BezierLine(new ArrayList<Pointd>());
				status = STATUS.OPTIONG_SELE;
			}
			
		});
		m.setVisible(true);
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
		gc.setAntialias(SWT.ON);
		gc.drawImage(base, 0, 0);
		if(STATUS.INITED.equals(status)) {
			maskRect(gc, nowWindow, 2);
		}else if(STATUS.DRAWING.equals(status)) {
			maskRect(gc, selecting, 1);
		}else if(STATUS.DRAWED.equals(status)) {
			maskRect(gc, selection, 1);
			drawOptions(gc);
		}else if(STATUS.OPTIONG.equals(status)) {
			System.out.println("OPTIONG");
			maskRect(gc, selection, 1);
			drawOptions(gc);
		}
		gc.dispose();
		
	}
	
	private void drawOptions(GC gc) {
//		Transform transform = new Transform(null);
//		transform.translate(selection.x, selection.y);
//		gc.setTransform(transform);
		for(Drawable draw : options) {
			draw.draw(gc);
		}
		if(null != option) {
			option.draw(gc);
		}
//		transform.dispose();
//		transform = new Transform(null);
//		transform.translate(-selection.x, -selection.y);
//		gc.setTransform(transform);
//		transform.dispose();
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
		OPTIONG_SELE,//选择操作模式
		OPTIONG_DOWN,//操作开始（画形状，涂鸦）
		OPTIONG,//操作中
	}
	
	
	
	
}

/**
 * 绘画接口
 * @author o-kui.xiao
 *
 */
interface Drawable{
	public void draw(GC gc);
	public void processEvent(MouseEvent e, int type);
}

class BezierLine implements Drawable{
	double t = 0.2;
	List<Pointd> points;
	
	public BezierLine(List<Pointd> points) {
		this.points = points;
	}
	
	@Override
	public void draw(GC gc) {
		Pointd last = null;
		Pointd control = null;
		Path path = new Path(null);
		for(Pointd point : points) {
			if(null == last) {
				last = point;
				path.moveTo((float)last.x, (float)last.y);
				continue;
			}else if(control == null) {
				control = point;
				continue;
			}else {
				System.out.println(last.x + " " + last.y + " " + point.x + " " + point.y);
				if(!point.equals(last)) {
					path.quadTo((float)control.x, (float)control.y, (float)point.x, (float)point.y);
					last = point;
					control = null;
				}
				
//				break;
			}
		}
		gc.drawPath(path);
		path.dispose();
		
		
//		for(int i = 0; i < points.size() ; i += 3) {
//        	if(i >= points.size() - 1) {
//        		break;
//        	}
//        	List<Pointd>ps = new ArrayList<Pointd>();
//        	ps.add(points.get(i));
//        	if(i + 1 < points.size()) {
//        		ps.add(points.get(i + 1));
//        	}
//        	if(i + 2 < points.size()) {
//        		ps.add(points.get(i + 2));
//        	}
//        	if(i + 3 < points.size()) {
//        		ps.add(points.get(i + 3));
//        	}
//        	for(double k=t; k<=1+t; k+=t) {  
//        		Pointd point = BezierUtil.computLine(k, ps.toArray(new Pointd[]{}));
//        		if(null == last) {
//        			gc.drawLine((int)point.x, (int)point.y, (int)point.x, (int)point.y);  
//        		}else {
//        			gc.drawLine((int)last.x, (int)last.y, (int)point.x, (int)point.y);  
//        		}
//        		last = point;
//        	}  
//        }
		
	}

	@Override
	public void processEvent(MouseEvent e, int type) {
		this.points.add(new Pointd(e.x, e.y));
	}
}

class RectDraw implements Drawable {

	Rectangle rect;
	
	public RectDraw(Rectangle rect) {
		this.rect = rect;
	}
	
	@Override
	public void draw(GC gc) {
		gc.drawRectangle(rect);
	}

	@Override
	public void processEvent(MouseEvent e, int type) {
		// TODO Auto-generated method stub
		
	}
}

class CircleDraw implements Drawable {
	
	Point loc, size;
	
	public CircleDraw(Point loc, Point size) {
		this.loc = loc;
		this.size = size;
	}
	
	@Override
	public void draw(GC gc) {
		gc.drawOval(loc.x, loc.y, size.x, loc.y);
		
	}

	@Override
	public void processEvent(MouseEvent e, int type) {
		// TODO Auto-generated method stub
		
	}
	
}
