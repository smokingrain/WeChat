package com.xk.ui.main.chat;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.wb.swt.SWTResourceManager;

import com.xk.bean.IMessageNode;
import com.xk.bean.StringNode;
import com.xk.chatlogs.ChatLog;
import com.xk.uiLib.MyList;
import com.xk.utils.FileUtils;
import com.xk.utils.WeChatUtil;

/**
 * 添加朋友的消息
 * @author Administrator
 *
 */
public class AddFriendItem extends ChatItem {

	
	public AddFriendItem (String user,Image head, List<IMessageNode> chatContent, boolean fromSelf, Font font, ChatLog log) {
		super(user, head, chatContent, fromSelf, font, log);
	}

	@Override
	public int getHeight() {
		super.getHeight();
		maxWidth = 300;
		return 130;
	}

	@Override
	protected void drawContentL(GC gc, int start, int width) {
		if(null != log.img) {
			ImageData id = log.img.getImg().getImageData();
			gc.drawImage(log.img.getImg(), 0, 0, id.width, id.height, HEAD_IMG_HEIGHT + LINE_SPACE_HEIGHT * 5 + MARGIN, start + nameHeight + LINE_SPACE_HEIGHT * 4 + MARGIN * 6, HEAD_IMG_HEIGHT, HEAD_IMG_HEIGHT);
		}
		
		Path titlePath = new Path(null);
		titlePath.addString(FileUtils.getLimitString(log.content, 15), HEAD_IMG_HEIGHT + LINE_SPACE_HEIGHT * 5 + MARGIN, start + nameHeight + LINE_SPACE_HEIGHT * 4 , font);
		gc.drawPath(titlePath);
		titlePath.dispose();
		
		gc.drawLine(HEAD_IMG_HEIGHT + LINE_SPACE_HEIGHT * 5 + MARGIN, start + nameHeight + LINE_SPACE_HEIGHT * 4 + MARGIN * 5, HEAD_IMG_HEIGHT + LINE_SPACE_HEIGHT * 5 + MARGIN + 280, start + nameHeight + LINE_SPACE_HEIGHT * 4 + MARGIN * 5);
		
		Color fg = gc.getBackground();
		Color back = SWTResourceManager.getColor(0xF4, 0xF4, 0xF4);
		gc.setBackground(back);
		gc.drawText(FileUtils.getLimitString(log.recommendInfo.getOrDefault("NickName", "").toString(), 20), HEAD_IMG_HEIGHT * 2 + LINE_SPACE_HEIGHT * 6 + MARGIN * 2, start + nameHeight + LINE_SPACE_HEIGHT * 6 + LINE_SPACE_HEIGHT + MARGIN * 5, StringNode.DRAW_FLAGS);
		gc.drawText(FileUtils.getLimitString(log.recommendInfo.getOrDefault("Content", "").toString(), 20), HEAD_IMG_HEIGHT * 2 + LINE_SPACE_HEIGHT * 6 + MARGIN * 2, start + nameHeight + LINE_SPACE_HEIGHT * 6 + LINE_SPACE_HEIGHT + MARGIN * 10, StringNode.DRAW_FLAGS);
		gc.setBackground(fg);
	}


	@Override
	protected void drawContentR(GC gc, int start, int width) {
		if(null != log.img) {
			ImageData id = log.img.getImg().getImageData();
			gc.drawImage(log.img.getImg(), 0, 0, id.width, id.height, width - (HEAD_IMG_HEIGHT + LINE_SPACE_HEIGHT * 4 + maxWidth + MyList.BAR_WIDTH - MARGIN), start + nameHeight + LINE_SPACE_HEIGHT * 3 + LINE_SPACE_HEIGHT + MARGIN * 7, HEAD_IMG_HEIGHT, HEAD_IMG_HEIGHT);
		} 
		
		Path titlePath = new Path(null);
		titlePath.addString(FileUtils.getLimitString(log.content, 15), width - (HEAD_IMG_HEIGHT + LINE_SPACE_HEIGHT * 4 + maxWidth + MyList.BAR_WIDTH - MARGIN), start + nameHeight + LINE_SPACE_HEIGHT * 4 + MARGIN , font);
		gc.drawPath(titlePath);
		titlePath.dispose();
		
		gc.drawLine(width - (HEAD_IMG_HEIGHT + LINE_SPACE_HEIGHT * 4 + maxWidth + MyList.BAR_WIDTH - MARGIN), start + nameHeight + LINE_SPACE_HEIGHT * 3 + LINE_SPACE_HEIGHT + MARGIN * 6, width - (HEAD_IMG_HEIGHT + LINE_SPACE_HEIGHT * 4 + maxWidth + MyList.BAR_WIDTH - MARGIN) + 280, start + nameHeight + LINE_SPACE_HEIGHT * 3 + LINE_SPACE_HEIGHT + MARGIN * 6);
		
		Color fg = gc.getBackground();
		Color back = SWTResourceManager.getColor(0xF4, 0xF4, 0xF4);
		gc.setBackground(back);
		gc.drawText(FileUtils.getLimitString(log.recommendInfo.getOrDefault("NickName", "").toString(), 10), width - (HEAD_IMG_HEIGHT + LINE_SPACE_HEIGHT * 4 + maxWidth + MyList.BAR_WIDTH - MARGIN *2) + HEAD_IMG_HEIGHT + LINE_SPACE_HEIGHT, start + nameHeight + LINE_SPACE_HEIGHT * 6 + LINE_SPACE_HEIGHT + MARGIN * 5, StringNode.DRAW_FLAGS);
		gc.drawText(FileUtils.getLimitString(log.recommendInfo.getOrDefault("Content", "").toString(), 20), width - (HEAD_IMG_HEIGHT + LINE_SPACE_HEIGHT * 4 + maxWidth + MyList.BAR_WIDTH - MARGIN *2) + HEAD_IMG_HEIGHT + LINE_SPACE_HEIGHT, start + nameHeight + LINE_SPACE_HEIGHT * 6 + LINE_SPACE_HEIGHT + MARGIN * 9, StringNode.DRAW_FLAGS);
		gc.setBackground(fg);
		
	}

	@Override
	protected void onDoubleClick(MouseEvent e) {
		MessageBox mb = new MessageBox(getParent().getShell(), SWT.OK|SWT.CANCEL);
		mb.setText("添加好友");
		mb.setMessage("确定要添加" + log.recommendInfo.getOrDefault("NickName", "") + "为好友?");
		int result = mb.open();
		if(SWT.OK == result) {
			WeChatUtil.acceptFriends(log);
		}
	}
	
}
