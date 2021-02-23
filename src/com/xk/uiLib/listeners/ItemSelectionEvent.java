package com.xk.uiLib.listeners;

import com.xk.uiLib.ListItem;
import com.xk.uiLib.MyList;

public class ItemSelectionEvent<T extends ListItem> {
	public T item;
	public int itemHeights;
	public int index;
	public MyList<T> source;
}
