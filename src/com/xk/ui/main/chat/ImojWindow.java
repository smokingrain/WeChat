package com.xk.ui.main.chat;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import com.xk.bean.Imoj;
import com.xk.ui.main.FloatWindow;
import com.xk.uiLib.MyList;
import com.xk.utils.ImojCache;

public class ImojWindow extends FloatWindow {

	private ChatComp cc;
	
	private static class WindowHolder {
		private static ImojWindow INSTANCE = new ImojWindow();
	}
	
	public static ImojWindow getInstance() {
		return WindowHolder.INSTANCE;
	}
	
	public void setCc(ChatComp cc) {
		this.cc = cc;
	}
	
	public void init(int x, int y){
		kill();
		createContents();
		shell.setLocation(x, y);
	}
	
	@Override
	protected void createContents() {
		shell = new Shell(SWT.FILL_WINDING);
		MyList<ImojItem> list = new MyList<>(shell, 30 * 15 + MyList.BAR_WIDTH, 215);
		list.setSimpleSelect(true);
		list.setLocation(1, 1);
		for(List<Imoj> imj : ImojCache.imjs) {
			ImojItem itm = new ImojItem(imj, cc);
			list.addItem(itm);
		}
	}
	
}
