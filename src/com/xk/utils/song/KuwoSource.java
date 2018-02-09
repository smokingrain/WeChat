package com.xk.utils.song;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.xk.utils.HTTPUtil;
import com.xk.utils.JSONUtil;

public class KuwoSource implements IDownloadSource {

	/**
	 * 从html中解析从歌词
	 * @param html
	 * @return
	 * @author xiaokui
	 */
	public LrcInfo perseFromHTML(String html){
		Document doc=Jsoup.parse(html);
		Elements lrcs=doc.select("script");
		LrcInfo lrc=new LrcInfo();
		Map<Long,String> infos=new HashMap<Long, String>();
		for(Element ele:lrcs){
			String content = ele.html();
			if(null != content) {
				content = content.trim();
				System.out.println(content);
				if(content.startsWith("var lrcList = ")) {
					content = content.replace("var lrcList = ", "");
					List<Map<String, String>> listLrcs = JSONUtil.toBean(content, JSONUtil.getCollectionType(List.class, Map.class));
					for(Map<String, String> lrcLine : listLrcs) {
						String time = lrcLine.get("time");
						double dtime = Double.parseDouble(time);
						long ltime = (long) (dtime*1000);
						String text = lrcLine.get("lineLyric");
						infos.put(ltime, text);
					}
					break;
				}
			}
		}
		lrc.setInfos(infos);
		return lrc;
	}
	
	
	@Override
	public List<XRCLine> parse(String content) {
		LrcInfo info = perseFromHTML(content);
		return new LrcParser((long)Integer.MAX_VALUE).toXrc(info);
	}

	@Override
	public String getArtist(String name) {
		String searchUrl=null;
		try {
			searchUrl="http://sou.kuwo.cn/ws/NSearch?type=artist&key="+URLEncoder.encode(name, "utf-8")+"&catalog=yueku20177";
		} catch (UnsupportedEncodingException e) {
			return null;
		}
		String html=HTTPUtil.getInstance().getHtml(searchUrl);
		if(!StringUtil.isBlank(html)){
			Document doc=Jsoup.parse(html);
			Elements texts=doc.getElementsByAttribute("lazy_src");
			for(Element ele:texts){
				String alt=ele.attr("alt");
				if(null!=alt&&alt.contains(name.replace(" ", "&nbsp;"))){
					return ele.attr("lazy_src");
				}
			}
		}
		return null;
	}
	
	@Override
	public List<SearchInfo> getLrc(String name) {
		List<SearchInfo> lrcs=new ArrayList<SearchInfo>();
		String searchUrl=null;
		try {
			searchUrl = "http://sou.kuwo.cn/ws/NSearch?type=music&key="+URLEncoder.encode(name, "utf-8")+"&catalog=yueku20177";
		} catch (UnsupportedEncodingException e) {
			return lrcs;
		}
		String html=HTTPUtil.getInstance().getHtml(searchUrl);
		if(!StringUtil.isBlank(html)){
			Document doc=Jsoup.parse(html);
			Elements eles=doc.select("li[class=clearfix]");
			for(Element ele:eles){
				SearchInfo info=new SearchInfo() {

					@Override
					public String getLrcUrl() {
						return "";
					}
					
				};
				lrcs.add(info);
				Elements names=ele.getElementsByAttributeValue("class", "m_name");
				for(Element nameP:names){
					Elements as=nameP.getElementsByTag("a");
					for(Element a:as){
						info.name=a.attr("title");
						info.url=a.attr("href");
						break;
					}
					break;
				}
				Elements singers=ele.getElementsByAttributeValue("class", "s_name");
				for(Element singer:singers){
					Elements as=singer.getElementsByTag("a");
					for(Element a:as){
						info.singer=a.attr("title");
						break;
					}
					break;
				}
			}
		}
		return lrcs;
	}

