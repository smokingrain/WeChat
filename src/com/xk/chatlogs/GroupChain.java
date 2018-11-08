package com.xk.chatlogs;

import java.util.Map;

import com.xk.chatlogs.interfaces.ChatLogChain;
import com.xk.chatlogs.interfaces.IChatLogChain;
import com.xk.utils.Constant;

/**
 * @Description:处理群消息
 * @author:肖逵
 * @date:2018年10月13日 下午5:07:52
 */
public class GroupChain extends ChatLogChain {

	private IChatLogChain nextChain = new ImageChain();
	
	@Override
	public ChatLog fromMap(ChatLog log, Map<String, Object> map) {
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
		return processReturn(log, map);
	}

	@Override
	public IChatLogChain getNext() {
		return nextChain;
	}

}
