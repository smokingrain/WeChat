package com.xk.chatlogs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用途：存储聊天记录
 *
 * @author xiaokui
 * @date 2017年1月3日
 */
public class ChatLogCache {

	private static final String LOG_PATH = "chatlogs";
	
	private static Map<String, List<ChatLog>> cache = new ConcurrentHashMap<String, List<ChatLog>>();
	
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
	
	public static void removeLog(String convs, ChatLog log) {
		List<ChatLog> logs = getLogs(convs);
		if(null != logs) {
			logs.remove(log);
			if(null != log.img && log.img.type == 1) {
				log.img.getImg().dispose();
			}
		}
	}
	
	public static void removeConv(String convs) {
		List<ChatLog> logs = getLogs(convs);
		if(null != logs) {
			for(ChatLog log : logs) {
				if(null != log.img && log.img.type == 1) {
					log.img.getImg().dispose();
				}
			}
			logs.clear();
		}
	}
}
