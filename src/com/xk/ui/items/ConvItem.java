package com.xk.ui.items;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Path;
import org.eclipse.wb.swt.SWTResourceManager;

import com.xk.bean.ContactsStruct;
import com.xk.uiLib.ListItem;
import com.xk.uiLib.MyList;
import com.xk.utils.FileUtils;

public class ConvItem extends ListItem {

	private ContactsStruct data;
	private String name;//会话名
	private String lastChat;//上一个发言人
	private String lastMsg;//上一条消息
	private String lastTime;//上一条消息的时间
	private boolean top;//是否置顶
	private boolean silence;//是否不提示消息数
	private Integer unread;//未读数
	private Image headDefault = SWTResourceManager.getImage(ConvItem.class, "/images/head.png");
	private Image silenceImage = SWTResourceManager.getImage(ConvItem.class, "/images/silence.png");
	private Image topImage = SWTResourceManager.getImage(ConvItem.class, "/images/top.png");
	
	
	public ConvItem(ContactsStruct data, String name, String lastChat, String lastMsg, String lastTime,
			boolean top, boolean silence, Integer unread) {
		super();
		this.data = data;
		this.name = name;
		this.lastChat = lastChat;
		this.lastMsg = lastMsg;
		this.lastTime = lastTime;
		this.top = top;
		this.silence = silence;
		this.unread = unread;
	}

	@Override
	public int getHeight() {
		return 65;
	}

	@Override
	public void draw(GC gc, int start, int width, int index) {
		if(selected) {
			int alf=gc.getAlpha();
			Color bk = gc.getBackground();
			gc.setBackground(SWTResourceManager.getColor(136, 136, 136));
			gc.setAlpha(155);
			gc.fillRectangle(0, start, width-MyList.BAR_WIDTH, getHeight());
			gc.setAlpha(alf);
			gc.setBackground(bk);
		}
		Font font=SWTResourceManager.getFont("宋体", 10, SWT.NORMAL);
		gc.drawImage((null == data.head || data.head.isDisposed()) ? headDefault : data.head, 15, start + 7);
		Path path=new Path(null);
		path.addString(name, 15 + 58f, start + 15f, font);
		if(top) {
			gc.drawImage(topImage, width - MyList.BAR_WIDTH - 35, start + 37);
		}else if(silence){
			gc.drawImage(silenceImage, width - MyList.BAR_WIDTH - 35, start + 37);
		}
		if(null != lastMsg && null != lastChat){
			path.addString(FileUtils.getLimitString(lastChat + ":" + lastMsg, 10), 15 + 58f, start + 37F, font);
		}
		if(null != lastTime) {
			path.addString(lastTime, width - MyList.BAR_WIDTH - 40f, start + 15f, font);
		}
		
		gc.drawPath(path);
		path.dispose();
		if(unread > 0) {
			Color bk = gc.getBackground();
			Color fo = gc.getForeground();
			Color outer = SWTResourceManager.getColor(0XFE, 0X01, 0X01);
			Color inner = SWTResourceManager.getColor(0XFE, 0XFE, 0XFE);
			gc.setForeground(outer);
			gc.fillOval(15 + 25 + 10, 3, 20, 20);
			gc.setForeground(inner);
			Path numPath = new Path(null);
			numPath.addString(unread + "", 15 + 25 + 20, 4, font);
			gc.drawPath(numPath);
			numPath.dispose();
			gc.setBackground(bk);
			gc.setForeground(fo);
		}
		
	}

	@Override
	public boolean oncliek(MouseEvent e, int itemHeight, int index) {
		return true;
	}

	public ContactsStruct getData() {
		return data;
	}

	public void setData(ContactsStruct data) {
		this.data = data;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLastChat() {
		return lastChat;
	}

	public void setLastChat(String lastChat) {
		this.lastChat = lastChat;
	}

	public String getLastMsg() {
		return lastMsg;
	}

	public void setLastMsg(String lastMsg) {
		this.lastMsg = lastMsg;
	}

	public String getLastTime() {
		return lastTime;
	}

	public void setLastTime(String lastTime) {
		this.lastTime = lastTime;
	}

	public boolean isTop() {
		return top;
	}

	public void setTop(boolean top) {
		this.top = top;
	}

	public boolean isSilence() {
		return silence;
	}

	public void setSilence(boolean silence) {
		this.silence = silence;
	}

	public Integer getUnread() {
		return unread;
	}

	public void setUnread(Integer unread) {
		this.unread = unread;
	}

}
