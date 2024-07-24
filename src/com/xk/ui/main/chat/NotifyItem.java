package com.xk.ui.main.chat;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.graphics.Point;
import org.eclipse.wb.swt.SWTResourceManager;
import org.jsoup.helper.StringUtil;

import com.xk.bean.IMessageNode;
import com.xk.bean.StringNode;
import com.xk.chatlogs.ChatLog;
import com.xk.ui.items.ContactItem;
import com.xk.utils.Constant;
import com.xk.utils.FileUtils;
import com.xk.utils.ImojCache;


public class NotifyItem extends ChatItem {

	private ChatLog log;
	private Integer height = -1;
	private List<StringNode> content;
	private int left = 10;
	
	public NotifyItem(ChatLog log) {
		this.log = log;
	}
	
	public NotifyItem(String user, Image head, List<IMessageNode> chatContent,
			boolean fromSelf, Font font, ChatLog log) {
		super(user, head, chatContent, fromSelf, font, log);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public int getHeight() {
		if(null == log || StringUtil.isBlank(FileUtils.getLimitString(log.content, 20))) {
			return 0;
		}
		if(height < 0) {
			content = ImojCache.computeNode(FileUtils.getLimitString(log.content, 20));
			GC gc = new GC(getParent());
			Point size = StringNode.textExtent(content, StringNode.DRAW_FLAGS, gc);
			height = 22 + size.y;
			gc.dispose();
		}
		
		return height;
	}
	
	
	@Override
	public void draw(GC gc, int start, int width, int index) {
		if(null == log || StringUtil.isBlank(log.content)) {
			return;
		}
		Point size = StringNode.textExtent(content, StringNode.DRAW_FLAGS, gc);
		int timeLen = size.x;
		int boxLen = Math.min(timeLen, ChatItem.ITEM_AREA_WIDTH) + 10 * 2;
		int alf=gc.getAlpha();
		Color bk = gc.getBackground();
		gc.setBackground(SWTResourceManager.getColor(111, 111, 111));
		gc.setAlpha(111);
		gc.fillRoundRectangle((width - boxLen) / 2, start + 7, boxLen, getHeight() - 14, 20, 20);
		gc.setAlpha(alf);
		gc.setBackground(bk);
		Image icons = SWTResourceManager.getImage(ContactItem.class, "/images/icons.png");
		Point cur = new Point((width - boxLen) / 2 + left, start + 11);
		int maxHeight = 0;
		Path path = new Path(null);
		for(StringNode node : content) {
			if(node.type == 0) {
				Point baseSize = gc.textExtent(node.base, StringNode.DRAW_FLAGS);
				maxHeight = Math.max(maxHeight, baseSize.y);
				if(baseSize.x + cur.x > ChatItem.ITEM_AREA_WIDTH) {
					cur.x = (width - boxLen) / 2 + left;
					cur.y += maxHeight + 1;
					maxHeight = 0;
				}
				path.addString(node.base, cur.x, cur.y, gc.getFont());
				cur.x += baseSize.x;
			} else {
				maxHeight = Math.max(maxHeight, 20);
				if(cur.x + 20 > ChatItem.ITEM_AREA_WIDTH) {
					cur.x = (width - boxLen) / 2 + left;
					cur.y += maxHeight + 1;
					maxHeight = 0;
				}
				gc.drawImage(icons, 0, ImojCache.computeLoc(node.base).y, 20, 20, cur.x, cur.y, 20, 20);
				cur.x += 20 + 1;
			}
		}
		gc.fillPath(path);
		path.dispose();
		
	}
	
	@Override
	public boolean oncliek(MouseEvent e, int itemHeight, int index, int type) {
		// TODO Auto-generated method stub
		return false;
	}
}
