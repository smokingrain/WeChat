package com.xk.utils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.internal.win32.OS;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.common.io.FileChannelWrapper;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.ColorSpace;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.ColorUtil;
import org.jcodec.scale.RgbToBgr;
import org.jcodec.scale.SwingUtil;

import com.xk.bean.StringNode;

public class SWTTools {

	private static final String ELLIPSIS = "...";
	
	public static void topWindow(Shell shell) {
		OS.SetWindowPos(shell.handle , OS.HWND_TOPMOST, shell.getLocation().x , shell.getLocation().y , shell.getSize().x , shell.getSize().y , SWT.NULL);
	}
	
	/**
	 * Shorten the given text <code>t</code> so that its length doesn't exceed
	 * the given width. The default implementation replaces characters in the
	 * center of the original string with an ellipsis ("...").
	 * Override if you need a different strategy.
	 * 
	 * @param gc the gc to use for text measurement
	 * @param t the text to shorten
	 * @param width the width to shorten the text to, in pixels
	 * @return the shortened text
	 */
	public static String shortenText(GC gc, String t, int width) {
		if (t == null) return null;
		int w = gc.textExtent(ELLIPSIS, StringNode.DRAW_FLAGS).x;
		if (width<=w) return t;
		int l = t.length();
		int max = l/2;
		int min = 0;
		int mid = (max+min)/2 - 1;
		if (mid <= 0) return t;
		TextLayout layout = new TextLayout (null);
		layout.setText(t);
		mid = validateOffset(layout, mid);
		while (min < mid && mid < max) {
			String s1 = t.substring(0, mid);
			String s2 = t.substring(validateOffset(layout, l-mid), l);
			int l1 = gc.textExtent(s1, StringNode.DRAW_FLAGS).x;
			int l2 = gc.textExtent(s2, StringNode.DRAW_FLAGS).x;
			if (l1+w+l2 > width) {
				max = mid;			
				mid = validateOffset(layout, (max+min)/2);
			} else if (l1+w+l2 < width) {
				min = mid;
				mid = validateOffset(layout, (max+min)/2);
			} else {
				min = max;
			}
		}
		String result = mid == 0 ? t : t.substring(0, mid) + ELLIPSIS + t.substring(validateOffset(layout, l-mid), l);
		layout.dispose();
	 	return result;
	}
	
	private static int validateOffset(TextLayout layout, int offset) {
		int nextOffset = layout.getNextOffset(offset, SWT.MOVEMENT_CLUSTER);
		if (nextOffset != offset) return layout.getPreviousOffset(nextOffset, SWT.MOVEMENT_CLUSTER);
		return offset;
	}
	
	/**
	 * 用途：允许拖拽
	 * @date 2017年1月5日
	 * @param ctrl
	 */
	public static void enableTrag(Control ctrl) {
		final Composite composite=ctrl.getShell();
		
		Listener listener = new Listener() {
		    int startX, startY;
		    public void handleEvent(Event e) {
		        if (e.type == SWT.MouseDown && e.button == 1) {
		            startX = e.x;
		            startY = e.y;
		        }
		        if (e.type == SWT.MouseMove && (e.stateMask & SWT.BUTTON1) != 0) {
		            Point p = composite.toDisplay(e.x, e.y);
		            p.x -= startX;
		            p.y -= startY;
		            composite.setLocation(p);
		            composite.setFocus();
		        }
		    }
		};
		ctrl.addListener(SWT.MouseDown, listener);
		ctrl.addListener(SWT.MouseMove, listener);
		
	}
	
	
	/**
	 * 用途：窗体居中显示
	 * @date 2017年1月5日
	 * @param shell
	 */
	public static void centerWindow(Shell shell){
		Rectangle rect=Display.getDefault().getClientArea();
		int x=rect.width/2-shell.getSize().x/2;
		int y=rect.height/2-shell.getSize().y/2;
		shell.setLocation(x,y);
	}
	

	/**
	 * 用途：图片缩放
	 * @date 2017年1月5日
	 * @param source
	 * @param width
	 * @param height
	 * @return
	 */
	public static Image scaleImage(ImageData source,int width,int height){
		Image img=new Image(null,source);
		ImageData dest = new ImageData(1, 1, source.depth, source.palette);
		dest.alphaData=new byte[]{-1,-1,-1,-1};
		dest.data=new byte[]{-1,-1,-1,-1};
		dest=dest.scaledTo(width, height);
		Image tmp=new Image(null, dest);
		GC gc=new GC(tmp);
		gc.setAdvanced(true);
		gc.setAntialias(SWT.ON);
		Transform trans=new Transform(null);
		trans.scale((float)width/source.width, (float)height/source.height);
		gc.setTransform(trans);
		gc.drawImage(img, 0, 0);
		gc.dispose();
		img.dispose();
		return tmp;
	}
	
