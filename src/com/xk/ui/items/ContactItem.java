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

public class ContactItem extends ListItem {

	private ContactsStruct data;
	private Image headDefault=SWTResourceManager.getImage(ContactItem.class, "/images/head.png");
	private boolean dir;
	private String name;
	
	public ContactItem(ContactsStruct data, boolean dir, String name) {
		super();
		this.data = data;
		this.dir = dir;
		this.name = name;
	}

	@Override
	public int getHeight() {
		return dir ? 20 : 60;
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
		if(dir) {
			int alf=gc.getAlpha();
			Color bk = gc.getBackground();
			gc.setBackground(SWTResourceManager.getColor(136, 136, 136));
			gc.setAlpha(155);
			gc.fillRectangle(0, start, width-MyList.BAR_WIDTH, getHeight());
			gc.setAlpha(alf);
			gc.setBackground(bk);
			Color fo = gc.getForeground();
			gc.setForeground(SWTResourceManager.getColor(112, 128, 144));
			Path path = new Path(null);
			path.addString(name, 15f, start + 5, font);
			gc.drawPath(path);
			path.dispose();
			gc.setForeground(fo);
		}else {
			gc.drawImage((null == data.head || data.head.isDisposed()) ? headDefault : data.head, 10, start + 5);
			Path path = new Path(null);
			path.addString(name, 15f + 60f, start + 25, font);
			gc.drawPath(path);
		}

	}

	@Override
	public boolean oncliek(MouseEvent e, int itemHeight, int index) {
		return !dir;
	}

}
