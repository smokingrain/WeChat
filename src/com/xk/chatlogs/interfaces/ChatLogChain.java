package com.xk.chatlogs.interfaces;

import java.util.Map;

import com.xk.chatlogs.ChatLog;

/**
 * 消息处理链
 * @author Administrator
 *
 */
public abstract class ChatLogChain implements IChatLogChain {

	
	@Override
	public abstract ChatLog fromMap(ChatLog log, Map<String, Object> map);
	
	/**
	 * 如果还有下一个，就调用，否则直接返回结果
	 * 作者 ：肖逵
	 * 时间 ：2019年8月31日 下午12:40:13
	 * @param log
	 * @param map
	 * @return
	 */
	protected ChatLog processReturn(ChatLog log, Map<String, Object> map) {
		if(null != getNext()) {
			return getNext().fromMap(log, map);
		}
		return log;
	}
	
	public abstract IChatLogChain getNext();

}
