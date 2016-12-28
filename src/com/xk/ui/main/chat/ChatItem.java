package com.xk.ui.main.chat;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.wb.swt.SWTResourceManager;

import com.xk.uiLib.ListItem;
import com.xk.uiLib.MyList;


public class ChatItem extends ListItem {

	private static final Integer ITEM_AREA_WIDTH = 450;
	private static final Integer LINE_SPACE_HEIGHT = 2;
	private static final Integer HEAD_FOOT_SAPCE = 15;
	private static final Integer HEAD_IMG_HEIGHT = 50;
	private static final Integer MARGIN = 5;
	
	private String user;
	private Image head;
	private List<Object> chatContent;
	private boolean fromSelf;
	private Font font;
	private int height = -1;//总高度
	private int lineNum = 0;
	private int maxWidth = 0;//气泡宽度
	private int nameHeight = 0;//名字高度
	private int allHeight = MARGIN;//气泡高度
	
	public ChatItem(String user,Image head, List<Object> chatContent, boolean fromSelf, Font font) {
		this.user = user;
		this.head = head;
		this.chatContent = chatContent;
		this.fromSelf = fromSelf;
		this.font = font;
	}
	
	
	@Override
	public int getHeight() {
		if(height == -1) {
			GC gc = new GC(getParent());
			gc.setFont(font);
			int allLength = 0;
			int maxHeight = 0;//本行最高高度
			//计算每一行行高
			for(Object content : chatContent) {
				if(content instanceof Image) {
					Image img = (Image) content;
					int width = img.getImageData().width;
					if(maxWidth < width) {
						maxWidth = width + 10 + MARGIN;
					}
					int height = img.getImageData().height;
					allLength += width;
					if(allLength > ITEM_AREA_WIDTH) {
						maxWidth = ITEM_AREA_WIDTH + 10;
						allHeight += maxHeight + LINE_SPACE_HEIGHT * 2 + LINE_SPACE_HEIGHT * 2;
						lineNum++;
						allLength = width;
						maxHeight = 0;
						continue;
					}
					if(maxHeight < height) {
						maxHeight = height;
					}
				}else if(content instanceof String) {
					String str = ((String) content).replace("\n", "").replace("\r", "");
					Point point = gc.stringExtent(str);
					if(maxWidth < point.x) {
						maxWidth = point.x + str.length() + 10;//字间距
					}
					allLength += point.x + str.length();//字间距
					if(allLength > ITEM_AREA_WIDTH) {
						maxWidth = ITEM_AREA_WIDTH + 10;
						int num = allLength / ITEM_AREA_WIDTH;
						lineNum += num;
						allHeight += maxHeight + point.y * num + num * LINE_SPACE_HEIGHT * 2 + MARGIN;
						maxHeight = 0;
						continue;
					}
					if(maxHeight < point.y) {
						maxHeight = point.y;
					}
				}
			}
			gc.dispose();
			Font ft = SWTResourceManager.getFont("宋体", 12, SWT.NORMAL);
			GC nameGC = new GC(getParent());
			nameGC.setFont(ft);
			nameHeight = nameGC.stringExtent("test").y;
			nameGC.dispose();
			//首尾间隙,行间距
			allHeight += maxHeight + HEAD_FOOT_SAPCE ;
			//名字高度
			int tempHeight = allHeight + nameHeight + LINE_SPACE_HEIGHT;
			if(tempHeight < HEAD_IMG_HEIGHT) {
				height = HEAD_IMG_HEIGHT;
			}else {
				height = tempHeight;
			}
			
		}
		return height + HEAD_FOOT_SAPCE;//聊天记录空间
	}

