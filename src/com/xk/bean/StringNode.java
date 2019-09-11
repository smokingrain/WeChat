package com.xk.bean;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

/**
 * 字符串节点，如果有表情，需要拆开成几个节点
 * @author Administrator
 *
 */
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
	
	/**
	 * 获取宽高
	 * 作者 ：肖逵
	 * 时间 ：2018年8月31日 下午12:34:39
	 * @param nodes
	 * @param flag
	 * @param gc
	 * @return
	 */
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

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return base;
	}
	
	
}
