package com.xk.utils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;

public class ImageCache {

	private static final String HEAD_PATH = "headscache"; 
	private static final String CHAT_PATH = "msgimages"; 
	
	private static Map<String, Image> userHeads = new HashMap<String, Image>();
	private static Map<String, Image> chatImages = new HashMap<String, Image>();
	
	public static Image getChatImage(String msgId, String url, Map<String, String> params) {
		Image temp = chatImages.get(msgId);
		if(null != temp && !temp.isDisposed()) {
			return temp;
		}
		File cache = new File(HEAD_PATH);
		return getImage(msgId, cache, userHeads, url, params);
	}
	
	
	private static Image getImage(String id, File cache, Map<String, Image> caches, String url, Map<String, String> params) {
		if(!cache.exists()) {
			cache.mkdirs();
		}else if(cache.exists() && !cache.isDirectory()) {
			cache.delete();
			cache.mkdirs();
		}
		File[] heads = cache.listFiles(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith(id);
			}
		});
		if(null != heads && heads.length > 0) {
			ImageLoader loader = new ImageLoader();
			ImageData[] datas = loader.load(heads[0].getAbsolutePath());
			if(null != datas && datas.length > 0) {
				Image img = new Image(null,datas[0]);
				userHeads.put(id, img);
				return img;
			}
		}
		HTTPUtil hu = HTTPUtil.getInstance();
		InputStream in = hu.getInput(url, params);
		try {
			ImageLoader loader = new ImageLoader();
			ImageData[] datas = loader.load(in);
			if(null != datas && datas.length > 0) {
				Image img = new Image(null,datas[0]);
				userHeads.put(id, img);
				loader.save(heads[0].getAbsolutePath(), loader.format);
				return img;
			}
		} catch (Exception e) {
			if(null != in) {
				try {
					in.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
		return null;
	}
	
	
	public static Image getUserHeadCache(String userName,String url, Map<String, String> params) {
		Image temp = userHeads.get(userName);
		if(null != temp && !temp.isDisposed()) {
			return temp;
		}
		File cache = new File(HEAD_PATH);
		return getImage(userName, cache, userHeads, url, params);
	}
	
}
