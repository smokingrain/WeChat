package com.xk.utils.chain;

import java.util.Map;

import com.xk.chatlogs.ChatLog;
import com.xk.chatlogs.ChatLogCache;
import com.xk.chatlogs.interfaces.ChatLogChain;
import com.xk.chatlogs.interfaces.IChatLogChain;
import com.xk.ui.main.MainWindow;
import com.xk.utils.Constant;

public class NotifyMsgChain extends ChatLogChain {

	private IChatLogChain next = new MessageChain();
	@Override
	public ChatLog fromMap(ChatLog log, Map<String, Object> map) {
		Integer MsgType = (Integer) map.get("MsgType");
		if(10000 == MsgType) {//通知消息
			if(null != log) {
				String FromUserName = (String) map.get("FromUserName");
				String ToUserName = (String) map.get("ToUserName");
				MainWindow main = MainWindow.getInstance();
				if(FromUserName.equals(Constant.user.UserName)){
					ChatLogCache.saveLogs(ToUserName, log);
					main.flushChatView(ToUserName, true);
				} else {
					ChatLogCache.saveLogs(FromUserName, log);
					main.flushChatView(FromUserName, true);
				}
			}
		}
		return processReturn(log, map);
	}

	@Override
	public IChatLogChain getNext() {
		return next;
	}

}
