package com.xk.uiLib.listeners;

import com.xk.uiLib.ListItem;

public interface ItemSelectionListener<T extends ListItem> {

	public void selected(ItemSelectionEvent<T> e);
	
}
