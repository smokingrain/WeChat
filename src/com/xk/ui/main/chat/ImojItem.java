package com.xk.ui.main.chat;

import java.util.List;

import org.eclipse.swt.custom.StyledTextUtils;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.GC;

import com.xk.bean.ImageNode;
import com.xk.bean.Imoj;
import com.xk.uiLib.ListItem;
import com.xk.utils.ImojCache;

public class ImojItem extends ListItem {
	
	private List<Imoj> data ;
	private ChatComp cc;
	
	public ImojItem(List<Imoj> data, ChatComp cc) {
		this.data = data;
		this.cc = cc;
	}

	@Override
	public int getHeight() {
		return 30;
	}

	@Override
	public void draw(GC gc, int start, int width, int index) {
		int indix = 0;
		for(Imoj imj : data) {
			if(null != imj) {
				gc.drawImage(imj.img, 30 * indix++ + 1, start + 1);
			}
		}

	}

	@Override
	public boolean oncliek(MouseEvent e, int itemHeight, int index, int type) {
		int indix = e.x / 30;
		if(indix < data.size() && null != data.get(indix)) {
			String base = "[" + data.get(indix).name + "]";
			List<Object> result = ImojCache.computeImoj(base);
			for(Object obj : result) {
				if(obj instanceof ImageNode) {
					ImageNode node = (ImageNode) obj;
					StyledTextUtils.addImage(cc.getText(), node);
				}
			}
		}
		return false;
	}

}
