package com.xk.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.wb.swt.SWTResourceManager;

public class ImojCache {
	private static final Integer IMG_WIDTH = 28;
	public static final Map<String, Image> qqface = new ConcurrentHashMap<String, Image>();
	
	
	static{
		Image base = SWTResourceManager.getImage(ImojCache.class, "/images/qqfacePanel31e225.png");
		splitImage(base, "微笑", 0, 0, IMG_WIDTH, IMG_WIDTH);
		splitImage(base, "撇嘴", 1, 0, IMG_WIDTH, IMG_WIDTH);
		splitImage(base, "色", 2, 0, IMG_WIDTH, IMG_WIDTH);
		splitImage(base, "发呆", 3, 0, IMG_WIDTH, IMG_WIDTH);
		splitImage(base, "得意", 4, 0, IMG_WIDTH, IMG_WIDTH);
		splitImage(base, "流泪", 5, 0, IMG_WIDTH, IMG_WIDTH);
		splitImage(base, "害羞", 6, 0, IMG_WIDTH, IMG_WIDTH);
		splitImage(base, "闭嘴", 7, 0, IMG_WIDTH, IMG_WIDTH);
		splitImage(base, "睡", 8, 0, IMG_WIDTH, IMG_WIDTH);
		splitImage(base, "大哭", 9, 0, IMG_WIDTH, IMG_WIDTH);
		splitImage(base, "尴尬", 10, 0, IMG_WIDTH, IMG_WIDTH);
		splitImage(base, "发怒", 11, 0, IMG_WIDTH, IMG_WIDTH);
		splitImage(base, "调皮", 12, 0, IMG_WIDTH, IMG_WIDTH);
		splitImage(base, "呲牙", 13, 0, IMG_WIDTH, IMG_WIDTH);
		splitImage(base, "惊讶", 14, 0, IMG_WIDTH, IMG_WIDTH);
		
		splitImage(base, "惊讶", 0, 1, IMG_WIDTH, IMG_WIDTH);
		splitImage(base, "囧", 2, 1, IMG_WIDTH, IMG_WIDTH);
		splitImage(base, "抓狂", 3, 1, IMG_WIDTH, IMG_WIDTH);
		splitImage(base, "吐", 4, 1, IMG_WIDTH, IMG_WIDTH);
		splitImage(base, "偷笑", 5, 1, IMG_WIDTH, IMG_WIDTH);
		splitImage(base, "愉快", 6, 1, IMG_WIDTH, IMG_WIDTH);
		splitImage(base, "白眼", 7, 1, IMG_WIDTH, IMG_WIDTH);
		splitImage(base, "傲慢", 8, 1, IMG_WIDTH, IMG_WIDTH);
		splitImage(base, "困", 10, 1, IMG_WIDTH, IMG_WIDTH);
		splitImage(base, "惊恐", 11, 1, IMG_WIDTH, IMG_WIDTH);
		splitImage(base, "流汗", 12, 1, IMG_WIDTH, IMG_WIDTH);
		splitImage(base, "憨笑", 13, 1, IMG_WIDTH, IMG_WIDTH);
		splitImage(base, "悠闲", 14, 1, IMG_WIDTH, IMG_WIDTH);
	}
	
	private static void splitImage(Image base, String name, int x, int y, int width, int height) {
		ImageData id = new ImageData(1, 1, 24, base.getImageData().palette, 4, new byte[]{-1,-1,-1,-1});
		Image target = new Image(null, id.scaledTo(28, 28));
		GC gc = new GC(target);
		gc.drawImage(base, x * width, y * height, width, height, 0, 0, width, height);
		gc.dispose();
		qqface.put(name, target);
	}
	
	
	
}
