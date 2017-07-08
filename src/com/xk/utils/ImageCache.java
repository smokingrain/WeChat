package com.xk.utils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
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
import com.xk.uiLib.ICallback;

public class ImageCache {

	private static final List<String> ALLOWED_TYPES = Arrays.asList(Constant.FORMATS);
	private static final String HEAD_PATH = "headscache"; 
	private static final String CHAT_PATH = "msgimages"; 
	
	private static Map<String, Image> userHeads = new ConcurrentHashMap<String, Image>();
	private static Map<String, Image> chatImages = new ConcurrentHashMap<String, Image>();
	
	private static ExecutorService service = Executors.newFixedThreadPool(20);
	
	public static void asyncLoadPicture(final ContactsStruct convs, final ICallback callBack) {
		service.submit(new Runnable() {
			
			@Override
			public void run() {
				String headUrl = Constant.BASE_URL + convs.HeadImgUrl;
				if("你去那边吃屎".equals(convs.NickName)) {
					System.out.println("url = " + headUrl);
				}
				try {
					convs.head = ImageCache.getUserHeadCache(convs.UserName, headUrl, null, null, null);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if("你去那边吃屎".equals(convs.NickName)) {
					System.out.println(headUrl);
					System.out.println(convs.head);
				}
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
					userHeads.put(id, img);
					count++;
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("load " + count + " caches!!");
	}
	
	public static Image getChatImage(String msgId, File file) {
		Image temp = chatImages.get(msgId);
		if(null != temp && !temp.isDisposed()) {
			return temp;
		}
		Image image = new Image(null, file.getAbsolutePath());
		if(image.getImageData().width > 200 || image.getImageData().height > 200) {
			if(image.getImageData().width > image.getImageData().height) {
				Integer w = 200;
				Integer h = (int) (image.getImageData().height * 200D / image.getImageData().width);
				Image tmp = SWTTools.scaleImage(image.getImageData(), w, h);
				chatImages.put(msgId, tmp);
				return tmp;
			}else {
				Integer h = 200;
				Integer w = (int) (image.getImageData().width * 200D / image.getImageData().height);
				Image tmp = SWTTools.scaleImage(image.getImageData(), w, h);
				chatImages.put(msgId, tmp);
				return tmp;
			}
		}
		
		return image;
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
	public static Image getChatImage(String msgId, String url, Map<String, String> params,Integer width, Integer height) {
		Image temp = chatImages.get(msgId);
		if(null != temp && !temp.isDisposed()) {
			if(null != width && null != height && (temp.getImageData().width != width || temp.getImageData().height != height)) {
				Image img = SWTTools.scaleImage(temp.getImageData(), width, height);
				chatImages.put(msgId, img);
				temp.dispose();
				return img;
			}
			return temp;
		}
		File cache = new File(CHAT_PATH);
		return getImage(msgId, cache, chatImages, url, params, width, height);
	}
	
	
	private static Image getImage(final String id, File cache, Map<String, Image> caches, String url, Map<String, String> params, Integer width, Integer height) {
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
					if(null != width && null != height && (datas[0].width != width || datas[0].height != height)) {
						Image img = SWTTools.scaleImage(datas[0], width, height);
						caches.put(id, img);
						return img;
					}else {
						Image img = new Image(null,datas[0]);
						caches.put(id, img);
						return img;
					}
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
				if(null != width && null != height && (datas[0].width != width || datas[0].height != height)) {
					Image img = SWTTools.scaleImage(datas[0], width, height);
					caches.put(id, img);
//					File dest = new File(cache, id + Constant.FORMATS[loader.format]);
//					loader.save(dest.getAbsolutePath(), loader.format);
					return img;
				}else {
					Image img = new Image(null,datas[0]);
					caches.put(id, img);
//					File dest = new File(cache, id + Constant.FORMATS[loader.format]);
//					loader.save(dest.getAbsolutePath(), loader.format);
					return img;
				}
			}
		} catch (Exception e) {
			System.out.println(url);
			e.printStackTrace();
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
	
	
	public static Image getUserHeadCache(String userName,String url, Map<String, String> params, Integer width, Integer height) {
		Image temp = userHeads.get(userName);
		if(null != temp && !temp.isDisposed()) {
			if(null !=width && null != height && (temp.getImageData().width != width || temp.getImageData().height != height)) {
				Image img = SWTTools.scaleImage(temp.getImageData(), width, height);
				userHeads.put(userName, img);
				return img;
			}
			return temp;
		}
		File cache = new File(HEAD_PATH);
		return getImage(userName, cache, userHeads, url, params, width, height);
	}
	
}
