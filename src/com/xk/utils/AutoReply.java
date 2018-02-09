package com.xk.utils;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jsoup.helper.StringUtil;

import com.xk.uiLib.ICallback;
import com.xk.utils.hat.HatHandler;
import com.xk.utils.interfaces.ICMDHandler;
import com.xk.utils.song.SongHandler;

public class AutoReply {
	
	private static ExecutorService handleService = Executors.newFixedThreadPool(8);
	
	public static void call(String text, String userid, ICallback<File> fileCall, ICallback<String> textCall, ICallback<File> imgCall) {
		if(StringUtil.isBlank(text)) {
			textCall.callback("说人话！！");
			return;
		}
		if(!text.contains(":") || text.indexOf(":") == text.length() - 1) {
			String reply = call(text, userid);
			textCall.callback(reply);
			return;
		}
		String[] txts = text.split(":");
		String cmd = txts[0];
		if(!Constant.SUPPORTED_CMD.contains(cmd)) {
			String reply = call(text, userid);
			textCall.callback(reply);
			return;
		}
		String content = text.substring(cmd.length() + 1, text.length()).trim();
		ICMDHandler handler = getHandler(cmd);
		CMDTask task = new CMDTask(handler);
		task.content = content;
		task.user = userid;
		task.fileCall = fileCall;
		task.textCall = textCall;
		task.imgCall = imgCall;
		handleService.submit(task);
		
	}
	
	private static String call(String cmd,String userid) {
		//图灵网站上的apiKey
		String apiKey = "0ead20e9c92e4a39ba6cfd9fe75dd28c";
		//待加密的json数据
		
		Map<String, String> dataMap = new HashMap<String, String>();
		try {
			dataMap.put("key", URLEncoder.encode(apiKey, "UTF-8"));
			dataMap.put("info", URLEncoder.encode(cmd, "UTF-8"));
			dataMap.put("userid", URLEncoder.encode(userid, "UTF-8"));
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		

		HTTPUtil hu = HTTPUtil.getInstance();
		try {
			String result = hu.getJsonfromURL2("http://www.tuling123.com/openapi/api", dataMap);
			Map<String, Object> map = JSONUtil.fromJson(result);
			if(new Integer(100000).equals(map.get("code"))) {
				return map.get("text").toString();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "我居然不明白你说什么！！";
	}
	
	public static void main(String[] args) {
		System.out.println(call("你好哦","test"));
	}
 	
	public static ICMDHandler getHandler(String cmd) {
		if("歌曲".equals(cmd)) {
			return new SongHandler();
		} if("圣诞帽".equals(cmd)) {
			return new HatHandler();
		}
		return null;
	}
	
	private static class CMDTask implements Callable<Void>{
		String content;
		String user;
		ICallback<File> fileCall;
		ICallback<String> textCall;
		ICallback<File> imgCall;
		ICMDHandler hander;
		
		
		CMDTask(ICMDHandler hander) {
			this.hander = hander;
		}
		
		@Override
		public Void call() throws Exception {
			hander.handle(content, user, fileCall, textCall, imgCall);
			return null;
		}
		
	}
	
}
