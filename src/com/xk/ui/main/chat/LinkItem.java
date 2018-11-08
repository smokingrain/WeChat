package com.xk.ui.main.chat;

import java.util.List;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Path;
import org.eclipse.wb.swt.SWTResourceManager;

import com.xk.bean.StringNode;
import com.xk.chatlogs.ChatLog;
import com.xk.uiLib.MyList;
import com.xk.utils.FileUtils;
import com.xk.utils.SWTTools;

public class LinkItem extends ChatItem {
	
	private static Image linkDefault=SWTResourceManager.getImage(LinkItem.class, "/images/link.png");

	public LinkItem (String user,Image head, List<Object> chatContent, boolean fromSelf, Font font, ChatLog log) {
		super(user, head, chatContent, fromSelf, font, log);
	}
	
	@Override
	public int getHeight() {
		super.getHeight();
		maxWidth = ITEM_AREA_WIDTH;
		return 120;
		
	}
	

	@Override
	protected void drawContentL(GC gc, int start, int width) {
		if(null != log.img) {
			ImageData id = log.img.getImg().getImageData();
			gc.drawImage(log.img.getImg(), 0, 0, id.width, id.height, HEAD_IMG_HEIGHT + LINE_SPACE_HEIGHT * 5 + MARGIN, start + nameHeight + LINE_SPACE_HEIGHT * 4 + MARGIN * 4, HEAD_IMG_HEIGHT, HEAD_IMG_HEIGHT);
		} else {
			gc.drawImage(linkDefault, 0, 0, linkDefault.getImageData().width, linkDefault.getImageData().height, HEAD_IMG_HEIGHT + LINE_SPACE_HEIGHT * 5 + MARGIN, start + nameHeight + LINE_SPACE_HEIGHT * 4 + MARGIN * 4, HEAD_IMG_HEIGHT, HEAD_IMG_HEIGHT);
		}
		
		Path titlePath = new Path(null);
		titlePath.addString(FileUtils.getLimitString(log.content, 15), HEAD_IMG_HEIGHT + LINE_SPACE_HEIGHT * 5 + MARGIN, start + nameHeight + LINE_SPACE_HEIGHT * 4 , font);
		gc.drawPath(titlePath);
		titlePath.dispose();
		
		Color fg = gc.getBackground();
		Color back = SWTResourceManager.getColor(0xF4, 0xF4, 0xF4);
		gc.setBackground(back);
		gc.drawText(FileUtils.getLimitString(log.url, 20), HEAD_IMG_HEIGHT * 2 + LINE_SPACE_HEIGHT * 6 + MARGIN * 2, start + nameHeight + LINE_SPACE_HEIGHT * 6 + LINE_SPACE_HEIGHT + MARGIN * 5, StringNode.DRAW_FLAGS);
		gc.setBackground(fg);
	}


	@Override
	protected void drawContentR(GC gc, int start, int width) {
		if(null != log.img) {
			ImageData id = log.img.getImg().getImageData();
			gc.drawImage(log.img.getImg(), 0, 0, id.width, id.height, width - (HEAD_IMG_HEIGHT + LINE_SPACE_HEIGHT * 4 + maxWidth + MyList.BAR_WIDTH - MARGIN), start + nameHeight + LINE_SPACE_HEIGHT * 3 + LINE_SPACE_HEIGHT + MARGIN * 5, HEAD_IMG_HEIGHT, HEAD_IMG_HEIGHT);
		} else {
			gc.drawImage(linkDefault, 0, 0, linkDefault.getImageData().width, linkDefault.getImageData().height, width - (HEAD_IMG_HEIGHT + LINE_SPACE_HEIGHT * 4 + maxWidth + MyList.BAR_WIDTH - MARGIN), start + nameHeight + LINE_SPACE_HEIGHT * 4 + MARGIN * 5, HEAD_IMG_HEIGHT, HEAD_IMG_HEIGHT);
		}
		
		Path titlePath = new Path(null);
		titlePath.addString(FileUtils.getLimitString(log.content, 15), width - (HEAD_IMG_HEIGHT + LINE_SPACE_HEIGHT * 4 + maxWidth + MyList.BAR_WIDTH - MARGIN), start + nameHeight + LINE_SPACE_HEIGHT * 4 + MARGIN , font);
		gc.drawPath(titlePath);
		titlePath.dispose();
		
		Color fg = gc.getBackground();
		Color back = SWTResourceManager.getColor(0xF4, 0xF4, 0xF4);
		gc.setBackground(back);
		gc.drawText(FileUtils.getLimitString(log.url, 20), width - (HEAD_IMG_HEIGHT + LINE_SPACE_HEIGHT * 4 + maxWidth + MyList.BAR_WIDTH - MARGIN *2) + HEAD_IMG_HEIGHT + LINE_SPACE_HEIGHT, start + nameHeight + LINE_SPACE_HEIGHT * 6 + LINE_SPACE_HEIGHT + MARGIN * 6, StringNode.DRAW_FLAGS);
		gc.setBackground(fg);
		
	}

	@Override
	protected void onDoubleClick(MouseEvent e) {
		String url = log.url;
		SWTTools.openBrowser(url);
	}


	
}
