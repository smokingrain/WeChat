package org.eclipse.swt.custom;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.HTMLTransfer;
import org.eclipse.swt.dnd.ImageTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.GlyphMetrics;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Event;
import org.eclipse.wb.swt.GifTransfer;
import org.jcodec.common.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import com.xk.bean.ImageNode;
import com.xk.bean.ImageNode.TYPE;
import com.xk.utils.Constant;
import com.xk.utils.HTTPUtil;
import com.xk.utils.song.SongLocation;

public class StyledTextUtils {

	/**
	 * 让输入框支持显示图片
	 * 作者 ：肖逵
	 * 时间 ：2021年1月25日 上午10:07:17
	 * @param text
	 */
	public static void enablePictures(StyledText text) {
		//如果有图片被删除，需要将对应对象回收
		text.addVerifyListener(new VerifyListener() {
			
			@Override
			public void verifyText(VerifyEvent event) {
				if (event.start == event.end) return;
				String str = text.getText(event.start, event.end - 1);
				int index = str.indexOf('\uFFFC');
				while (index != -1) {
					StyleRange style = text.getStyleRangeAtOffset(event.start + index);
					if (style != null) {
						ImageNode image = (ImageNode)style.data;
						if (image != null && image.type == TYPE.IMAGE) {
							image.getImg().dispose();
							System.out.println("无用图片删除");
						}
					}
					index = str.indexOf('\uFFFC', index + 1);
				}
				
			}
		});
		
		//绘制图片
		text.addPaintObjectListener(new PaintObjectListener() {
			
			@Override
			public void paintObject(PaintObjectEvent event) {
				StyleRange style = event.style;
				ImageNode image = (ImageNode)style.data;
				if (!image.getImg().isDisposed()) {
					ImageData id = image.getImg().getImageData();
					int x = event.x;
					int y = event.y + event.ascent - style.metrics.ascent + text.getBorderWidth();
					event.gc.drawImage(image.getImg(), 0, 0, id.width, id.height, x, y, style.metrics.width, style.metrics.ascent);
				}
			}
		});
		//编辑框被销毁，携带的数据要回收
		text.addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent event) {
				StyleRange[] styles = text.getStyleRanges();
				for (int i = 0; i < styles.length; i++) {
					StyleRange style = styles[i];
					if (style.data != null) {
						ImageNode image = (ImageNode)style.data;
						if (image != null && image.type == TYPE.IMAGE) image.getImg().dispose();
					}
				}
				
			}
		});
	}
	
	/**
	 * 输入框剪切
	 * 作者 ：肖逵
	 * 时间 ：2021年1月25日 上午10:07:38
	 * @param text
	 */
	public static void cut(StyledText text) {
		if(copy(text)) {
			if (text.blockSelection && text.blockXLocation != -1) {
				text.insertBlockSelectionText((char)0, SWT.NULL);
			} else {
				text.doDelete();
			}
		}
	}
	
	
	/**
	 * 保存图片节点
	 * 作者 ：肖逵
	 * 时间 ：2021年1月22日 下午4:49:09
	 * @param node
	 */
	public static void copyImageNode(ImageNode node) {
		if(node.type == TYPE.IMOJ) {
			String base = "[" + node.getBase() + "]";
			Clipboard board = new Clipboard(null);
			board.setContents(new Object[]{base}, new Transfer[]{TextTransfer.getInstance()});
			board.dispose();
		} else {
			ImageLoader loader = node.getLoader();
			Clipboard board = new Clipboard(null);
			
			if(loader.data.length == 0) {
				Transfer[] transfers = new Transfer[2];
				Object[] data = new Object[2];
				transfers[0] = ImageTransfer.getInstance();
				transfers[1] = GifTransfer.getInstance();
				data[0] = loader.data[0];
				data[1] = loader;
				board.setContents(data, transfers);
			} else {
				Transfer[] transfers = new Transfer[1];
				Object[] data = new Object[1];
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss.SSS");
				File file = new File("temp","shortcut" + sdf.format(new Date()) + Constant.FORMATS[loader.format]);
				file.getParentFile().mkdirs();
				loader.save(file.getAbsolutePath(), loader.format);
				file.deleteOnExit();
				StringBuffer msg = new StringBuffer("<div>");
				msg.append("<img src=\"file:///").append(file.getAbsolutePath()).append("\" />");
				msg.append("</div>");
				transfers[0] = HTMLTransfer.getInstance();
				data[0] = msg.toString();
				board.setContents(data, transfers);
			}
			
			board.dispose();
			
		}
		
	}
	
	/**
	 * 将输入框选中内容复制到剪切板
	 * 作者 ：肖逵
	 * 时间 ：2021年1月22日 上午9:40:14
	 */
	public static boolean copy(StyledText text) {
		
		
		String selectionText = text.getSelectionText();
		Point selection = text.getSelection();
		if(StringUtils.isEmpty(selectionText)) {
			return false;
		}
		if(selectionText.indexOf('\uFFFC') < 0) {
			Clipboard board = new Clipboard(null);
			board.setContents(new Object[]{selectionText}, new Transfer[]{TextTransfer.getInstance()});
			board.dispose();
			return true;
		}
		if(selectionText.length() == 1 && '\uFFFC' == selectionText.charAt(0)) {
			StyleRange style = text.getStyleRangeAtOffset(text.getSelection().x);
			if(null == style) {
				return false;
			}
			ImageNode image = (ImageNode)style.data;
			if(null == image) {
				return false;
			}
			copyImageNode(image);
			return true;
		}
		if(!"".equals(selectionText) ) {
			//找到有存储图片的节点
			Object[] content = new Object[2];
			content[0] = selectionText;
			Transfer[] transfers = new Transfer[]{TextTransfer.getInstance(), HTMLTransfer.getInstance()};
			int index = selectionText.indexOf('\uFFFC');
			int lastIndex = 0;
			StringBuffer msg = new StringBuffer("<div>");
			while (index != -1) {
				String temp = selectionText.substring(lastIndex, index).trim();
				msg.append(new TextNode(temp, "").toString());
				
				StyleRange style = text.getStyleRangeAtOffset(selection.x + index);
				if(null == style) {
					lastIndex = index + 1;
					index = selectionText.indexOf('\uFFFC', lastIndex);
					continue;
				}
				ImageNode image = (ImageNode)style.data;
				if (image != null) {
					if(image.type == TYPE.IMOJ) {
						msg.append("[").append(image.getBase()).append("]");
					} else {
						
						SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss.SSS");
						File file = new File("temp",index + "shortcut" + sdf.format(new Date()) + ".jpg");
						file.getParentFile().mkdirs();
						if(null == image.getLoader()) {
							ImageLoader loader = new ImageLoader();
							loader.data = new ImageData[]{image.getImg().getImageData()};
							loader.save(file.getAbsolutePath(), SWT.IMAGE_JPEG);
						} else {
							file = new File("temp","shortcut" + sdf.format(new Date()) + Constant.FORMATS[image.getLoader().format]);
							image.getLoader().save(file.getAbsolutePath(), image.getLoader().format);
						}
						file.deleteOnExit();
						
						msg.append("<img src=\"file:///").append(file.getAbsolutePath()).append("\" />");
					}
				}
				lastIndex = index + 1;
				index = selectionText.indexOf('\uFFFC', lastIndex);
			}
			if(lastIndex < selectionText.length()) {
				if(lastIndex < selectionText.length()) {
					msg.append(new TextNode(selectionText.substring(lastIndex, selectionText.length()), "").toString());
				}
			}
			msg.append("</div>");
			content[1] = msg.toString();
			Clipboard board = new Clipboard(null);
			board.setContents(content, transfers);
			board.dispose();
			return true;
		}
		
		return false;
	}
	
	/**
	 * 递归遍历所有节点，获取文字和图片节点。
	 * 作者 ：肖逵
	 * 时间 ：2021年1月22日 下午3:10:09
	 * @param sub
	 */
	private static void traversal(StyledText txt, List<Node> sub) {
		for(Node ele : sub) {
			if(ele instanceof TextNode) {
				String text = ((TextNode) ele).text();
				if(!StringUtils.isEmpty(text)) {
					appendText(text, txt);
				}
			} else if (ele instanceof Element) {
				if("img".equalsIgnoreCase(((Element) ele).tagName())) {
					String src = ele.attr("src");
					if(StringUtils.isEmpty(src)) {
						continue;
					}
					InputStream in = null;
					SongLocation loc = null;
					if(src.startsWith("file:///")) {
						try {
							in = new FileInputStream(src.replace("file:///", ""));
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						}
					} else if(new File(src).exists()){
						try {
							in = new FileInputStream(new File(src));
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						}
					} else {
						loc = HTTPUtil.getInstance("clipboard").getInput(src);
						in = loc.input;
					}
					if(null == in) {
						continue;
					}
					ImageLoader loader = new ImageLoader();
					try {
						ImageData[] data = loader.load(in);
						if(null == data || data.length == 0) {
							continue;
						}
						ImageNode node = new ImageNode(TYPE.IMAGE, new Image(null, data[0]), loader, "");
						addImage(txt, node);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} finally {
						if(null != loc) {
							try {
								loc.response.close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						if(null != in) {
							try {
								in.close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						
					}
					
				}
				List<Node> nextSub = ele.childNodes();
				traversal(txt, nextSub);
			}
		}
	}
	
	/**
	 * 将图片文件插入输入框
	 * 作者 ：肖逵
	 * 时间 ：2021年1月25日 上午9:51:44
	 * @param path
	 */
	public static void addFile(StyledText text, String path) {
		if(!new File(path).exists() || new File(path).isDirectory()) {
			return;
		}
		String suffix = path.substring(path.lastIndexOf("."), path.length()).toLowerCase();
		for(String format : Constant.FORMATS) {
			if(format.equals(suffix)) {
				ImageLoader loader = new ImageLoader();
				loader.load(path);
				ImageNode node = new ImageNode(TYPE.IMAGE, new Image(null, loader.data[0]), loader, null);
				addImage(text, node);
				return;
			}
		}
	}
	
	/**
	 * 将剪切板内容粘贴到输入框
	 * 作者 ：肖逵
	 * 时间 ：2021年1月22日 上午9:31:17
	 */
	public static void pause(StyledText text) {
		Clipboard board = new Clipboard(null);
		Object fileContent = board.getContents(FileTransfer.getInstance());//剪切板的文件内容
		Object htmlContent = board.getContents(HTMLTransfer.getInstance());//剪切板的html格式内容
		Object imgContent = board.getContents(ImageTransfer.getInstance());//剪切板的位图图片内容
		Object loaderContent = board.getContents(GifTransfer.getInstance());//剪切板中自定义动态图(与其他程序不互通)
		Object strContent = board.getContents(TextTransfer.getInstance());//剪切板中纯文字内容
		board.dispose();
		if(null != loaderContent) {
			ImageLoader loader = (ImageLoader) loaderContent;
			addImage(text, new ImageNode(TYPE.IMAGE, new Image(null, loader.data[0]), loader, null));
			return;
		}
		if(null != htmlContent) {
			String html = (String) htmlContent;
			Document doc = Jsoup.parse(html);
			List<Node> sub = doc.childNodes();
			traversal(text, sub);
			return;
		}
		if(null != fileContent) {
			String[] files = (String[]) fileContent;
			for(String file : files) {
				addFile(text, file);
			}
			return;
		}
		
		if(null != imgContent) {
			ImageData idata = (ImageData) imgContent;
			Image img = new Image(null, idata);
			addImage(text, img, TYPE.IMAGE);
		} 
		if(null != strContent) {
			text.paste();
		}
		
	}
	
	/**
	 * 插入一个图片/表情节点
	 * 作者 ：肖逵
	 * 时间 ：2020年12月18日 下午5:06:21
	 * @param node
	 */
	public static void addImage(StyledText text, ImageNode node) {
		double limit = 120d;//宽高限制
		int offset = text.getCaretOffset();
		int fix = 1;//修正插入图片后光标的位置
		Point selection = text.getSelection();
		//选择了文字
		if(selection.x != selection.y) {
			if(offset == selection.y) {
				offset = selection.x;
				fix =0 ;
			}
		} 
		text.insert("\uFFFC");
		StyleRange style = new StyleRange ();
		style.start = offset;
		style.length = 1;
		style.data = node;
		Point size = node.getSize();
		int width = size.x;
		int height = size.y;
		if(width > limit || height > limit) {
			if(width > height) {
				height = (int)(height * (limit / width));
				width = (int)limit;
			}else {
				width = (int)(width * (limit / height));
				height = (int)limit;
			}
		}
		style.metrics = new GlyphMetrics(height, 0, width);
		text.setStyleRange(style);
		text.setFocus();
		text.setCaretOffset(text.getCaretOffset() + fix);
		text.forceFocus();
	}

	/**
	 * 编辑框插入一张图
	 * 作者 ：肖逵
	 * 时间 ：2018年8月31日 下午12:55:35
	 * @param image
	 * @param type 0，表情，1，图片
	 */
	public static void addImage(StyledText text, Image image, TYPE type) {
		addImage(text, new ImageNode(type, image, null, null));
	}
	
	/**
	 * 将文字插入输入框
	 * 作者 ：肖逵
	 * 时间 ：2021年1月25日 上午9:46:21
	 * @param text
	 * @param txt
	 */
	public static void appendText(String text, StyledText txt) {
        if(text != null && text.length() > 0)
        {
            if(txt.blockSelection)
            {
                boolean fillWithSpaces = txt.isFixedLineHeight() && txt.renderer.fixedPitch;
                int offset = txt.insertBlockSelectionText(text, fillWithSpaces);
                txt.setCaretOffset(offset, -1);
                txt.clearBlockSelection(true, true);
                txt.setCaretLocation();
                return;
            }
            Event event = new Event();
            event.start = txt.selection.x;
            event.end = txt.selection.y;
            String delimitedText = txt.getModelDelimitedText(text);
            if(txt.textLimit > 0)
            {
                int uneditedTextLength = txt.getCharCount() - (txt.selection.y - txt.selection.x);
                if(uneditedTextLength + delimitedText.length() > txt.textLimit)
                {
                    int endIndex = txt.textLimit - uneditedTextLength;
                    delimitedText = delimitedText.substring(0, Math.max(endIndex, 0));
                }
            }
            event.text = delimitedText;
            txt.sendKeyEvent(event);
        }
	}
}
