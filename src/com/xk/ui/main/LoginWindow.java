package com.xk.ui.main;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;

import com.xk.utils.Constant;
import com.xk.utils.HTTPUtil;
import com.xk.utils.SWTTools;

import org.eclipse.swt.widgets.Label;

public class LoginWindow {

	protected Shell shell;

	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			LoginWindow window = new LoginWindow();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
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
		final Color back = SWTResourceManager.getColor(245, 245, 245);
		final Color red = SWTResourceManager.getColor(SWT.COLOR_RED);
		shell = new Shell(SWT.FILL);
		shell.setBackground(back);
		shell.setSize(280, 400);
		shell.setText("SWT Application");
		SWTTools.centerWindow(shell);
		SWTTools.enableTrag(shell);
		
		final CLabel  closeL = new CLabel (shell, SWT.NONE);
		closeL.setCursor(SWTResourceManager.getCursor(SWT.CURSOR_HAND));
		closeL.setBackground(back);
		closeL.setAlignment(SWT.CENTER);
		closeL.setBounds(235, 0, 45, 35);
		closeL.setText("\nX");
		closeL.addMouseTrackListener(new MouseTrackListener() {
			
			@Override
			public void mouseHover(MouseEvent arg0) {
			}
			
			@Override
			public void mouseExit(MouseEvent arg0) {
				closeL.setBackground(back);
				
			}
			
			@Override
			public void mouseEnter(MouseEvent arg0) {
				closeL.setBackground(red);
				
			}
		});
		closeL.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseUp(MouseEvent e) {
				shell.dispose();
			}
			
		});
		
		Label nameL = new Label(shell, SWT.NONE);
		nameL.setBackground(back);
		nameL.setBounds(20, 18, 29, 17);
		nameL.setText("微信");
		
		Label headL = new Label(shell, SWT.NONE);
		headL.setCursor(SWTResourceManager.getCursor(SWT.CURSOR_HAND));
		headL.setBackground(back);
		headL.setBounds(94, 102, 93, 93);
		headL.setImage(SWTResourceManager.getImage(LoginWindow.class, "/images/wechat.jpg"));
		
		Label accountL = new Label(shell, SWT.NONE);
		accountL.setForeground(SWTResourceManager.getColor(112, 128, 144));
		accountL.setBackground(back);
		accountL.setAlignment(SWT.CENTER);
		accountL.setBounds(0, 220, 280, 17);
		accountL.setText("肖逵");
		
		Label loginL = new Label(shell, SWT.SHADOW_NONE);
		loginL.setCursor(SWTResourceManager.getCursor(SWT.CURSOR_HAND));
		loginL.setBackground(SWTResourceManager.getColor(61, 206, 61));
		loginL.setAlignment(SWT.CENTER);
		loginL.setBounds(35, 262, 210, 40);
		loginL.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseUp(MouseEvent arg0) {
				String url = Constant.GET_CONV_ID.replace("{TIME}", System.currentTimeMillis() + "");
				HTTPUtil hu = HTTPUtil.getInstance();
				try {
					String result = hu.readJsonfromURL2(url, null);
					String sign = "window.QRLogin.code = 200; window.QRLogin.uuid = ";
					if(null != result && result.contains(sign)){
						String uuid = result.replace(sign, "").replace("\"", "").replace(";", "");
						String qrUrl = Constant.GET_QR_IMG.replace("{UUID}", uuid);
						Image img = new Image(null, hu.getInput(qrUrl));
						
					}
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		});
		
		Label link = new Label(shell, SWT.NONE);
		link.setCursor(SWTResourceManager.getCursor(SWT.CURSOR_HAND));
		link.setAlignment(SWT.CENTER);
		link.setBounds(114, 336, 53, 17);
		link.setForeground(SWTResourceManager.getColor(161, 223, 245));
		link.setBackground(back);
		link.setText("切换账号");
		
		loginL.addPaintListener(new PaintListener() {
			
			@Override
			public void paintControl(PaintEvent e) {
				GC gc = e.gc;
				gc.setAdvanced(true);
				gc.setAntialias(SWT.ON);
				gc.setForeground(SWTResourceManager.getColor(255, 255, 255));
				gc.setBackground(SWTResourceManager.getColor(255, 255, 255));
				Path path = new Path(null);
				path.addString("登录", 90f, 12f, SWTResourceManager.getFont("黑体", 12, SWT.NORMAL));
				gc.fillPath(path);
				path.dispose();
				gc.dispose();
			}
		});

	}
}
