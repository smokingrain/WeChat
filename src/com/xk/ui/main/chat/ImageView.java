package com.xk.ui.main.chat;

import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Composite;

import com.xk.uiLib.ICallable;
import com.xk.uiLib.ICallback;
import com.xk.uiLib.ImageViewer;

public class ImageView extends Composite implements ICallable{
	
	private ICallback callBack;
	public ImageViewer iv;

	public ImageView(Composite parent, int style) {
		super(parent, style);
		StackLayout layout = new StackLayout();
		setLayout(layout);
		iv = new ImageViewer(this);
		iv.setLocation(0, 0);
		iv.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseDoubleClick(MouseEvent mouseevent) {
				if(null != callBack) {
					callBack.callback(1);
				}
			}
			
		});
		layout.topControl = iv;
	}
	
	

	@Override
	public void setSize(int width, int height) {
		super.setSize(width, height);
		iv.setSize(width, height);
	}



	@Override
	public void setCallBack(ICallback callBack) {
		this.callBack = callBack;
		
	}
}
