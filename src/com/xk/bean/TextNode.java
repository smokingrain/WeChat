package com.xk.bean;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

import com.xk.bean.ImageNode.TYPE;

public class TextNode implements IMessageNode {

	
	private Point size;
	private String text;
	
	public TextNode(String text) {
		this.text = text;
	}
	
	@Override
	public Point getSize() {
		return size;
	}

	@Override
	public void draw(GC gc, int x, int y) {
		gc.drawText(text, x, y, StringNode.DRAW_FLAGS);

	}

	@Override
	public TYPE getType() {
		return TYPE.TEXT;
	}

	@Override
	public void computeSize(GC gc) {
		size = gc.textExtent(text, StringNode.DRAW_FLAGS);
	}

	@Override
	public String getBase() {
		return text;
	}

}
