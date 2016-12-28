package com.xk.chatlogs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageLoader;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.xk.bean.ContactsStruct;
import com.xk.utils.Constant;
import com.xk.utils.ImageCache;
import com.xk.utils.WeChatUtil;

public class ChatLog {

	public String msgid;
	public Long newMsgId;
	public Integer msgType;
	@JsonIgnore
	public Image img;
	public String imgPath;
	public String voicePath;
	public String content;
	public String fromId;
	public String toId;
	public Long createTime;
	public Integer voiceLength;
	
	
	public static ChatLog fromMap(Map<String, Object> msg) {
		ChatLog log = new ChatLog();
		log.msgid = (String) msg.get("MsgId");
		log.newMsgId = (Long) msg.get("NewMsgId");
		log.msgType = (Integer) msg.get("MsgType");
		log.content = (String) msg.get("Content");
		log.fromId = (String) msg.get("FromUserName");
		log.toId = (String) msg.get("ToUserName");
		log.createTime = System.currentTimeMillis();
		
		if(log.fromId.startsWith("@@") && !Constant.user.UserName.equals(log.fromId)) {
			String[] splt = log.content.split(":<br/>");
			if(null != splt && splt.length == 2) {
				log.fromId = splt[0];//ContactsStruct.getGroupMember(splt[0], Constant.contacts.get(log.fromId));
				log.content = splt[1];
			}
			
		}
		
		if(log.msgType == 3 || log.msgType == 47) {
			Map<String, String> params = new HashMap<String, String>();
			params.put("MsgID", log.msgid);
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
