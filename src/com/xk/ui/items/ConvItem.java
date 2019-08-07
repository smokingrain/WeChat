package com.xk.ui.items;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.wb.swt.SWTResourceManager;

import com.xk.bean.ContactsStruct;
import com.xk.bean.StringNode;
import com.xk.chatlogs.ChatLogCache;
import com.xk.ui.main.MainWindow;
import com.xk.uiLib.ICallback;
import com.xk.uiLib.ListItem;
import com.xk.uiLib.MyList;
import com.xk.utils.Constant;
import com.xk.utils.DateUtil;
import com.xk.utils.FileUtils;
import com.xk.utils.ImageCache;
import com.xk.utils.ImojCache;
import com.xk.utils.WeChatUtil;

/**
 * 用途：会话单元格
 *
 * @author xiaokui
 * @date 2017年1月3日
 */
public class ConvItem extends ListItem {

	private ContactsStruct data;
	private List<StringNode> name;//会话名
	private String lastChat;//上一个发言人
	private String lastMsg;//上一条消息
	private Long lastTime;//上一条消息的时间
	private boolean top;//是否置顶
	private boolean silence;//是否不提示消息数
	private Integer unread = 0;//未读数
	private static Image headDefault = SWTResourceManager.getImage(ConvItem.class, "/images/head.png");
	private static Image silenceImage = SWTResourceManager.getImage(ConvItem.class, "/images/silence.png");
	private static Image topImage = SWTResourceManager.getImage(ConvItem.class, "/images/top.png");
	
	
	public ConvItem(ContactsStruct data, String name, String lastChat, String lastMsg, Long lastTime,
			boolean top, boolean silence, Integer unread) {
		super();
		this.data = data;
		this.name = ImojCache.computeNode(name);
		if(null != data.MemberCount && data.MemberCount > 0) {
			this.name.add(new StringNode(0, "(" + data.MemberCount + ")"));
		}
		this.lastChat = lastChat;
		this.lastMsg = lastMsg;
		this.lastTime = lastTime;
		this.top = top;
		this.silence = silence;
		this.unread = unread;
		ImageCache.asyncLoadPicture(data, new ICallback() {
			
			@Override
			public Object callback(Object obj) {
				Display.getDefault().asyncExec(new Runnable() {

					@Override
					public void run() {
						MyList ml = getParent();
						if(null != ml) {
							ml.flush();
						}
					}
					
				});
				return null;
			}
		});
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
		Font old = gc.getFont();
		Font font=SWTResourceManager.getFont("宋体", 10, SWT.NORMAL);
		gc.setFont(font);
		Image showHead =(null == data.head || data.head.isDisposed()) ? headDefault : data.head;
		gc.drawImage(showHead, 0, 0, showHead.getImageData().width, showHead.getImageData().height, 15, start + 7, 50, 50);
		Path path=new Path(null);
		float offset = 15 + 58f;
		Image icons = SWTResourceManager.getImage(ContactItem.class, "/images/icons.png");
		for(StringNode node : name) {
			if(node.type == 0) {
				path.addString(node.base, offset, start + 15f, font);
				offset += gc.textExtent(node.base, StringNode.DRAW_FLAGS).x + StringNode.SPACE;
			}else {
				Point loc = ImojCache.computeLoc(node.base);
				if(null != loc) {
					gc.drawImage(icons, 0, loc.y, 20, 20, (int)offset, start + 15, 20, 20);
				}
				offset += 20 + StringNode.SPACE;
			}
		}
		if(top) {
			gc.drawImage(topImage, width - MyList.BAR_WIDTH - 35, start + 37);
		}else if(silence){
			gc.drawImage(silenceImage, width - MyList.BAR_WIDTH - 35, start + 37);
		}
		if(null != lastMsg){
			Color fg = gc.getBackground();
			Color back = null;
			if(selected || focused) {
				back = SWTResourceManager.getColor(0xF4, 0xF4, 0xF4);
			} else {
				back = SWTResourceManager.getColor(0xAD, 0xAD, 0xAD);
			}
			gc.setBackground(back);
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
		gc.setFont(old);
		
	}

	@Override
	public boolean oncliek(MouseEvent e, int itemHeight, int index, int type) {
		if(e.button==3){
			Menu m=new Menu(getParent());
			Menu menu=getParent().getMenu();
			if (menu != null) {
				menu.dispose();
			}
			
			MenuItem top=new MenuItem(m, SWT.NONE);
			if(isTop()) {
				top.setText("取消置顶");
			}else {
				top.setText("会话置顶");
			}
			
			top.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					if(WeChatUtil.OPlog(data, isTop() ? 0 : 1)) {
						MainWindow.getInstance().topUser(getData(), isTop() ? 0 : 1);
					}
				}
			});
			
			MenuItem remove=new MenuItem(m, SWT.NONE);
			remove.setText("删除会话");
			remove.addSelectionListener(new SelectionAdapter() {
				
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					getParent().removeItem(ConvItem.this);
					ChatLogCache.removeConv(data.UserName);
				}
				
			});
			
			MenuItem global = new MenuItem(m, SWT.NONE);
			global.setText(Constant.globalSilence ? "启用全局回复" : "禁用全局回复");
			global.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent arg0) {
					Constant.globalSilence = !Constant.globalSilence;
				}
				
			});
			
			
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
						Constant.noReply.add(data.UserName);
//						if(!data.UserName.startsWith("@@")) {
//							
//						}
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

	public List<StringNode> getName() {
		return name;
	}

	public void setName(String name) {
		this.name = ImojCache.computeNode(name);
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
