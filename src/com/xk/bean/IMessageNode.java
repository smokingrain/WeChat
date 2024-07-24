package com.xk.bean;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

import com.xk.bean.ImageNode.TYPE;

public interface IMessageNode {

	
	/**
	 * 重置宽高
	 * 作者 ：肖逵
	 * 时间 ：2024年7月13日 上午11:46:48
	 * @param gc
	 */
	public void computeSize(GC gc);
	
	/**
	 * 获取宽高
	 * 作者 ：肖逵
	 * 时间 ：2024年7月13日 上午11:23:44
	 * @return
	 */
	public Point getSize();
	
	/**
	 * 绘制该节点
	 * 作者 ：肖逵
	 * 时间 ：2024年7月13日 上午11:39:28
	 * @param gc
	 * @param x
	 * @param y
	 */
	public void draw(GC gc, int x, int y);
	
	/**
	 * 类型
	 * 作者 ：肖逵
	 * 时间 ：2024年7月13日 上午11:39:15
	 * @return
	 */
	public TYPE getType();
	
	/**
	 * 获取基础字符串
	 * 作者 ：肖逵
	 * 时间 ：2024年7月13日 上午11:58:27
	 * @return
	 */
	public String getBase();
}
