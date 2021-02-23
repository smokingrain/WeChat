package com.xk.vlc;

import java.io.File;
import java.io.InputStream;

import com.xk.utils.song.SongLocation;

public class M3U8CallbackMedia extends HTTPUrlCallbackMedia {

	public M3U8CallbackMedia(SongLocation url, File target) {
		super(url, target);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected InputStream getStream() {
		super.length = 0;
		return new M3U8InputStream(new File("d:/yixingzaibiain.m3u8"), "https://fangao.zzwc120.com/concat/20200904/f7f0b3f27a604724ab3da1a8f86b6345/cloudv-transfer/");
	}

}
