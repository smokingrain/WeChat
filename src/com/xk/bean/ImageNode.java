package com.xk.bean;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageLoader;

public class ImageNode {
	public int type;//0，小表情，不可点击，1，正常图片
	private Image img;
	private ImageLoader loader;
	
	public ImageNode(int type, Image img, ImageLoader loader) {
		this.type = type;
		this.img = img;
		this.loader = loader;
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
