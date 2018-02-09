package com.xk.utils.song;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.http.Header;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicHeader;

import com.xk.utils.HTTPUtil;
import com.xk.utils.JSONUtil;

import sun.misc.BASE64Decoder;


public class QierSource implements IDownloadSource {

	@Override
	public List<SearchInfo> getLrc(String name) {
		List<SearchInfo> songs = new ArrayList<SearchInfo>();
		List<Map<String, Object>> list = search(name, "song", "0");
		if(null == list) {
			return songs;
		}
		for(Map<String, Object> map : list) {
			SearchInfo info = new SearchInfo() {

				@Override
				public String getUrl() {
					return getLrcUrl();
				}

				@Override
				public String getLrcUrl() {
					if(urlFound) {
						return url;
					}
					headers = new Header[]{new BasicHeader("referer", "https://y.qq.com/portal/player.html")};
					url = String.format("https://c.y.qq.com/lyric/fcgi-bin/fcg_query_lyric_new.fcg?callback=MusicJsonCallback_lrc&pcachetime=%s&songmid=%s&g_tk=5381&jsonpCallback=MusicJsonCallback_lrc&loginUin=0&hostUin=0&format=jsonp&inCharset=utf8&outCharset=utf-8&notice=0&platform=yqq&needNewCode=0", System.currentTimeMillis() + "", url);
					urlFound = true;
					return url;
				}
				
			};
			info.name = (String) map.get("name");
			info.type = "mp3";
			Map<String, Object> album = (Map<String, Object>) map.get("album");
			info.album = (String) album.get("name");
			List<Map<String, Object>> singer = (List<Map<String, Object>>) map.get("singer");
			if(null != singer && singer.size() >= 1) {
				info.singer = (String) singer.get(0).get("name");
			}
			Map<String, Object> file = (Map<String, Object>) map.get("file");
			if(null == file || file.isEmpty()) {
				continue;
			}
			info.url = (String) file.get("media_mid");
			songs.add(info);
		}
		return songs;
	}

	@Override
	public List<SearchInfo> getMV(String name) {
		List<SearchInfo> songs = new ArrayList<SearchInfo>();
		List<Map<String, Object>> list = search(name, "mv", "12");
		if(null == list) {
			return songs;
		}
		for(Map<String, Object> map : list) {
			SearchInfo info = new SearchInfo() {

				@Override
				public String getLrcUrl() {
					return lrcUrl;
				}
				
			};
			info.name = (String) map.get("mv_name");
			info.singer = (String) map.get("singer_name");
			info.type = "mv";
			info.swfUrl = "https://imgcache.qq.com/tencentvideo_v1/playerv3/TPout.swf?max_age=86400&v=" + System.currentTimeMillis();
			info.flashVars.clear();
			info.flashVars.put("vid", (String)map.get("v_id"));
			info.flashVars.put("autoplay", "1");
			info.flashVars.put("volume", "50");
			info.flashVars.put("searchbar", "0");
			info.flashVars.put("showcfg", "1");
			info.flashVars.put("showend", "1");
			info.flashVars.put("openbc", "1");
			info.flashVars.put("list", "2");
			info.flashVars.put("pay", "0");
			info.flashVars.put("canreplay", "0");
			info.flashVars.put("shownext", "0");
			info.flashVars.put("share", "1");
			info.flashVars.put("bullet", "0");
			info.flashVars.put("theater", "0");
			info.flashVars.put("skin", "https://imgcache.qq.com/minivideo_v1/vd/res/skins/TencentPlayerOutSkinV5.swf");
			info.flashVars.put("switch2h5", "0");
			info.flashVars.put("bulletinput", "0");
			info.flashVars.put("attstart", "");
			info.flashVars.put("defnpayver", "0");
			info.flashVars.put("fmt", "auto");
			info.flashVars.put("vstart", "0");
			info.flashVars.put("ptag", "y_qq_com");
			info.flashVars.put("guid", "3c8beccf47b4863554b3c30e2f18e37f");
			info.flashVars.put("mbid", "&ch%2F" + map.get("v_id") + ".html");
			info.flashVars.put("pageInitTime", "");
			info.flashVars.put("playerInitTime", "" + System.currentTimeMillis());
			info.flashVars.put("fakefull", "0");
			info.flashVars.put("playertype", "6");
			info.flashVars.put("adext", "");
			info.flashVars.put("rcd_info", "");
			songs.add(info);
		}
		return songs;
	}

