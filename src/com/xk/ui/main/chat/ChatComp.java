package com.xk.ui.main.chat;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.wb.swt.SWTResourceManager;

import com.xk.bean.ContactsStruct;
import com.xk.ui.items.ConvItem;

import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;

public class ChatComp extends Composite {

	private Label nameL;
	
	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public ChatComp(Composite parent, int style) {
		super(parent, style);
		setLocation(300,0);
		setSize(550, 590);
		setBackground(SWTResourceManager.getColor(245, 245, 245));
		
		nameL = new Label(this, SWT.NONE);
		nameL.setBackground(getBackground());
		nameL.setAlignment(SWT.CENTER);
		nameL.setBounds(0, 0, 470, 17);
	}
	
	public void flush(ConvItem item) {
		
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
}
