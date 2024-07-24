package com.xk.ui.main.chat;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.wb.swt.GifTransfer;
import org.eclipse.wb.swt.SWTResourceManager;

import com.sun.jna.platform.win32.WinUser.MSG;
import com.xk.bean.ContactsStruct;
import com.xk.bean.IMessageNode;
import com.xk.bean.ImageNode;
import com.xk.bean.MemberStruct;
import com.xk.bean.ImageNode.TYPE;
import com.xk.chatlogs.ChatLog;
import com.xk.chatlogs.ChatLogCache;
import com.xk.hook.HotKeyListener;
import com.xk.hook.HotKeys;
import com.xk.ui.items.ConvItem;
import com.xk.ui.main.CutScreen;
import com.xk.ui.main.MainWindow;
import com.xk.uiLib.ICallback;
import com.xk.uiLib.MyList;
import com.xk.uiLib.XLabel;
import com.xk.uiLib.listeners.ItemEvent;
import com.xk.uiLib.listeners.ItemListener;
import com.xk.utils.Constant;
import com.xk.utils.FileUtils;
import com.xk.utils.ImageCache;
import com.xk.utils.ImojCache;
import com.xk.utils.SWTTools;
import com.xk.utils.WeChatUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.PaintObjectEvent;
import org.eclipse.swt.custom.PaintObjectListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.StyledTextUtils;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.jsoup.helper.StringUtil;

/**
 * 用途：聊天面板
 *
 * @author xiaokui
 * @date 2017年1月4日
 */
public class ChatComp extends Composite implements HotKeyListener{

	private XLabel nameL;
	private MyList<ChatItem> chatList;
	private StyledText text;
	private ConvItem item;
	private CLabel lbls;
	private String convId;//会话id
	
	
	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public ChatComp(Composite parent, int style) {
		super(parent, style);
		setLocation(300,0);
		setSize(550, 590);
		setBackground(SWTResourceManager.getColor(245, 245, 245));
		addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent arg0) {
				HotKeys keys = HotKeys.getInstance();
				keys.unregister();
				keys.remove(this);
			}
		});
		
		
		//聊天会话对象名字
		nameL = new XLabel(this, SWT.CENTER);
		nameL.setFont(SWTResourceManager.getFont("微软雅黑", 11, SWT.NORMAL));
		nameL.setBackground(getBackground());
		nameL.setAlignment(SWT.CENTER);
		nameL.setBounds(0, 0, 470, 49);
		SWTTools.enableTrag(nameL);
		
		//聊天记录内容
		chatList = new MyList<ChatItem>(this, 550, 350);
		chatList.setLocation(0, 50);
//		chatList.setItemLimit(1500);
		
		DropTarget dropTarget = new DropTarget(chatList, DND.DROP_NONE);
		dropTarget.setTransfer(new Transfer[]{FileTransfer.getInstance()});
		dropTarget.addDropListener(new DropTargetAdapter(){

			@Override
			public void drop(DropTargetEvent event) {
				if(null == convId) {
					return;
				}
				String[] pathes = (String[]) event.data;
				for(String path : pathes) {
					sendFile(path);
				}
				flush(item);
			}
			
		});
		
		Image tempPic = SWTResourceManager.getImage(ChatComp.class, "/images/select.png");
		Image tempEmoj = SWTResourceManager.getImage(ChatComp.class, "/images/emoj.png");
		Image cutPic = SWTResourceManager.getImage(ChatComp.class, "/images/cutscreen.png");
		//发送表情按钮
		final CLabel emojL = new CLabel(this, SWT.CENTER);
		emojL.setBounds(5, 405, 19, 19);
		emojL.setBackground(tempEmoj);
		emojL.setToolTipText("发送表情");
		emojL.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseUp(MouseEvent mouseevent) {
				Point loc = emojL.toDisplay(0, 0);
				loc.x -= 30 * 15 / 2;
				loc.y -= 215;
				ImojWindow fw = ImojWindow.getInstance();
				fw.setCc(ChatComp.this);
				fw.init(loc.x, loc.y);
				fw.shell.setSize(30 * 15 + MyList.BAR_WIDTH + 54, 215);
				fw.setTimeOut(-1L);
				fw.open(-1, -1);
				
				
			}
		});
		
		//发送图片按钮
		CLabel picL = new CLabel(this, SWT.CENTER);
		picL.setBounds(32, 406, 19, 17);
		picL.setBackground(tempPic);
		picL.setToolTipText("发送图片");
		picL.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseUp(MouseEvent mouseevent) {
				sendImage();
			}
			
		});
		
		//截图按钮
		CLabel cutScreen = new CLabel(this, SWT.CENTER);
		cutScreen.setBounds(60, 406, 15, 18);
		cutScreen.setBackground(cutPic);
		cutScreen.setToolTipText("屏幕截图(CTRL + J)");
		cutScreen.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseUp(MouseEvent mouseevent) {
				cutScreen();
			}
			
		});
		
		//内容输入框
		text = new StyledText(this, SWT.MULTI | SWT.V_SCROLL);
