package com.xk.utils.song;

public class SourceFactory {

	
	public static IDownloadSource getSource(String name) {
		if("kuwo".equals(name)) {
			return new KuwoSource();
		} else if("kugou".equals(name)) {
			return new KugouSource();
		} else if("ne".equals(name)) {
			return new NetEasySource();
		} else if("qier".equals(name)) {
			return new QierSource();
		}
		return null;
	}
	
}
