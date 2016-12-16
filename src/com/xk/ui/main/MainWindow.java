package com.xk.ui.main;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.client.ClientProtocolException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;

import com.xk.bean.ContactsStruct;
import com.xk.bean.User;
import com.xk.bean.WeChatSign;
import com.xk.ui.items.ContactItem;
import com.xk.ui.items.ConvItem;
import com.xk.ui.items.TypeItem;
import com.xk.uiLib.ListItem;
import com.xk.uiLib.MyList;
import com.xk.uiLib.MyText;
import com.xk.uiLib.MyText.DeleteListener;
import com.xk.uiLib.listeners.ItemSelectionEvent;
import com.xk.uiLib.listeners.ItemSelectionListener;
import com.xk.utils.AutoReply;
import com.xk.utils.Constant;
import com.xk.utils.HTTPUtil;
import com.xk.utils.JSONUtil;
import com.xk.utils.SWTTools;
import com.xk.utils.WeChatUtil;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.wb.swt.SWTResourceManager;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;
import org.eclipse.swt.widgets.Label;

public class MainWindow {

	private Timer timer;
	public User user;
	protected Shell shell;
	private WeChatSign sign;
	public Map<ListItem, MyList> lists = new HashMap<ListItem, MyList>();
	private MyText text;
	public Map<String, ContactsStruct> contacts = new HashMap<>();
	private boolean syncGroup = false;

	public MainWindow(WeChatSign sign) {
		this.sign = sign;
	}
	
