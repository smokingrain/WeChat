package com.xk.utils.song;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 解析后的歌词行
 * @author xiaokui
 *
 */
public class XRCLine {
	public Long start;
	public Long length;
	public List<XRCNode> nodes=new ArrayList<XRCNode>();
	
	public XRCLine() {
	}
	
	public XRCLine(Long start, Long length, List<XRCNode> nodes) {
		this.start = start;
		this.length = length;
		this.nodes = nodes;
	} 

	public void destroy(){
		start=null;
		length=null;
		nodes=null;
	}
	
	@JsonIgnore
	public String getWord(){
		StringBuilder sb=new StringBuilder();
		for(XRCNode node:nodes){
			sb.append(node.word);
		}
		return sb.toString();
	}
}
