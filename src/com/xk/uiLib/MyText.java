package com.xk.uiLib;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import org.eclipse.wb.swt.SWTResourceManager;

public class MyText extends StyledText implements PaintListener{
	private Image end;
	private DeleteListener listener;
	public MyText(Composite parent, int style) {
		super(parent, style);
		this.addPaintListener(this);
		this.setForeground(SWTResourceManager.getColor(253, 254, 255));
	}
	
	public void setNoTrim(){
		Region region=new Region();
		int borders=this.getBorderWidth()*2;
		region.add(new Rectangle(borders/2, borders/2, this.getSize().x-borders*2, this.getSize().y-borders*2));
		this.setRegion(region);
	}
	

	public void setInnerImage(final Image img){
		this.end=img;
		if(end!=null){
			this.addMouseMoveListener(new MouseMoveListener(){

				@Override
				public void mouseMove(MouseEvent e) {
					if(MyText.this.getSize().x-e.x<end.getImageData().width+12){
						MyText.this.setCursor(new Cursor(Display.getDefault(),SWT.CURSOR_HAND));
					}else{
						MyText.this.setCursor(new Cursor(Display.getDefault(),SWT.CURSOR_IBEAM));
					}
					
				}
				
			});
		}
	}
	public void addDeleteListener(DeleteListener listener){
		this.listener=listener;
		this.addMouseListener(new MouseAdapter(){

			@Override
			public void mouseUp(MouseEvent e) {
				if(MyText.this.getCursor().equals(new Cursor(Display.getDefault(),SWT.CURSOR_HAND))){
					MyText.this.listener.deleteClicked();
				}
			}
			
		});
	}
	
	@Override
	protected void checkSubclass() {
		
	}
	public  interface DeleteListener{
		public void deleteClicked();
	}
	@Override
	public void paintControl(PaintEvent e) {
		int border=getBorderWidth();
		GC gc=e.gc;
		if(end!=null){
			gc.drawImage(this.end, this.getSize().x-end.getImageData().width-12,(this.getSize().y-end.getImageData().height)/2-border*2);
		}
		int alf=gc.getAlpha();
		gc.setAlpha(60);
		gc.fillRoundRectangle(0, 0, this.getSize().x-border*5, this.getSize().y-border*5, 5, 5);
		gc.setAlpha(alf);
		gc.dispose();
		
	}
	
}
