package com.xk.vlc;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Canvas;

public class ScaledCallbackPainter implements ICallbackImagePainter {

	protected Canvas c;
	
	@Override
	public void setControl(Canvas c) {
		this.c = c;
	}
	
	@Override
	public void redraw(GC g, ImageData id, Integer rotate) {
		if(null == c || c.isDisposed()) {
			return;
		}
		
		Rectangle rect = c.getClientArea();
		Transform transf = new Transform(c.getDisplay());
        if(rotate == 90) {
        	transf.translate(rect.width, 0);
        } else if(rotate == 180) {
        	transf.translate(rect.width, rect.height);
        } else if(rotate == 270) {
        	transf.translate(0, rect.height);
        } 
        transf.rotate(rotate);
        
        g.setTransform(transf);
		Image img = new Image(null, id);
		g.drawImage(img, 0, 0, id.width, id.height, 0, 0, id.width, id.height);
		img.dispose();
		g.dispose();
	}

}
