package com.xk.utils.song;

import java.util.HashMap;   
import java.util.Map;

/**  
 * 用来封装歌词信息的类  
 * @author Administrator  
 *  
 */  
public class LrcInfo {   
    private String title;//歌曲名   
    private String singer;//演唱者   
    private String album;//专辑      
    private Map<Long,String> infos;//保存歌词信息和时间点一一对应的Map   
   //以下为getter()  setter()   
	public void setInfos(Map<Long, String> maps) {
		this.infos=(HashMap<Long, String>) maps;
		
	}
	public void setTitle(String title2) {
		this.title=title2;
		
	}
	public void setSinger(String singer2) {
		this.singer=singer2;
		
	}
	public void setAlbum(String album2) {
		this.album=album2;
		
	}
	public String getTitle() {
		return title;
	}
	public String getSinger() {
		return singer;
	}
	public String getAlbum() {
		return album;
	}
	public HashMap<Long, String> getInfos() {
		return (HashMap<Long, String>) infos;
	}
       
}  