	@Override
	public List<SearchInfo> getSong(String name) {
		return getSong(name, "mp3");
	}

	private List<Map<String, Object>> search(String name, String type, String searchType) {
		String url = "https://c.y.qq.com/soso/fcgi-bin/client_search_cp";
		String jsonpCallback = "searchCallbacksong" + random4Num(4);
		Map<String, String> params = new HashMap<String, String>();
		params.put("jsonpCallback", jsonpCallback);
		params.put("ct", "24");
		params.put("qqmusic_ver", "1298");
		params.put("new_json", "1");
		params.put("remoteplace", "txt.yqq.song");
		params.put("searchid", "1");
		params.put("t", searchType);
		params.put("aggr", "1");
		params.put("cr", "1");
		params.put("catZhida", "1");
		params.put("lossless", "0");
		params.put("flag_qc", "0");
		params.put("p", "0");
		params.put("n", "20");
		params.put("g_tk", "5381");
		params.put("w", name);
		params.put("loginUin", "0");
		params.put("hostUin", "0");
		params.put("format", "jsonp");
		params.put("inCharset", "utf-8");
		params.put("outCharset", "utf-8");
		params.put("notice", "0");
		params.put("platform", "yqq");
		params.put("needNewCode", "0");
		String rst = HTTPUtil.getInstance().getHtml(url, params);
		if(null != rst && rst.startsWith(jsonpCallback)) {
			rst = rst.substring(jsonpCallback.length() + 1, rst.length() - 1);
			Map<String, Object> result = JSONUtil.fromJson(rst);
			Map<String, Object> data = (Map<String, Object>) result.get("data");
			if(null == data) {
				return null;
			}
			Map<String, Object> song = (Map<String, Object>) data.get(type);
			if(null == song) {
				return null;
			}
			List<Map<String, Object>> list = (List<Map<String, Object>>) song.get("list");
			return list;
		}
		return null;
	}
	
	@Override
	public List<SearchInfo> getSong(String name, String type) {
		List<SearchInfo> songs = new ArrayList<SearchInfo>();
		List<Map<String, Object>> list = search(name, "song", "0");
		if(null == list) {
			return songs;
		}
		for(Map<String, Object> map : list) {
			SearchInfo info = new SearchInfo() {

				@Override
				public String getUrl() {
					if(urlFound) {
						return url;
					}
					String guid = String.valueOf(Math.floor(Math.random() * 1000000000));
					String text = HTTPUtil.getInstance().getHtml("https://c.y.qq.com/base/fcgi-bin/fcg_musicexpress.fcg?json=3&guid=" + guid);
					text = text.replace("jsonCallback", "").trim();
					text = text.substring(1, text.length() - 2);
					Map<String, Object> map = JSONUtil.fromJson(text);
					String vkey = (String) map.get("key");
					lrcUrl = String.format("https://c.y.qq.com/lyric/fcgi-bin/fcg_query_lyric_new.fcg?callback=MusicJsonCallback_lrc&pcachetime=%s&songmid=%s&g_tk=5381&jsonpCallback=MusicJsonCallback_lrc&loginUin=0&hostUin=0&format=jsonp&inCharset=utf8&outCharset=utf-8&notice=0&platform=yqq&needNewCode=0", System.currentTimeMillis() + "", url);
					url = String.format("http://dl.stream.qqmusic.qq.com/M800%s.mp3?vkey=%s&guid=%s&fromtag=30", url, vkey, guid);
					headers = new Header[]{new BasicHeader("referer", "https://y.qq.com/portal/player.html")};
					urlFound = true;
					return url;
				}

				@Override
				public String getLrcUrl() {
					if(urlFound) {
						return lrcUrl;
					}
					headers = new Header[]{new BasicHeader("referer", "https://y.qq.com/portal/player.html")};
					lrcUrl = String.format("https://c.y.qq.com/lyric/fcgi-bin/fcg_query_lyric_new.fcg?callback=MusicJsonCallback_lrc&pcachetime=%s&songmid=%s&g_tk=5381&jsonpCallback=MusicJsonCallback_lrc&loginUin=0&hostUin=0&format=jsonp&inCharset=utf8&outCharset=utf-8&notice=0&platform=yqq&needNewCode=0", System.currentTimeMillis() + "", url);
					return lrcUrl;
				}
				
			};
			info.name = (String) map.get("name");
			info.type = "mp3";
			info.length = Long.parseLong(map.get("interval").toString()) * 1000 * 1000;
			Map<String, Object> album = (Map<String, Object>) map.get("album");
			info.album = (String) album.get("name");
			List<Map<String, Object>> singer = (List<Map<String, Object>>) map.get("singer");
			if(null != singer && singer.size() >= 1) {
				info.singer = (String) singer.get(0).get("name");
			}
			Map<String, Object> file = (Map<String, Object>) map.get("file");
			if(null == file || file.isEmpty()) {
				continue;
			}
			info.url = (String) file.get("media_mid");
			songs.add(info);
		}
		return songs;
	}
	
