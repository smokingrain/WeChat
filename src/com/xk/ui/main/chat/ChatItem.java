package com.xk.ui.main.chat;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.wb.swt.SWTResourceManager;

import com.xk.chatlogs.ChatLog;
import com.xk.ui.main.FloatWindow;
import com.xk.uiLib.ImageViewer;
import com.xk.uiLib.ListItem;
import com.xk.uiLib.MyList;
import com.xk.utils.SWTTools;
import com.xk.utils.WeChatUtil;

/**
 * 用途：聊天气泡
 *
 * @author xiaokui
 * @date 2017年1月5日
 */
public class ChatItem extends ListItem {

	private static final Integer ITEM_AREA_WIDTH = 450;
	private static final Integer LINE_SPACE_HEIGHT = 2;
	private static final Integer HEAD_FOOT_SAPCE = 15;
	private static final Integer HEAD_IMG_HEIGHT = 50;
	private static final Integer MARGIN = 5;
	
	private ChatLog log;
	private String user;//聊天发送者
	private Image head;//发送者头像
	private List<Object> chatContent;//聊天内容
	private boolean fromSelf;//是不是自己发送的 
	private Font font;//文本字体
	private int height = -1;//总高度
	private int lineNum = 0;
	private int maxWidth = 0;//气泡宽度
	private int nameHeight = 0;//名字高度
	private int allHeight = MARGIN;//气泡高度
	
