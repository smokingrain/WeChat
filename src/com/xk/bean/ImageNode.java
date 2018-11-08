package com.xk.bean;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageLoader;

public class ImageNode {
	public int type;//0，小表情，不可点击，1，正常图片
	private Image img;
	private ImageLoader loader;
	private int width;
	private int height;
	
	public ImageNode(int type, Image img, ImageLoader loader) {
		this.type = type;
		this.img = img;
		this.loader = loader;
		int w = img.getImageData().width;
		int h = img.getImageData().height;
		if(Math.max(w, h) > 200) {
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
		if(null!= this.img) {
			this.img.dispose();
		}
		this.img = img;
	}
	
}
