package com.xk.bean;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

public class StringNode {
	
	public static final int SPACE = 2;
	public static int DRAW_FLAGS = SWT.DRAW_MNEMONIC | SWT.DRAW_TAB | SWT.DRAW_TRANSPARENT | SWT.DRAW_DELIMITER;

	public int type = 0;
	public String base;
	
	public StringNode(){
		
	}
	
	public StringNode(int type, String base) {
		this.type = type;
		this.base = base;
	}
	
	
	public static Point textExtent(java.util.List<StringNode> nodes, int flag, GC gc) {
		int x = 0;
		int y = 0;
		for(StringNode node : nodes) {
			if(node.type == 0) {
				Point pt = gc.textExtent(node.base, flag);
				x += pt.x + StringNode.SPACE;
				y = Math.max(pt.y, y);
			}else {
				x += 20 + StringNode.SPACE;
				y = Math.max(20 + StringNode.SPACE, y);
			}
		}
		return new Point(x, y);
	}
}