	public ChatItem(String user,Image head, List<Object> chatContent, boolean fromSelf, Font font, ChatLog log) {
		this.user = user;
		this.head = head;
		this.chatContent = chatContent;
		this.fromSelf = fromSelf;
		this.font = font;
		this.log = log;
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
					allLength += width + LINE_SPACE_HEIGHT;
					if(lineNum == 0) {
						if(maxWidth < width) {
							maxWidth += width + 10 + MARGIN;
						}else {
							maxWidth += width + LINE_SPACE_HEIGHT;
						}
					}
					int height = img.getImageData().height;
					if(maxHeight < height) {
						maxHeight = height;
					}
					if(allLength > ITEM_AREA_WIDTH) {
						maxWidth = ITEM_AREA_WIDTH + 10;
						allHeight += maxHeight + LINE_SPACE_HEIGHT;
						lineNum++;
						maxHeight = 0;
						allLength = width + LINE_SPACE_HEIGHT;
						continue;
					}
					
				}else if(content instanceof String) {
					String str = ((String) content).replace("\n", "").replace("\r", "");
					Point point = gc.textExtent(str);
					if(lineNum == 0) {
						if(maxWidth < point.x) {
							maxWidth += point.x + str.length() + 10;//字间距
						}else {
							maxWidth +=point.x + str.length() + LINE_SPACE_HEIGHT;
						}
					}
					int curLen = point.x + str.length();
					allLength += curLen;//字间距
					if(maxHeight < point.y) {
						maxHeight = point.y;
					}
					if(allLength > ITEM_AREA_WIDTH) {
						maxWidth = ITEM_AREA_WIDTH + 10;
						int num = allLength / ITEM_AREA_WIDTH ;
						lineNum += num;
						allHeight += maxHeight + point.y * (num - 1) + num * LINE_SPACE_HEIGHT * 3;
						maxHeight = 0;
						allLength = allLength - ITEM_AREA_WIDTH * num;
						continue;
					}
					
				}
			}
			gc.dispose();
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
		if(fromSelf) {//如果是自己，从右往左
			gc.setBackground(SWTResourceManager.getColor(0x12, 0x12, 0x12));
			gc.setForeground(SWTResourceManager.getColor(0x12, 0x12, 0x12));
			Path namePath = new Path(null);
			Point nameSize = gc.textExtent(user);
			namePath.addString(user, width - ( HEAD_IMG_HEIGHT + LINE_SPACE_HEIGHT * 2 + nameSize.x + MyList.BAR_WIDTH + MARGIN), start + LINE_SPACE_HEIGHT, ft);
			gc.fillPath(namePath);//绘制发送者
//			gc.drawPath(namePath);
			gc.drawImage(head,width - ( HEAD_IMG_HEIGHT + LINE_SPACE_HEIGHT * 2 + MyList.BAR_WIDTH), start + LINE_SPACE_HEIGHT);
			gc.setBackground(SWTResourceManager.getColor(0x9E, 0xEE, 0x6B));
			gc.setForeground(SWTResourceManager.getColor(0xff, 0xff, 0xff));
			gc.fillRoundRectangle(width - (HEAD_IMG_HEIGHT + LINE_SPACE_HEIGHT * 2 + maxWidth + MyList.BAR_WIDTH + MARGIN), start + nameHeight + LINE_SPACE_HEIGHT * 2, maxWidth, allHeight, 3, 3);
			gc.setForeground(SWTResourceManager.getColor(0x91, 0xe1, 0x61));
			gc.drawRoundRectangle(width - (HEAD_IMG_HEIGHT + LINE_SPACE_HEIGHT * 2 + maxWidth + MyList.BAR_WIDTH + MARGIN), start + nameHeight + LINE_SPACE_HEIGHT * 2, maxWidth, allHeight - 3, 3, 3);
			gc.setFont(font);
			gc.setBackground(SWTResourceManager.getColor(0x12, 0x12, 0x12));
			gc.setForeground(SWTResourceManager.getColor(0x12, 0x12, 0x12));
			//气泡绘制完毕
			int cLineWidth = 0;
			int cHeight = 0;
			int cMaxHeight = 0;
			Path contentPath = new Path(null);
			for(Object content : chatContent) {
				if(content instanceof Image) {//绘制图片
					Image img = (Image) content;
					int imgWidth = img.getImageData().width;
					int imgHeight = img.getImageData().height;
					cLineWidth += imgWidth + LINE_SPACE_HEIGHT;
					if(cLineWidth > ITEM_AREA_WIDTH) {
						cHeight += cMaxHeight + LINE_SPACE_HEIGHT;
						gc.drawImage(img, width - (HEAD_IMG_HEIGHT + LINE_SPACE_HEIGHT * 4 + maxWidth + MyList.BAR_WIDTH - MARGIN), start + nameHeight + LINE_SPACE_HEIGHT * 2  + cHeight + LINE_SPACE_HEIGHT + MARGIN);
						cMaxHeight = 0;
						cLineWidth = imgWidth + LINE_SPACE_HEIGHT;
						continue;
					} else {
						gc.drawImage(img, width - (HEAD_IMG_HEIGHT + LINE_SPACE_HEIGHT * 4 + maxWidth - cLineWidth  + imgWidth + MyList.BAR_WIDTH - MARGIN), start + nameHeight + LINE_SPACE_HEIGHT * 2  + cHeight + LINE_SPACE_HEIGHT + MARGIN);
					}
					if(imgHeight > cMaxHeight) {
						cMaxHeight = imgHeight;
					}
				} else if(content instanceof String) {//绘制文字
					String str = ((String) content).replace("\n", "").replace("\r", "");//先去掉换行，懒得计算换行了
					Point point = gc.textExtent(str);
					int temp = cLineWidth;
					cLineWidth += point.x + str.length();//字间距
					if(cMaxHeight < point.y) {
						cMaxHeight = point.y;
					}
					if(cLineWidth < ITEM_AREA_WIDTH) {//聊天内容比较短，只有一行
						contentPath.addString(str,  width - ( HEAD_IMG_HEIGHT + LINE_SPACE_HEIGHT * 4 + maxWidth - temp + MyList.BAR_WIDTH - MARGIN), start + nameHeight + LINE_SPACE_HEIGHT + LINE_SPACE_HEIGHT + cHeight + LINE_SPACE_HEIGHT, font);
					}else {
						int lines = cLineWidth / ITEM_AREA_WIDTH;//有多少行
						double wordWidth = (point.x + str.length()) / (double)str.length();//【平均一个字长度
						int wordNum = (int) ((ITEM_AREA_WIDTH - temp) / wordWidth);//补齐第一行需要
						String first = str.substring(0, wordNum);
						contentPath.addString(first, width - ( HEAD_IMG_HEIGHT + LINE_SPACE_HEIGHT * 4 + maxWidth - temp + MyList.BAR_WIDTH - MARGIN), start + nameHeight + LINE_SPACE_HEIGHT + LINE_SPACE_HEIGHT + cHeight + LINE_SPACE_HEIGHT, font);
						cHeight += cMaxHeight + LINE_SPACE_HEIGHT *2;
						for(int i = 0; i < lines; i++) {
							int fullNum = (int) (ITEM_AREA_WIDTH / wordWidth);//补齐一行需要
							if(wordNum + fullNum >= str.length()) {
								String fullStr = str.substring(wordNum, str.length());
								cLineWidth = gc.textExtent(fullStr).x + fullStr.length();
								contentPath.addString(fullStr, width - ( HEAD_IMG_HEIGHT + LINE_SPACE_HEIGHT * 4 + maxWidth + MyList.BAR_WIDTH - MARGIN), start + nameHeight + LINE_SPACE_HEIGHT + LINE_SPACE_HEIGHT + cHeight + LINE_SPACE_HEIGHT, font);
								break;
							} else {
								String fullStr = str.substring(wordNum, wordNum += fullNum);
								contentPath.addString(fullStr, width - ( HEAD_IMG_HEIGHT + LINE_SPACE_HEIGHT * 4 + maxWidth + MyList.BAR_WIDTH - MARGIN), start + nameHeight + LINE_SPACE_HEIGHT + LINE_SPACE_HEIGHT + cHeight + LINE_SPACE_HEIGHT, font);
							}
							cHeight += point.y + LINE_SPACE_HEIGHT *2;
						}
						//重置宽高
						cMaxHeight = 0;
						continue;
					}
					
				}
			}
//			gc.drawPath(contentPath);
			gc.fillPath(contentPath);
		} else {//逻辑同上，不过是从左往右计算
			gc.setBackground(SWTResourceManager.getColor(0x12, 0x12, 0x12));
			gc.setForeground(SWTResourceManager.getColor(0x12, 0x12, 0x12));
			Path namePath = new Path(null);
			namePath.addString(user,HEAD_IMG_HEIGHT + LINE_SPACE_HEIGHT * 2, start + LINE_SPACE_HEIGHT, ft);
			gc.fillPath(namePath);
//			gc.drawPath(namePath);
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
					Point point = gc.textExtent(str);
					int temp = cLineWidth;
					cLineWidth += point.x + str.length();//字间距
					if(cMaxHeight < point.y) {
						cMaxHeight = point.y;
					}
					if(cLineWidth < ITEM_AREA_WIDTH) {
						contentPath.addString(str,  HEAD_IMG_HEIGHT + LINE_SPACE_HEIGHT * 2 + temp + MARGIN, start + nameHeight + LINE_SPACE_HEIGHT + LINE_SPACE_HEIGHT + cHeight + LINE_SPACE_HEIGHT, font);
					}else {
						int lines = cLineWidth / ITEM_AREA_WIDTH;//有多少行
						double wordWidth = (point.x + str.length()) / (double)str.length();//【平均一个子长度
						int wordNum = (int) ((ITEM_AREA_WIDTH - temp) / wordWidth);//补齐第一行需要
						String first = str.substring(0, wordNum);
						contentPath.addString(first, HEAD_IMG_HEIGHT + LINE_SPACE_HEIGHT * 2 + temp + MARGIN, start + nameHeight + LINE_SPACE_HEIGHT + LINE_SPACE_HEIGHT + cHeight + LINE_SPACE_HEIGHT, font);
						cHeight += cMaxHeight + LINE_SPACE_HEIGHT *2;
						boolean full = false;
						for(int i = 0; i < lines; i++) {
							int fullNum = (int) (ITEM_AREA_WIDTH / wordWidth);//补齐一行需要
							if(wordNum + fullNum > str.length()) {
								full = true;
								String fullStr = str.substring(wordNum, str.length());
								cLineWidth = gc.textExtent(fullStr).x + fullStr.length();
								contentPath.addString(fullStr, HEAD_IMG_HEIGHT + LINE_SPACE_HEIGHT * 2 + MARGIN, start + nameHeight + LINE_SPACE_HEIGHT + LINE_SPACE_HEIGHT + cHeight + LINE_SPACE_HEIGHT, font);
								break;
							} else {
								String fullStr = str.substring(wordNum, wordNum += fullNum);
								contentPath.addString(fullStr, HEAD_IMG_HEIGHT + LINE_SPACE_HEIGHT * 2 + MARGIN, start + nameHeight + LINE_SPACE_HEIGHT + LINE_SPACE_HEIGHT + cHeight + LINE_SPACE_HEIGHT, font);
							}
							cHeight += point.y + LINE_SPACE_HEIGHT *2;
						}
						if(!full) {
							cLineWidth = 0;
						}
						continue;
					}
					
				}
			}
