package com.xk.chatlogs;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.xk.bean.ImageNode;
import com.xk.chatlogs.interfaces.IChatLogChain;
import com.xk.utils.Constant;
import com.xk.utils.ImageCache;
import com.xk.utils.SWTTools;

/**
 * 用途：聊天记录
 *
 * @author xiaokui
 * @date 2017年1月3日
 */
public class ChatLog {

	public String msgid;
	public Long newMsgId;
	public Integer msgType;
	
	public String imgPath;
	public String relatedPath;
	public String content;
	public String fromId;
	public String toId;
	public String url;
	public Long createTime;
	public Integer voiceLength;
	
	@JsonIgnore
	public Map<String, Object> recommendInfo;
	
	@JsonIgnore
	public ImageNode img;
	
	@JsonIgnore
	public File file;
	
	@JsonIgnore
	public int persent = 0;
	
	@JsonIgnore
	public boolean recalled = false;
	
	@JsonIgnore
	public boolean sent = true;
	
	@JsonIgnore
	public boolean local = false;
	
	/**
	 * 创建普通聊天记录
	 * 作者 ：肖逵
	 * 时间 ：2019年8月31日 下午12:44:14
	 * @param msg
	 * @param to
	 * @return
	 */
	public static ChatLog createSimpleLog(String msg, String to) {
		ChatLog log = new ChatLog();
		log.createTime = System.currentTimeMillis();
		log.toId = to;
		log.fromId = Constant.user.UserName;
		log.msgType = 1;
		log.content = msg;
		return log;
	}
	
	/**
	 * 创建文件发送接收记录
	 * 作者 ：肖逵
	 * 时间 ：2019年8月31日 下午12:44:27
	 * @param file
	 * @param to
	 * @return
	 */
	public static ChatLog createFileLog(File file, String to) {
		ChatLog log = new ChatLog();
		log.createTime = System.currentTimeMillis();
		log.toId = to;
		log.fromId = Constant.user.UserName;
		log.msgType = 6;
		log.content = "[" + file.getName() + "]";
		log.file = file;
		return log;
	}
	
	/**
	 * 创建图片聊天记录
	 * 作者 ：肖逵
	 * 时间 ：2019年8月31日 下午12:44:42
	 * @param file
	 * @param to
	 * @return
	 */
	public static ChatLog createImageLog(File file, String to) {
		ChatLog log = new ChatLog();
		log.local = true;
		log.sent = false;
		log.createTime = System.currentTimeMillis();
		log.toId = to;
		log.fromId = Constant.user.UserName;
		log.msgType = 3;
		log.content = "[图片]";
		log.file = file;
		
		ImageLoader loader = new ImageLoader();
		loader.load(file.getAbsolutePath());
		ImageData data = loader.data[0];
		ImageNode node = new ImageNode(1, new Image(null, loader.data[0]), loader);
		log.img = node;
		//图片宽高固定不能超过200
		if(data.width > 200 || data.height > 200) {
			if(data.width > data.height) {
				Integer w = 200;
				Integer h = (int) (data.height * 200D / data.width);
				log.img.setImg(SWTTools.scaleImage(data, w, h));
			}else {
				Integer h = 200;
				Integer w = (int) (data.width * 200D / data.height);
				log.img.setImg(SWTTools.scaleImage(data, w, h));
			}
		}
		return log;
	}
	
	/**
	 * 用途：从服务器返回的数据转化成记录
	 * @date 2017年1月3日
	 * @param msg
	 * @return
	 */
	public static ChatLog fromMap(Map<String, Object> msg) {
		ChatLog log = new ChatLog();
		log.msgid = (String) msg.get("MsgId");
		log.newMsgId = (Long) msg.get("NewMsgId");
		log.msgType = (Integer) msg.get("MsgType");
		log.content = (String) msg.get("Content");
		log.fromId = (String) msg.get("FromUserName");
		log.toId = (String) msg.get("ToUserName");
		log.url = (String) msg.get("Url");
		log.voiceLength = (Integer) msg.get("VoiceLength");
		log.createTime = System.currentTimeMillis();
		IChatLogChain firstChain = new GroupChain();
		
		
		return firstChain.fromMap(log, msg);
	}
	
}
