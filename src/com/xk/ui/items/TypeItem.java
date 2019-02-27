package com.xk.ui.items;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;

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
		boolean adv = gc.getAdvanced();
		int ant = gc.getAntialias();
		gc.setAdvanced(true);
		gc.setAntialias(SWT.ON);
		if(selected) {
			ImageData id = sele.getImageData();
//			gc.drawImage(sele, 0, 0, id.width, id.height, 0, start, 50, 50);
			gc.drawImage(sele, (50 - id.width) / 2, start + (50 - id.height) / 2);
		} else {
			ImageData id = back.getImageData();
//			gc.drawImage(back, 0, 0, id.width, id.height, 0, start, 50, 50);
			gc.drawImage(back, (50 - id.width) / 2, start + (50 - id.height) / 2);
		}
		
		gc.setAdvanced(adv);
		gc.setAntialias(ant);
	}

	@Override
	public boolean oncliek(MouseEvent e, int itemHeight, int index, int type) {
		return true;
	}

}
