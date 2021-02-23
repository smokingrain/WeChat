package com.xk.utils.pic;

import java.io.File;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.wb.swt.SWTResourceManager;
import org.opencv.core.Rect;

import com.xk.bean.ImageNode.TYPE;
import com.xk.chatlogs.ChatLog;
import com.xk.chatlogs.ChatLogCache;
import com.xk.uiLib.ICallback;
import com.xk.utils.Constant;
import com.xk.utils.hat.HatHandler;
import com.xk.utils.hat.ImageFace;
import com.xk.utils.interfaces.ICMDHandler;

public class PictureHandler implements ICMDHandler {

	@Override
	public void handle(String content, String user, ICallback<File> fileCall,
			ICallback<String> textCall, ICallback<File> imgCall) {
		List<ChatLog> logs = ChatLogCache.getLogs(user);
		if(null == logs || logs.size() < 2) {
			return;
		}
		ChatLog log = logs.get(logs.size() - 2);
		if(null == log.img) {
			return;
		}
		if(log.img.type == TYPE.IMAGE) {
			String filePath = "temp/pic" + System.currentTimeMillis() + Constant.FORMATS[SWT.IMAGE_JPEG];
			ImageLoader loader = new ImageLoader();
			loader.data = new ImageData[]{log.img.getImg().getImageData()};
			loader.save(filePath, SWT.IMAGE_JPEG);
			List<Rect> faces = ImageFace.findFaces(filePath);
			if(null == faces || faces.isEmpty()) {
				textCall.callback("找不到正脸，给不了图。");
				return;
			}
			Image greenHat = SWTResourceManager.getImage(HatHandler.class, "/images/green.png");
			ImageData id = greenHat.getImageData();
			Image img = new Image(null, filePath);
			GC gc = new GC(img);
			for(Rect rect : faces) {
				int width = rect.width;
				int realHeight = (int) (((double)width / id.width) * id.height);
				gc.drawImage(greenHat, 0, 0, id.width, id.height, (int)(rect.x - width * 0.2), (int)(rect.y - realHeight + rect.height * 0.1d), width, realHeight);
			}
			gc.dispose();
			loader.data = new ImageData[]{img.getImageData()};
			loader.save(filePath, SWT.IMAGE_JPEG);
			File targetFile = new File(filePath);
			imgCall.callback(targetFile);
			textCall.callback("来来来,存好你的新图片...");
			img.dispose();
		}
		
		
		
	}

}
