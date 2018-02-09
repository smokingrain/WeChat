package com.xk.utils.song;

import java.io.BufferedReader;   
import java.io.File;   
import java.io.FileInputStream;   
import java.io.FileNotFoundException;   
import java.io.IOException;   
import java.io.InputStream;   
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;   
import java.util.regex.Matcher;   
import java.util.regex.Pattern;   

  
/**  
 * 此类用来解析LRC文件 将解析完整的LRC文件放入一个LrcInfo对象中 并且返回这个LrcInfo对象s 
 * 
 *  @author xiaokui
 */  
public class LrcParser {   
    private Long allLength;
  
    public LrcParser(Long allLength){
    	this.allLength=allLength;
    }
    
    /**  
     * 根据文件路径，读取文件，返回一个输入流  
     *   
     * @param path  
     *            路径  
     * @return 输入流  
     * @throws FileNotFoundException  
     */  
    private InputStream readLrcFile(String path) throws FileNotFoundException {   
        File f = new File(path);   
        InputStream ins = new FileInputStream(f);   
        return ins;   
    }   
    
    public List<XRCLine> toXrc(LrcInfo lrcinfo) {
    	Map<Long, String> maps = lrcinfo.getInfos();
        Long[]templist = new Long[maps.keySet().size()];
		Iterator<Long> itr = maps.keySet().iterator();
		int index = 0;
		while (itr.hasNext()) {
			templist[index] = itr.next();
			index++;
		}
		List<Long>timelist = Arrays.asList(sortList(templist));
		List<XRCLine> lines=new ArrayList<XRCLine>();
		String lastWord=null;
		XRCLine lastLine=null;
		for(Long time:timelist){
			XRCLine line=new XRCLine();
			lines.add(line);
			line.start=time;
			if(null!=lastLine&&null!=lastWord){
				lastLine.length=time-lastLine.start;
				long per=(long) (((double)lastLine.length)/lastWord.length());
				for(int i=0;i<lastWord.length();i++){
					XRCNode node=new XRCNode();
					node.start=per*i;
					node.length=per;
					node.word=lastWord.charAt(i)+"";
					lastLine.nodes.add(node);
				}
			}
			lastWord=maps.get(time);
			lastLine=line;
		}
		if(null!=allLength&&null!=lastWord&&null!=lastLine){
			lastLine.length=allLength-lastLine.start;
			long per=(long) (((double)lastLine.length)/lastWord.length());
			for(int i=0;i<lastWord.length();i++){
				XRCNode node=new XRCNode();
				node.start=per*i;
				node.length=per;
				node.word=lastWord.charAt(i)+"";
				lastLine.nodes.add(node);
			}
		}
        return lines;
    }
    
    public List<XRCLine> parserToXrc(Reader in) {
    	LrcInfo lrcinfo = parser(in);
        return toXrc(lrcinfo);
    }
    public List<XRCLine> parserToXrc(InputStream in) {
    	LrcInfo lrcinfo = parser(in);
        return toXrc(lrcinfo);
    }
    
    public List<XRCLine> parser(String path) throws Exception {   
        InputStream in = readLrcFile(path); 
        return parserToXrc(in);
    }   
       
    private Long[] sortList(Long[] timelist) {
		long t = 0;
		for (int i = 0; i < timelist.length; i++) {
			for (int j = i; j < timelist.length; j++) {
				if (timelist[i] > timelist[j]) {
					t = timelist[i];
					timelist[i] = timelist[j];
					timelist[j] = t;
				}
			}
		}
		return timelist;

	}
    