//		text.setBackground(SWTResourceManager.getColor(0xF5, 0xFF, 0xFA));
		text.setBounds(0, 430, 549, 115);
		text.setAlignment(SWT.CENTER);
		
		text.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseUp(MouseEvent e) {
				if(e.button == 3) {
					Menu m=new Menu(getParent());
					Menu menu=getParent().getMenu();
					if (menu != null) {
						menu.dispose();
					}
					MenuItem cut = new MenuItem(m, SWT.NONE);//剪切菜单
					cut.setText("剪切");
					cut.addSelectionListener(new SelectionAdapter() {

						@Override
						public void widgetSelected(SelectionEvent e) {
							StyledTextUtils.cut(text);
						}
					});
					cut.setEnabled(!text.getText().trim().isEmpty());
					
					MenuItem copy = new MenuItem(m, SWT.NONE);//复制菜单
					copy.setText("复制");
					copy.addSelectionListener(new SelectionAdapter() {
						
						@Override
						public void widgetSelected(SelectionEvent e) {
							StyledTextUtils.copy(text);
						}
					});
					copy.setEnabled(!text.getText().trim().isEmpty());
					
					MenuItem pause = new MenuItem(m, SWT.NONE);//粘贴菜单
					pause.setText("粘贴");
					pause.addSelectionListener(new SelectionAdapter() {
						
						@Override
						public void widgetSelected(SelectionEvent e) {
							StyledTextUtils.pause(text);
						}
					});
					
					
					getParent().setMenu(m);
					m.setVisible(true);
				}
			}
			
		});
		
		//发送消息shift+回车
		text.addVerifyKeyListener(new VerifyKeyListener() {
			
			@Override
			public void verifyKey(VerifyEvent event) {
				
				if((event.keyCode == SWT.CR || event.keyCode == 16777296) && (event.stateMask & SWT.MODIFIER_MASK)==SWT.SHIFT) {
					sendMsg();
					event.doit = false;
					return;
				}
				if(event.keyCode == 'a' && (event.stateMask & SWT.MODIFIER_MASK) == SWT.CTRL) {
					event.doit = false;
					text.selectAll();
					return;
				}
				if(event.keyCode == 'v' && (event.stateMask & SWT.MODIFIER_MASK) == SWT.CTRL) {
					event.doit = false;
					StyledTextUtils.pause(text);
					return;
				}
				if(event.keyCode == 'c' && (event.stateMask & SWT.MODIFIER_MASK) == SWT.CTRL) {
					event.doit = false;
					StyledTextUtils.copy(text);
					return;
				}
				if(event.keyCode == 'x' && (event.stateMask & SWT.MODIFIER_MASK) == SWT.CTRL) {
					event.doit = false;
					StyledTextUtils.cut(text);
					return;
				}
			}
		});
		
		StyledTextUtils.enablePictures(text);
		
		//发送按钮
		lbls = new CLabel(this, SWT.CENTER);
		lbls.setCursor(SWTResourceManager.getCursor(SWT.CURSOR_HAND));
		lbls.setBounds(479, 551, 60, 29);
		lbls.setText("发送(S)");
		lbls.setBackground(getBackground());
		lbls.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseUp(MouseEvent paramMouseEvent) {
				sendMsg();
			}
			
			
		});
		//绘制边框
		lbls.addPaintListener(new PaintListener() {
			
			@Override
			public void paintControl(PaintEvent paramPaintEvent) {
				GC gc = paramPaintEvent.gc;
				gc.setAdvanced(true);
				gc.setAntialias(SWT.ON);
				gc.setAlpha(55);
				gc.drawRoundRectangle(1, 1, 58, 27, 3, 3);
				gc.dispose();
			}
		});
		
		chatList.addItemListener(new ItemListener<ChatItem>() {
			
			@Override
			public void itemRemove(ItemEvent<ChatItem> e) {
				ChatLogCache.removeLog(convId, e.item.getLog());
				flush(item);
			}
		});
		
		clearTemp();
		registerHotKey();
	}
	
	
	/**
	 * 清理上一次启动后临时文件
	 * 作者 ：肖逵
	 * 时间 ：2021年1月25日 上午10:51:18
	 */
	private void clearTemp() {
		File file = new File("temp");
		if(file.exists() && file.isDirectory()) {
			File[] tempFiles = file.listFiles();
			for(File temp : tempFiles) {
				temp.delete();
			}
		}
		
	}
	
	/**
	 * 发送图片
	 * 用途：
	 * @date 2017年1月5日
	 */
	private void sendImage() {
		if(null != convId) {
			FileDialog fd = new FileDialog(getShell());
			fd.setText("选择图片");
			fd.setFilterExtensions(new String[]{"*.png;*.jpg;*.jpeg;*.bmp;*.gif"});
			fd.setFilterNames(new String[]{"图片"});
			String path = fd.open();
			if(null != path) {
				File file = new File(path);
				ChatLog log = ChatLog.createImageLog(file, convId);
				sendLog(log, false);
				flush(item);
			}
		}
	}
	
	/**
	 * 发送文本消息
	 * 用途：
	 * @date 2017年1月5日
	 */
	private void sendMsg() {
		String msg = text.getText();
		if(sendText(msg)) {
			text.setText("");
		}
 	}
	
	/**
	 * 创建文件发送进度回调
	 * 作者 ：肖逵
	 * 时间 ：2018年9月2日 下午8:12:59
	 * @param log
	 * @return
	 */
	private ICallback<Long> createCount(final ChatLog log) {
		ICallback<Long> callBack = new ICallback<Long>() {
			private long count = 0;
			@Override
			public Long callback(Long obj) {
				if(null == obj || !convId.equals(ChatComp.this.convId)) {
					return null;
				}
				count += obj;
				return count;
			}
			
		};
		return callBack;
	}
	
	/**
	 * 创建图片发送进度回调
	 * 作者 ：肖逵
	 * 时间 ：2018年9月2日 下午8:11:55
	 * @param log
	 * @return
	 */
	private ICallback<Long> createProcess(final ChatLog log) {
		ICallback<Long> callBack = new ICallback<Long>() {
			private String convId = ChatComp.this.convId;
			double length = log.file.length();
			private long total = 0;
			@Override
			public Long callback(Long obj) {
				if(null == obj || !convId.equals(ChatComp.this.convId)) {
					return null;
				}
				total += obj;
				int persent =((Double)(total / length * 100)).intValue();
				if(persent != log.persent) {
					log.persent = persent;
					Display.getDefault().asyncExec(new Runnable() {
						
						@Override
						public void run() {
							chatList.flush();
						}
					});
					
				}
				return null;
			}
		};
		return callBack;
	}
	
	/**
	 * 消息发送完毕回调
	 * 作者 ：肖逵
	 * 时间 ：2018年9月2日 下午8:13:19
	 * @param log
	 * @param del 临时文件需要发送完毕就删掉
	 */
	private void sendLog(final ChatLog log, final boolean del) {
		ChatLogCache.saveLogs(convId, log);
		ICallback callBack = new ICallback() {
			private String convId = ChatComp.this.convId;
			@Override
			public Object callback(Object obj) {
				if(null != obj && convId.equals(ChatComp.this.convId)) {
					log.sent = true;
					if(del) {
						log.file.delete();
					}
				}
				return null;
			}
		};
		WeChatUtil.sendLog(log, 3 == log.msgType ? createProcess(log) : createCount(log), callBack);
		MainWindow.getInstance().flushChatView(convId, false);
	}
	
	/**
	 * 发送文件
	 * 作者 ：肖逵
	 * 时间 ：2018年9月2日 下午8:14:46
	 * @param path
	 * @return
	 */
	private boolean sendFile(String path) {
		boolean sent = false;
		if(!"".equals(path) && null != convId) {
			File file = new File(path);
			String ext = FileUtils.getFileExt(file);
			ChatLog log = Constant.imgTypes.keySet().contains(ext) ? ChatLog.createImageLog(file, convId) : ChatLog.createFileLog(file, convId);
			sendLog(log, false);
			sent = true;
		}
		return sent;
	}
	
	/**
	 * 发送消息，如果是图文，要分条发
	 * @param str
	 * @return
	 * @author kui.xiao
	 */
	public boolean sendText(String str) {
		boolean sent = false;
		if(!"".equals(str) && null != convId) {
			//找到有存储图片的节点
			int index = str.indexOf('\uFFFC');
			int lastIndex = 0;
			String msg = "";
			while (index != -1) {
				String temp = str.substring(lastIndex, index).trim();
				msg += temp;
				StyleRange style = text.getStyleRangeAtOffset(index);
				if(null == style) {
					lastIndex = index + 1;
					index = str.indexOf('\uFFFC', lastIndex);
					continue;
				}
				ImageNode image = (ImageNode)style.data;
				if (image != null) {
					if(image.type == TYPE.IMOJ) {
						msg += "[" + image.getBase() + "]";
					} else {
						if(!StringUtil.isBlank(msg)) {
							ChatLog log = ChatLog.createSimpleLog(msg, convId);
							sendLog(log, false);
							msg = "";
						}
						SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmmss");
						File file = new File("temp","shortcut" + sdf.format(new Date()) + ".jpg");
						file.getParentFile().mkdirs();
						if(null == image.getLoader()) {
							ImageLoader loader = new ImageLoader();
							loader.data = new ImageData[]{image.getImg().getImageData()};
							loader.save(file.getAbsolutePath(), SWT.IMAGE_JPEG);
						} else {
							file = new File("temp","shortcut" + sdf.format(new Date()) + Constant.FORMATS[image.getLoader().format]);
							image.getLoader().save(file.getAbsolutePath(), image.getLoader().format);
						}
						
						ChatLog imgLog = ChatLog.createImageLog(file, convId);
						sendLog(imgLog, true);
						image.getImg().dispose();
					}
				}
				lastIndex = index + 1;
				index = str.indexOf('\uFFFC', lastIndex);
			}
			if(!StringUtil.isBlank(msg) || lastIndex < str.length()) {
				if(lastIndex < str.length()) {
					msg += str.substring(lastIndex, str.length());
				}
				if(!msg.trim().isEmpty()) {
					ChatLog log = ChatLog.createSimpleLog(msg.trim(), convId);
					sendLog(log, false);
				}
			}
			flush(item);
			sent = true;
			MainWindow.getInstance().flushChatView(convId, false);
		}
		text.setFocus();
		return sent;
	}
	
	/**
	 * 调用截图类
	 * 作者 ：肖逵
	 * 时间 ：2019年9月2日 下午8:18:43
	 */
	private void cutScreen() {
		CutScreen cs = new CutScreen();
		cs.open();
		Image img = cs.img;
		if(null != img && null != convId) {
			StyledTextUtils.addImage(text, cs.img, TYPE.IMAGE);
		}
	}
	
	/**
	 * 用途：刷新界面
	 * @date 2016年12月23日
	 * @param item
	 */
	public void flush(final ConvItem item) {
		if(null == item) {
			convId = null;
			this.item = null;
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					nameL.setText(null);
					chatList.clearAll();
					chatList.scrollToBottom();
					chatList.flush();
				}
			});
			return;
		}
		
		item.clearUnread();
		convId = item.getData().UserName;
		this.item = item;
		chatList.clearAll();
		List<ChatLog> logs = ChatLogCache.getLogs(convId);
		if(item.getUnread() > 0) {
			WeChatUtil.statusNotify(Constant.user.UserName, convId);
		}
		if(null != logs) {
			long current = 0;
			long limitTime = 3 * 60 * 1000;//五分钟刷一次时间戳 
			long limitCount = 10;//或者每十条刷一次时间戳
			List<ChatLog> tempLogs = new ArrayList<ChatLog>(logs);
			for(ChatLog log : tempLogs) {
				if(10000 == log.msgType) {
					ChatItem itm = new NotifyItem(log);
					itm.setWeight(log.createTime);
					chatList.addItem(itm);
					continue;
				}
				String user = log.fromId;
				boolean fromSelf = user.equals(Constant.user.UserName);
				Image head = null;
				if(convId.startsWith("@@")) {
					ContactsStruct struct = Constant.getContact(convId);
					for(MemberStruct ms : struct.MemberList) {
						if(log.fromId.equals(ms.UserName)) {
							Map<String, String> params = new HashMap<String, String>();
							params.put("seq", "0");
							params.put("chatroomid", "0");
							params.put("username", ms.UserName);
							params.put("skey", Constant.sign.skey);
							ImageNode node = ImageCache.getUserHeadCache(log.fromId, String.format(Constant.GET_MEMBER_ICON, Constant.HOST), params);
							if(null != node) {
								head = node.getImg();
							}
							break;
						}
					}
					user = ContactsStruct.getGroupMember(log.fromId, Constant.getContact(convId));
				}else if(Constant.user.UserName.equals(user)){
					head = ImageCache.getUserHeadCache(user, Constant.user.HeadImgUrl, null).getImg();
					user = Constant.user.NickName;
				}else {
					head = ImageCache.getUserHeadCache(user, Constant.getContact(user).HeadImgUrl, null).getImg();
					user = ContactsStruct.getContactName(Constant.getContact(log.fromId));
				}
				List<IMessageNode> chatContent = new ArrayList<>();
				if(3 == log.msgType || 47 == log.msgType || 49 == log.msgType || 43 == log.msgType) {
					if(null != log.img) {
						chatContent.add(log.img);
					}else {
						chatContent.addAll(ImojCache.computeImoj(log.content.replace("<br/>", "\n").replace("\r", "").replace("&gt;", ">").replace("&lt;", "<")));
					}
				}else {
					chatContent.addAll(ImojCache.computeImoj(log.content.replace("<br/>", "\n").replace("\r", "").replace("&gt;", ">").replace("&lt;", "<")));
				}
				
				if(log.createTime - current > limitTime || ++limitCount  >= 10) {
					ChatItem time = new TimeItem(log.createTime);
					time.setWeight(log.createTime - 1);
					chatList.addItem(time);
					current = log.createTime;
					limitCount = 0;
				}
				
				ChatItem ci = null;
				//这个是创建具体
				if(log.msgType == 37) {
					ci = new AddFriendItem(user, head, chatContent, fromSelf, SWTResourceManager.getFont("楷体", 12, SWT.NORMAL), log);
				} else if(null != log.url && !"".equals(log.url)) {
					ci = new LinkItem(user, head, chatContent, fromSelf, SWTResourceManager.getFont("楷体", 12, SWT.NORMAL), log);
				} else if(43 == log.msgType) {
					ci = new VideoItem(user, head, chatContent, fromSelf, SWTResourceManager.getFont("楷体", 12, SWT.NORMAL), log);
				} else if(34 == log.msgType) {
					ci = new VoiceItem(user, head, chatContent, fromSelf, SWTResourceManager.getFont("楷体", 12, SWT.NORMAL), log);
				} else{
					ci = new ChatItem(user, head, chatContent, fromSelf, SWTResourceManager.getFont("楷体", 12, SWT.NORMAL), log);
				}
				ci.setWeight(log.createTime);
				chatList.addItem(ci);
			}
		}
		
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				nameL.setText(item.getName());
				chatList.scrollToBottom();
				chatList.flush();
			}
		});
	}

	
	private void registerHotKey() {
		HotKeys keys = HotKeys.getInstance();
		keys.registerHotKey();
		keys.add(this);
	}
	
	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
	
	public StyledText getText() {
		return text;
	}

	@Override
	public void notify(MSG msg) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				cutScreen();
			}
		});
	}
}
