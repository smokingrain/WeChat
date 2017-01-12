package com.xk.ui.items;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;

import com.xk.uiLib.ListItem;

/**
 * 用途：最左侧列表
 *
 * @author xiaokui
 * @date 2017年1月3日
 */
public class TypeItem extends ListItem {

	private Image back;
	private Image sele;
	
	public TypeItem(Image back ,Image  sele) {
		this.back = back;
		this.sele = sele;
	}
	
	@Override
	public int getHeight() {
		return 50;
	}

	@Override
	public void draw(GC gc, int start, int width, int index) {
		if(selected) {
			gc.drawImage(sele, 0, start);
		} else {
			gc.drawImage(back, 0, start);
		}
		
	}

	@Override
	public boolean oncliek(MouseEvent e, int itemHeight, int index, int type) {
		return true;
	}

}
