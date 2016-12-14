package com.xk.ui.main;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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
import com.xk.utils.Constant;
import com.xk.utils.HTTPUtil;
import com.xk.utils.JSONUtil;
import com.xk.utils.SWTTools;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.wb.swt.SWTResourceManager;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;
import org.eclipse.swt.widgets.Label;

public class MainWindow {

	private Timer timer;
	private User user;
	protected Shell shell;
	private WeChatSign sign;
	private Map<ListItem, MyList> lists = new HashMap<ListItem, MyList>();
	private MyText text;
	private Map<String, ContactsStruct> contacts = new HashMap<>();

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
		Color back = SWTResourceManager.getColor(234, 234, 234);
		Color red = SWTResourceManager.getColor(SWT.COLOR_RED);
		Color dark = SWTResourceManager.getColor(220, 220, 220);
		shell = new Shell(SWT.FILL_WINDING);
		shell.setBackground(back);
		shell.setSize(850, 590);
		shell.setBackgroundMode(SWT.INHERIT_DEFAULT);
		shell.setText("微信");
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
		
		Label minL = new Label(shell, SWT.NONE);
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
		
		
		Label closeL = new Label(shell, SWT.NONE);
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
			}
			
		});
		
		loadConvers(ctItem);
		loadContacts(conItem);
		loadGroups(conItem);
		startNotify();
		syncData();
		
	}
	
	/**
	 * 用途：开启消息通知
	 * @date 2016年12月14日
	 */
	public void startNotify(){
		HTTPUtil hu = HTTPUtil.getInstance();
		String url = Constant.STATUS_NOTIFY + "?pass_ticket=" + sign.pass_ticket;
		Map<String, Object> body = new HashMap<String, Object>();
		Map<String, Object> BaseRequest = new HashMap<>();
		BaseRequest.put("DeviceID", sign.deviceid);
		BaseRequest.put("Sid", sign.wxsid);
		BaseRequest.put("Skey", sign.skey);
		BaseRequest.put("Uin", sign.wxuin);
		body.put("BaseRequest", BaseRequest);
		try {
			String result = hu.postBody(url, JSONUtil.toJson(body));
			Map<String, Object> rst = JSONUtil.fromJson(result);
			Map<String, Object> BaseResponse = (Map<String, Object>) rst.get("BaseResponse");
			if(new Integer(0).equals(BaseResponse.get("Ret"))) {
				System.out.println("消息提醒成功！！！！");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void syncData() {
		HTTPUtil hu = HTTPUtil.getInstance();
		timer = new Timer();
		timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				Map<String, String> params = new HashMap<String, String>();
				params.put("r", System.currentTimeMillis() + "");
				params.put("_", System.currentTimeMillis() + "");
				params.put("Uin", sign.wxuin);
				params.put("Sid", sign.wxsid);
				params.put("Skey", sign.skey);
				params.put("DeviceID", sign.deviceid);
				params.put("synckey", sign.synckey);
				try {
					String rst = hu.readJsonfromURL2(Constant.SYNC_CHECK, params);
					if(null != rst && rst.contains("window.synccheck=")) {
						String result = rst.replace("window.synccheck=", "");
						Map<String, String> map = JSONUtil.toBean(result, JSONUtil.getCollectionType(Map.class, String.class, String.class));
						if("0".equals(map.get("retcode")) && "2".equals(map.get("selector"))) {
							
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

	private void webwxsync() {
		HTTPUtil hu = HTTPUtil.getInstance();
		Map<String,Object> bodyMap = new HashMap<String,Object>();
		Map<String,Object> bodyInner = new HashMap<String,Object>();
		bodyInner.put("Uin", sign.wxuin);
		bodyInner.put("Sid", sign.wxsid);
		bodyInner.put("Skey", sign.skey);
		bodyInner.put("DeviceID", sign.deviceid);
		bodyMap.put("BaseRequest", bodyInner);
		bodyMap.put("SyncKey", sign.syncKeyOringe);
		bodyMap.put("rr", System.currentTimeMillis());
		Map<String, String> params = new HashMap<String, String>();
		params.put("sid", sign.wxsid);
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
							System.out.println("系统初始化信息获取完毕！！");
						}else if(1 == MsgType) {
							if(Constant.FILTER_USERS.contains(FromUserName)) {
								System.out.println("忽略特殊用户信息！！" + Content);
							}else if(FromUserName.equals(user.getUserName())){
								System.out.println("来自手机端自己的消息：" + Content);
							}else if(ToUserName.indexOf("@@") > -1) {
								String[] splt = Content.split(":<br/>");
								String sender = ContactsStruct.getGroupMember(splt[0], contacts.get(FromUserName));
								
								System.out.println(sender + " 在群里说:" + splt[1]);
							}else {
								String sender = ContactsStruct.getContactName(contacts.get(FromUserName));
								System.out.println(sender + " 说：" + Content);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	/**
	 * 用途：加载会话
	 * @date 2016年12月14日
	 * @param ctItem
	 */
	private void loadConvers(TypeItem ctItem) {
		HTTPUtil hu = HTTPUtil.getInstance();
		Map<String,Map<String,String>> bodyMap = new HashMap<String,Map<String,String>>();
		Map<String,String> bodyInner = new HashMap<String,String>();
		bodyInner.put("Uin", sign.wxuin);
		bodyInner.put("Sid", sign.wxsid);
		bodyInner.put("Skey", sign.skey);
		bodyInner.put("DeviceID", sign.deviceid);
		bodyMap.put("BaseRequest", bodyInner);
		
		try {
			String result = hu.postBody(Constant.GET_INIT.replace("{TIME}", System.currentTimeMillis() + ""), JSONUtil.toJson(bodyMap));
			Map<String, Object> rstMap = JSONUtil.fromJson(result);
			Map<String, Object> baseResponse = (Map<String, Object>) rstMap.get("BaseResponse");
			if(null != baseResponse && new Integer(0).equals(baseResponse.get("Ret"))) {
				List<Map<String, Object>> contactList = (List<Map<String, Object>>) rstMap.get("ContactList");
				if(null != contactList) {
					for(Map<String, Object> cmap : contactList) {
						ContactsStruct convs = ContactsStruct.fromMap(cmap);
						String headUrl = Constant.BASE_URL + convs.HeadImgUrl;
						Image img = null;
						try {
							img = new Image(null, hu.getInput(headUrl));
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						String nick = convs.NickName;
						String remark = convs.RemarkName;
						String name = (null == remark || remark.trim().isEmpty()) ? nick : remark; 
						Integer Statues = convs.Statues;
						Integer ContactFlag = convs.ContactFlag;
						ConvItem ci = new ConvItem(convs, img, name, null, null, null, ContactFlag == 2051, Statues == 0, null);
						MyList list = lists.get(ctItem);
						list.addItem(ci);
					}
					
					Map<String, Object> SyncKey = (Map<String, Object>) rstMap.get("SyncKey");
					flushSyncKey(SyncKey);
					
					user = (User) rstMap.get("User");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("获取会话失败");
			System.exit(0);
		}
		
	}
	
	private void flushSyncKey(Map<String, Object> SyncKey) {
		List<Map<String, Integer>> List = (java.util.List<Map<String, Integer>>) SyncKey.get("List");
		StringBuffer sb = new StringBuffer();
		for(Map<String, Integer> v : List) {
			Integer Key = v.get("Key");
			Integer Val = v.get("Val");
			sb.append(Key).append("_").append(Val).append("|");
		}
		try {
			sign.synckey = URLEncoder.encode(sb.substring(0, sb.length() - 1), "UTF-8");
			sign.syncKeyOringe = SyncKey;
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 用途：抓取联系人
	 * @date 2016年12月14日
	 * @param ctItem
	 */
	private void loadContacts(TypeItem ctItem) {
		HTTPUtil hu = HTTPUtil.getInstance();
		Map<String, String> params = new HashMap<String, String>();
		params.put("pass_ticket", sign.pass_ticket);
		params.put("r", System.currentTimeMillis() + "");
		params.put("seq", "0");
		params.put("skey", sign.skey);
		
		try {
			String result = hu.readJsonfromURL2(Constant.GET_CONTACT, params);
			Map<String, Object> rstMap = JSONUtil.fromJson(result);
			Map<String, Object> baseResponse = (Map<String, Object>) rstMap.get("BaseResponse");
			if(null != baseResponse && new Integer(0).equals(baseResponse.get("Ret"))) {
				List<Map<String, Object>> contactList = (List<Map<String, Object>>) rstMap.get("ContactList");
				if(null != contactList) {
					for(Map<String, Object> cmap : contactList) {
						ContactsStruct convs = ContactsStruct.fromMap(cmap);
						contacts.put(convs.UserName, convs);
						String headUrl = Constant.BASE_URL + convs.HeadImgUrl;
						Image img = null;
						try {
							img = new Image(null, hu.getInput(headUrl));
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						String nick = convs.NickName;
						String remark = convs.RemarkName;
						String name = (null == remark || remark.trim().isEmpty()) ? nick : remark; 
						ContactItem ci = new ContactItem(convs, false, img, name);
						MyList list = lists.get(ctItem);
						list.addItem(ci);
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
	
	/**
	 * 用途：抓取群组
	 * @date 2016年12月14日
	 * @param ctItem
	 */
	private void loadGroups(TypeItem ctItem) {
		HTTPUtil hu = HTTPUtil.getInstance();
		Map<String,Map<String,String>> bodyMap = new HashMap<String,Map<String,String>>();
		Map<String,String> bodyInner = new HashMap<String,String>();
		bodyInner.put("Uin", sign.wxuin);
		bodyInner.put("Sid", sign.wxsid);
		bodyInner.put("Skey", sign.skey);
		bodyInner.put("DeviceID", sign.deviceid);
		bodyMap.put("BaseRequest", bodyInner);
		
		try {
			String url = Constant.GET_GROUPS.replace("{TIME}", System.currentTimeMillis() + "").replace("{TICKET}", sign.pass_ticket);
			String result = hu.postBody(url, JSONUtil.toJson(bodyMap));
			Map<String, Object> rstMap = JSONUtil.fromJson(result);
			Map<String, Object> baseResponse = (Map<String, Object>) rstMap.get("BaseResponse");
			if(null != baseResponse && new Integer(0).equals(baseResponse.get("Ret"))) {
				List<Map<String, Object>> contactList = (List<Map<String, Object>>) rstMap.get("ContactList");
				if(null != contactList) {
					for(Map<String, Object> cmap : contactList) {
						ContactsStruct convs = ContactsStruct.fromMap(cmap);
						contacts.put(convs.UserName, convs);
						String headUrl = Constant.BASE_URL + convs.HeadImgUrl;
						Image img = null;
						try {
							img = new Image(null, hu.getInput(headUrl));
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						String nick = convs.NickName;
						String remark = convs.RemarkName;
						String name = (null == remark || remark.trim().isEmpty()) ? nick : remark; 
						ContactItem ci = new ContactItem(convs, false, img, name);
						MyList list = lists.get(ctItem);
						list.addItem(ci);
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
	
	
	
}
