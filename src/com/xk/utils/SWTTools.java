package com.xk.utils;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.internal.win32.OS;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

public class SWTTools {

	private static int DRAW_FLAGS = SWT.DRAW_MNEMONIC | SWT.DRAW_TAB | SWT.DRAW_TRANSPARENT | SWT.DRAW_DELIMITER;
	private static final String ELLIPSIS = "...";
	
	public static void topWindow(Shell shell) {
		OS.SetWindowPos(shell.handle , OS.HWND_TOPMOST, shell.getLocation().x , shell.getLocation().y , shell.getSize().x , shell.getSize().y , SWT.NULL);
	}
	
	/**
	 * Shorten the given text <code>t</code> so that its length doesn't exceed
	 * the given width. The default implementation replaces characters in the
	 * center of the original string with an ellipsis ("...").
	 * Override if you need a different strategy.
	 * 
	 * @param gc the gc to use for text measurement
	 * @param t the text to shorten
	 * @param width the width to shorten the text to, in pixels
	 * @return the shortened text
	 */
	public static String shortenText(GC gc, String t, int width) {
		if (t == null) return null;
		int w = gc.textExtent(ELLIPSIS, DRAW_FLAGS).x;
		if (width<=w) return t;
		int l = t.length();
		int max = l/2;
		int min = 0;
		int mid = (max+min)/2 - 1;
		if (mid <= 0) return t;
		TextLayout layout = new TextLayout (null);
		layout.setText(t);
		mid = validateOffset(layout, mid);
		while (min < mid && mid < max) {
			String s1 = t.substring(0, mid);
			String s2 = t.substring(validateOffset(layout, l-mid), l);
			int l1 = gc.textExtent(s1, DRAW_FLAGS).x;
			int l2 = gc.textExtent(s2, DRAW_FLAGS).x;
			if (l1+w+l2 > width) {
				max = mid;			
				mid = validateOffset(layout, (max+min)/2);
			} else if (l1+w+l2 < width) {
				min = mid;
				mid = validateOffset(layout, (max+min)/2);
			} else {
				min = max;
			}
		}
		String result = mid == 0 ? t : t.substring(0, mid) + ELLIPSIS + t.substring(validateOffset(layout, l-mid), l);
		layout.dispose();
	 	return result;
	}
	
	private static int validateOffset(TextLayout layout, int offset) {
		int nextOffset = layout.getNextOffset(offset, SWT.MOVEMENT_CLUSTER);
		if (nextOffset != offset) return layout.getPreviousOffset(nextOffset, SWT.MOVEMENT_CLUSTER);
		return offset;
	}
	
	/**
	 * 用途：允许拖拽
	 * @date 2017年1月5日
	 * @param ctrl
	 */
	public static void enableTrag(Control ctrl) {
		final Composite composite=ctrl.getShell();
		
		Listener listener = new Listener() {
		    int startX, startY;
		    public void handleEvent(Event e) {
		        if (e.type == SWT.MouseDown && e.button == 1) {
		            startX = e.x;
		            startY = e.y;
		        }
		        if (e.type == SWT.MouseMove && (e.stateMask & SWT.BUTTON1) != 0) {
		            Point p = composite.toDisplay(e.x, e.y);
		            p.x -= startX;
		            p.y -= startY;
		            composite.setLocation(p);
		            composite.setFocus();
		        }
		    }
		};
		ctrl.addListener(SWT.MouseDown, listener);
		ctrl.addListener(SWT.MouseMove, listener);
		
	}
	
	
	/**
	 * 用途：窗体居中显示
	 * @date 2017年1月5日
	 * @param shell
	 */
	public static void centerWindow(Shell shell){
		Rectangle rect=Display.getDefault().getClientArea();
		int x=rect.width/2-shell.getSize().x/2;
		int y=rect.height/2-shell.getSize().y/2;
		shell.setLocation(x,y);
	}
	

	/**
	 * 用途：图片缩放
	 * @date 2017年1月5日
	 * @param source
	 * @param width
	 * @param height
	 * @return
	 */
	public static Image scaleImage(ImageData source,int width,int height){
		Image img=new Image(null,source);
		ImageData dest = new ImageData(1, 1, source.depth, source.palette);
		dest.alphaData=new byte[]{-1,-1,-1,-1};
		dest.data=new byte[]{-1,-1,-1,-1};
		dest=dest.scaledTo(width, height);
		Image tmp=new Image(null, dest);
		GC gc=new GC(tmp);
		gc.setAdvanced(true);
		gc.setAntialias(SWT.ON);
		Transform trans=new Transform(null);
		trans.scale((float)width/source.width, (float)height/source.height);
		gc.setTransform(trans);
		gc.drawImage(img, 0, 0);
		gc.dispose();
		img.dispose();
		return tmp;
	}
}