	@Override
	public void draw(GC gc, int start, int width, int index) {
		if(null == head) {
			head = SWTResourceManager.getImage(ChatItem.class, "/images/head.png");
		}
		//名字字体
		Font ft = SWTResourceManager.getFont("宋体", 12, SWT.NORMAL);
		gc.setFont(ft);
		Transform trans = new Transform(null);//向下偏移15像素，预留每条聊天记录空间
		trans.translate(0, HEAD_FOOT_SAPCE);
		gc.setTransform(trans);
		if(fromSelf) {
			gc.setBackground(SWTResourceManager.getColor(0x12, 0x12, 0x12));
			gc.setForeground(SWTResourceManager.getColor(0x12, 0x12, 0x12));
			Path namePath = new Path(null);
			Point nameSize = gc.stringExtent(user);
			namePath.addString(user, width - ( HEAD_IMG_HEIGHT + LINE_SPACE_HEIGHT * 2 + nameSize.x + MyList.BAR_WIDTH + MARGIN), start + LINE_SPACE_HEIGHT, ft);
			gc.fillPath(namePath);
			gc.drawPath(namePath);
			gc.drawImage(head,width - ( HEAD_IMG_HEIGHT + LINE_SPACE_HEIGHT * 2 + MyList.BAR_WIDTH), start + LINE_SPACE_HEIGHT);
			gc.setBackground(SWTResourceManager.getColor(0x9E, 0xEE, 0x6B));
			gc.setForeground(SWTResourceManager.getColor(0xff, 0xff, 0xff));
			gc.fillRoundRectangle(width - (HEAD_IMG_HEIGHT + LINE_SPACE_HEIGHT * 2 + maxWidth + MyList.BAR_WIDTH + MARGIN), start + nameHeight + LINE_SPACE_HEIGHT * 2, maxWidth, allHeight, 3, 3);
			gc.setForeground(SWTResourceManager.getColor(0x91, 0xe1, 0x61));
			gc.drawRoundRectangle(width - (HEAD_IMG_HEIGHT + LINE_SPACE_HEIGHT * 2 + maxWidth + MyList.BAR_WIDTH + MARGIN), start + nameHeight + LINE_SPACE_HEIGHT * 2, maxWidth, allHeight - 3, 3, 3);
			gc.setFont(font);
			gc.setBackground(SWTResourceManager.getColor(0x12, 0x12, 0x12));
			gc.setForeground(SWTResourceManager.getColor(0x12, 0x12, 0x12));
			
			int cLineWidth = 0;
			int cHeight = 0;
			int cMaxHeight = 0;
			Path contentPath = new Path(null);
			for(Object content : chatContent) {
				if(content instanceof Image) {
					Image img = (Image) content;
					int imgWidth = img.getImageData().width;
					int imgHeight = img.getImageData().height;
					cLineWidth += imgWidth;
					if(cLineWidth > ITEM_AREA_WIDTH) {
						gc.drawImage(img, width - (HEAD_IMG_HEIGHT + LINE_SPACE_HEIGHT * 3 + maxWidth + MyList.BAR_WIDTH - MARGIN), start + nameHeight + LINE_SPACE_HEIGHT * 2  + cHeight + LINE_SPACE_HEIGHT + MARGIN);
						cHeight += cMaxHeight;
						cMaxHeight = 0;
						cLineWidth = 0;
						continue;
					} else {
						gc.drawImage(img, width - (HEAD_IMG_HEIGHT + LINE_SPACE_HEIGHT * 3 + maxWidth + cLineWidth - imgWidth + MyList.BAR_WIDTH - MARGIN), start + nameHeight + LINE_SPACE_HEIGHT * 2  + cHeight + LINE_SPACE_HEIGHT + MARGIN);
					}
					if(imgHeight > cMaxHeight) {
						cMaxHeight = imgHeight;
					}
				} else if(content instanceof String) {
					String str = ((String) content).replace("\n", "").replace("\r", "");
					Point point = gc.stringExtent(str);
					int temp = cLineWidth;
					cLineWidth += point.x + str.length();//字间距
					if(cLineWidth < ITEM_AREA_WIDTH) {
						contentPath.addString(str,  width - ( HEAD_IMG_HEIGHT + LINE_SPACE_HEIGHT * 2 + maxWidth + temp + MyList.BAR_WIDTH - MARGIN), start + nameHeight + LINE_SPACE_HEIGHT + LINE_SPACE_HEIGHT + cHeight + MARGIN, font);
					}else {
						int lines = cLineWidth / ITEM_AREA_WIDTH;//有多少行
						double wordWidth = (point.x + str.length()) / (double)str.length();//【平均一个子长度
						int wordNum = (int) ((ITEM_AREA_WIDTH - temp) / wordWidth);//补齐第一行需要
						String first = str.substring(0, wordNum);
						contentPath.addString(first, width - ( HEAD_IMG_HEIGHT + LINE_SPACE_HEIGHT * 2 + maxWidth + temp + MyList.BAR_WIDTH - MARGIN), start + nameHeight + LINE_SPACE_HEIGHT + LINE_SPACE_HEIGHT + cHeight + MARGIN, font);
						cHeight += point.y + LINE_SPACE_HEIGHT *2;
						for(int i = 0; i < lines; i++) {
							int fullNum = (int) (ITEM_AREA_WIDTH / wordWidth);//补齐一行需要
							if(wordNum + fullNum > str.length()) {
								String fullStr = str.substring(wordNum, str.length());
								contentPath.addString(fullStr, width - ( HEAD_IMG_HEIGHT + LINE_SPACE_HEIGHT * 2 + maxWidth + MyList.BAR_WIDTH - MARGIN), start + nameHeight + LINE_SPACE_HEIGHT + LINE_SPACE_HEIGHT + cHeight + MARGIN, font);
								break;
							} else {
								String fullStr = str.substring(wordNum, wordNum += fullNum);
								contentPath.addString(fullStr, width - ( HEAD_IMG_HEIGHT + LINE_SPACE_HEIGHT * 2 + maxWidth + MyList.BAR_WIDTH - MARGIN), start + nameHeight + LINE_SPACE_HEIGHT + LINE_SPACE_HEIGHT + cHeight + MARGIN, font);
							}
							cHeight += point.y + LINE_SPACE_HEIGHT *2;
						}
						
						cMaxHeight = 0;
						cLineWidth = 0;
						continue;
					}
					if(cMaxHeight < point.y) {
						cMaxHeight = point.y;
					}
				}
			}
			gc.drawPath(contentPath);
			gc.fillPath(contentPath);
		} else {
			gc.setBackground(SWTResourceManager.getColor(0x12, 0x12, 0x12));
			gc.setForeground(SWTResourceManager.getColor(0x12, 0x12, 0x12));
			Path namePath = new Path(null);
			namePath.addString(user,HEAD_IMG_HEIGHT + LINE_SPACE_HEIGHT * 2, start + LINE_SPACE_HEIGHT, ft);
			gc.fillPath(namePath);
			gc.drawPath(namePath);
			gc.drawImage(head,LINE_SPACE_HEIGHT, start + LINE_SPACE_HEIGHT);
			gc.setBackground(SWTResourceManager.getColor(0xff, 0xff, 0xff));
			gc.setForeground(SWTResourceManager.getColor(0xff, 0xff, 0xff));
			gc.fillRoundRectangle(HEAD_IMG_HEIGHT + LINE_SPACE_HEIGHT  + MARGIN, start + nameHeight + LINE_SPACE_HEIGHT * 2, maxWidth, allHeight, 3, 3);
			gc.setForeground(SWTResourceManager.getColor(0xe1, 0xe1, 0xe1));
			gc.drawRoundRectangle(HEAD_IMG_HEIGHT + LINE_SPACE_HEIGHT  + MARGIN, start + nameHeight + LINE_SPACE_HEIGHT * 2, maxWidth, allHeight - 4, 3, 3);
			gc.setFont(font);
			gc.setBackground(SWTResourceManager.getColor(0x12, 0x12, 0x12));
			gc.setForeground(SWTResourceManager.getColor(0x12, 0x12, 0x12));
			int cLineWidth = 0;
			int cHeight = 0;
			int cMaxHeight = 0;
			Path contentPath = new Path(null);
			for(Object content : chatContent) {
				if(content instanceof Image) {
					Image img = (Image) content;
					int imgWidth = img.getImageData().width;
					int imgHeight = img.getImageData().height;
					cLineWidth += imgWidth;
					if(cLineWidth > ITEM_AREA_WIDTH) {
						gc.drawImage(img, HEAD_IMG_HEIGHT + LINE_SPACE_HEIGHT * 5 + MARGIN, start + nameHeight + LINE_SPACE_HEIGHT * 3  + cHeight + LINE_SPACE_HEIGHT + MARGIN);
						cHeight += cMaxHeight;
						cMaxHeight = 0;
						cLineWidth = 0;
						continue;
					} else {
						gc.drawImage(img, HEAD_IMG_HEIGHT + LINE_SPACE_HEIGHT * 5 + cLineWidth - imgWidth + MARGIN, start + nameHeight + LINE_SPACE_HEIGHT * 3  + cHeight + LINE_SPACE_HEIGHT + MARGIN);
					}
					if(imgHeight > cMaxHeight) {
						cMaxHeight = imgHeight;
					}
				} else if(content instanceof String) {
					String str = ((String) content).replace("\n", "").replace("\r", "");
					Point point = gc.stringExtent(str);
					int temp = cLineWidth;
					cLineWidth += point.x + str.length();//字间距
					if(cLineWidth < ITEM_AREA_WIDTH) {
						contentPath.addString(str,  HEAD_IMG_HEIGHT + LINE_SPACE_HEIGHT * 2 + temp + MARGIN, start + nameHeight + LINE_SPACE_HEIGHT + LINE_SPACE_HEIGHT + cHeight + MARGIN, font);
					}else {
						int lines = cLineWidth / ITEM_AREA_WIDTH;//有多少行
						double wordWidth = (point.x + str.length()) / (double)str.length();//【平均一个子长度
						int wordNum = (int) ((ITEM_AREA_WIDTH - temp) / wordWidth);//补齐第一行需要
						String first = str.substring(0, wordNum);
						contentPath.addString(first, HEAD_IMG_HEIGHT + LINE_SPACE_HEIGHT * 2 + temp + MARGIN, start + nameHeight + LINE_SPACE_HEIGHT + LINE_SPACE_HEIGHT + cHeight + MARGIN, font);
						cHeight += point.y + LINE_SPACE_HEIGHT *2;
						for(int i = 0; i < lines; i++) {
							int fullNum = (int) (ITEM_AREA_WIDTH / wordWidth);//补齐一行需要
							if(wordNum + fullNum > str.length()) {
								String fullStr = str.substring(wordNum, str.length());
								contentPath.addString(fullStr, HEAD_IMG_HEIGHT + LINE_SPACE_HEIGHT * 2 + MARGIN, start + nameHeight + LINE_SPACE_HEIGHT + LINE_SPACE_HEIGHT + cHeight + MARGIN, font);
								break;
							} else {
								String fullStr = str.substring(wordNum, wordNum += fullNum);
								contentPath.addString(fullStr, HEAD_IMG_HEIGHT + LINE_SPACE_HEIGHT * 2 + MARGIN, start + nameHeight + LINE_SPACE_HEIGHT + LINE_SPACE_HEIGHT + cHeight + MARGIN, font);
							}
							cHeight += point.y + LINE_SPACE_HEIGHT *2;
						}
						
						cMaxHeight = 0;
						cLineWidth = 0;
						continue;
					}
					if(cMaxHeight < point.y) {
						cMaxHeight = point.y;
					}
				}
			}
			gc.drawPath(contentPath);
			gc.fillPath(contentPath);
		}
		trans.translate(0, -HEAD_FOOT_SAPCE);
		gc.setTransform(trans);
	}

	@Override
	public boolean oncliek(MouseEvent e, int itemHeight, int index) {
		// TODO Auto-generated method stub
		return false;
	}

}
