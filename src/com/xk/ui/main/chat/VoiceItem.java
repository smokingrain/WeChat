package com.xk.ui.main.chat;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.widgets.Display;
import org.eclipse.wb.swt.SWTResourceManager;

import com.xk.bean.StringNode;
import com.xk.chatlogs.ChatLog;
import com.xk.player.core.BasicController;
import com.xk.player.core.BasicPlayer;
import com.xk.player.core.BasicPlayerEvent;
import com.xk.player.core.BasicPlayerException;
import com.xk.player.core.BasicPlayerListener;
import com.xk.uiLib.MyList;
import com.xk.utils.WeChatUtil;

public class VoiceItem extends ChatItem implements BasicPlayerListener{
	
	private static BasicPlayer player;
	private static ExecutorService executor;
	
	static{
		executor = Executors.newFixedThreadPool(3);
	}
	
	private int stat = -1;//-1,未下载，0,下载中。1,2,3,4播放中
	private long playedMill = 0;
	private File file;

	public VoiceItem(String user,Image head, List<Object> chatContent, boolean fromSelf, Font font, ChatLog log) {
		super(user, head, chatContent, fromSelf, font, log);
	}
	
	@Override
	public int getHeight() {
		if(height == -1) {
			int maxHeight = 18;
			if(getLog().voiceLength > 25 * 1000) {//25秒以上的语音固定宽度
				maxWidth = 300;
			} else {
				maxWidth = 50 + getLog().voiceLength / 100;
			}
			
			Font ft = SWTResourceManager.getFont("宋体", 12, SWT.NORMAL);
			GC nameGC = new GC(getParent());
			nameGC.setFont(ft);
			nameHeight = nameGC.textExtent("test").y;
			nameGC.dispose();
			//首尾间隙,行间距
			allHeight += maxHeight + HEAD_FOOT_SAPCE;
			//名字高度
			int tempHeight = allHeight + nameHeight + LINE_SPACE_HEIGHT;
			if(tempHeight < HEAD_IMG_HEIGHT) {
				height = HEAD_IMG_HEIGHT;
			}else {
				height = tempHeight;
			}
			nameGC.dispose();
		}

		return height + HEAD_FOOT_SAPCE;
	}

	@Override
	protected void drawContentL(GC gc, int start, int width) {
		int destX = HEAD_IMG_HEIGHT + LINE_SPACE_HEIGHT * 3 + MARGIN;
		int destY = start + nameHeight + LINE_SPACE_HEIGHT * 1 + LINE_SPACE_HEIGHT + MARGIN;
		
		int textX = destX + maxWidth;
		String lengthText = Math.round(getLog().voiceLength / 1000d) + "\"";
		gc.drawText(lengthText, textX, destY, StringNode.DRAW_FLAGS);
		//先将画笔范围固定在一个等腰三角形，再以顶点为圆心画圆即可画成语音消息样式
		int[] pointArray = new int[]{destX, destY + 12, destX + 25, destY, destX + 25, destY + 25};
		Region region = new Region();
		region.add(pointArray);
		gc.setClipping(region);
		gc.setLineWidth(2);
		gc.setForeground(SWTResourceManager.getColor(0x32, 0xCD, 0x32));
		if(stat < 2) {
			gc.setForeground(SWTResourceManager.getColor(0xAb, 0xAB, 0xAB));
		}
		gc.drawOval(destX - 8, destY + 12 - 8, 16, 16);
		if(stat < 3) {
			gc.setForeground(SWTResourceManager.getColor(0xAb, 0xAB, 0xAB));
		}
		gc.drawOval(destX - 14, destY + 12 - 14, 28, 28);
		if(stat < 4) {
			gc.setForeground(SWTResourceManager.getColor(0xAb, 0xAB, 0xAB));
		}
		gc.drawOval(destX - 20, destY + 12 - 20, 40, 40);
		region.dispose();
	}

