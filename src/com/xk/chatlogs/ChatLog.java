package com.xk.chatlogs;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.graphics.Image;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
	public String voicePath;
	public String content;
	public String fromId;
	public String toId;
	public Long createTime;
	public Integer voiceLength;
	
	@JsonIgnore
	public Image img;
	
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
	
	public static ChatLog createSimpleLog(String msg, String to) {
		ChatLog log = new ChatLog();
		log.createTime = System.currentTimeMillis();
		log.toId = to;
		log.fromId = Constant.user.UserName;
		log.msgType = 1;
		log.content = msg;
		return log;
	}
	
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
		
		Image image = new Image(null, file.getAbsolutePath());
		log.img = image;
		if(image.getImageData().width > 200 || image.getImageData().height > 200) {
			if(image.getImageData().width > image.getImageData().height) {
				Integer w = 200;
				Integer h = (int) (image.getImageData().height * 200D / image.getImageData().width);
				log.img = SWTTools.scaleImage(image.getImageData(), w, h);
			}else {
				Integer h = 200;
				Integer w = (int) (image.getImageData().width * 200D / image.getImageData().height);
				log.img = SWTTools.scaleImage(image.getImageData(), w, h);
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
		log.createTime = System.currentTimeMillis();
		
		//获取群消息发送者
		if(log.fromId.startsWith("@@") && !Constant.user.UserName.equals(log.fromId)) {
			String[] splt = log.content.split(":<br/>");
			if(null != splt && splt.length > 0) {
				log.fromId = splt[0];//ContactsStruct.getGroupMember(splt[0], Constant.contacts.get(log.fromId));
				if(splt.length > 1) {
					log.content = splt[1];
				}
				
			}
			
		}
		
		//获取图片
		if(log.msgType == 3 || log.msgType == 47 || log.msgType == 49) {
			Map<String, String> params = new HashMap<String, String>();
			params.put("MsgID", log.msgid);
			params.put("type", "big");
			try {
				params.put("skey", URLEncoder.encode(Constant.sign.skey, "UTF-8"));
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			Image temp = ImageCache.getChatImage(log.msgid, Constant.LOAD_IMG, params, null, null);
			if(temp != null) {
				if(temp.getImageData().width > 200 || temp.getImageData().height > 200) {
					if(temp.getImageData().width > temp.getImageData().height) {
						Integer width = 200;
						Integer height = (int) (temp.getImageData().height * 200D / temp.getImageData().width);
						log.img = ImageCache.getChatImage(log.msgid, Constant.LOAD_IMG, params, width, height);
					}else {
						Integer height = 200;
						Integer width = (int) (temp.getImageData().width * 200D / temp.getImageData().height);
						log.img = ImageCache.getChatImage(log.msgid, Constant.LOAD_IMG, params, width, height);
					}
					
				}else {
					log.img = temp;
				}
				
			}
			log.content = 3 == log.msgType ? "[图片]" : "[表情]" ;
			
		}
		
		return log;
	}
	
}
