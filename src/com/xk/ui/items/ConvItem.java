package com.xk.ui.items;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.wb.swt.SWTResourceManager;

import com.xk.bean.ContactsStruct;
import com.xk.uiLib.ListItem;
import com.xk.uiLib.MyList;
import com.xk.utils.Constant;
import com.xk.utils.DateUtil;
import com.xk.utils.FileUtils;

/**
 * 用途：会话单元格
 *
 * @author xiaokui
 * @date 2017年1月3日
 */
public class ConvItem extends ListItem {

	private ContactsStruct data;
	private String name;//会话名
	private String lastChat;//上一个发言人
	private String lastMsg;//上一条消息
	private Long lastTime;//上一条消息的时间
	private boolean top;//是否置顶
	private boolean silence;//是否不提示消息数
	private Integer unread = 0;//未读数
	private Image headDefault = SWTResourceManager.getImage(ConvItem.class, "/images/head.png");
	private Image silenceImage = SWTResourceManager.getImage(ConvItem.class, "/images/silence.png");
	private Image topImage = SWTResourceManager.getImage(ConvItem.class, "/images/top.png");
	
	
	public ConvItem(ContactsStruct data, String name, String lastChat, String lastMsg, Long lastTime,
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
		if(selected || focused) {
			int alf=gc.getAlpha();
			Color bk = gc.getBackground();
			gc.setBackground(SWTResourceManager.getColor(136, 136, 136));
			gc.setAlpha(selected ? 155 : 65);
			gc.fillRectangle(0, start, width-MyList.BAR_WIDTH, getHeight());
			gc.setAlpha(alf);
			gc.setBackground(bk);
		}
		Font font=SWTResourceManager.getFont("宋体", 10, SWT.NORMAL);
		gc.drawImage((null == data.head || data.head.isDisposed()) ? headDefault : data.head, 15, start + 7);
		Path path=new Path(null);
		path.addString(FileUtils.getLimitString(name, 10), 15 + 58f, start + 15f, font);
		if(top) {
			gc.drawImage(topImage, width - MyList.BAR_WIDTH - 35, start + 37);
		}else if(silence){
			gc.drawImage(silenceImage, width - MyList.BAR_WIDTH - 35, start + 37);
		}
		if(null != lastMsg){
			Color fg = gc.getBackground();
			gc.setBackground(SWTResourceManager.getColor(0xBD, 0xBD, 0xBD));
			Path temp = new Path(null);
			temp.addString(FileUtils.getLimitString(FileUtils.getLimitString(lastMsg, 7), 10), 15 + 58f, start + 37F, font);
			gc.fillPath(temp);
			gc.setBackground(fg);
			temp.dispose();
		}
		if(null != lastTime) {
			path.addString(DateUtil.getChatTime(lastTime), width - MyList.BAR_WIDTH - 50f, start + 15f, font);
		}
		gc.setBackground(SWTResourceManager.getColor(0x11, 0x11, 0x11));
		gc.fillPath(path);
		path.dispose();
		if(unread > 0) {
			Color bk = gc.getBackground();
			Color fo = gc.getForeground();
			Color outer = SWTResourceManager.getColor(0XFE, 0X01, 0X01);
			Color inner = SWTResourceManager.getColor(0XFE, 0XFE, 0XFE);
			gc.setBackground(outer);
			gc.fillOval(15 + 25 + 15, start + 3, 16, 16);
			gc.setForeground(inner);
			Path numPath = new Path(null);
			numPath.addString(unread + "", 15 + 25 + 20, start + 4, font);
			gc.drawPath(numPath);
			numPath.dispose();
			gc.setBackground(bk);
			gc.setForeground(fo);
		}
		
	}

	@Override
	public boolean oncliek(MouseEvent e, int itemHeight, int index) {
		if(e.button==3){
			Menu m=new Menu(getParent());
			Menu menu=getParent().getMenu();
			if (menu != null) {
				menu.dispose();
			}
			
			if(Constant.noReply.contains(data.UserName)) {
				MenuItem noReply=new MenuItem(m, SWT.NONE);
				noReply.setText("启用自动回复");
				noReply.addSelectionListener(new SelectionAdapter() {

					@Override
					public void widgetSelected(SelectionEvent arg0) {
						if(!data.UserName.startsWith("@@")) {
							Constant.noReply.remove(data.UserName);
						}
					}
					
				});
			}else {
				MenuItem noReply=new MenuItem(m, SWT.NONE);
				noReply.setText("禁用自动回复");
				noReply.addSelectionListener(new SelectionAdapter() {

					@Override
					public void widgetSelected(SelectionEvent arg0) {
						if(!data.UserName.startsWith("@@")) {
							Constant.noReply.add(data.UserName);
						}
					}
					
				});
			}
			
			
			getParent().setMenu(m);
			m.setVisible(true);
		}
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

	public Long getLastTime() {
		return lastTime;
	}

	public void setLastTime(Long lastTime) {
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

	public void incrUnread() {
		unread++;
	}
	
	public void clearUnread() {
		unread = 0;
	}
}
