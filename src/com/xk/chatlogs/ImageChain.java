package com.xk.chatlogs;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import com.xk.bean.ImageNode;
import com.xk.chatlogs.interfaces.ChatLogChain;
import com.xk.chatlogs.interfaces.IChatLogChain;
import com.xk.utils.Constant;
import com.xk.utils.ImageCache;

/**
 * @Description:处理图片， 表情
 * @author:肖逵
 * @date:2018年10月13日 下午5:08:06
 */
public class ImageChain extends ChatLogChain{

	private IChatLogChain nextChain = new AddFriendChain();
	
	@Override
	public ChatLog fromMap(ChatLog log, Map<String, Object> msg) {
		//获取图片或者连接
		if(log.msgType == 3 || log.msgType == 47 || log.msgType == 49) {
			Map<String, String> params = new HashMap<String, String>();
			params.put("MsgID", log.msgid);
			params.put("type", "big");
			try {
				params.put("skey", URLEncoder.encode(Constant.sign.skey, "UTF-8"));
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			if(null != log.url && !"".equals(log.url)) {
				params.put("type", "slave");
				params.put("skey", Constant.sign.skey);
				log.content = (String) msg.get("FileName");
			} else {
				log.content = 3 == log.msgType ? "[图片]" : "[表情]" ;
			}
			ImageNode temp = ImageCache.getChatImage(log.msgid, String.format(Constant.LOAD_IMG, Constant.HOST), params);
			if(temp != null) {
				log.img = temp;
			}
		}
		return processReturn(log, msg);
	}

	@Override
	public IChatLogChain getNext() {
		return nextChain;
	}
}
