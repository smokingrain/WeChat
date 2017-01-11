package com.xk.uiLib.listeners;

import com.xk.uiLib.ListItem;


public interface ItemListener<T extends ListItem> {

	public void itemRemove(ItemEvent<T> e);
	
}
