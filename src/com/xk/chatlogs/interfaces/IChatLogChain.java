package com.xk.chatlogs.interfaces;

import java.util.Map;

import com.xk.chatlogs.ChatLog;


/**
 * @Description:处理消息，生成chatlog
 * @author:肖逵
 * @date:2018年10月13日 下午5:07:24
 */
public interface IChatLogChain {

	public ChatLog fromMap(ChatLog log, Map<String, Object> map);
	
}
