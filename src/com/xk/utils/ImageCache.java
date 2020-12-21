package com.xk.utils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;

import com.xk.bean.ContactsStruct;
import com.xk.bean.ImageNode;
import com.xk.uiLib.ICallback;

public class ImageCache {

	private static final List<String> ALLOWED_TYPES = Arrays.asList(Constant.FORMATS);
	private static final String HEAD_PATH = "headscache"; 
	private static final String CHAT_PATH = "msgimages"; 
	
	private static Map<String, ImageNode> userHeads = new ConcurrentHashMap<String, ImageNode>();
	private static Map<String, ImageNode> chatImages = new ConcurrentHashMap<String, ImageNode>();
	
	private static List<String> failedImg = new ArrayList<String>();
	
	private static ExecutorService service = Executors.newFixedThreadPool(20);
	
	public static void asyncLoadPicture(final ContactsStruct convs, final ICallback callBack) {
		service.submit(new Runnable() {
			
			@Override
			public void run() {
				String headUrl = String.format(Constant.BASE_URL, Constant.HOST) + convs.HeadImgUrl;
				convs.head = ImageCache.getUserHeadCache(convs.UserName, headUrl, null).getImg();
				callBack.callback(convs);
			}
		});
	}
	
	
	public static void loadHeadCache() {
		File cache = new File(HEAD_PATH);
		if(!cache.exists()) {
			cache.mkdirs();
		}else if(cache.exists() && !cache.isDirectory()) {
			cache.delete();
			cache.mkdirs();
		}
		
		File[] heads = cache.listFiles(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				if(!name.contains(".")) {
					return false;
				}
				String subfix = name.substring(name.indexOf("."));
				return ALLOWED_TYPES.contains(subfix);
			}
		});
		int count = 0;
		for(File file : heads) {
			try {
				String name = file.getName();
				String id = name.substring(0, name.indexOf("."));
				ImageLoader loader = new ImageLoader();
				ImageData[] datas = loader.load(file.getAbsolutePath());
				if(null != datas && datas.length > 0) {
					Image img = new Image(null,datas[0]);
					ImageNode node = new ImageNode(1, img, loader, null);
					userHeads.put(id, node);
					count++;
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("load " + count + " caches!!");
	}
	
	public static ImageNode getChatImage(String msgId, File file) {
		ImageNode temp = chatImages.get(msgId);
		if(null != temp && !temp.getImg().isDisposed()) {
			return temp;
		}
		ImageLoader loader = new ImageLoader();
		loader.load(file.getAbsolutePath());
		ImageData data = loader.data[0];
		ImageNode node = new ImageNode(1, new Image(null, loader.data[0]), loader, null);
		if(data.width > 200 || data.height > 200) {
			if(data.width > data.height) {
				Integer w = 200;
				Integer h = (int) (data.height * 200D / data.width);
				node.setImg(SWTTools.scaleImage(data, w, h));
				chatImages.put(msgId, node);
			}else {
				Integer h = 200;
				Integer w = (int) (data.width * 200D / data.height);
				node.setImg(SWTTools.scaleImage(data, w, h));
				chatImages.put(msgId, node);
			}
		}
		return node;
	}
	
	
	
	/**
	 * 用途：获取聊天图片，有缓存优先加载缓存
	 * @date 2017年1月12日
	 * @param msgId
	 * @param url
	 * @param params
	 * @param width
	 * @param height
	 * @return
	 */
	public static ImageNode getChatImage(String msgId, String url, Map<String, String> params) {
		ImageNode temp = chatImages.get(msgId);
		if(null != temp && !temp.getImg().isDisposed()) {
			return temp;
		}
		File cache = new File(CHAT_PATH);
		return getImage(msgId, cache, chatImages, url, params);
	}
	
	
	private static ImageNode getImage(final String id, File cache, Map<String, ImageNode> caches, String url, Map<String, String> params) {
		if(failedImg.contains(id)) {
			return null;
		}
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
			try {
				ImageLoader loader = new ImageLoader();
				ImageData[] datas = loader.load(heads[0].getAbsolutePath());
				if(null != datas && datas.length > 0) {
					ImageNode node = new ImageNode(1, new Image(null, datas[0]), loader, null);
					caches.put(id, node);
					return node;
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		HTTPUtil hu = HTTPUtil.getInstance();
		InputStream in = hu.getInput(url, params);
		try {
			ImageLoader loader = new ImageLoader();
			ImageData[] datas = loader.load(in);
			if(null != datas && datas.length > 0) {
				ImageNode node = new ImageNode(1, new Image(null, datas[0]), loader, null);
				caches.put(id, node);
				return node;
			}
		} catch (Exception e) {
			System.out.println(url);
			e.printStackTrace();
			failedImg.add(id);
		} finally {
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
	
	
	public static ImageNode getUserHeadCache(String userName,String url, Map<String, String> params) {
		ImageNode temp = userHeads.get(userName);
		if(null != temp && !temp.getImg().isDisposed()) {
			return temp;
		}
		File cache = new File(HEAD_PATH);
		return getImage(userName, cache, userHeads, url, params);
	}
	
}
