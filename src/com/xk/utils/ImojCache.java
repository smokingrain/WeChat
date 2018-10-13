package com.xk.utils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Point;
import org.eclipse.wb.swt.SWTResourceManager;

import com.xk.bean.ImageNode;
import com.xk.bean.Imoj;
import com.xk.bean.StringNode;

public class ImojCache {
	private static final Integer IMG_WIDTH = 28;
	public static final Map<String, Image> qqface = new ConcurrentHashMap<String, Image>();
	public static List<List<Imoj>> imjs = new ArrayList<List<Imoj>>();
	private static final Map<String, Point> iconLoc = new HashMap<String, Point>();
	
	
	
	
	static{
		init();
	}
	
	public static List<Object> computeImoj(String base) {
		List<Object> chatContent = new ArrayList<Object>();
		boolean hasImoj = false;
		String reg = "\\[(\\w+|[\u4E00-\u9FA5]+)\\]";
		Pattern patternNode = Pattern.compile(reg);
		Matcher matcherNode = patternNode.matcher(base);
		String[] splt = base.split(reg);
		int index = 0;
		while (matcherNode.find()) {
			hasImoj = true;
			if(index < splt.length) {
				String txt = splt[index++];
				for(int i = 0; i < txt.length(); i++ ) {
					chatContent.add(String.valueOf(txt.charAt(i)));
				}
			}
			String match = matcherNode.group();
			chatContent.add(getContent(match));
		}
		
		if(hasImoj) {
			for(int i = index ;i < splt.length ; i++) {
				String txt = splt[i];
				for(int j = 0; j < txt.length(); j++ ) {
					chatContent.add(String.valueOf(txt.charAt(j)));
				}
			}
		}else {
			String txt = base;
			for(int i = 0; i < txt.length(); i++ ) {
				chatContent.add(String.valueOf(txt.charAt(i)));
			}
		}
		return chatContent;
	}
	
	private static Object getContent(String content) {
		if(null == content) {
			return "";
		}
		String name = content.replace("[", "").replace("]", "");
		Image img = ImojCache.qqface.get(name);
		return null == img ? content : new ImageNode(0, img, null);
	}
	
	/**
	 * 将文字中的表情提取出来
	 * @param base
	 * @return
	 * @author xiaokui
	 */
	public static List<StringNode> computeNode(String base) {
		String classReg = "\\<span\\sclass=\"emoji\\s\\w+\"\\>\\</span\\>";
		Pattern patternClass = Pattern.compile(classReg);
		Matcher matcherClass = patternClass.matcher(base);
		String[] toShow = base.split(classReg);
		List<StringNode> content = new ArrayList<StringNode>();
		int index = 0;
		boolean hasImoj = false;
		while (matcherClass.find()) {
			hasImoj = true;
			String msg = matcherClass.group();
			msg = msg.replace("<span class=\"emoji ", "").replace("\"></span>", "");
			if(index < toShow.length) {
				content.add(new StringNode(0, toShow[index++]));
			}
			content.add(new StringNode(1, msg));
		}
		if(hasImoj) {
			for(int i = index; i<toShow.length; i++) {
				content.add(new StringNode(0, toShow[i]));
			}
		} else {
			content.add(new StringNode(0, base));
		}
		return content;
	}
	
	/**
	 * 计算这个表情位置
	 * @param className
	 * @return
	 * @author xiaokui
	 */
	public static Point computeLoc(String className) {
		String text = FileUtils.readString(ImojCache.class.getResourceAsStream("/images/imoj.txt"));
		Point point = iconLoc.get(className);
		if(null != point) {
			return point;
		}
		String reg = className + "\\{background\\-position:0\\s\\-\\d+px\\}";
		Pattern patternNode = Pattern.compile(reg);
		Matcher matcherNode = patternNode.matcher(text);

		while (matcherNode.find()) {
			String pix = matcherNode.group();
			pix = pix.replace(className + "{background-position:0 -", "").replace("px}", "");
			int y = Integer.parseInt(pix);
			point = new Point(0, y);
			iconLoc.put(className, point);
			break;
		}
		return point;
	}
	
	
	/**
	 * 用途：初始化表情图片
	 * @date 2017年1月10日
	 */
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
		
