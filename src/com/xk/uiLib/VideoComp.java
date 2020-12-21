package com.xk.uiLib;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.wb.swt.SWTResourceManager;

import com.xk.vlc.CallbackSWTMediaPlayer;

import uk.co.caprica.vlcj.media.callback.CallbackMedia;
import uk.co.caprica.vlcj.media.callback.seekable.RandomAccessFileMedia;
import uk.co.caprica.vlcj.player.component.CallbackMediaPlayerComponent;
import uk.co.caprica.vlcj.player.component.callback.ScaledCallbackImagePainter;
import uk.co.caprica.vlcj.support.Info;

public class VideoComp extends Composite implements ICallable,ICallback<Integer>,MouseListener,DisposeListener{
	private CallbackMedia media;
	private CallbackSWTMediaPlayer mediaPlayerComponent;
	
	private ICallback call;
	
	public VideoComp(Composite parent) {
		super(parent, SWT.NONE);
		Info.getInstance();
        StackLayout layout = new StackLayout();
        setLayout(layout);
		mediaPlayerComponent = new CallbackSWTMediaPlayer(this, null);
		mediaPlayerComponent.addMouseListener(this);
		layout.topControl = mediaPlayerComponent;
		setBackground(SWTResourceManager.getColor(SWT.COLOR_RED));
		addDisposeListener(this);
	}
	

	@Override
	public void setCallBack(ICallback callBack) {
		this.call = callBack;
		
	}
	
	
	public void play(File video) {
		media = new RandomAccessFileMedia(video);
		mediaPlayerComponent.mediaPlayer().media().play(media);
//		mediaPlayerComponent.mediaPlayer().media().play(video.getAbsolutePath());
	}

	public void play(CallbackMedia media) {
		this.media = media;
		mediaPlayerComponent.mediaPlayer().media().play(this.media);
	}
	
	@Override
	public Integer callback(Integer obj) {
		mediaPlayerComponent.setPerc(obj);
		return obj;
	}

	@Override
	public void mouseDoubleClick(MouseEvent var1) {
		dispose();
		
	}

	@Override
	public void mouseDown(MouseEvent var1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseUp(MouseEvent e) {
		if(e.button == 3 && e.count == 1) {
			Menu menu = getParent().getMenu();
			if(null != menu) {
				menu.dispose();
			}
			Menu m = new Menu(getParent());
			MenuItem rota = new MenuItem(m, SWT.NONE);
			rota.setText("旋转");
			rota.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					Integer rotate = mediaPlayerComponent.getRotate();
					rotate += 90;
					if(rotate == 360) {
						rotate = 0;
					}
					mediaPlayerComponent.setRotate(rotate);
					Point size = getParent().getSize();
					getParent().setSize(size.y, size.x);
				}
			});
			m.setVisible(true);
		}
	}


	@Override
	public void widgetDisposed(DisposeEvent arg0) {
		mediaPlayerComponent.mediaPlayer().controls().stop();
		mediaPlayerComponent.mediaPlayer().release();
		if(null != call) {
			call.callback(null);
			call = null;
		}
	}

}
