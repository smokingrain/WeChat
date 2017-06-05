package com.xk.ui.main.chat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.graphics.Point;
import org.eclipse.wb.swt.SWTResourceManager;

import com.xk.chatlogs.ChatLog;
import com.xk.uiLib.MyList;

public class TimeItem extends ChatItem {

	private static SimpleDateFormat fullFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static SimpleDateFormat todayFmt = new SimpleDateFormat("HH:mm:ss");
	
	private Long time = null;
	private String timeText = null;
	
	public TimeItem(Long time) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(time);
		Calendar today = Calendar.getInstance();    //今天  
        today.set( Calendar.HOUR_OF_DAY, 0);  
        today.set( Calendar.MINUTE, 0);  
        today.set(Calendar.SECOND, 0); 
        Date date = new Date(time);
        if(c.before(today)) {
        	timeText = fullFmt.format(date);
        } else {
        	timeText = todayFmt.format(date);
        }
		this.time = time;
	}
	
	public TimeItem(String user, Image head, List<Object> chatContent,
			boolean fromSelf, Font font, ChatLog log) {
		super(user, head, chatContent, fromSelf, font, log);
		// TODO Auto-generated constructor stub
	}

	@Override
	public int getHeight() {
		return 42;
	}

	@Override
	public void draw(GC gc, int start, int width, int index) {
		Point size = gc.textExtent(timeText);
		int timeLen = size.x;
		int timeHei = size.y;
		int boxLen = timeLen + 10 * 2;
		int alf=gc.getAlpha();
		Color bk = gc.getBackground();
		gc.setBackground(SWTResourceManager.getColor(111, 111, 111));
		gc.setAlpha(111);
		gc.fillRoundRectangle((width - boxLen) / 2, start + 7, boxLen, getHeight() - 14, 20, 20);
		gc.setAlpha(alf);
		gc.setBackground(bk);
		Path path = new Path(null);
		path.addString(timeText, (width - boxLen) / 2 + 10,   start + 7 + ((getHeight() - 14 - timeHei) / 2), gc.getFont());
		gc.fillPath(path);
		path.dispose();
	}

	@Override
	public boolean oncliek(MouseEvent e, int itemHeight, int index, int type) {
		// TODO Auto-generated method stub
		return false;
	}

}
