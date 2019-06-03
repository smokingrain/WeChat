package com.xk.utils.song;

import java.io.File;
import java.util.List;

import org.jsoup.helper.StringUtil;

import com.xk.uiLib.ICallback;
import com.xk.utils.Constant;
import com.xk.utils.FileUtils;
import com.xk.utils.interfaces.ICMDHandler;
import com.xk.utils.song.IDownloadSource.SearchInfo;

public class SongHandler implements ICMDHandler {

	@Override
	public void handle(String content, String user, ICallback<File> fileCall, ICallback<String> textCall, ICallback<File> imgCall) {
		if(null == fileCall || null == textCall) {
			return;
		}
		if(StringUtil.isBlank(content)) {
			return;
		}
		File parent = new File(Constant.DOWNLOAD_TEMP);
		if(!parent.exists()) {
			parent.mkdirs();
		}
		File target = new File(parent, content + ".mp3");
		if(target.exists()) {
			textCall.callback("找到历史记录,从记录中转发...");
			fileCall.callback(target);
			return;
		}
		String source = Constant.SONG_SOURCE.get(user);
		if(StringUtil.isBlank(source)) {
			source = "kugou";
			Constant.SONG_SOURCE.put(user, source);
		}
		IDownloadSource dSource = SourceFactory.getSource(source);
		if(null == dSource) {
			return;
		}
		List<SearchInfo> infos = dSource.getSong(content);
		if(null == infos) {
			return;
		}
		textCall.callback("找到" + infos.size() + "个结果，筛选中...");
		for(SearchInfo info : infos) {
			if(content.equals(info.name)) {
				String url = info.getUrl();
				if(null != url) {
					SongLocation location = dSource.getInputStream(url);
					if(null == location || location.length == 0) {
						continue;
					}
					File saved = FileUtils.saveStream(target, location.input);
					if(null != saved) {
						fileCall.callback(saved);
						break;
					}
				}
			}
		}

	}

}
