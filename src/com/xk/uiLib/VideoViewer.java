package com.xk.uiLib;


import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.wb.swt.SWTResourceManager;
import org.jcodec.common.model.ColorSpace;
import org.jcodec.common.model.Picture;
import org.jcodec.common.model.Rational;
import org.jcodec.common.model.Rect;
import org.jcodec.player.Player;
import org.jcodec.player.filters.VideoOutput;

import com.xk.bean.StringNode;
import com.xk.utils.SWTTools;

public class VideoViewer extends Canvas implements VideoOutput ,PaintListener, ICallable, MouseListener, ICallback<Integer>{

	private Image img;
	private Rational pasp;
	private Rectangle rect;
	private ICallback call;
	private Player player;
	private Integer perc = 0;
	
	public VideoViewer(Composite parent) {
		super(parent, SWT.NO_BACKGROUND | SWT.NO_REDRAW_RESIZE | SWT.DOUBLE_BUFFERED);
		addPaintListener(this);
		addMouseListener(this);
	}

	@Override
	public ColorSpace getColorSpace() {
		return ColorSpace.RGB;
	}

	@Override
	public void show(Picture pic, Rational pasp) {
		
		Image old = img;
		img = SWTTools.toSWTImage(pic);
		this.pasp = pasp;
		Rect crop = pic.getCrop();
		if(null != crop) {
			this.rect = new Rectangle(crop.getX(), crop.getY(), crop.getWidth(), crop.getHeight());
		} else {
			this.rect = null;
		}
		if(null != old) {
			old.dispose();
		}
		
		Display.getDefault().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				if(isDisposed()) {
					return;
				}
				redraw();
			}
		});
	}

	@Override
	public void paintControl(PaintEvent e) {
		GC g = e.gc;
		Rectangle client = getClientArea();
		if (img == null || pasp == null) {
			g.setBackground(SWTResourceManager.getColor(0x00, 0x00, 0x00));
			g.fillRectangle(client);
			g.setForeground(SWTResourceManager.getColor(0xFF, 0xFF, 0xFF));
			String text = "下载中:" + perc + "%";
			g.drawText(text, (client.width - g.textExtent(text, StringNode.DRAW_FLAGS).x) / 2,
					(client.height - g.textExtent(text, StringNode.DRAW_FLAGS).y) / 2, StringNode.DRAW_FLAGS);
			return;
		}
         
		Image image = img;
		img = null;
        ImageData bi = null;
        try {
			bi = image.getImageData();
		} catch (Exception e1) {
			return;
		}
        if (rect == null
                || (rect.x == 0 && rect.y == 0 && rect.width == bi.width && rect.height == bi
                        .height)) {

            g.drawImage(image, 0, 0, bi.width, bi.height, 0, 0, client.width, client.height);
        } else {

            g.drawImage(image, rect.x, rect.y, rect.width, rect.height, 0, 0, client.width, client.height);
        }
        g.dispose();
        image.dispose();
		
	}

	@Override
	public void setCallBack(ICallback callBack) {
		this.call = callBack;
		
	}

	@Override
	public void mouseDoubleClick(MouseEvent paramMouseEvent) {
		if(null != player) {
			player.destroy();
			player = null;
		}
		if(null != call) {
			call.callback(call);
			call = null;
		}
		
	}

	@Override
	public void mouseDown(MouseEvent paramMouseEvent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseUp(MouseEvent paramMouseEvent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Integer callback(Integer obj) {
		this.perc = obj;
		Display.getDefault().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				redraw();
			}
		});
		return obj;
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}
	
	

}
