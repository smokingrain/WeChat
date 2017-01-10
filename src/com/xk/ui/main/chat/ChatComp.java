package com.xk.ui.main.chat;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.wb.swt.SWTResourceManager;

import com.xk.bean.ContactsStruct;
import com.xk.bean.MemberStruct;
import com.xk.chatlogs.ChatLog;
import com.xk.chatlogs.ChatLogCache;
import com.xk.ui.items.ConvItem;
import com.xk.uiLib.MyList;
import com.xk.utils.Constant;
import com.xk.utils.ImageCache;
import com.xk.utils.ImojCache;
import com.xk.utils.SWTTools;
import com.xk.utils.WeChatUtil;

import org.eclipse.swt.widgets.Label;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;

/**
 * 用途：聊天面板
 *
 * @author xiaokui
 * @date 2017年1月4日
 */
public class ChatComp extends Composite {

	private CLabel nameL;
	private MyList<ChatItem> chatList;
	private Text text;
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
		
		//聊天会话对象名字
		nameL = new CLabel(this, SWT.CENTER);
		nameL.setFont(SWTResourceManager.getFont("微软雅黑", 11, SWT.NORMAL));
		nameL.setBackground(getBackground());
		nameL.setAlignment(SWT.CENTER);
		nameL.setBounds(0, 0, 470, 49);
		SWTTools.enableTrag(nameL);
		
		//聊天记录内容
		chatList = new MyList<ChatItem>(this, 550, 350);
		chatList.setLocation(0, 50);
		
		Image tempEmoj = SWTResourceManager.getImage(ChatComp.class, "/images/emoj.png");
		//发送表情按钮
		CLabel emojL = new CLabel(this, SWT.CENTER);
		emojL.setBounds(0, 400, 30, 30);
		emojL.setBackground(SWTTools.scaleImage(tempEmoj.getImageData(), 30, 30));
		emojL.setToolTipText("发送表情");
		
		Image tempPic = SWTResourceManager.getImage(ChatComp.class, "/images/select.png");
		//发送图片按钮
		CLabel picL = new CLabel(this, SWT.CENTER);
		picL.setBounds(32, 400, 30, 30);
		picL.setBackground(tempPic);
		picL.setToolTipText("发送图片");
		picL.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseUp(MouseEvent mouseevent) {
				sendImage();
			}
			
		});
		
		//内容输入框
		text = new Text(this, SWT.MULTI);
		text.setBounds(0, 430, 549, 115);
		text.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				if(e.keyCode == SWT.CR || e.keyCode == 16777296) {
					sendMsg();
					e.doit = false;
				}
			}
			
		});
		
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
			fd.setFilterExtensions(new String[]{"*.png;*.jpg;*.bmp;*.gif"});
			fd.setFilterNames(new String[]{"图片"});
			String path = fd.open();
			if(null != path) {
				File file = new File(path);
				ChatLog log = WeChatUtil.sendImg(file, convId);
				if(null != log) {
					ChatLogCache.saveLogs(convId, log);
					flush(item);
				}
			}
		}
	}
	
	/**
	 * 发送文本消息
	 * 用途：
	 * @date 2017年1月5日
	 */
	private void sendMsg() {
		String msg = text.getText().trim();
		if(!"".equals(msg) && null != convId) {
			ChatLog log = WeChatUtil.sendMsg(msg, convId);
			ChatLogCache.saveLogs(convId, log);
			text.setText("");
			flush(item);
		}
		text.setFocus();
	}
	
	/**
	 * 用途：刷新界面
	 * @date 2016年12月23日
	 * @param item
	 */
	public void flush(final ConvItem item) {
		if(null == item) {
			return;
		}
		item.clearUnread();
		convId = item.getData().UserName;
		this.item = item;
		chatList.clearAll();
		List<ChatLog> logs = ChatLogCache.getLogs(convId);
		if(null != logs) {
			for(ChatLog log : logs) {
				String user = log.fromId;
				boolean fromSelf = user.equals(Constant.user.UserName);
				Image head = null;
				if(convId.startsWith("@@")) {
					ContactsStruct struct = Constant.contacts.get(convId);
					for(MemberStruct ms : struct.MemberList) {
						if(log.fromId.equals(ms.UserName)) {
							Map<String, String> params = new HashMap<String, String>();
							params.put("seq", "0");
							params.put("username", ms.UserName);
							params.put("chatroomid", struct.EncryChatRoomId);
							params.put("skey", Constant.sign.skey);
							head = ImageCache.getUserHeadCache(log.fromId, Constant.GET_MEMBER_ICON, params, 50, 50);
							break;
						}
					}
					user = ContactsStruct.getGroupMember(log.fromId, Constant.contacts.get(convId));
				}else if(Constant.user.UserName.equals(user)){
					head = ImageCache.getUserHeadCache(user, Constant.user.HeadImgUrl, null, 50, 50);
					user = Constant.user.NickName;
				}else {
					head = ImageCache.getUserHeadCache(user, Constant.contacts.get(user).HeadImgUrl, null, 50, 50);
					user = ContactsStruct.getContactName(Constant.contacts.get(log.fromId));
				}
				List<Object> chatContent = new ArrayList<>();
				if(3 == log.msgType || 47 == log.msgType) {
					if(null != log.img) {
						chatContent.add(log.img);
					}else {
						chatContent.add(log.content);
					}
				}else {
					boolean hasImoj = false;
					String reg = "\\[(\\w+|[\u4E00-\u9FA5]+)\\]";
					Pattern patternNode = Pattern.compile(reg);
					Matcher matcherNode = patternNode.matcher(log.content);
					String[] splt = log.content.split(reg);
					int index = 0;
					while (matcherNode.find()) {
						hasImoj = true;
						if(index < splt.length) {
							chatContent.add(splt[index++]);
						}
						String match = matcherNode.group();
						chatContent.add(getContent(match));
					}
					
					if(hasImoj) {
						for(int i = index ;i < splt.length ; i++) {
							chatContent.add(splt[i]);
						}
					}else {
						chatContent.add(log.content);
					}
					
					
				}
				
				
				ChatItem ci = new ChatItem(user, head, chatContent, fromSelf, SWTResourceManager.getFont("楷体", 12, SWT.NORMAL));
				chatList.addItem(ci);
			}
		}
		
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if(null != item.getData().MemberCount && item.getData().MemberCount > 0){
					nameL.setText(item.getName() + "(" + item.getData().MemberCount + ")");
				}else {
					nameL.setText(item.getName());
				}
				
				chatList.scrollToBottom();
				chatList.flush();
			}
		});
	}

	private Object getContent(String content) {
		if(null == content) {
			return "";
		}
		String name = content.replace("[", "").replace("]", "");
		Image img = ImojCache.qqface.get(name);
		return null == img ? content : img;
	}
	
	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
}
