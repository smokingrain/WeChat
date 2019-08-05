package com.xk.utils.chain;

import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;

import com.xk.chatlogs.ChatLog;
import com.xk.chatlogs.ChatLogCache;
import com.xk.chatlogs.interfaces.ChatLogChain;
import com.xk.chatlogs.interfaces.IChatLogChain;
import com.xk.ui.main.MainWindow;

public class RevokeMsgChain extends ChatLogChain {
	private IChatLogChain next = new NotifyMsgChain();
	
	@Override
	public ChatLog fromMap(ChatLog log, Map<String, Object> map) {
		Integer MsgType = (Integer) map.get("MsgType");
		if(10002 == MsgType) {
			String FromUserName = (String) map.get("FromUserName");
			MainWindow main = MainWindow.getInstance();
			String xml = log.content.replace("&lt;", "<").replace("&gt;", ">");
			Document doc;
			try {
				doc = DocumentHelper.parseText(xml);
			} catch (DocumentException e1) {
				e1.printStackTrace();
				return null;
			}
			String oldid = null;
			try {
				oldid = doc.getRootElement().element("revokemsg").element("msgid").getText();
			} catch (Exception e) {
			}
			for(ChatLog his : ChatLogCache.getLogs(FromUserName)) {
				if(his.msgid.equals(oldid)) {
					his.recalled = true;
					break;
				}
			}
			main.flushChatView(FromUserName, true);
		}
		return processReturn(log, map);
	}

	@Override
	public IChatLogChain getNext() {
		return next;
	}

}
