package com.xk.utils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.imageio.ImageIO;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.wb.swt.SWTResourceManager;

public class ImojCache {
	private static final Integer IMG_WIDTH = 28;
	public static final Map<String, Image> qqface = new ConcurrentHashMap<String, Image>();
	
	
	static{
		init();
	}
	
	private static void init() {
		BufferedImage buff = null;
		InputStream in = ImojCache.class.getResourceAsStream("/images/qqfacePanel31e225.png");
		try {
			buff= ImageIO.read(in);
		} catch (IOException e) {
			System.out.println("表情初始化失败");
			return;
		}finally {
			try {
				in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		splitImage(buff, "微笑", 0, 0, IMG_WIDTH, IMG_WIDTH);
		splitImage(buff, "撇嘴", 1, 0, IMG_WIDTH, IMG_WIDTH);
		splitImage(buff, "色", 2, 0, IMG_WIDTH, IMG_WIDTH);
		splitImage(buff, "发呆", 3, 0, IMG_WIDTH, IMG_WIDTH);
		splitImage(buff, "得意", 4, 0, IMG_WIDTH, IMG_WIDTH);
		splitImage(buff, "流泪", 5, 0, IMG_WIDTH, IMG_WIDTH);
		splitImage(buff, "害羞", 6, 0, IMG_WIDTH, IMG_WIDTH);
		splitImage(buff, "闭嘴", 7, 0, IMG_WIDTH, IMG_WIDTH);
		splitImage(buff, "睡", 8, 0, IMG_WIDTH, IMG_WIDTH);
		splitImage(buff, "大哭", 9, 0, IMG_WIDTH, IMG_WIDTH);
		splitImage(buff, "尴尬", 10, 0, IMG_WIDTH, IMG_WIDTH);
		splitImage(buff, "发怒", 11, 0, IMG_WIDTH, IMG_WIDTH);
		splitImage(buff, "调皮", 12, 0, IMG_WIDTH, IMG_WIDTH);
		splitImage(buff, "呲牙", 13, 0, IMG_WIDTH, IMG_WIDTH);
		splitImage(buff, "惊讶", 14, 0, IMG_WIDTH, IMG_WIDTH);
		
		splitImage(buff, "难过", 0, 1, IMG_WIDTH, IMG_WIDTH);
		splitImage(buff, "囧", 2, 1, IMG_WIDTH, IMG_WIDTH);
		splitImage(buff, "抓狂", 3, 1, IMG_WIDTH, IMG_WIDTH);
		splitImage(buff, "吐", 4, 1, IMG_WIDTH, IMG_WIDTH);
		splitImage(buff, "偷笑", 5, 1, IMG_WIDTH, IMG_WIDTH);
		splitImage(buff, "愉快", 6, 1, IMG_WIDTH, IMG_WIDTH);
		splitImage(buff, "白眼", 7, 1, IMG_WIDTH, IMG_WIDTH);
		splitImage(buff, "傲慢", 8, 1, IMG_WIDTH, IMG_WIDTH);
		splitImage(buff, "困", 10, 1, IMG_WIDTH, IMG_WIDTH);
		splitImage(buff, "惊恐", 11, 1, IMG_WIDTH, IMG_WIDTH);
		splitImage(buff, "流汗", 12, 1, IMG_WIDTH, IMG_WIDTH);
		splitImage(buff, "憨笑", 13, 1, IMG_WIDTH, IMG_WIDTH);
		splitImage(buff, "悠闲", 14, 1, IMG_WIDTH, IMG_WIDTH);
	}
	
	private static void splitImage(BufferedImage base, String name, int x, int y, int width, int height) {
		BufferedImage sub = base.getSubimage(x * width + x, y * height + y, width, height);
		Image target = AWTImg2SWTImg(sub, name);
		if(null == target) {
			return;
		}
		qqface.put(name, target);
	}
	
	private static Image AWTImg2SWTImg(BufferedImage base, String name) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			ImageIO.write(base, "png", out);
		} catch (IOException e) {
			System.out.println(name + "初始化失败！");
			e.printStackTrace();
			return null;
		}
		byte[] data = out.toByteArray();
		ByteArrayInputStream in = new ByteArrayInputStream(data);
		ImageData id = new ImageData(in);
		try {
			out.close();
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Image img = new Image(null, id);
		return img;
	}
	
	
}
