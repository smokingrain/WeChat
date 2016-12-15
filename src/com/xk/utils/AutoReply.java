package com.xk.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AutoReply {
	
	public static String call(String cmd,String userid) {
		//图灵网站上的apiKey
		String apiKey = "xxx";
		//待加密的json数据
		
		Map<String, String> dataMap = new HashMap<String, String>();
		dataMap.put("key", apiKey);
		dataMap.put("info", cmd);
		dataMap.put("userid", userid);

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
		call("你好哦","test");
	}
 	
}
