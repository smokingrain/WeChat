package com.xk.chatlogs.interfaces;

import java.util.Map;

import com.xk.chatlogs.ChatLog;

public abstract class ChatLogChain implements IChatLogChain {

	
	@Override
	public abstract ChatLog fromMap(ChatLog log, Map<String, Object> map);
	
	protected ChatLog processReturn(ChatLog log, Map<String, Object> map) {
		if(null != getNext()) {
			return getNext().fromMap(log, map);
		}
		return log;
	}
	
	public abstract IChatLogChain getNext();

}
