package com.xk.utils.chain;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.xk.chatlogs.ChatLog;
import com.xk.chatlogs.interfaces.ChatLogChain;
import com.xk.chatlogs.interfaces.IChatLogChain;
import com.xk.ui.main.MainWindow;
import com.xk.utils.WeChatUtil;

public class SysMsgChain extends ChatLogChain {

	private IChatLogChain next = new RevokeMsgChain();
	
	
	@Override
	public ChatLog fromMap(ChatLog log, Map<String, Object> msg) {
		Integer MsgType = (Integer) msg.get("MsgType");
		if(51 == MsgType) {
			String StatusNotifyUserName = (String) msg.get("StatusNotifyUserName");
			MainWindow main = MainWindow.getInstance();
			if(null != StatusNotifyUserName && !main.syncGroup) {
				String[] spl = StatusNotifyUserName.split(",");
				List<String> groups = Arrays.asList(spl);
				WeChatUtil.loadGroups(groups);
				main.showGroupsAndFriends();
				main.syncGroup = true;
			}
		}
		return processReturn(log, msg);
	}

	@Override
	public IChatLogChain getNext() {
		// TODO Auto-generated method stub
		return next;
	}

}
