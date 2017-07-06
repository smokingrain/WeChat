package com.xk.bean;

public class StringNode {
	
	public static final int SPACE = 2;

	public int type = 0;
	public String base;
	
	public StringNode(){
		
	}
	
	public StringNode(int type, String base) {
		this.type = type;
		this.base = base;
	}
}
