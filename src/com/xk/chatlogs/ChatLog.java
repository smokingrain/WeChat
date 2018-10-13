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
	public String url;
	public Long createTime;
	public Integer voiceLength;
	
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
		
		ImageLoader loader = new ImageLoader();
		loader.load(file.getAbsolutePath());
		ImageData data = loader.data[0];
		ImageNode node = new ImageNode(1, new Image(null, loader.data[0]), loader);
		log.img = node;
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
		
		
		//获取图片或者连接
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
			if(null != log.url && !"".equals(log.url)) {
				params.put("type", "slave");
				params.put("skey", Constant.sign.skey);
				log.content = (String) msg.get("FileName");
			} else {
				log.content = 3 == log.msgType ? "[图片]" : "[表情]" ;
			}
			ImageNode temp = ImageCache.getChatImage(log.msgid, Constant.LOAD_IMG, params);
			if(temp != null) {
				log.img = temp;
			}
			
			
			
		}
		
		return log;
	}
	
}
