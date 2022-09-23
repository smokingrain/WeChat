package com.xk.utils.song;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.xk.utils.HTTPUtil;
import com.xk.utils.HttpClientUtils;
import com.xk.utils.JSONUtil;


public class KuwoSource implements IDownloadSource {
	
	private BasicCookieStore cookieStore = new BasicCookieStore();
	private CloseableHttpClient client = HttpClientUtils.createSSLClientDefault(cookieStore);
	
	
	@Override
	public List<XRCLine> parse(String content) {
		Map<String, Object> map = JSONUtil.fromJson(content);
		List<Map<String, Object>> lrcs = (List<Map<String, Object>>) ((Map<String, Object>)map.get("data")).get("lrclist");
		LrcInfo lrc = new LrcInfo();
		Map<Long, String> infos = new HashMap<Long, String>();
		for(Map<String, Object> lrcObj : lrcs) {
			String time = (String) lrcObj.get("time");
			double dtime = Double.parseDouble(time);
			long ltime = (long) (dtime * 1000);
			String text = (String) lrcObj.get("lineLyric");
			infos.put(ltime, text);
		}
		
		lrc.setInfos(infos);
		return new LrcParser((long)Integer.MAX_VALUE).toXrc(lrc);
	}

	@Override
	public String getArtist(String name) {
		List<SearchInfo> songs=new ArrayList<SearchInfo>();
		String searchUrl=null;
		try {
			searchUrl = String.format("https://www.kuwo.cn/api/www/search/searchArtistBykeyWord?key=%s&pn=%d&rn=%d&reqId=%s&httpsStatus=%d", URLEncoder.encode(name, "utf-8"), 1, 30, UUID.randomUUID(), 1);
		} catch (UnsupportedEncodingException e) {
			return null;
		}
		get("https://www.kuwo.cn/");
		String json = get(searchUrl);
		if(!StringUtil.isBlank(json)){
			Map<String, Object> map = JSONUtil.fromJson(json);
			if(null == map.get("data") || !(map.get("data") instanceof Map)) {
				return null;
			}
			Map<String, Object> data = (Map<String, Object>) map.get("data");
			if(null == data.get("artistList") || !(data.get("artistList") instanceof List)) {
				return null;
			}
			List<Map<String, Object>> songinfos = (List<Map<String, Object>>) data.get("artistList");
			for(Map<String, Object> singer : songinfos) {
				if(name.equals(singer.get("name"))) {
					return (String) singer.get("pic");
				}
			}
		}
		
		return null;
	}
	
	@Override
	public List<SearchInfo> getLrc(String name) {
		List<SearchInfo> songs=new ArrayList<SearchInfo>();
		String searchUrl=null;
		try {
			searchUrl = String.format("https://www.kuwo.cn/api/www/search/searchMusicBykeyWord?key=%s&pn=%d&rn=%d&reqId=%s&httpsStatus=%d", URLEncoder.encode(name, "utf-8"), 1, 30, UUID.randomUUID(), 1);
		} catch (UnsupportedEncodingException e) {
			return songs;
		}
		get("https://www.kuwo.cn/");
		String json = get(searchUrl);
		if(!StringUtil.isBlank(json)){
			Map<String, Object> map = JSONUtil.fromJson(json);
			if(null == map.get("data") || !(map.get("data") instanceof Map)) {
				return songs;
			}
			Map<String, Object> data = (Map<String, Object>) map.get("data");
			if(null == data.get("list") || !(data.get("list") instanceof List)) {
				return songs;
			}
			List<Map<String, Object>> songinfos = (List<Map<String, Object>>) data.get("list");
			for(Map<String, Object> songinfo : songinfos) {
				SearchInfo info = new SearchInfo(){

					@Override
					public String getLrcUrl() {
						if(urlFound) {
							return lrcUrl;
						}
						String uuid = UUID.randomUUID().toString();
						lrcUrl = String.format("http://m.kuwo.cn/newh5/singles/songinfoandlrc?musicId=%s&httpsStatus=1&reqId=%s", url, uuid);
						return lrcUrl;
					}

					
				};
				info.lrcUrl =String.valueOf(songinfo.get("rid"));
				info.url =String.valueOf(songinfo.get("rid"));
				info.album = (String) songinfo.get("album");
				info.length = (Integer) songinfo.get("duration") * 1000L;
				info.name = (String) songinfo.get("name");
				info.singer = (String) songinfo.get("artist");
				songs.add(info);
			}
		}
		
		return songs;
	}