	/**
	 * Open the window.
	 * @wbp.parser.entryPoint
	 */
	public void open() {
		createContents();
		shell.open();
		shell.layout();
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		final Color back = SWTResourceManager.getColor(234, 234, 234);
		final Color red = SWTResourceManager.getColor(SWT.COLOR_RED);
		final Color dark = SWTResourceManager.getColor(220, 220, 220);
		shell = new Shell(SWT.FILL_WINDING);
		shell.setBackground(back);
		shell.setSize(850, 590);
		shell.setBackgroundMode(SWT.INHERIT_DEFAULT);
		shell.setText("微信");
		shell.setImage(SWTResourceManager.getImage(MainWindow.class, "/images/wechat.jpg"));
		SWTTools.enableTrag(shell);

		
		
		Composite composite = new Composite(shell, SWT.NONE);
		composite.setBackground(SWTResourceManager.getColor(62, 62, 64));
		composite.setBounds(0, 0, 50, 590);
		composite.setBackgroundMode(SWT.INHERIT_DEFAULT);
		
		MyList types = new MyList(composite ,50 , 490);
		types.setMask(10);
		types.setLocation(0, 50);
		types.setSimpleSelect(true);
		
		Image chatImg=SWTResourceManager.getImage(MainWindow.class, "/images/chat.png");
		Image chatImgSele=SWTResourceManager.getImage(MainWindow.class, "/images/chatSele.png");
		TypeItem ctItem = new TypeItem(chatImg, chatImgSele);
		types.addItem(ctItem);
		
		MyList convers = new MyList(shell, 250, 540);
		convers.setMask(120);
		convers.setLocation(50, 50);
		convers.setSimpleSelect(true);
		
		lists.put(ctItem, convers);
		
		Image contactImg=SWTResourceManager.getImage(MainWindow.class, "/images/contact.png");
		Image contactImgSele=SWTResourceManager.getImage(MainWindow.class, "/images/contactSele.png");
		TypeItem conItem = new TypeItem(contactImg, contactImgSele);
		types.addItem(conItem);
		
		
		MyList contacts = new MyList(shell, 250, 540);
		contacts.setMask(120);
		contacts.setLocation(50, 50);
		contacts.setSimpleSelect(true);
		
		lists.put(conItem, contacts);
		
		types.add(new ItemSelectionListener() {
			
			MyList current;
			
			@Override
			public void selected(ItemSelectionEvent e) {
				if(null != current) {
					current.setVisible(false);
				}
				current = lists.get(e.item);
				current.setVisible(true);
			}
		});
		
		types.select(ctItem, false);
		
		
		Image search=SWTResourceManager.getImage(MainWindow.class, "/images/search.png");
		text = new MyText(shell, SWT.BORDER|SWT.SINGLE);
		text.setForeground(SWTResourceManager.getColor(0, 0, 0));
		text.setFont(SWTResourceManager.getFont("微软雅黑", 9, SWT.NORMAL));
		text.setBounds(66, 17, 190, 25);
		text.setInnerImage( search);
		text.setNoTrim();
		text.addDeleteListener(new DeleteListener() {
			
			@Override
			public void deleteClicked() {
				
			}
		});
		text.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				if(e.keyCode==SWT.CR){
					
				}
			}
			
		});
		
		Label label = new Label(shell, SWT.NONE);
		label.setCursor(SWTResourceManager.getCursor(SWT.CURSOR_HAND));
		label.setBackground(dark);
		label.setFont(SWTResourceManager.getFont("微软雅黑", 12, SWT.NORMAL));
		label.setAlignment(SWT.CENTER);
		label.setBounds(262, 16, 29, 24);
		label.setText("+");
		
		final Label minL = new Label(shell, SWT.NONE);
		minL.setCursor(SWTResourceManager.getCursor(SWT.CURSOR_HAND));
		minL.setFont(SWTResourceManager.getFont("微软雅黑", 6, SWT.NORMAL));
		minL.setAlignment(SWT.CENTER);
		minL.setOrientation(SWT.RIGHT_TO_LEFT);
		minL.setBounds(791, 0, 29, 25);
		minL.setText("\n__");
		minL.setBackground(back);
		minL.setToolTipText("最小化");
		minL.addMouseTrackListener(new MouseTrackListener() {
			
			@Override
			public void mouseHover(MouseEvent arg0) {
			}
			
			@Override
			public void mouseExit(MouseEvent arg0) {
				minL.setBackground(back);
				
			}
			
			@Override
			public void mouseEnter(MouseEvent arg0) {
				minL.setBackground(dark);
				
			}
		});
		minL.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseUp(MouseEvent e) {
				shell.setMinimized(true);
			}
			
		});
		
		
		final Label closeL = new Label(shell, SWT.NONE);
		closeL.setCursor(SWTResourceManager.getCursor(SWT.CURSOR_HAND));
		closeL.setText("\nX");
		closeL.setOrientation(SWT.RIGHT_TO_LEFT);
		closeL.setFont(SWTResourceManager.getFont("微软雅黑", 6, SWT.NORMAL));
		closeL.setAlignment(SWT.CENTER);
		closeL.setBounds(819, 0, 29, 25);
		closeL.setBackground(back);
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
				shell.dispose();
				System.exit(0);
			}
			
		});
		
		List<String> g = WeChatUtil.loadConvers(ctItem, sign, this);
		WeChatUtil.startNotify(sign, user);
		WeChatUtil.loadGroups(conItem,g, sign, this);
		syncData(conItem);
		List<String> group = WeChatUtil.loadContacts(conItem, sign, this);
		WeChatUtil.loadGroups(conItem,group, sign, this);
		
		
	}
	
	
	
	private void syncData(final TypeItem conItem) {
		final HTTPUtil hu = HTTPUtil.getInstance();
		timer = new Timer();
		timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				Map<String, String> params = new HashMap<String, String>();
				params.put("_", System.currentTimeMillis() + "");
				params.put("r", (System.currentTimeMillis() + 91136) + "");
				params.put("uin", sign.wxuin);
				try {
					params.put("sid", URLEncoder.encode(sign.wxsid, "UTF-8"));
					params.put("skey", URLEncoder.encode(sign.skey, "UTF-8"));
				} catch (UnsupportedEncodingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				params.put("deviceid", sign.deviceid);
				params.put("synckey", sign.synckey);
				try {
					String rst = hu.getJsonfromURL2(Constant.SYNC_CHECK, params);
					if(null != rst && rst.contains("window.synccheck=")) {
						String result = rst.replace("window.synccheck=", "");
						System.out.println("checksync + " + result);
						Map<String, String> map = JSONUtil.toBean(result, JSONUtil.getCollectionType(Map.class, String.class, String.class));
						if("0".equals(map.get("retcode")) ) {
							String selector = map.get("selector");
							try {
								Integer sele = Integer.parseInt(selector);
								if(sele > 0) {
									webwxsync(conItem);
								}
								
							} catch (NumberFormatException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
						}else if("1101".equals(map.get("retcode"))) {
							System.out.println("已在其它端登陆！！");
							System.exit(0);
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

	private void webwxsync(final TypeItem conItem) {
		HTTPUtil hu = HTTPUtil.getInstance();
		Map<String,Object> bodyMap = new HashMap<String,Object>();
		Map<String,Object> bodyInner = new HashMap<String,Object>();
		bodyInner.put("Uin", sign.wxuin);
		bodyInner.put("Sid", sign.wxsid);
		bodyInner.put("Skey", sign.skey);
		bodyInner.put("DeviceID", sign.deviceid);
		bodyMap.put("BaseRequest", bodyInner);
		bodyMap.put("SyncKey", sign.syncKeyOringe);
		bodyMap.put("rr", System.currentTimeMillis() / 1000 * -1);
		Map<String, String> params = new HashMap<String, String>();
		params.put("sid", sign.wxsid);
		params.put("lang", "zh_CN");
		params.put("skey", sign.skey);
		params.put("pass_ticket", sign.pass_ticket);
		try {
			String result =  hu.postBody(Constant.GET_STATUS, params, JSONUtil.toJson(bodyMap));
			Map<String, Object> rst = JSONUtil.fromJson(result);
			Map<String, Object> BaseResponse = (Map<String, Object>) rst.get("BaseResponse");
			if(new Integer(0).equals(BaseResponse.get("Ret"))) {
				Integer msgCount = (Integer) rst.get("AddMsgCount");
				if(null != msgCount && msgCount > 0) {
					List<Map<String, Object>> AddMsgList = (List<Map<String, Object>>) rst.get("AddMsgList");
					for(Map<String, Object> msg : AddMsgList) {
						Integer MsgType = (Integer) msg.get("MsgType");
						String Content = (String) msg.get("Content");
						String ToUserName = (String) msg.get("ToUserName");
						String FromUserName = (String) msg.get("FromUserName");
						if(51 == MsgType) {
							String StatusNotifyUserName = (String) msg.get("StatusNotifyUserName");
							if(null != StatusNotifyUserName && !syncGroup) {
								String[] spl = StatusNotifyUserName.split(",");
								List<String> groups = Arrays.asList(spl);
								WeChatUtil.loadGroups(conItem, groups, sign, this);
								Display.getDefault().asyncExec(new Runnable() {
									public void run() {
										lists.get(conItem).flush();
									}
								});
								syncGroup = true;
							}
						}else if(1 == MsgType) {
							if(Constant.FILTER_USERS.contains(FromUserName)) {
								System.out.println("忽略特殊用户信息！！" + Content);
							}else if(FromUserName.equals(user.UserName)){
								System.out.println("来自手机端自己的消息：" + Content);
							}else if(FromUserName.indexOf("@@") > -1) {
								String[] splt = Content.split(":<br/>");
								String sender = ContactsStruct.getGroupMember(splt[0], contacts.get(FromUserName));
								String ctt = splt[1].replace("<br/>", "\n");
								System.out.println(sender + " 在群里说:" + ctt);
								if(ctt.contains("@" + user.NickName)) {
									String reply = AutoReply.call(ctt.replace("@" + user.NickName, ""), sender);
									WeChatUtil.sendMsg(reply, FromUserName, sign, user);
								}
								
							}else {
								String sender = ContactsStruct.getContactName(contacts.get(FromUserName));
								String ctt = Content.replace("<br/>", "\n");
								System.out.println(sender + " 说：" + ctt);
								String reply = AutoReply.call(ctt, sender);
								WeChatUtil.sendMsg(reply, FromUserName, sign, user);
							}
						}
					}
				}
			}
			Map<String, Object> SyncKey = (Map<String, Object>) rst.get("SyncKey");
			WeChatUtil.flushSyncKey(SyncKey,sign);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
}
