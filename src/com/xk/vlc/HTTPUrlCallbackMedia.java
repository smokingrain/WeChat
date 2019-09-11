package com.xk.vlc;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import com.xk.utils.song.SongLocation;

import uk.co.caprica.vlcj.media.callback.seekable.SeekableCallbackMedia;

public class HTTPUrlCallbackMedia extends SeekableCallbackMedia {

	private WriteOnReadInputStream input;
	private long length;
	private SongLocation url;
	private File target;
	
	public HTTPUrlCallbackMedia(SongLocation url, File target) {
		this.url = url;
		this.target = target;
		init();
	}
	
	private void init() {
		
		InputStream in = getStream();
		input = new WriteOnReadInputStream(in, length, null) {
			
			@Override
			public void onDownloadEnd(File file) {
				System.out.println("download path : " + file.getAbsolutePath());
				
			}
		};
	}
	
	private InputStream getStream() {
		this.length = url.length;
		return url.input;
	}
	
	
	@Override
	protected int onRead(byte[] buffer, int bufferSize) throws IOException {
		return input.read(buffer, 0, bufferSize);
	}

	@Override
	protected long onGetSize() {
		return length;
	}

	@Override
	protected boolean onOpen() {
		try {
			input.init(target);
			return true;
		} catch (IOException e) {
			return false;
		}
		
	}

	@Override
	protected boolean onSeek(long offset) {
		// TODO Auto-generated method stub
		try {
			return input.skip(offset) > 0;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	@Override
	protected void onClose() {
		try {
			input.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