	@Override
	protected void drawContentR(GC gc, int start, int width) {
		int destX = width - (HEAD_IMG_HEIGHT + LINE_SPACE_HEIGHT * 5 + MyList.BAR_WIDTH - MARGIN) - MARGIN - 25;
		int destY = start + nameHeight + LINE_SPACE_HEIGHT + LINE_SPACE_HEIGHT + MARGIN;
		
		int textX = width - (HEAD_IMG_HEIGHT + LINE_SPACE_HEIGHT * 5 + maxWidth + MyList.BAR_WIDTH + MARGIN);
		String lengthText = Math.round(getLog().voiceLength / 1000d) + "\"";
		Point size = gc.textExtent(lengthText, StringNode.DRAW_FLAGS);
		gc.drawText(lengthText, textX - size.x, destY, StringNode.DRAW_FLAGS);
		int[] pointArray = new int[]{destX, destY, destX + 25, destY + 12, destX, destY + 25};
		Region region = new Region();
		region.add(pointArray);
		gc.setClipping(region);
		gc.setLineWidth(2);
		gc.setForeground(SWTResourceManager.getColor(0x32, 0xCD, 0x32));
		if(stat < 2) {
			gc.setForeground(SWTResourceManager.getColor(0xAb, 0xAB, 0xAB));
		}
		gc.drawOval(destX + 25 - 8, destY + 12 - 8, 16, 16);
		if(stat < 3) {
			gc.setForeground(SWTResourceManager.getColor(0xAb, 0xAB, 0xAB));
		}
		gc.drawOval(destX + 25 - 14, destY + 12 - 14, 28, 28);
		if(stat < 4) {
			gc.setForeground(SWTResourceManager.getColor(0xAb, 0xAB, 0xAB));
		}
		gc.drawOval(destX + 25 - 20, destY + 12 - 20, 40, 40);
		region.dispose();
	}

	@Override
	public boolean oncliek(MouseEvent e, int itemHeight, int index, int type) {
		if(e.button == 3 && e.count == 1) {//右键
			onRightClick(e);
		}else if(e.button == 1 && e.count == 2 && type == MyList.CLICK_DOUBLE) {//双击
			onDoubleClick(e);
		} else if(e.button == 1) {
			playVoice();
		}
		return false;
	}
	
	private void playVoice() {
		executor.submit(new Runnable() {
			
			@Override
			public void run() {
				boolean playing = stat > 0;
				if(null != player) {
					try {
						player.stop();
						player.close();
						player = null;
					} catch (BasicPlayerException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return;
					}
				}
				if(playing || stat == 0) {//防止快速点击
					return;
				}
				stat = 0;
				if(null == file || !file.exists()) {
					file = WeChatUtil.loadVoice(getLog().msgid, null);
				}
				
				player = new BasicPlayer();
				player.addBasicPlayerListener(VoiceItem.this);
				try {
					player.open(file, new HashMap<String, Object>());
					player.play();
				} catch (BasicPlayerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}

	@Override
	public void opened(Object stream, Map<String, Object> properties) {
		stat = 1;
		Display.getDefault().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				getParent().flush();
			}
		});
		
	}

	@Override
	public void progress(int bytesread, long microseconds, byte[] pcmdata,
			Map<String, Object> properties) {
		if(microseconds / 1000 - playedMill > 500) {
			playedMill = microseconds / 1000;
			stat++;
			if(stat > 4) {
				stat = 1;
			}
			Display.getDefault().asyncExec(new Runnable() {
				
				@Override
				public void run() {
					getParent().flush();
				}
			});
		}
		
	}

	@Override
	public void stateUpdated(BasicPlayerEvent event) {
		switch(event.getCode()) {
		case BasicPlayerEvent.STOPPED :
			stat = -1;
			playedMill = 0;
			System.out.println("播放停止了！");
			Display.getDefault().asyncExec(new Runnable() {
				
				@Override
				public void run() {
					getParent().flush();
				}
			});
			break;
			default:break;
		}
		
	}

	@Override
	public void setController(BasicController controller) {
		// TODO Auto-generated method stub
		
	}

}
