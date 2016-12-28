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
import com.xk.utils.SWTTools;
import com.xk.utils.WeChatUtil;

import org.eclipse.swt.widgets.Label;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
		
		nameL = new CLabel(this, SWT.CENTER);
		nameL.setFont(SWTResourceManager.getFont("微软雅黑", 11, SWT.NORMAL));
		nameL.setBackground(getBackground());
		nameL.setAlignment(SWT.CENTER);
		nameL.setBounds(0, 0, 470, 49);
		SWTTools.enableTrag(nameL);
		
		chatList = new MyList<ChatItem>(this, 550, 350);
		chatList.setLocation(0, 50);
		
		Image temp = SWTResourceManager.getImage(ChatComp.class, "/images/emoj.png");
		
		CLabel emojL = new CLabel(this, SWT.CENTER);
		emojL.setBounds(0, 400, 30, 30);
		emojL.setBackground(SWTTools.scaleImage(temp.getImageData(), 30, 30));
		
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
					head = ImageCache.getUserHeadCache(user, Constant.contacts.get(user).HeadImgUrl, null, 50, 50);
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
					chatContent.add(log.content);
					
				}
				
				
				ChatItem ci = new ChatItem(user, head, chatContent, fromSelf, SWTResourceManager.getFont("楷体", 12, SWT.NORMAL));
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

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
}
