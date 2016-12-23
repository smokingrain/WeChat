package com.xk.chatlogs;

import java.util.Map;

import org.eclipse.swt.graphics.Image;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.xk.bean.ContactsStruct;
import com.xk.utils.Constant;

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
		if(log.fromId.startsWith("@@") && !Constant.user.UserName.equals(log.fromId)) {
			String[] splt = log.content.split(":<br/>");
			log.fromId = splt[0];//ContactsStruct.getGroupMember(splt[0], Constant.contacts.get(log.fromId));
			log.content = splt[1];
		}
		
		return log;
	}
	
}
