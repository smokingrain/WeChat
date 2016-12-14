package com.xk.uiLib;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.GC;

public abstract class ListItem {
	
	private MyList parent;
	
	protected boolean selected=false;
	protected boolean focused=false;
	
	public abstract int getHeight();
	
	public abstract void draw(GC gc,int start,int width,int index);
	
	/**
	 * item被点击
	 * @param x 相对父组件x坐标
	 * @param y 相对父组件Y坐标
	 * @param itemHeight 当前itemY位移，包含本身高度
	 * @return
	 */
	public abstract boolean oncliek(MouseEvent e,int itemHeight,int index);
	
	void focus(){
		focused=true;
	}
	
	public void select(){
		selected=true;
		if(null!=parent){
			parent.selectIndex=parent.getItems().indexOf(this);
			parent.selected=this;
		}
	}
	
	void unFocus(){
		focused=false;
	}
	
	public void unSelect(){
		selected=false;
	}

	public MyList getParent() {
		return parent;
	}

	public void setParent(MyList parent) {
		this.parent = parent;
	}
}
