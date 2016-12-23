package com.xk.chatlogs;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatLogCache {

	private static final String LOG_PATH = "chatlogs";
	
	private static Map<String, List<ChatLog>> cache = new HashMap<String, List<ChatLog>>();
	
	public static void saveLogs(String conv, ChatLog log) {
		if(null == conv || null == log) {
			return ;
		}
		File folder = new File(LOG_PATH + "/" + conv);
		if(!folder.exists()) {
			folder.mkdirs();
		} else if (folder.isFile()) {
			folder.delete();
			folder.mkdirs();
		}
		List<ChatLog> logs = cache.get(conv);
		if(null == logs) {
			logs = new ArrayList<ChatLog>();
			cache.put(conv, logs);
		}
		logs.add(log);
	}
	
	public static List<ChatLog> getLogs(String convs) {
		return cache.get(convs);
	}
}