		splitImage(buff, "微笑", 0, 0, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "撇嘴", 1, 0, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "色", 2, 0, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "发呆", 3, 0, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "得意", 4, 0, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "流泪", 5, 0, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "害羞", 6, 0, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "闭嘴", 7, 0, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "睡", 8, 0, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "大哭", 9, 0, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "尴尬", 10, 0, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "发怒", 11, 0, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "调皮", 12, 0, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "呲牙", 13, 0, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "惊讶", 14, 0, IMG_WIDTH, IMG_WIDTH, 1);
		
		splitImage(buff, "难过", 0, 1, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "囧", 2, 1, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "抓狂", 3, 1, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "吐", 4, 1, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "偷笑", 5, 1, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "愉快", 6, 1, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "白眼", 7, 1, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "傲慢", 8, 1, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "困", 10, 1, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "惊恐", 11, 1, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "流汗", 12, 1, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "憨笑", 13, 1, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "悠闲", 14, 1, IMG_WIDTH, IMG_WIDTH, 1);
		
		splitImage(buff, "奋斗", 0, 2, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "咒骂", 1, 2, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "疑问", 2, 2, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "嘘", 3, 2, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "晕", 4, 2, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "狂躁", 5, 2, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "衰", 6, 2, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "骷髅", 7, 2, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "敲打", 8, 2, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "再见", 9, 2, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "擦汗", 10, 2, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "抠鼻", 11, 2, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "鼓掌", 12, 2, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "坏笑", 14, 2, IMG_WIDTH, IMG_WIDTH, 1);
		
		splitImage(buff, "左哼哼", 0, 3, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "右哼哼", 1, 3, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "哈欠", 2, 3, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "鄙视", 3, 3, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "委屈", 4, 3, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "快哭了", 5, 3, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "阴险", 6, 3, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "亲亲", 7, 3, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "可怜", 9, 3, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "菜刀", 10, 3, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "西瓜", 11, 3, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "啤酒", 12, 3, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "篮球", 13, 3, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "乒乓球", 14, 3, IMG_WIDTH, IMG_WIDTH, 1);
		
		splitImage(buff, "咖啡", 0, 4, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "饭", 1, 4, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "猪头", 2, 4, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "玫瑰", 3, 4, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "凋谢", 4, 4, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "嘴唇", 5, 4, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "爱心", 6, 4, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "心碎", 7, 4, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "蛋糕", 8, 4, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "闪电", 9, 4, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "炸弹", 10, 4, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "小刀", 11, 4, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "足球", 12, 4, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "爬虫", 13, 4, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "便便", 14, 4, IMG_WIDTH, IMG_WIDTH, 1);
		
		splitImage(buff, "月亮", 0, 5, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "太阳", 1, 5, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "礼物", 2, 5, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "拥抱", 3, 5, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "强", 4, 5, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "弱", 5, 5, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "握手", 6, 5, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "胜利", 7, 5, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "抱拳", 8, 5, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "勾引", 9, 5, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "拳头", 10, 5, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "爱你", 12, 5, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "NO", 13, 5, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "OK", 14, 5, IMG_WIDTH, IMG_WIDTH, 1);
		
		splitImage(buff, "恋爱", 0, 6, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "飞吻", 1, 6, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "跳跳", 2, 6, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "发抖", 3, 6, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "怄火", 4, 6, IMG_WIDTH, IMG_WIDTH, 1);
		splitImage(buff, "转圈", 5, 6, IMG_WIDTH, IMG_WIDTH, 1);
		
	}
	
	/**
	 * 用途：从原图截取单个表情
	 * @date 2017年1月10日
	 * @param base
	 * @param name
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	private static void splitImage(BufferedImage base, String name, int x, int y, int width, int height, int type) {
		BufferedImage sub = base.getSubimage(x * width + x, y * height + y, width, height);
		Image target = AWTImg2SWTImg(sub, name);
		if(null == target) {
			return;
		}
		qqface.put(name, target);
		Imoj imj = new Imoj();
		imj.img = target;
		imj.name = name;
		if(imjs.size() == 0) {
			List<Imoj> line = new ArrayList<Imoj>();
			line.add(imj);
			imjs.add(line);
		}else {
			List<Imoj> line = imjs.get(imjs.size() - 1);
			if(line.size() < 15) {
				line.add(imj);
			}else {
				line = new ArrayList<Imoj>();
				line.add(imj);
				imjs.add(line);
			}
		}
	}
	
	
	/**
	 * 用途：由SWING的图片换成SWT的图片对象
	 * @date 2017年1月10日
	 * @param base
	 * @param name
	 * @return
	 */
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
