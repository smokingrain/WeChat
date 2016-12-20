package com.xk.utils;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.internal.win32.OS;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

public class SWTTools {

	public static void topWindow(Shell shell) {
		OS.SetWindowPos(shell.handle , OS.HWND_TOPMOST, shell.getLocation().x , shell.getLocation().y , shell.getSize().x , shell.getSize().y , SWT.NULL);
	}
	
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
	
	public static void centerWindow(Shell shell){
		Rectangle rect=Display.getDefault().getClientArea();
		int x=rect.width/2-shell.getSize().x/2;
		int y=rect.height/2-shell.getSize().y/2;
		shell.setLocation(x,y);
	}
	
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
