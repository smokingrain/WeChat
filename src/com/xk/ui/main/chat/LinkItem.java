package com.xk.ui.main.chat;

import java.util.List;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Path;

import com.xk.chatlogs.ChatLog;
import com.xk.uiLib.MyList;
import com.xk.utils.FileUtils;
import com.xk.utils.SWTTools;

public class LinkItem extends ChatItem {

	public LinkItem (String user,Image head, List<Object> chatContent, boolean fromSelf, Font font, ChatLog log) {
		super(user, head, chatContent, fromSelf, font, log);
	}
	
	@Override
	public int getHeight() {
		super.getHeight();
		maxWidth = ITEM_AREA_WIDTH;
		return 105;
		
	}
	

	@Override
	protected void drawContentL(GC gc, int start, int width) {
		if(null != log.img) {
			ImageData id = log.img.getImg().getImageData();
			gc.drawImage(log.img.getImg(), 0, 0, id.width, id.height, HEAD_IMG_HEIGHT + LINE_SPACE_HEIGHT * 5 + MARGIN, start + nameHeight + LINE_SPACE_HEIGHT * 3 + LINE_SPACE_HEIGHT + MARGIN, HEAD_IMG_HEIGHT, HEAD_IMG_HEIGHT);
		}
		Path contentPath = new Path(null);
		contentPath.addString(FileUtils.getLimitString(log.content, 20), HEAD_IMG_HEIGHT * 2 + LINE_SPACE_HEIGHT * 6 + MARGIN, start + nameHeight + LINE_SPACE_HEIGHT * 6 + LINE_SPACE_HEIGHT + MARGIN, font);
		gc.drawPath(contentPath);
		contentPath.dispose();
	}


	@Override
	protected void drawContentR(GC gc, int start, int width) {
		if(null != log.img) {
			ImageData id = log.img.getImg().getImageData();
			gc.drawImage(log.img.getImg(), 0, 0, id.width, id.height, width - (HEAD_IMG_HEIGHT + LINE_SPACE_HEIGHT * 4 + maxWidth + MyList.BAR_WIDTH - MARGIN), start + nameHeight + LINE_SPACE_HEIGHT * 3 + LINE_SPACE_HEIGHT + MARGIN, HEAD_IMG_HEIGHT, HEAD_IMG_HEIGHT);
		}
		Path contentPath = new Path(null);
		contentPath.addString(FileUtils.getLimitString(log.content, 20), width - (HEAD_IMG_HEIGHT + LINE_SPACE_HEIGHT * 4 + maxWidth + MyList.BAR_WIDTH - MARGIN) + HEAD_IMG_HEIGHT + LINE_SPACE_HEIGHT, start + nameHeight + LINE_SPACE_HEIGHT * 6 + LINE_SPACE_HEIGHT + MARGIN, font);
		gc.drawPath(contentPath);
		contentPath.dispose();
	}

	@Override
	protected void onDoubleClick(MouseEvent e) {
		String url = log.url;
		SWTTools.openBrowser(url);
	}


	
}
