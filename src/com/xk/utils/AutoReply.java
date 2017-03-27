package com.xk.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class AutoReply {
	
	public static String call(String cmd,String userid) {
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
 	
}