    public LrcInfo parser(Reader read) {
    	LrcInfo lrcinfo = new LrcInfo();
    	try {
			BufferedReader reader = new BufferedReader(read);   
			// 一行一行的读，每读一行，解析一行   
			String line = null;   
			Map<Long, String> maps = new HashMap<Long, String>();
			while ((line = reader.readLine()) != null) {  
			    parserLine(line, maps, lrcinfo);   
			}
			lrcinfo.setInfos(maps);   
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			// 全部解析完后，设置info   
	        try {
	        	if(null != read) {
	        		read.close();
	        	}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
        
        
        
        return lrcinfo; 
    }
    
    /**  
     * 将输入流中的信息解析，返回一个LrcInfo对象  
     *   
     * @param inputStream  
     *            输入流  
     * @return 解析好的LrcInfo对象  
     * @throws IOException  
     */  
    public LrcInfo parser(InputStream inputStream) {   
        // 三层包装   
    	LrcInfo lrcinfo = new LrcInfo();
		try {
			InputStreamReader inr = new InputStreamReader(inputStream, StandardCharsets.UTF_8); 
			BufferedReader reader = new BufferedReader(inr);   
			// 一行一行的读，每读一行，解析一行   
			String line = null;   
			Map<Long, String> maps = new HashMap<Long, String>();
			while ((line = reader.readLine()) != null) {  
			    parserLine(line, maps, lrcinfo);   
			}
			lrcinfo.setInfos(maps);   
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			// 全部解析完后，设置info   
	        try {
	        	if(null != inputStream) {
	        		inputStream.close();
	        	}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
        
        
        
        return lrcinfo;   
    }   
  
    /**  
     * 利用正则表达式解析每行具体语句  
     * 并在解析完该语句后，将解析出来的信息设置在LrcInfo对象中  
     *   
     * @param str  
     * @param maps 
     */  
    private void parserLine(String str, Map<Long, String> maps, LrcInfo lrcinfo) {   
        // 取得歌曲名信息   
        if (str.startsWith("[ti:")) {   
            String title = str.substring(4, str.length() - 1);   
           // System.out.println("title--->" + title);   
            lrcinfo.setTitle(title);   
  
        }// 取得歌手信息   
        else if (str.startsWith("[ar:")) {   
            String singer = str.substring(4, str.length() - 1);   
            //System.out.println("singer--->" + singer);   
            lrcinfo.setSinger(singer);   
  
        }// 取得专辑信息   
        else if (str.startsWith("[al:")) {   
            String album = str.substring(4, str.length() - 1);   
           // System.out.println("album--->" + album);   
            lrcinfo.setAlbum(album);   
  
        }// 通过正则取得每句歌词信息   
        else {   
            // 设置正则规则   
            String reg = "\\[(\\d{2}:\\d{2}\\.\\d{2})\\]";   
            // 编译   
            Pattern pattern = Pattern.compile(reg);   
            Matcher matcher = pattern.matcher(str);   
  
            // 如果存在匹配项，则执行以下操作   
            while (matcher.find()) {   
  
                // 得到这个匹配项中的组数   
                int groupCount = matcher.groupCount();   
                
                String[] content = pattern.split(str);
                String currentContent=null;
                if(content.length>0){
                	currentContent=content[content.length-1];
                }else{
                	continue;
                }
                
                
                // 得到每个组中内容   
                for (int i = 0; i <= groupCount; i++) {   
                    String timeStr = matcher.group(i);   
                    long currentTime = strToLong(timeStr);   
                    maps.put(currentTime, currentContent);
                }   
  
            }   
        }   
    }   
  
    /**  
     * 将解析得到的表示时间的字符转化为Long型  
     *   
     * @param group  
     *            字符形式的时间点  
     * @return Long形式的时间  
     */  
    private long strToLong(String timeStr) {   
        // 因为给如的字符串的时间格式为XX:XX.XX,返回的long要求是以毫秒为单位   
        // 1:使用：分割 2：使用.分割   
    	timeStr=timeStr.replace("[", "").replace("]", "");
        String[] s = timeStr.split(":");   
        int min = Integer.parseInt(s[0]);   
        String[] ss = s[1].split("\\.");   
        int sec = Integer.parseInt(ss[0]);   
        return min * 60 * 1000 + sec * 1000 ;//+ mill * 10;   
    }   
  
}  

