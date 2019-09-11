package com.xk.bean;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageLoader;

/**
 * 图片对象，表情对象
 * @author Administrator
 *
 */
public class ImageNode {
	public int type;//0，小表情，不可点击，1，正常图片
	private Image img;//具体图片对象
	private ImageLoader loader;//图片加载器，存储部分数据，也可以方便的保存图片
	private int width;//宽
	private int height;//高
	
	public ImageNode(int type, Image img, ImageLoader loader) {
		this.type = type;
		this.img = img;
		this.loader = loader;
		int w = img.getImageData().width;
		int h = img.getImageData().height;
		if(Math.max(w, h) > 200) {//固定图片不能太大。否则显示很奇怪
			if(w > h) {
				this.width = 200;
				this.height = h * 200 / w;
			} else {
				this.height = 200;
				this.width = w * 200 / h;
			}
		} else {
			this.width = w;
			this.height = h;
		}
	}

	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	
	public ImageLoader getLoader() {
		return loader;
	}
	
	public Image getImg() {
		return img;
	}

	public void setImg(Image img) {
		if(null!= this.img) {//必须先将原来的清楚，否则可能内存泄露
			this.img.dispose();
		}
		this.img = img;
	}
	
}
