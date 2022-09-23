package com.xk.ui.main;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.client.ClientProtocolException;
import org.dom4j.Document;
import org.dom4j.Element;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;

import com.xk.bean.WeChatSign;
import com.xk.utils.Constant;
import com.xk.utils.HTTPUtil;
import com.xk.utils.SWTTools;
import com.xk.utils.XMLUtils;
import com.xk.utils.song.SongLocation;


/**
 * 用途：二维码扫描登陆，程序入口
 *
 * @author xiaokui
 * @date 2017年1月5日
 */
public class QRLoginWindow {

	protected Shell shell;
	private Label qrImage;
	private Label tips;
	private Timer timer;

	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			QRLoginWindow window = new QRLoginWindow();
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
		shell.setText("微信");
		shell.addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent paramDisposeEvent) {
				System.exit(0);
			}
		});
		
		SWTTools.centerWindow(shell);
		SWTTools.enableTrag(shell);
		
		final CLabel  closeL = new CLabel (shell, SWT.NONE);
		closeL.setCursor(SWTResourceManager.getCursor(SWT.CURSOR_HAND));
		closeL.setBackground(back);
		closeL.setAlignment(SWT.CENTER);
		closeL.setBounds(235, 0, 45, 35);
		closeL.setText("\nX");
		closeL.setToolTipText("关闭");
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
				if(null != timer) {
					timer.cancel();
				}
				shell.dispose();
			}
			
		});
		
		Label nameL = new Label(shell, SWT.NONE);
		nameL.setBackground(back);
		nameL.setBounds(20, 18, 29, 17);
		nameL.setText("微信");
		
		//二维码
		qrImage = new Label(shell, SWT.NONE);
		qrImage.setBounds(48, 80, 186, 186);
		qrImage.setBackground(back);
		
		//提示
		tips = new Label(shell, SWT.NONE);
		tips.setForeground(SWTResourceManager.getColor(112, 128, 144));
		tips.setBackground(back);
		tips.setFont(SWTResourceManager.getFont("微软雅黑", 10, SWT.NORMAL));
		tips.setAlignment(SWT.CENTER);
		tips.setBounds(0, 308, 280, 17);
		tips.setText("请使用微信扫一扫以登录");
		loadQRImage();
		SWTTools.topWindow(shell);
	}
	
	/**
	 * 用途：获取登录二维码
	 * @date 2016年12月13日
	 */
	private void loadQRImage() {
		String url = Constant.GET_CONV_ID.replace("{TIME}", System.currentTimeMillis() + "");
		HTTPUtil hu = HTTPUtil.getInstance();
		try {
			String result = hu.readJsonfromURL2(url, null);
			String sign = "window.QRLogin.code = 200; window.QRLogin.uuid = ";
			if(null != result && result.contains(sign)){
				String uuid = result.replace(sign, "").replace("\"", "").replace(";", "");
				String qrUrl = Constant.GET_QR_IMG.replace("{UUID}", uuid);
				SongLocation in = hu.getInput(qrUrl);
				Image img = new Image(null, in.input);
				in.input.close();
				if(null != in.response) {
					in.response.close();
				}
				Image dest = SWTTools.scaleImage(img.getImageData(), 186, 186);
				qrImage.setImage(dest);
				img.dispose();
				loopGetState(uuid);
			}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 用途：获取当前状态
	 * @date 2016年12月13日
	 */
	private void loopGetState(String uuid) {
		final String url = Constant.GET_STATUE.replace("{TIME}", System.currentTimeMillis() + "").replace("{UUID}", uuid);
		timer = new Timer();
		timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				HTTPUtil hu = HTTPUtil.getInstance();
				try {
					String result = hu.readJsonfromURL2(url, null);
					System.out.println("获取状态:" + result);
					if(null != result){
						if(result.contains("window.code=408;")) {
							
						}else if(result.contains("window.code=201;")) {
							Display.getDefault().asyncExec(new Runnable() {
								public void run() {
									tips.setText("请在手机上确认登录！");
								}
							});
						}else if(result.contains("window.code=200;window.redirect_uri=")){
							timer.cancel();
							timer = null ;
							result = result.replace("window.code=200;window.redirect_uri=", "").replace("\"", "");
							Display.getDefault().asyncExec(new Runnable() {
								public void run() {
									tips.setText("正在登录...");
								}
							});
							for(String host : Constant.HOSTS) {
								if(result.contains(host)) {
									Constant.HOST = host;
									break;
								}
							}
							Map<String, String> params = new HashMap<String, String>();
//							params.put("mod", "desktop");
							Map<String, String> header = new HashMap<String, String>();
//							header.put("extspam", "Gp8ICJkIEpkICggwMDAwMDAwMRAGGoAI1GiJSIpeO1RZTq9QBKsRbPJdi84ropi16EYI10WB6g74sGmRwSNXjPQnYUKYotKkvLGpshucCaeWZMOylnc6o2AgDX9grhQQx7fm2DJRTyuNhUlwmEoWhjoG3F0ySAWUsEbH3bJMsEBwoB//0qmFJob74ffdaslqL+IrSy7LJ76/G5TkvNC+J0VQkpH1u3iJJs0uUYyLDzdBIQ6Ogd8LDQ3VKnJLm4g/uDLe+G7zzzkOPzCjXL+70naaQ9medzqmh+/SmaQ6uFWLDQLcRln++wBwoEibNpG4uOJvqXy+ql50DjlNchSuqLmeadFoo9/mDT0q3G7o/80P15ostktjb7h9bfNc+nZVSnUEJXbCjTeqS5UYuxn+HTS5nZsPVxJA2O5GdKCYK4x8lTTKShRstqPfbQpplfllx2fwXcSljuYi3YipPyS3GCAqf5A7aYYwJ7AvGqUiR2SsVQ9Nbp8MGHET1GxhifC692APj6SJxZD3i1drSYZPMMsS9rKAJTGz2FEupohtpf2tgXm6c16nDk/cw+C7K7me5j5PLHv55DFCS84b06AytZPdkFZLj7FHOkcFGJXitHkX5cgww7vuf6F3p0yM/W73SoXTx6GX4G6Hg2rYx3O/9VU2Uq8lvURB4qIbD9XQpzmyiFMaytMnqxcZJcoXCtfkTJ6pI7a92JpRUvdSitg967VUDUAQnCXCM/m0snRkR9LtoXAO1FUGpwlp1EfIdCZFPKNnXMeqev0j9W9ZrkEs9ZWcUEexSj5z+dKYQBhIICviYUQHVqBTZSNy22PlUIeDeIs11j7q4t8rD8LPvzAKWVqXE+5lS1JPZkjg4y5hfX1Dod3t96clFfwsvDP6xBSe1NBcoKbkyGxYK0UvPGtKQEE0Se2zAymYDv41klYE9s+rxp8e94/H8XhrL9oGm8KWb2RmYnAE7ry9gd6e8ZuBRIsISlJAE/e8y8xFmP031S6Lnaet6YXPsFpuFsdQs535IjcFd75hh6DNMBYhSfjv456cvhsb99+fRw/KVZLC3yzNSCbLSyo9d9BI45Plma6V8akURQA/qsaAzU0VyTIqZJkPDTzhuCl92vD2AD/QOhx6iwRSVPAxcRFZcWjgc2wCKh+uCYkTVbNQpB9B90YlNmI3fWTuUOUjwOzQRxJZj11NsimjOJ50qQwTTFj6qQvQ1a/I+MkTx5UO+yNHl718JWcR3AXGmv/aa9rD1eNP8ioTGlOZwPgmr2sor2iBpKTOrB83QgZXP+xRYkb4zVC+LoAXEoIa1+zArywlgREer7DLePukkU6wHTkuSaF+ge5Of1bXuU4i938WJHj0t3D8uQxkJvoFi/EYN/7u2P1zGRLV4dHVUsZMGCCtnO6BBigFMAA=");
//							header.put("client-version", "2.0.0");
							String winAndsid = hu.readJsonfromURL2(result, params, header);
							System.out.println("获取wxsid和wxuin:" + winAndsid);
							if(null != winAndsid) {
								Document doc = XMLUtils.fromText(winAndsid);
								if(null != doc) {
									Element root = doc.getRootElement();
									final WeChatSign sign = new WeChatSign();
									sign.pass_ticket = root.elementTextTrim("pass_ticket");
									sign.skey = root.elementTextTrim("skey");
									sign.wxsid = root.elementTextTrim("wxsid");
									sign.wxuin = root.elementTextTrim("wxuin");
									Constant.sign = sign;
									Display.getDefault().asyncExec(new Runnable() {
										public void run() {
											shell.setVisible(false);
											MainWindow main = MainWindow.getInstance();
											main.open();
										}
									});
								}
							}
							
							
						}
					}
					
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}, 1000, 1000);
	}
}
