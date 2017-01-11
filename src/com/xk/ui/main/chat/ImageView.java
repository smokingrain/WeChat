package com.xk.ui.main.chat;

import org.eclipse.swt.widgets.Composite;

import com.xk.uiLib.ICallable;
import com.xk.uiLib.ICallback;

public class ImageView extends Composite implements ICallable{
	
	private ICallback callBack;

	public ImageView(Composite parent, int style) {
		super(parent, style);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setCallBack(ICallback callBack) {
		this.callBack = callBack;
		
	}

}
