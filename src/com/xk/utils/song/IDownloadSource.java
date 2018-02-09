package com.xk.utils.song;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;


public interface IDownloadSource {

	public List<SearchInfo> getLrc(String name);
	
	public List<SearchInfo> getMV(String name);
	
	public List<SearchInfo> getSong(String name);
	
	public List<SearchInfo> getSong(String name, String type);
	
	public Map<String, String> fastSearch(String name);
	
	public String getArtist(String name);
	
	public SongLocation getInputStream(String url);
	
	public List<XRCLine> parse(String content);
	
	public static class SearchInfo{
		boolean urlFound = false;
		String url = "";
		String lrcUrl = "";
		public Long length = 272560L;
		public String name = "";
		public String singer = "";
		public String album = "";
		public String type = "mp3";
		public Header[] headers = null;
		public String swfUrl = "http://static.kgimg.com/common/swf/video/videoPlayer.swf?20141014061415";
		public Map<String, String> flashVars = new LinkedHashMap<String, String>(){
			/**
			 * 
			 */
			private static final long serialVersionUID = 8488577499381226518L;

			{
				put("skinurl", "http://static.kgimg.com/common/swf/video/skin.swf");
				put("aspect", "true");
				put("autoplay", "true");
				put("fullscreen", "true");
				put("initfun", "flashinit");
			}
		};
		public String getUrl() {
			return url;
		}
		
		public String getLrcUrl() {
			return lrcUrl;
		}
	}
	
}
