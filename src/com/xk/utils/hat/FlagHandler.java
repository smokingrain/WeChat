package com.xk.utils.hat;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.wb.swt.SWTResourceManager;

import com.xk.bean.ContactsStruct;
import com.xk.uiLib.ICallback;
import com.xk.utils.Constant;
import com.xk.utils.interfaces.ICMDHandler;

public class FlagHandler implements ICMDHandler {

	@Override
	public void handle(String content, String user, ICallback<File> fileCall,
			ICallback<String> textCall, ICallback<File> imgCall) {
		ContactsStruct target = Constant.getContact(user);
		if(null == target) {
			textCall.callback("没有找到你的头像");
			return;
		}
		if(null == target.head) {
			textCall.callback("老哥，你没有头像P个jj");
			return;
		}
		ImageLoader loader = new ImageLoader();
		ImageData source = target.head.getImageData();
		loader.data = new ImageData[]{source};
		Image flag = SWTResourceManager.getImage(HatHandler.class, "/images/China.png");
		String pathTarget = "temp/target" + target.NickName + Constant.FORMATS[SWT.IMAGE_JPEG];

		Image img = new Image(null, source);
		GC gc = new GC(img);
		
		int width = source.width;
		int height = source.height;
		ImageData flagData = flag.getImageData();
		int realHeight = (int) (flagData.height * ((width / 3.0d) / flagData.width));
		
		gc.drawImage(flag, 0, 0, flagData.width, flagData.height, width - width / 3, height - realHeight, width / 3, realHeight);
		
		gc.dispose();
		loader.data = new ImageData[]{img.getImageData()};
		loader.save(pathTarget, SWT.IMAGE_JPEG);
		File targetFile = new File(pathTarget);
		imgCall.callback(targetFile);
		textCall.callback("来来来,存好你的新头像...");
		img.dispose();
	}

}
