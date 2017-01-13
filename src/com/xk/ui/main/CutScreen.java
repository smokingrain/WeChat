package com.xk.ui.main;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.xk.utils.SWTTools;

public class CutScreen {

	protected Shell shell;
	private Display display;

	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			CutScreen window = new CutScreen();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		display = Display.getDefault();
		createContents();
		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		Rectangle rect = display.getBounds();
		shell = new Shell(SWT.FILL);
		shell.setBounds(rect);
		shell.setText("SWT Application");
		SWTTools.topWindow(shell);
		Rectangle di = display.getBounds();  
        Image temps = new Image(display, di.width, di.height);  
        GC gc = new GC(display);  
        gc.copyArea(temps, 0, 0);  
        gc.dispose();  
        ScreenCanvas ts = new ScreenCanvas(shell, temps);
		ts.setBounds(rect);
		SWTTools.enableTrag(ts);
	}

}
