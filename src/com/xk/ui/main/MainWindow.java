package com.xk.ui.main;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.client.ClientProtocolException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.platform.win32.WinUser.FLASHWINFO;
import com.xk.bean.ContactsStruct;
import com.xk.chatlogs.ChatLog;
import com.xk.chatlogs.ChatLogCache;
import com.xk.ui.items.ContactItem;
import com.xk.ui.items.ConvItem;
import com.xk.ui.items.TypeItem;
import com.xk.ui.main.chat.ChatComp;
import com.xk.uiLib.ListItem;
import com.xk.uiLib.MyList;
import com.xk.uiLib.MyText;
import com.xk.uiLib.MyText.DeleteListener;
import com.xk.uiLib.listeners.ItemEvent;
import com.xk.uiLib.listeners.ItemListener;
import com.xk.uiLib.listeners.ItemSelectionEvent;
import com.xk.uiLib.listeners.ItemSelectionListener;
import com.xk.utils.AutoReply;
import com.xk.utils.Constant;
import com.xk.utils.HTTPUtil;
import com.xk.utils.ImageCache;
import com.xk.utils.JSONUtil;
import com.xk.utils.SWTTools;
import com.xk.utils.WeChatUtil;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.widgets.Label;

/**
 * 
 * 用途：主窗口
 *
 * @author xiaokui
 * @date 2017年1月5日
 */
public class MainWindow {

	private Timer timer;//心跳包，轮询拉消息的计时器
	protected Shell shell;
	public Map<ListItem, MyList<? extends ListItem>> lists = new HashMap<ListItem, MyList<? extends ListItem>>();
	private MyText text;
	private ChatComp cc;
	private MyList<ConvItem> convers;
	private MyList<TypeItem> types;
	private boolean syncGroup = false;

