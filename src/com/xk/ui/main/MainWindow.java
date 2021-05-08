package com.xk.ui.main;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
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
import com.xk.uiLib.AutoCombo;
import com.xk.uiLib.ListItem;
import com.xk.uiLib.MyList;
import com.xk.uiLib.MyText;
import com.xk.uiLib.listeners.ItemEvent;
import com.xk.uiLib.listeners.ItemListener;
import com.xk.uiLib.listeners.ItemSelectionEvent;
import com.xk.uiLib.listeners.ItemSelectionListener;
import com.xk.utils.Constant;
import com.xk.utils.FileUtils;
import com.xk.utils.ImageCache;
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

	protected Shell shell;
	public Map<ListItem, MyList<? extends ListItem>> lists = new HashMap<ListItem, MyList<? extends ListItem>>();
	private MyText text;
	private ChatComp cc;
	public MyList<ConvItem> convers;
	public MyList<ContactItem> contacts;
	private MyList<TypeItem> types;
	public boolean syncGroup = false;
	
	public static MainWindow getInstance() {
		return WindowHolder.instance;
	}

	private static class WindowHolder {
		private static MainWindow instance = new MainWindow();
	}
	
	private MainWindow() {
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
		shell.setImage(SWTResourceManager.getImage(MainWindow.class, "/images/wechat.png"));
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
				FloatWindow<Integer> bb = FloatWindow.getInstance();
				bb.init();
				bb.setTimeOut(2000L);
				bb.setSize(180, 255);
				MyInfoComp mic = new MyInfoComp(bb.shell, SWT.NONE, Constant.user);
				bb.add(mic);
				Integer rst = bb.open(globPos.x + e.x, globPos.y + e.y);
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
		contacts = new MyList<ContactItem>(shell, 250, 540);
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
		
		AutoCombo ac = new AutoCombo(text) {
			
			@Override
			public void onSelect(String key, ContactsStruct value) {
				ConvItem itm = addConversition(value, true);
				if(null != itm) {
					convers.select(itm, false);
				}
				types.select(0, false);
				
			}
		};
		ac.init();
		
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
		WeChatUtil.loadGroups(g);//拉取最近会话中的群组
		
		List<String> group = WeChatUtil.loadContacts();//拉取联系人
		WeChatUtil.loadGroups(group);//拉取联系人中的群组
		
		WeChatUtil.syncData();//开始发送心跳包，拉消息
		
		//加载自己的头像
		String headUrl = String.format(Constant.BASE_URL, Constant.HOST) + Constant.user.HeadImgUrl + "&type=big";
		Image temp = ImageCache.getUserHeadCache(Constant.user.UserName, headUrl, null).getImg();
		Image img = SWTTools.scaleImage(temp.getImageData(), 30, 30);
		Constant.user.head = temp;
		me.setImage(img);
	}
	
	
	/**
	 * 搜索联系人
	 */
	private void searchUser() {
//		String name = text.getText().trim();
//		if(!name.isEmpty()) {
//			for(ContactsStruct convs : Constant.contacts.values()) {
//				if((null != convs.RemarkName && convs.RemarkName.contains(name)) || (null != convs.NickName && convs.NickName.contains(name))) {
//					ConvItem itm = addConversition(convs);
//					if(null != itm) {
//						convers.select(itm, false);
//					}
//					break;
//				}
//			}
//			types.select(0, false);
//		}
		
		
	}
	
	/**
	 * 用途：置顶或取消置顶
	 * @date 2017年2月13日
	 * @param user 用户id
	 * @param type 类型 0，取消，1，置顶
	 */
	public void topUser(ContactsStruct cs, int type) {
//		ContactsStruct cs = Constant.contacts.get(user);
		ConvItem item = addConversition(cs);
		if(null == item) {
			return;
		}
		item.setTop(type == 1);
		convers.sortItem();
	}
	
	
	public ConvItem addConversition(ContactsStruct convs, boolean first) {
		if(null == convs) {
			return null;
		}
//		String headUrl = Constant.BASE_URL + convs.HeadImgUrl;
//		convs.head = ImageCache.getUserHeadCache(convs.UserName, headUrl, null, 50, 50);
		String name = ContactsStruct.getContactsStructName(convs);
		System.out.println("load conver " + name + ", " + convs.UserName);
		Integer Statues = convs.Statues;
		Integer ContactFlag = convs.ContactFlag;
		boolean top = ContactFlag == 2051 || ContactFlag == 2049;
		List<ConvItem> items = convers.getItems();
		for(ConvItem item : items) {
			if(item.getData().UserName.equals(convs.UserName)) {
				item.setActiveTime(System.currentTimeMillis());
				return item;
			}
		}
		ConvItem ci = new ConvItem(convs, name, null, null, null, top, (convs.MemberCount > 0 && Statues == 0) || (convs.MemberCount == 0 && ContactFlag == 513), 0);
		convers.addItem(0, ci);
		return ci;
	}
	
	/**
	 * 用途：添加会话
	 * @date 2016年12月30日
	 * @param convs 会话对象属性
	 * @return
	 */
	public ConvItem addConversition(ContactsStruct convs) {
		return addConversition(convs, false);
	}
	
	/**
	 * 渲染联系人，群组
	 * 
	 * @author kui.xiao
	 */
	public void showGroupsAndFriends() {
		Map<String, List<ContactsStruct>> friends = new HashMap<String, List<ContactsStruct>>();
		//先按字母分组，群组另外分开
		for(ContactsStruct convs : Constant.contacts.values()) {
			String name = ContactsStruct.getContactsStructName(convs);
			if(convs.MemberCount > 1) {
				String spell = "群组";
				WeChatUtil.computeGroup(friends, spell, convs);
			} else {
				String spell = FileUtils.getFirstSpell(name);
				if(null == spell) {
					spell = "其他";
				}
				WeChatUtil.computeGroup(friends, spell, convs);
			}
		}
		
		//先渲染群组
		renderContact("群组",friends);
		
		//找到A-Z的分组
		for(char ch = 65; ch <= 90; ch++) {
			renderContact(String.valueOf(ch), friends);
		}
		
		//再渲染其它
		renderContact("其他",friends);
		
		Display.getDefault().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				contacts.flush();
				convers.flush();
				Map<String, ContactsStruct> items = new HashMap<String, ContactsStruct>();
				items.putAll(Constant.contacts);
				text.setData("items", items);
			}
		});
		
	}
	
	/**
	 * 渲染联系人，群组
	 * @param gName
	 * @param friends
	 * @author xiaokui
	 */
	private void renderContact(String gName, Map<String, List<ContactsStruct>> friends) {
		List<ContactsStruct> friend = friends.get(gName);
		if(null != friend) {
			ContactItem ci = new ContactItem(null, true, gName);
			contacts.addItem(ci);
			for(ContactsStruct cs : friend) {
				boolean top = cs.ContactFlag == 2049 || cs.ContactFlag == 2051;
				if(top) {
					addConversition(cs);
				}
				String name = ContactsStruct.getContactsStructName(cs);
				ContactItem coni = new ContactItem(cs, false, name);
				contacts.addItem(coni);
				
			}
		}
	}
	
	/**
	 * 用途：刷新聊天界面
	 * @date 2016年12月30日
	 * @param conv
	 * @param flush
	 */
	public void flushChatView (final String conv, final boolean flush) {
		Display.getDefault().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				ContactsStruct struct = Constant.getContact(conv);
				ConvItem ci = addConversition(struct);
				//此时没有在会话列表找到需要从好友中创建新的
				if(null == ci) {
					return;
				}
				convers.sortItem();
				
				
				ConvItem itm = ci;
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
				if(flush && null != itm && !itm.isSilence()) {
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