	private String random4Num(int num) {
		StringBuffer sb = new StringBuffer();
		Random random = new Random();
		for(int i = 0; i < num; i++) {
			int next = random.nextInt(8) + 1;
			sb.append(next);
		}
		return sb.toString();
	}

	@Override
	public Map<String, String> fastSearch(String name) {
		Map<String, Object> map = fast(name);
		if(null == map) {
			return null;
		}
		Map<String, Object> data = (Map<String, Object>) map.get("data");
		if(null == data) {
			return null;
		}
		Map<String, Object> songs = (Map<String, Object>) data.get("song");
		if(null == songs) {
			return null;
		}
		List<Map<String, Object>> list = (List<Map<String, Object>>) songs.get("itemlist");
		if(null == list) {
			return null;
		}
		Map<String, String> result = new HashMap<String, String>();
		for(Map<String, Object> item : list) {
			result.put(item.get("name").toString(), item.get("name").toString());
		}
		return result;
	}
	
	private Map<String, Object> fast(String name) {
		String callBack = "SmartboxKeysCallbackmod_search" + random4Num(4);
		String url = null;
		try {
			url = String.format("https://c.y.qq.com/splcloud/fcgi-bin/smartbox_new.fcg?is_xml=0&format=jsonp&key=%s&g_tk=5381&jsonpCallback=%s&loginUin=0&hostUin=0&format=jsonp&inCharset=utf8&outCharset=utf-8&notice=0&platform=yqq&needNewCode=0", URLEncoder.encode(name, "UTF-8"), callBack);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
		String rst = HTTPUtil.getInstance().getHtml(url);
		if(null != rst && rst.startsWith(callBack)) {
			rst = rst.replace(callBack, "");
			rst = rst.substring(1, rst.length() - 1).trim();
			Map<String, Object> map = JSONUtil.fromJson(rst);
			return map;
		}
		return null;
	}

	@Override
	public String getArtist(String name) {
		if(null == name) {
			return null;
		}
		Map<String, Object> map = fast(name);
		if(null == map) {
			return null;
		}
		Map<String, Object> data = (Map<String, Object>) map.get("data");
		if(null == data) {
			return null;
		}
		Map<String, Object> singers = (Map<String, Object>) data.get("singer");
		if(null == singers) {
			return null;
		}
		List<Map<String, Object>> list = (List<Map<String, Object>>) singers.get("itemlist");
		if(null == list) {
			return null;
		}
		for(Map<String, Object> item : list) {
			if(name.equals(item.get("name"))) {
				return (String) item.get("pic");
			}
		}
		return null;
	}

	@Override
	public SongLocation getInputStream(String url) {
		return HTTPUtil.getInstance().getInputStream(url);
	}

	@Override
	public List<XRCLine> parse(String content) {
		if(null != content && content.startsWith("MusicJsonCallback_lrc(")) {
			content = content.replace("MusicJsonCallback_lrc(", "");
			content = content.substring(0, content.length() - 1);
			try {
				Map<String, Object> map = JSONUtil.fromJson(content);
				if(null != map) {
					String lrc = (String)map.get("lyric");
					if(null == lrc) {
						return null;
					}
					content = new String(new BASE64Decoder().decodeBuffer(lrc), StandardCharsets.UTF_8);
					StringReader in = new StringReader(content);
					return new LrcParser((long)Integer.MAX_VALUE).parserToXrc(in);
				}
			} catch (IOException e) {
				return null;
			}
			
		}
		return null;
	}

}