	public static void openBrowser(String url) {
		if (java.awt.Desktop.isDesktopSupported()) {
			try {				
				// 创建一个URI实例				
				java.net.URI uri = java.net.URI.create(url);
				// 获取当前系统桌面扩展
				java.awt.Desktop dp = java.awt.Desktop.getDesktop();
				// 判断系统桌面是否支持要执行的功能
				if (dp.isSupported(java.awt.Desktop.Action.BROWSE)) {
					// 获取系统默认浏览器打开链接					
					dp.browse(uri);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}		
		}
	}
	
	public static Image toImage(Picture src) {
		BufferedImage bi = SwingUtil.toBufferedImage(src);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			ImageIO.write(bi, "png", out);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		byte[] data = out.toByteArray();
		ByteArrayInputStream in = new ByteArrayInputStream(data);
		ImageData id = new ImageData(in);
		try {
			out.close();
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Image img = new Image(null, id);
		return img;
	}
	
	
	public static Image toSWTImage(Picture src) {
		if (src.getColor() != ColorSpace.BGR) {
			Picture bgr = Picture.createCropped(src.getWidth(), src.getHeight(), ColorSpace.BGR, src.getCrop());
			if (src.getColor() == ColorSpace.RGB) {
				new RgbToBgr().transform(src, bgr);
			} else {
				org.jcodec.scale.Transform transform = ColorUtil.getTransform(src.getColor(), ColorSpace.RGB);
				transform.transform(src, bgr);
				new RgbToBgr().transform(bgr, bgr);				
			}
			src = bgr;
		}
		Image img = new Image(null, src.getCroppedWidth(), src.getCroppedHeight());
		if(null == src.getCrop()) {
			return toSWTImage(src, img);
		} else {
			return toSWTImageCropped(src, img);
		}
		
	}
	
	public static Image toSWTImageCropped(Picture src, Image dest) {
		ImageData idata = dest.getImageData();
		byte[] data = idata.data;
		byte[] srcData = src.getPlaneData(0);
        int dstStride = idata.width * 4;
        int srcStride = src.getWidth() * 3;
        for (int line = 0, srcOff = 0, dstOff = 0; line < idata.height; line++) {
            for (int id = dstOff, is = srcOff; id < dstOff + dstStride; id += 4, is += 3) {
                data[id] = (byte) (srcData[is] + 128);
                data[id + 1] = (byte) (srcData[is + 1] + 128);
                data[id + 2] = (byte) (srcData[is + 2] + 128);
            }
            srcOff += srcStride;
            dstOff += dstStride;
        }
        idata.data =data;
		dest.dispose();
		return new Image(null, idata);
	}
	
	public static Image toSWTImage(Picture src, Image dest) {
		ImageData id = dest.getImageData();
		byte[] data = id.data;
		byte[] srcData = src.getPlaneData(0);
		int pixs = data.length / 4;
		for (int i = 0; i < pixs; i++) {
            data[i * 4] = (byte) (srcData[i * 3] + 128);
            data[i * 4 + 1] = (byte) (srcData[i * 3 + 1] + 128);
            data[i * 4 + 2] = (byte) (srcData[i * 3 + 2] + 128);
        }
		id.data =data;
		dest.dispose();
		return new Image(null, id);
	}
	
	public static void main(String[] args) {
		try {
    		FileChannelWrapper ch = NIOUtils.readableChannel(new File("F:\\git\\WeChat\\temp\\1564988874286.mp4"));
    		FrameGrab fg = FrameGrab.createFrameGrab(ch);
    		Picture pic = fg.getNativeFrame();
    		Image img = SWTTools.toSWTImage(pic);
    		ImageLoader loader = new ImageLoader();
    		loader.data = new ImageData[]{img.getImageData()};
    		loader.save("D:\\tttttest\\aaaa.jpg", SWT.IMAGE_JPEG);
//    		long start = System.currentTimeMillis();
//    		for(int i = 0; i < frames ; i++) {
////    			fg.seekToFramePrecise(i);
//    			Picture pic = fg.getNativeFrame();
//    			BufferedImage img = SwingUtil.toBufferedImage(pic);
//    			ImageIO.write(img, "jpg", new File("d:/tttttest",i + ".jpg"));
//    		}
//    		System.out.println("cost:" + (System.currentTimeMillis() - start));
//    		FrameGrab.getFrameAtSec(new File(""), 12d);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JCodecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 用途：由SWING的图片换成SWT的图片对象
	 * @date 2017年1月10日
	 * @param base
	 * @param name
	 * @return
	 */
	public static Image AWTImg2SWTImg(BufferedImage base, String name) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			ImageIO.write(base, "png", out);
		} catch (IOException e) {
			System.out.println(name + "初始化失败！");
			e.printStackTrace();
			return null;
		}
		byte[] data = out.toByteArray();
		ByteArrayInputStream in = new ByteArrayInputStream(data);
		ImageData id = new ImageData(in);
		try {
			out.close();
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Image img = new Image(null, id);
		return img;
	}
	
}
