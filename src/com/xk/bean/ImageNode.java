package com.xk.bean;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Point;

/**
 * 图片对象，表情对象
 * @author Administrator
 *
 */
public class ImageNode implements IMessageNode{
	public TYPE type;//0，小表情，不可点击，1，正常图片
	private Image img;//具体图片对象
	private ImageLoader loader;//图片加载器，存储部分数据，也可以方便的保存图片
	private String base;
	private int width;//宽
	private int height;//高
	private Point size;
	
	public ImageNode(TYPE type, Image img, ImageLoader loader, String base) {
		this.type = type;
		this.img = img;
		this.loader = loader;
		this.setBase(base);
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
		size = new Point(TYPE.IMOJ.equals(type) ? StringNode.IMOJ_WIDTH : width, TYPE.IMOJ.equals(type) ? StringNode.IMOJ_WIDTH : height);
	}

	@Override
	public Point getSize() {
		return size;
	}
	
	@Override
	public void draw(GC gc, int x, int y) {
		gc.drawImage(img, 0, 0, img.getImageData().width, img.getImageData().height, x, y, size.x, size.y);
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

	public String getBase() {
		return base;
	}

	public void setBase(String base) {
		this.base = base;
	}
	
	public enum TYPE{
		TEXT(2),IMAGE(1),IMOJ(0);
		private int type = 0;
		private TYPE(int type){
			this.type = type;
		}
		
		public int getType() {
			return type;
		}
		
	}

	@Override
	public void computeSize(GC gc) {
		
	}

	@Override
	public TYPE getType() {
		return type;
	}
	
}
