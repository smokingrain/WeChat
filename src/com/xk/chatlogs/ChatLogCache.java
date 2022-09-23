package com.xk.chatlogs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.xk.bean.ImageNode.TYPE;

/**
 * 用途：存储聊天记录
 *
 * @author xiaokui
 * @date 2017年1月3日
 */
public class ChatLogCache {

	private static final String LOG_PATH = "chatlogs";
	
	private static Map<String, List<ChatLog>> cache = new ConcurrentHashMap<String, List<ChatLog>>();
	
	private static AtomicLong logSeq = new AtomicLong(0L);
	
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
		log.seqNum = logSeq.incrementAndGet();
		List<ChatLog> logs = cache.get(conv);
		if(null == logs) {
			logs = new ArrayList<ChatLog>();
			cache.put(conv, logs);
		}
		//此处要存数据库
		logs.add(log);
	}
	
	public static List<ChatLog> getLogs(String convs) {
		return cache.get(convs);
	}
	
	public static void removeLog(String convs, ChatLog log) {
		List<ChatLog> logs = getLogs(convs);
		if(null != logs) {
			//此处要存数据库
			logs.remove(log);
			if(null != log.img && log.img.type == TYPE.IMAGE) {
				log.img.getImg().dispose();
			}
		}
	}
	
	public static void removeConv(String convs) {
		List<ChatLog> logs = getLogs(convs);
		if(null != logs) {
			for(ChatLog log : logs) {
				if(null != log.img && log.img.type == TYPE.IMAGE) {
					log.img.getImg().dispose();
				}
			}
			logs.clear();
		}
	}
}
