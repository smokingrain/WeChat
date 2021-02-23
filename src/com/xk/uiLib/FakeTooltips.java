package com.xk.uiLib;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Shell;

public class FakeTooltips {

	private Shell shell;
	private Shell parent;
	private Object bind;
	
	private static class ToolTipsHolder {
		private static final FakeTooltips INSTANCE = new FakeTooltips();
	}
	
	public static FakeTooltips getInstance() {
		return ToolTipsHolder.INSTANCE;
	}
	
	private FakeTooltips() {
	}
	
	public void setParent(Shell parent) {
		this.parent = parent;
	}
	
	public boolean inited() {
		return null != shell && !shell.isDisposed();
	}
	
	public void init(Rectangle rect, ImageLoader loader) {
		if(null == shell || shell.isDisposed()) {
			shell = new Shell(parent, SWT.ON_TOP | SWT.NO_FOCUS | SWT.TOOL | SWT.NO_TRIM);
			shell.setBounds(rect);
			shell.setLayout(new StackLayout());
			shell.setVisible(true);
		} else {
			shell.setBounds(rect);
		}
		this.bind = loader;
		if(null != ((StackLayout)shell.getLayout()).topControl) {
			((StackLayout)shell.getLayout()).topControl.dispose();
		}
		ImageViewer iv = new ImageViewer(shell);
		iv.setLocation(0, 0);
		iv.setSize(rect.width, rect.height);
		((StackLayout)shell.getLayout()).topControl = iv;
		iv.setImages(loader.data, loader.repeatCount);
		
	}
	
	public void setLocation(int x, int y) {
		shell.setLocation(x, y);
	}
	
	public void hide() {
		if(null != shell && !shell.isDisposed()) {
			shell.dispose();
			shell = null;
		}
	}

	public Object getBind() {
		return bind;
	}

	public void setBind(Object bind) {
		this.bind = bind;
	}
	
	
}