	public MainWindow() {
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
		shell = new Shell(SWT.NO_TRIM);
		shell.setBackground(back);
		shell.setSize(850, 590);
		shell.setBackgroundMode(SWT.INHERIT_DEFAULT);
		shell.setText("微信");
		shell.setImage(SWTResourceManager.getImage(MainWindow.class, "/images/wechat.jpg"));
		shell.addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent arg0) {
				WeChatUtil.exitWeChat();//主窗口关闭的时候通知服务器我退出了
				System.exit(0);
			}
		});
		SWTTools.enableTrag(shell);//允许拖拽窗体

		
		//左侧面板
		Composite composite = new Composite(shell, SWT.NONE);
		composite.setBackground(SWTResourceManager.getColor(62, 62, 64));
		composite.setBounds(0, 0, 50, 590);
		composite.setBackgroundMode(SWT.INHERIT_DEFAULT);
		
		//我的头像
		final Label me = new Label(composite, SWT.NONE);
		me.setBounds(10, 10, 30, 30);
		me.setCursor(SWTResourceManager.getCursor(SWT.CURSOR_HAND));
		me.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseUp(MouseEvent e) {
				Point globPos = me.toDisplay(me.getLocation());
				System.out.println(globPos.x  + "  " + globPos.y);
				FloatWindow bb = FloatWindow.getInstance();
				bb.init();
				bb.setSize(180, 255);
				MyInfoComp mic = new MyInfoComp(bb.shell, SWT.NONE, Constant.user);
				bb.add(mic);
				Integer rst = (Integer) bb.open(globPos.x + e.x, globPos.y + e.y);
				if(null != rst && 1 == rst) {
					//打开和自己聊天的窗口
				}
			}
			
		});
		
		//功能列表
		types = new MyList<TypeItem>(composite ,50 , 490);
		types.setMask(10);
		types.setLocation(0, 50);
		types.setSimpleSelect(true);
		
		Image chatImg=SWTResourceManager.getImage(MainWindow.class, "/images/chat.png");
		Image chatImgSele=SWTResourceManager.getImage(MainWindow.class, "/images/chatSele.png");
		TypeItem ctItem = new TypeItem(chatImg, chatImgSele);
		types.addItem(ctItem);
		
		//会话列表
		convers = new MyList<ConvItem>(shell, 250, 540);
		convers.setMask(120);
		convers.setLocation(50, 50);
		convers.setSimpleSelect(false);
		
		lists.put(ctItem, convers);
		
		Image contactImg=SWTResourceManager.getImage(MainWindow.class, "/images/contact.png");
		Image contactImgSele=SWTResourceManager.getImage(MainWindow.class, "/images/contactSele.png");
		TypeItem conItem = new TypeItem(contactImg, contactImgSele);
		types.addItem(conItem);
		
		//好友列表
		MyList<ContactItem> contacts = new MyList<ContactItem>(shell, 250, 540);
		contacts.setMask(120);
		contacts.setLocation(50, 50);
		contacts.setSimpleSelect(false);
		
		lists.put(conItem, contacts);
		
		types.add(new ItemSelectionListener<TypeItem>() {
			
			MyList<? extends ListItem> current;
			
			@Override
			public void selected(ItemSelectionEvent<TypeItem> e) {
				if(null != current) {
					current.setVisible(false);
				}
				current = lists.get(e.item);
				current.setVisible(true);
			}
		});
		
		types.select(ctItem, false);
		
		
		//搜索，暂未实现
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
				searchUser();
			}
		});
		text.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				if(e.keyCode==SWT.CR){
					searchUser();
				}
			}
			
		});
		
		//添加，暂未实现
		Label label = new Label(shell, SWT.NONE);
		label.setCursor(SWTResourceManager.getCursor(SWT.CURSOR_HAND));
		label.setBackground(dark);
		label.setFont(SWTResourceManager.getFont("微软雅黑", 12, SWT.NORMAL));
		label.setAlignment(SWT.CENTER);
		label.setBounds(262, 16, 29, 24);
		label.setText("+");
		
		//右侧聊天面板
		cc = new ChatComp(shell, SWT.NONE);
		SWTTools.enableTrag(cc);
		
		contacts.add(new ItemSelectionListener<ContactItem>() {
			
			@Override
			public void selected(ItemSelectionEvent<ContactItem> e) {
				ContactItem item = e.item;
				ContactsStruct convs = item.getData();
				ConvItem ci = addConversition(convs);
				System.out.println(convs.UserName + " selected!!!");
				convers.select(ci, false);
				types.select(0, false);
				
			}
		});
		
		//选中聊天会话
		convers.add(new ItemSelectionListener<ConvItem>() {
			
			@Override
			public void selected(ItemSelectionEvent<ConvItem> e) {
				ConvItem item = e.item;
				System.out.println(item.getName() + " selected!!!");
				cc.flush(item);
				
			}
		});
		//删除聊天会话
		convers.addItemListener(new ItemListener<ConvItem>() {
			
			@Override
			public void itemRemove(ItemEvent<ConvItem> e) {
				if(e.item.equals(convers.getSelection())) {
					cc.flush(null);
				}
				convers.flush();
				
			}
		});
		
		//最小化按钮
		final CLabel minL = new CLabel(cc, SWT.CENTER);
		minL.setOrientation(SWT.RIGHT_TO_LEFT);
		minL.setCursor(SWTResourceManager.getCursor(SWT.CURSOR_HAND));
		minL.setFont(SWTResourceManager.getFont("微软雅黑", 9, SWT.NORMAL));
		minL.setAlignment(SWT.CENTER);
		minL.setBounds(491, -5, 29, 30);
		minL.setText("__");
		minL.setBackground(SWTResourceManager.getColor(245, 245, 245));
		minL.setToolTipText("最小化");
		minL.addMouseTrackListener(new MouseTrackListener() {
			
			@Override
			public void mouseHover(MouseEvent arg0) {
			}
			
			@Override
			public void mouseExit(MouseEvent arg0) {
				minL.setBackground(cc.getBackground());
				
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
		
		//关闭按钮
		final CLabel closeL = new CLabel(cc, SWT.CENTER);
		closeL.setCursor(SWTResourceManager.getCursor(SWT.CURSOR_HAND));
		closeL.setText("X");
		closeL.setOrientation(SWT.RIGHT_TO_LEFT);
		closeL.setFont(SWTResourceManager.getFont("微软雅黑", 8, SWT.NORMAL));
		closeL.setAlignment(SWT.CENTER);
		closeL.setBounds(519, 0, 29, 25);
		closeL.setBackground(SWTResourceManager.getColor(245, 245, 245));
		closeL.setToolTipText("关闭");
		closeL.addMouseTrackListener(new MouseTrackListener() {
			
			@Override
			public void mouseHover(MouseEvent arg0) {
			}
			
			@Override
			public void mouseExit(MouseEvent arg0) {
				closeL.setBackground(cc.getBackground());
				
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
		
		//头像缓存，然并卵，微信每次返回用户id不一样缓存不了
		ImageCache.loadHeadCache();
		
		List<String> g = WeChatUtil.loadConvers(ctItem, this);//先加载最近会话
		WeChatUtil.startNotify();//通知服务器我准备收消息了
		WeChatUtil.loadGroups(conItem, g, this);//拉取最近会话中的群组
		syncData(conItem);//开始发送心跳包，拉消息
		List<String> group = WeChatUtil.loadContacts(conItem, this);//拉取联系人
		WeChatUtil.loadGroups(conItem,group, this);//拉取联系人中的群组
		
		//加载自己的头像
		Image img = null;
		String headUrl = Constant.BASE_URL + Constant.user.HeadImgUrl + "&type=big";
		Image temp = ImageCache.getUserHeadCache(Constant.user.UserName, headUrl, null, 180, 180);
		img = SWTTools.scaleImage(temp.getImageData(), 30, 30);
		Constant.user.head = temp;
		me.setImage(img);
	}
	
	
	/**
	 * 搜索联系人
	 */
	private void searchUser() {
		String name = text.getText().trim();
		if(!name.isEmpty()) {
			for(ContactsStruct convs : Constant.contacts.values()) {
				if((null != convs.RemarkName && convs.RemarkName.contains(name)) || (null != convs.NickName && convs.NickName.contains(name))) {
					ConvItem itm = addConversition(convs);
					ConvItem temp = convers.removeItem(itm);
					if(null != temp) {
						convers.addItem(0, temp);
						convers.select(temp, false);
					}
					break;
				}
			}
			types.select(0, false);
		}
		
		
	}
	
	/**
	 * 用途：发心跳包，获取微信状态是否有新消息
	 * @date 2016年12月30日
	 * @param conItem
	 */
	private void syncData(final TypeItem conItem) {
		final HTTPUtil hu = HTTPUtil.getInstance();
		timer = new Timer();
		timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				Map<String, String> params = new HashMap<String, String>();
				params.put("_", System.currentTimeMillis() + "");
				params.put("r", (System.currentTimeMillis() + 91136) + "");
				params.put("uin", Constant.sign.wxuin);
				try {
					params.put("sid", URLEncoder.encode(Constant.sign.wxsid, "UTF-8"));
					params.put("skey", URLEncoder.encode(Constant.sign.skey, "UTF-8"));
				} catch (UnsupportedEncodingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				params.put("deviceid", Constant.sign.deviceid);
				params.put("synckey", Constant.sign.synckey);
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

	
	/**
	 * 用途：同步到数据的时候刷新数据
	 * @date 2016年12月30日
	 * @param conItem
	 */
	private void webwxsync(final TypeItem conItem) {
		HTTPUtil hu = HTTPUtil.getInstance();
		Map<String,Object> bodyMap = new HashMap<String,Object>();
		Map<String,Object> bodyInner = new HashMap<String,Object>();
		bodyInner.put("Uin", Constant.sign.wxuin);
		bodyInner.put("Sid", Constant.sign.wxsid);
		bodyInner.put("Skey", Constant.sign.skey);
		bodyInner.put("DeviceID", Constant.sign.deviceid);
		bodyMap.put("BaseRequest", bodyInner);
		bodyMap.put("SyncKey", Constant.sign.syncKeyOringe);
		bodyMap.put("rr", System.currentTimeMillis() / 1000 * -1);
		Map<String, String> params = new HashMap<String, String>();
		params.put("sid", Constant.sign.wxsid);
		params.put("lang", "zh_CN");
		params.put("skey", Constant.sign.skey);
		params.put("pass_ticket", Constant.sign.pass_ticket);
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
								WeChatUtil.loadGroups(conItem, groups, this);
								Display.getDefault().asyncExec(new Runnable() {
									public void run() {
										lists.get(conItem).flush();
									}
								});
								syncGroup = true;
							}
						}else if(1 == MsgType || 3 == MsgType || 47 == MsgType) {
							if(Constant.FILTER_USERS.contains(FromUserName)) {
								System.out.println("忽略特殊用户信息！！" + Content);
							}else if(FromUserName.equals(Constant.user.UserName)){
								ChatLog log = ChatLog.fromMap(msg);
								if(null != log) {
									ChatLogCache.saveLogs(ToUserName, log);
									flushChatView(ToUserName, false );
									System.out.println("来自手机端自己的消息：" + Content);
								}
								
							}else if(FromUserName.startsWith("@@")) {
								ChatLog log = ChatLog.fromMap(msg);
								if(null != log) {
									ChatLogCache.saveLogs(FromUserName, log);
//									String[] splt = Content.split(":<br/>");
//									String sender = ContactsStruct.getGroupMember(splt[0], Constant.contacts.get(FromUserName));
//									String ctt = splt[1].replace("<br/>", "\n");
//									if(ctt.contains("@" + Constant.user.NickName)) {
//										String detail = ctt.replace("@" + Constant.user.NickName, "");
//										String reply = "什么情况?";
//										if(!detail.trim().isEmpty()) {
//											reply = AutoReply.call(detail, sender);
//										}
//										
//										ChatLog replyLog = WeChatUtil.sendMsg(reply, FromUserName);
//										if(null != replyLog) {
//											ChatLogCache.saveLogs(FromUserName, replyLog);
//										}
//									}
									flushChatView(FromUserName, true);
								}
								
								
							}else {
								ChatLog log = ChatLog.fromMap(msg);
								if(null != log) {
									ChatLogCache.saveLogs(FromUserName, log);
									String sender = ContactsStruct.getContactName(Constant.contacts.get(FromUserName));
									String ctt = Content.replace("<br/>", "\n");
									System.out.println(sender + " 说：" + ctt);
//									if(!Constant.noReply.contains(FromUserName)) {
//										String reply = AutoReply.call(ctt, sender);
//										ChatLog replyLog = WeChatUtil.sendMsg(reply, FromUserName);
//										if(null != replyLog) {
//											ChatLogCache.saveLogs(FromUserName, replyLog);
//										}
//										
//									}
									flushChatView(FromUserName, true);
								}
								
							}
						}
					}
				}
			}
			Map<String, Object> SyncKey = (Map<String, Object>) rst.get("SyncKey");
			WeChatUtil.flushSyncKey(SyncKey);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 用途：添加会话
	 * @date 2016年12月30日
	 * @param convs 会话对象属性
	 * @return
	 */
	public ConvItem addConversition(ContactsStruct convs) {
		if(null == convs) {
			return null;
		}
		String headUrl = Constant.BASE_URL + convs.HeadImgUrl;
		convs.head = ImageCache.getUserHeadCache(convs.UserName, headUrl, null, 50, 50);
		String nick = convs.NickName;
		String remark = convs.RemarkName;
		String name = (null == remark || remark.trim().isEmpty()) ? nick : remark; 
		System.out.println("load conver " + name);
		Integer Statues = convs.Statues;
		Integer ContactFlag = convs.ContactFlag;
		boolean top = ContactFlag == 2051;
		List<ConvItem> items = convers.getItems();
		for(ConvItem item : items) {
			if(item.getData().UserName.equals(convs.UserName)) {
				return item;
			}
		}
		ConvItem ci = new ConvItem(convs, name, null, null, null, top, Statues == 0, 0);
		if(top) {
			convers.addItem(0, ci);
		} else {
			convers.addItem(ci);
		}
		return ci;
	}
	
	/**
	 * 用途：刷新聊天界面
	 * @date 2016年12月30日
	 * @param conv
	 * @param flush
	 */
	private void flushChatView (final String conv, final boolean flush) {
		Display.getDefault().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				List<ConvItem> items = convers.getItems();
				ConvItem ci = null;
				for(ConvItem temp : items) {
					if(temp.getData().UserName.equals(conv)) {
						ci = temp;
						break;
					}
				}
				//此时没有在会话列表找到需要从好友中创建新的
				if(null == ci) {
					ContactsStruct struct = Constant.contacts.get(conv);
					ci = addConversition(struct);
				}
				ConvItem itm = convers.changeItemIndex(ci, 0);
				if(null != itm) {//这时候还找不到的话，那就真的跪了，目测是新好友
					if(itm.equals(convers.getSelection())) {
						cc.flush(itm);
					} else {
						itm.incrUnread();
					}
					List<ChatLog> logs = ChatLogCache.getLogs(conv);
					if(null != logs && !logs.isEmpty()) {
						ChatLog log = logs.get(logs.size() - 1);
						itm.setLastMsg(log.content);
						itm.setLastTime(log.createTime);
					}
				}
				convers.flush();
				//来自自己的消息不闪烁
				if(flush) {
					User32 user32 = User32.INSTANCE;
					HANDLE handle = new HANDLE();
					handle.setPointer(Pointer.createConstant(shell.handle));//获取窗口句柄
					FLASHWINFO info = new FLASHWINFO();
					info.hWnd = handle;
					info.dwFlags = User32.FLASHW_TRAY | User32.FLASHW_TIMERNOFG;//闪烁直到窗口前端显示
					info.dwTimeout = 0;
					info.uCount = 100000;
					user32.FlashWindowEx(info);//闪烁窗口
				}
				return ;
			
			}
		});
		
	}
	
}
