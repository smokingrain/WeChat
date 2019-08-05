package com.xk.utils.chain;

import java.util.Map;

import com.xk.chatlogs.ChatLog;
import com.xk.chatlogs.interfaces.ChatLogChain;
import com.xk.chatlogs.interfaces.IChatLogChain;
import com.xk.utils.Constant;
import com.xk.utils.WeChatUtil;

public class VideoChain extends ChatLogChain {

	
	private IChatLogChain next;
	@Override
	public ChatLog fromMap(ChatLog log, Map<String, Object> msg) {
		Integer MsgType = (Integer) msg.get("MsgType");
		if( 43 == MsgType) {
			String FromUserName = (String) msg.get("FromUserName");
			WeChatUtil.statusNotify(Constant.user.UserName, FromUserName);
		}
		return null;
	}

	@Override
	public IChatLogChain getNext() {
		// TODO Auto-generated method stub
		return next;
	}

}