//			gc.drawPath(contentPath);
			gc.fillPath(contentPath);
		}
		trans.translate(0, -HEAD_FOOT_SAPCE);
		gc.setTransform(trans);
	}

	@Override
	public boolean oncliek(MouseEvent e, int itemHeight, int index, int type) {
		if(e.button == 3 && e.count == 1) {//右键
			Menu m=new Menu(getParent());
			Menu menu=getParent().getMenu();
			if (menu != null) {
				menu.dispose();
			}
			
			MenuItem cp=new MenuItem(m, SWT.NONE);//复制菜单
			cp.setText("复制");
			cp.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					StringBuffer sb = new StringBuffer();
					for(Object obj : chatContent) {
						if(obj instanceof String) {
							sb.append(obj);
						}
					}
					if(sb.length() > 0) {
						Clipboard board = new Clipboard(null);
						board.setContents(new String[]{sb.toString()}, new Transfer[]{TextTransfer.getInstance()});
						board.dispose();
						System.out.println("复制成功！！");
					}
				}
				
			});
			getParent().setMenu(m);
			m.setVisible(true);
			
			
			
		}else if(e.button == 1 && e.count == 2 && type == MyList.CLICK_DOUBLE) {//双击
			if(log.msgType == 3 || log.msgType == 47) {
				if(chatContent.get(0) instanceof Image) {
					ImageLoader loader = WeChatUtil.loadImage(log.msgid, null);
					if(null != loader) {
						final FloatWindow fw = FloatWindow.getInstance();
						fw.init();
						int width = loader.logicalScreenWidth == 0 ? loader.data[0].width : loader.logicalScreenWidth;
						int height = loader.logicalScreenHeight ==0 ? loader.data[0].height : loader.logicalScreenHeight;
						fw.setSize(width + 2, height + 2);
						ImageViewer iv = new ImageViewer(fw.shell);
						iv.addMouseListener(new MouseAdapter() {

							@Override
							public void mouseDoubleClick(MouseEvent mouseevent) {
								fw.kill();
							}
							
						});
						if(loader.data.length > 1) {
							iv.setImages(loader.data, loader.repeatCount);
						}else {
							iv.setImage(loader.data[0]);
						}
						fw.add(iv);
						fw.setTimeOut(-1L);
						SWTTools.centerWindow(fw.shell);
						SWTTools.enableTrag(iv);
						fw.open(-1, -1);
					}
				}
			}
		}
		return false;
	}

}