	@Override
	public List<SearchInfo> getMV(String name) {
		List<SearchInfo> songs=new ArrayList<SearchInfo>();
		String searchUrl=null;
		try {
			searchUrl = String.format("https://www.kuwo.cn/api/www/search/searchMvBykeyWord?key=%s&pn=%d&rn=%d&reqId=%s&httpsStatus=%d", URLEncoder.encode(name, "utf-8"), 1, 30, UUID.randomUUID(), 1);
		} catch (UnsupportedEncodingException e) {
			return songs;
		}
		get("https://www.kuwo.cn/");
		String json = get(searchUrl);
		if(!StringUtil.isBlank(json)){
			Map<String, Object> map = JSONUtil.fromJson(json);
			if(null == map.get("data") || !(map.get("data") instanceof Map)) {
				return songs;
			}
			Map<String, Object> data = (Map<String, Object>) map.get("data");
			if(null == data.get("mvlist") || !(data.get("mvlist") instanceof List)) {
				return songs;
			}
			List<Map<String, Object>> mvlist = (List<Map<String, Object>>) data.get("mvlist");
			for(Map<String, Object> mv : mvlist) {
				SearchInfo info = new SearchInfo(){

					@Override
					public String getUrl() {
						if(this.urlFound) {
							return this.url;
						}
						String uuid = UUID.randomUUID().toString();
						String requestUrl = String.format("https://www.kuwo.cn/api/www/music/musicInfo?mid=%s&httpsStatus=1&reqId=%s", this.url,  uuid);
						String json = get(requestUrl);
						Map<String, Object> map = JSONUtil.fromJson(json);
						String id = (String) ((Map<String, Object>)map.get("data")).get("musicrid");
						String mp4Url = "http://www.kuwo.cn/yy/st/mvurl?rid=" + id;
						this.url = HTTPUtil.getInstance().getHtml(mp4Url);
						this.flashVars.put("url", this.url);
						this.urlFound = true;
						return this.url;
					}

					@Override
					public String getLrcUrl() {
						return this.lrcUrl;
					}
					
				};
				info.lrcUrl =String.valueOf(mv.get("id"));
				info.url =String.valueOf(mv.get("id"));
				info.length = (Integer) mv.get("duration") * 1000L;
				info.name = (String) mv.get("name");
				info.singer = (String) mv.get("artist");
				info.type = "mv";
				songs.add(info);
			}
		}
		
		return songs;
		
		
	}
	
	private void addCookies(HttpResponse response) {
		Header[] headers = response.getHeaders("set-cookie");
		if(null != headers) {
			for(Header header : headers) {
				cookieStore.addCookie(new BasicClientCookie(header.getName(), header.getValue()));
			}
		}
	}
	
	private String cookieToString() {
		List<Cookie> cookies = cookieStore.getCookies();
		StringBuffer buffer  = new StringBuffer();
		if(null != cookies) {
			for(Cookie cookie : cookies) {
				buffer.append(cookie.getName()).append("=").append(cookie.getValue()).append(";");	
			}
		}
		return buffer.toString();
	}
	
	private void addHeaders(HttpRequestBase httppost) {
		httppost.addHeader("Accept", "*/*");
		httppost.addHeader("Accept-Encoding", "gzip,deflate,sdch");
		httppost.addHeader("Accept-Language", "zh-CN,zh;q=0.8,gl;q=0.6,zh-TW;q=0.4");
		httppost.addHeader("Connection", "keep-alive");
		httppost.addHeader("Host", "www.kuwo.cn");
		httppost.addHeader("Origin", "https://www.kuwo.cn");
		httppost.addHeader("Referer", "https://www.kuwo.cn");
		httppost.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.66 Safari/537.36");
		httppost.addHeader("Cookie", cookieToString());
		List<Cookie> cookies = cookieStore.getCookies();
		for(Cookie cookie : cookies) {
			if("kw_token".equals(cookie.getName())) {
				httppost.addHeader("csrf", cookie.getValue());
			}
		}
	}
	
