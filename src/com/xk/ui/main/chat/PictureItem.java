package com.xk.ui.main.chat;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledTextUtils;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.wb.swt.SWTResourceManager;

import com.xk.bean.ImageNode;
import com.xk.bean.ImageNode.TYPE;
import com.xk.uiLib.FakeTooltips;
import com.xk.uiLib.ListItem;
import com.xk.utils.HTTPUtil;
import com.xk.utils.song.SongLocation;

public class PictureItem extends ListItem {
	
	public static ExecutorService loadPicture = Executors.newFixedThreadPool(3);
	private static Image emptyPic=SWTResourceManager.getImage(PictureItem.class, "/images/emptyPic.png");
	private ChatComp cc;
	private ImageLoader[] pictures;
	
	public PictureItem(ChatComp cc, List<String> srcs) {
		if(loadPicture.isShutdown()) {
			loadPicture = Executors.newSingleThreadExecutor();
		}
		this.cc = cc;
		this.pictures = new ImageLoader[srcs.size()];
		for(int i = 0; i < srcs.size(); i++) {
			loadPicture.submit(new LoadPictureTask(i, pictures, srcs.get(i)));
		}
	}
	
	
	@Override
	public int getHeight() {
		return 60;
	}

	@Override
	public void draw(GC gc, int start, int width, int index) {
		int indix = 0;
		for(ImageLoader loader : pictures) {
			if(null != loader) {
				Image img = new Image(null, loader.data[0]);
				gc.drawImage(img, 0, 0, loader.data[0].width, loader.data[0].height, 60 * indix + 17, start + 1, 60, 60);
				img.dispose();
			} else {
				ImageData data = emptyPic.getImageData();
				gc.drawImage(emptyPic, 0, 0, data.width, data.height, 60 * indix + 17, start + 1, 60, 60);
			}
			indix++;
		}

	}
	
	

	@Override
	public void onMove(MouseEvent e, int itemHeight, int index) {
		if(e.x < 17) {
			FakeTooltips.getInstance().hide();
			return;
		}
		int indix = (e.x - 17) / 60;
		if(indix < pictures.length && null != pictures[indix]) {
			Point loc = getParent().toDisplay(0, 0);
			int width = pictures[indix].logicalScreenWidth == 0 ? pictures[indix].data[0].width : pictures[indix].logicalScreenWidth;
			int height = pictures[indix].logicalScreenHeight == 0 ? pictures[indix].data[0].height : pictures[indix].logicalScreenHeight;
			Rectangle rect = new Rectangle(loc.x + e.x, loc.y - height - 35, width, height);
			FakeTooltips toolTips = FakeTooltips.getInstance();
			if(!toolTips.inited() || !pictures[indix].equals(toolTips.getBind())) {
				toolTips.init(rect, pictures[indix]);
			} else {
				toolTips.setLocation(rect.x, rect.y);
			}
		} else {
			FakeTooltips.getInstance().hide();
		}
	}


	@Override
	public boolean oncliek(MouseEvent e, int itemHeight, int index, int type) {
		if(e.x < 17 || e.button != 1) {
			return false;
		}
		int indix = (e.x - 17) / 60;
		if(indix < pictures.length && null != pictures[indix]) {
			Image img = new Image(null, pictures[indix].data[0]);
			ImageNode node = new ImageNode(TYPE.IMAGE, img, pictures[indix], null);
			StyledTextUtils.addImage(cc.getText(), node);
			FakeTooltips.getInstance().hide();
		}
		return false;
	}
	
	private class LoadPictureTask implements Runnable {
		private int index;
		private ImageLoader[] pictures;
		private String url;
		LoadPictureTask(int index, ImageLoader[] pictures, String url) {
			this.index = index;
			this.pictures = pictures;
			this.url = url;
		}
		
		@Override
		public void run() {
			SongLocation in = HTTPUtil.getInstance("picture").getInput(url);
			if(null != in && null != in.input) {
				ImageLoader loader = new ImageLoader();
				loader.load(in.input);
				pictures[index] = loader;
				try {
					in.input.close();
					if(null != in.response) {
						in.response.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			Display.getDefault().asyncExec(new Runnable() {
				
				@Override
				public void run() {
					PictureItem.this.getParent().flush();
				}
			});
			
			
		}
		
	}

}
