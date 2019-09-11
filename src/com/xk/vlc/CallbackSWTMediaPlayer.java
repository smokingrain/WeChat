package com.xk.vlc;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.wb.swt.SWTResourceManager;

import com.xk.bean.StringNode;
import com.xk.utils.SWTTools;

import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.component.MediaPlayerComponent;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormat;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormatCallbackAdapter;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.RenderCallbackAdapter;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.format.RV32BufferFormat;

public class CallbackSWTMediaPlayer extends EmbeddedMediaPlayerSWTBase implements MediaPlayerComponent, PaintListener{

	/**
     * Default factory initialisation arguments.
     */
    static final String[] DEFAULT_FACTORY_ARGUMENTS = MediaPlayerComponentDefaults.EMBEDDED_MEDIA_PLAYER_ARGS;


    /**
     * Media player factory.
     */
    protected final MediaPlayerFactory mediaPlayerFactory;
    
    protected DefaultRenderCallback renderCallback;
    
    protected ICallbackImagePainter imagePainter;
    
    protected ImageData imageData;
    
    protected Integer perc = 0;
    protected Integer rotate = 0;
    
    /**
     * Media player.
     */
    private final EmbeddedMediaPlayer mediaPlayer;
	
	
	public CallbackSWTMediaPlayer(Composite parent,MediaPlayerFactory mediaPlayerFactory) {
		super(parent, SWT.DOUBLE_BUFFERED);
		this.mediaPlayerFactory = initMediaPlayerFactory(mediaPlayerFactory);
		this.renderCallback = new DefaultRenderCallback();
		this.imagePainter = new ScaledCallbackPainter();
		imagePainter.setControl(this);
		DefaultBufferFormatCallback formater = new DefaultBufferFormatCallback();
		this.mediaPlayer = this.mediaPlayerFactory.mediaPlayers().newEmbeddedMediaPlayer();
		this.mediaPlayer.events().addMediaPlayerEventListener(this);
        this.mediaPlayer.events().addMediaEventListener(this);
        
        this.mediaPlayer.videoSurface().set(this.mediaPlayerFactory.videoSurfaces().newVideoSurface(formater, renderCallback, true));
        setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
        addPaintListener(this);
        setVisible(true);
        System.out.println("aaaaaaaaa visiable" + this.getVisible());
        SWTTools.enableTrag(this);
	}

	private MediaPlayerFactory initMediaPlayerFactory(MediaPlayerFactory mediaPlayerFactory) {
		if (mediaPlayerFactory == null) {
            mediaPlayerFactory = new MediaPlayerFactory(DEFAULT_FACTORY_ARGUMENTS);
        }
        return mediaPlayerFactory;
	}
	
	@Override
	public MediaPlayerFactory mediaPlayerFactory() {
		return mediaPlayerFactory;
	}
	
	public MediaPlayer mediaPlayer() {
		return mediaPlayer;
	}
	
	public void setPerc(Integer perc) {
		this.perc = perc;
	}
	
	public void setRotate(Integer rotate) {
		this.rotate = rotate;
	}
	
	public Integer getRotate() {
		return rotate;
	}
	
	private class DefaultRenderCallback extends RenderCallbackAdapter {

		@Override
		protected void onDisplay(MediaPlayer mediaPlayer, int[] srcData) {
			int width = imageData.width;
			int height = imageData.height;
			for(int i = 0; i < height; i++) {
				int[] line = new int[width];
				System.arraycopy(srcData, i * width, line, 0, width);
				imageData.setPixels(0, i, width, line, 0);
			}
			
	        Display.getDefault().asyncExec(new Runnable() {
				
				@Override
				public void run() {
					if(!isDisposed()) {
						redraw();
					}
					
				}
			});
			
		}
		
	}
	
	private class DefaultBufferFormatCallback extends BufferFormatCallbackAdapter {

        @Override
        public BufferFormat getBufferFormat(int sourceWidth, int sourceHeight) {
            newVideoBuffer(sourceWidth, sourceHeight);
            return new RV32BufferFormat(sourceWidth, sourceHeight);
        }

    }
	
	private void newVideoBuffer(int width, int height) {
		PaletteData palette = new PaletteData(0xFF0000, 0x00FF00, 0x0000FF); 
		imageData = new ImageData(width, height, 24, palette);
		imageData.palette = palette;
		Display.getDefault().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				getShell().setSize(width, height);
			}
		});
		
		renderCallback.setBuffer(new int[width * height]);
    }

	@Override
	public void paintControl(PaintEvent e) {
		GC g = e.gc;
		Rectangle client = getClientArea();
		if (imageData == null ) {
			g.setBackground(SWTResourceManager.getColor(0x00, 0x00, 0x00));
			g.fillRectangle(client);
			g.setForeground(SWTResourceManager.getColor(0xFF, 0xFF, 0xFF));
			String text = "下载中:" + perc + "%";
			g.drawText(text, (client.width - g.textExtent(text, StringNode.DRAW_FLAGS).x) / 2,
					(client.height - g.textExtent(text, StringNode.DRAW_FLAGS).y) / 2, StringNode.DRAW_FLAGS);
			return;
		}
		if(null != imageData) {//此时没有初始化
			imagePainter.redraw(e.gc, imageData, rotate);
		}
		
		
	}

}