	private String get(String url) {
		StringBuffer result=new StringBuffer();
		HttpGet httppost = new HttpGet(url);
		addHeaders(httppost);
		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(2000).setConnectTimeout(2000).build();//设置请求和传输超时时间
		httppost.setConfig(requestConfig);
		CloseableHttpResponse response = null;
		try {
			response = client.execute(httppost);  
			addCookies(response);
			HttpEntity entity = response.getEntity();  
			InputStream instream=entity.getContent();
			BufferedReader br = new BufferedReader(new InputStreamReader(instream,StandardCharsets.UTF_8));  
			String temp = "";  
			while ((temp = br.readLine()) != null) {  
			    result.append(temp);  
			}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			try {
				response.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
        
        return result.toString();
	}
	
	private String post(String url, List<BasicNameValuePair> formparams) {
		UrlEncodedFormEntity entity1 = new UrlEncodedFormEntity(formparams, StandardCharsets.UTF_8);  
		
		//新建Http  post请求  
		HttpPost httppost = new HttpPost(url);  
		httppost.setEntity(entity1);  
		addHeaders(httppost);
		//处理请求，得到响应  
		CloseableHttpResponse response = null;
		StringBuilder result = new StringBuilder();
		try {
			response = client.execute(httppost);  
			addCookies(response);
			//打印返回的结果  
			HttpEntity entity = response.getEntity();  
			
			if (entity != null) {  
				InputStream instream = entity.getContent();  
				BufferedReader br = new BufferedReader(new InputStreamReader(instream, StandardCharsets.UTF_8));  
				String temp = "";  
				while ((temp = br.readLine()) != null) {  
					result.append(temp);  
				}  
			}  
			httppost.releaseConnection();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if(null != response) {
					response.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return result.toString();
	}

	@Override
	public List<SearchInfo> getSong(String name, String type) {
		List<SearchInfo> songs=new ArrayList<SearchInfo>();
		String searchUrl=null;
		try {
			searchUrl = String.format("https://www.kuwo.cn/api/www/search/searchMusicBykeyWord?key=%s&pn=%d&rn=%d&reqId=%s&httpsStatus=%d", URLEncoder.encode(name, "utf-8"), 1, 30, UUID.randomUUID(), 1);
		} catch (UnsupportedEncodingException e) {
			return songs;
		}
		get("https://www.kuwo.cn/");
		String json = get(searchUrl);
		if(!StringUtil.isBlank(json)){
			Map<String, Object> map = JSONUtil.fromJson(json);
			if(null == map.get("data") || !(map.get("data") instanceof Map)) {
				return songs;
			}
			Map<String, Object> data = (Map<String, Object>) map.get("data");
			if(null == data.get("list") || !(data.get("list") instanceof List)) {
				return songs;
			}
			List<Map<String, Object>> songinfos = (List<Map<String, Object>>) data.get("list");
			for(Map<String, Object> songinfo : songinfos) {
				SearchInfo info = new SearchInfo(){

					@Override
					public String getUrl() {
						if(urlFound) {
							return url;
						}
						String uuid = UUID.randomUUID().toString();
						String requestUrl = String.format("http://www.kuwo.cn/api/v1/www/music/playUrl?mid=%s&type=music&httpsStatus=1&reqId=%s", this.url, uuid);
						String json = get(requestUrl);
						Map<String, Object> map = JSONUtil.fromJson(json);
						this.lrcUrl = String.format("http://m.kuwo.cn/newh5/singles/songinfoandlrc?musicId=%s&httpsStatus=1&reqId=%s", this.url, uuid);
						this.url =(String) (( Map<String, Object>) map.get("data")).get("url");
						this.urlFound = true;
						return this.url;
					}

					@Override
					public String getLrcUrl() {
						return this.lrcUrl;
					}
					
				};
				info.lrcUrl =String.valueOf(songinfo.get("rid"));
				info.url =String.valueOf(songinfo.get("rid"));
				info.album = (String) songinfo.get("album");
				info.length = (Integer) songinfo.get("duration") * 1000L;
				info.name = (String) songinfo.get("name");
				info.singer = (String) songinfo.get("artist");
				info.type = type;
				songs.add(info);
			}
		}
		
		return songs;
	}
	
	@Override
	public List<SearchInfo> getSong(String name) {
		return getSong(name, "mp3");
	}

	@Override
	public Map<String, String> fastSearch(String name) {
		if(StringUtil.isBlank(name)) {
			return Collections.emptyMap();
		}
		try {
			name = URLEncoder.encode(name, "utf-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		String url = "https://kuwo.cn/api/www/search/searchKey?key=" + name + "&httpsStatus=1&reqId=" + UUID.randomUUID().toString();
		String url = "http://search.kuwo.cn/r.s?all=" + name + "&ft=music&client=kt&cluster=0&pn=0&rn=50&rformat=json&callback=searchMusicResult&encoding=utf8&r=" + System.currentTimeMillis();
		String html=HTTPUtil.getInstance().getHtml(url);
		if(StringUtil.isBlank(html)) {
			return Collections.emptyMap();
		}
		html = html.replace("; searchMusicResult(jsondata);}catch(e){jsonError(e)}", "").replace("try{var jsondata=", "");
		Map<String, Object> rst = JSONUtil.fromJson(html);
		if(null == rst || rst.isEmpty()) {
			return Collections.emptyMap();
		}
		List<Map<String, String>> list = (List<Map<String, String>>) rst.get("abslist");
		Map<String, String> result = new HashMap<String, String>();
		for(Map<String, String> map : list) {
			result.put(map.get("NAME"), map.get("SONGNAME"));
		}
		return result;
	}

	@Override
	public SongLocation getInputStream(String url) {
		return HTTPUtil.getInstance().getInputStream(url);
	}

}
