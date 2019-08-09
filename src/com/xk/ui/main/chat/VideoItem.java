package com.xk.ui.main.chat;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.wb.swt.SWTResourceManager;
import org.jcodec.player.Player;
import org.jcodec.player.filters.FrameGrabSource;
import org.jcodec.player.filters.JCodecPacketSource;
import org.jcodec.player.filters.JSoundAudioOut;
import org.jcodec.player.filters.MediaInfo.VideoInfo;
import org.jcodec.player.filters.PacketSource;
import org.jcodec.player.filters.audio.AudioMixer;
import org.jcodec.player.filters.audio.AudioSource;
import org.jcodec.player.filters.audio.JCodecAudioSource;

import com.xk.chatlogs.ChatLog;
import com.xk.ui.main.FloatWindow;
import com.xk.uiLib.MyList;
import com.xk.uiLib.VideoViewer;
import com.xk.utils.SWTTools;
import com.xk.utils.WeChatUtil;

public class VideoItem extends ChatItem {
	
	private Image img = SWTResourceManager.getImage(getClass(), "/images/playfoc.png");
	private File video;
	
	private Player player;
	
	public VideoItem(String user,Image head, List<Object> chatContent, boolean fromSelf, Font font, ChatLog log) {
		super(user, head, chatContent, fromSelf, font, log);
	}
	
	

	@Override
	protected void drawContentL(GC gc, int start, int width) {
		super.drawContentL(gc, start, width);
		int junkY = nameHeight + LINE_SPACE_HEIGHT * 3 + LINE_SPACE_HEIGHT + MARGIN;
		int imgW = img.getImageData().width;
		int imgH = img.getImageData().height;
		int realSize = Math.min(maxWidth, getHeight() - junkY) / 2;
		
		int destX = HEAD_IMG_HEIGHT + LINE_SPACE_HEIGHT * 5 + (maxWidth - realSize) / 2;
		int destY = start + junkY + (getHeight() - junkY - realSize - MARGIN * 3) / 2;
		Color backup=gc.getForeground();
		Color white=new Color(null, 0Xff,0Xff,0Xff);
		//绘制半透明背景
		gc.setForeground(white);
		gc.setAlpha(55);
		gc.fillOval(destX, destY, realSize, realSize);
		gc.setForeground(backup);
		gc.setAlpha(255);
		gc.drawImage(img, 0, 0, imgW, imgH, destX, destY, realSize, realSize);
	}



	@Override
	protected void drawContentR(GC gc, int start, int width) {
		super.drawContentR(gc, start, width);
		int junkY = nameHeight + LINE_SPACE_HEIGHT * 2 + LINE_SPACE_HEIGHT + MARGIN;
		int imgW = img.getImageData().width;
		int imgH = img.getImageData().height;
		int realSize = Math.min(maxWidth, getHeight() - junkY) / 2;
		
		int destX = width - (HEAD_IMG_HEIGHT + LINE_SPACE_HEIGHT * 5 + maxWidth + MyList.BAR_WIDTH) + (maxWidth - realSize) / 2;
		int destY = start + junkY + (getHeight() - junkY - realSize - MARGIN * 3) / 2;
		Color backup=gc.getForeground();
		Color white=new Color(null, 0Xff,0Xff,0Xff);
		//绘制半透明背景
		gc.setForeground(white);
		gc.setAlpha(55);
		gc.fillOval(destX, destY, realSize, realSize);
		gc.setForeground(backup);
		gc.setAlpha(255);
		gc.drawImage(img, 0, 0, imgW, imgH, destX, destY, realSize, realSize);
	}



	@Override
	protected void saveContent(MouseEvent e, Menu m) {
		// TODO Auto-generated method stub
		super.saveContent(e, m);
	}



	@Override
	protected void onDoubleClick(MouseEvent e) {
		if(log.msgType == 43) {
			Point point = new Point(e.x, e.y);
			for(Rectangle rect : imgs.keySet()) {
				if(rect.contains(point)) {
					final FloatWindow fw = FloatWindow.getInstance();
					fw.init();
					fw.setSize(100, 178);
					final VideoViewer vv = new VideoViewer(fw.shell);
					fw.add(vv);
					new Thread(new Runnable() {
						
						@Override
						public void run() {
							if(null == video) {
								video = WeChatUtil.loadVideo(log.msgid, vv);
							}
							if(null == video || !video.exists()) {
								video = null;
								return;
							}
							try {
								JCodecPacketSource jcp = new JCodecPacketSource(video);
								final PacketSource videoTrack = jcp.getVideo();///http.getVideoTrack();
								FrameGrabSource fgs = null;
								if(null != videoTrack) {
									fgs = new FrameGrabSource(VideoItem.this.video, videoTrack); 
									VideoInfo vi = fgs.getMediaInfo();
									if(null != vi) {
										Display.getDefault().asyncExec(new Runnable() {
											
											@Override
											public void run() {
												fw.setSize(vi.getDim().getWidth(), vi.getDim().getHeight());
											}
										});
									}
						        }

						        List<PacketSource> audioTracks = jcp.getAudio();//http.getAudioTracks();
						        AudioSource[] audio = new AudioSource[audioTracks.size()];
						        for (int i = 0; i < audioTracks.size(); i++) {
						        	audio[i] = new JCodecAudioSource(audioTracks.get(i));
						        }
						        AudioMixer mixer = new AudioMixer(audio.length, audio);
						        
						        if(null != player) {
						        	player.destroy();
						        }
						        player = new Player(fgs, mixer, vv, new JSoundAudioOut());
						        vv.setPlayer(player);
						        player.play();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
						}
					}).start();
					fw.setTimeOut(-1L);
					SWTTools.centerWindow(fw.shell);
					SWTTools.enableTrag(vv);
					fw.open(-1, -1);
					
					return;
				}
			}
		}
	}

}