	@Override
	public List<SearchInfo> getMV(String name) {
		List<SearchInfo> songs=new ArrayList<SearchInfo>();
		String searchUrl=null;
		try {
			searchUrl = "http://sou.kuwo.cn/ws/NSearch?type=mv&key="+URLEncoder.encode(name, "utf-8")+"&catalog=yueku20177";
		} catch (UnsupportedEncodingException e) {
			return songs;
		}
		String html=HTTPUtil.getInstance().getHtml(searchUrl);
		if(!StringUtil.isBlank(html)){
			Document doc=Jsoup.parse(html);
			Elements eles=doc.select("div[class=mvalbum]");
			for(Element ele : eles) {
				Elements uls = ele.select("ul[class=clearfix]");
				for(Element ul : uls) {
					Elements lis = ul.getElementsByTag("li");
					for(Element li : lis) {
						SearchInfo info = new SearchInfo(){

							@Override
							public String getUrl() {
								if(this.urlFound) {
									return this.url;
								}
								String mp4Url = "http://www.kuwo.cn/yy/st/mvurl?rid=MUSIC_" + this.url;
								this.url = HTTPUtil.getInstance().getHtml(mp4Url);
								this.flashVars.put("url", this.url);
								this.urlFound = true;
								return this.url;
							}

							@Override
							public String getLrcUrl() {
								return null;
							}
							
						};
						
						songs.add(info);
						info.type= "mv";
						info.album = "";
						Elements as = li.select("a[class=img]");
						for(Element a : as) {
							info.name = a.attr("title");
							String href = a.attr("href");
							String songId = href.replace("http://www.kuwo.cn/mv/", "").replace("/", "");
							info.url = songId;
							break;
						}
						Elements ps = li.select("p[class=singerName]");
						for(Element p : ps) {
							Elements pas = p.getElementsByTag("a");
							for(Element pa : pas) {
								info.singer = pa.attr("title");
							}
							break;
						}
						
					}
				}
			}
		}
		return songs;
	}

	@Override
	public List<SearchInfo> getSong(String name, String type) {
		List<SearchInfo> songs=new ArrayList<SearchInfo>();
		String searchUrl=null;
		try {
			searchUrl = "http://sou.kuwo.cn/ws/NSearch?type=music&key="+URLEncoder.encode(name, "utf-8")+"&catalog=yueku20177";
		} catch (UnsupportedEncodingException e) {
			return songs;
		}
		String html=HTTPUtil.getInstance().getHtml(searchUrl);
		if(!StringUtil.isBlank(html)){
			Document doc=Jsoup.parse(html);
			Elements eles=doc.select("li[class=clearfix]");
			for(Element ele:eles){
				SearchInfo info = new SearchInfo(){

					@Override
					public String getUrl() {
						if(urlFound) {
							return url;
						}
						this.urlFound = true;
						this.url = HTTPUtil.getInstance().getHtml(this.url);
						return this.url;
					}

					@Override
					public String getLrcUrl() {
						return this.lrcUrl;
					}
					
				};
				info.type = type;
				songs.add(info);
				Elements names = ele.getElementsByAttributeValue("class", "m_name");
				for(Element nameP : names){
					Elements as = nameP.getElementsByTag("a");
					for(Element a :as){
						info.name = a.attr("title");
						info.lrcUrl = a.attr("href");
						break;
					}
					break;
				}
				Elements albums = ele.getElementsByAttributeValue("class", "a_name");
				for(Element albumP:albums){
					Elements as = albumP.getElementsByTag("a");
					for(Element a : as){
						info.album = a.attr("title");
						break;
					}
					break;
				}
				Elements singers = ele.getElementsByAttributeValue("class", "s_name");
				for(Element singer:singers){
					Elements as = singer.getElementsByTag("a");
					for(Element a : as){
						info.singer=a.attr("title");
						break;
					}
					break;
				}
				Elements numbers=ele.getElementsByAttributeValue("class", "number");
				String download="response=url&type=convert%5Furl&rid=MUSIC%5F{mid}&format="+type;
				String baseHost="http://antiserver.kuwo.cn/anti.s?";
				for(Element number:numbers){
					Elements as=number.getElementsByTag("input");
					for(Element a:as){
						String mid=a.attr("mid");
						String url = baseHost+download.replace("{mid}", mid);
						info.url=url;
						break;
					}
					break;
				}
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
		String url = "http://search.kuwo.cn/r.s?SONGNAME=" + name + "&ft=music&rformat=json&encoding=utf8&rn=8&callback=song&vipver=MUSIC_8.0.3.1&_=" + System.currentTimeMillis();
		String html=HTTPUtil.getInstance().getHtml(url);
		if(StringUtil.isBlank(html)) {
			return Collections.emptyMap();
		}
		html = html.replace(";song(jsondata);}catch(e){jsonError(e)}", "").replace("try {var jsondata =", "");
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
