package com.xk.vlc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.xk.utils.HTTPUtil;

public class M3U8InputStream extends InputStream {
	
	private boolean inited = false;
	private String base = "";
	List<String> sites = new ArrayList<String>();
	private String key;
	private long readed = 0;
	
	private InputStream current;
	/**
	 * 从文件初始化
	 * @param file m3u8文件
	 * @param base 如果m3u8是相对路径显示，这里需要基础url
	 */
	public M3U8InputStream(File file, String base) {
		if(null != base && base.startsWith("http")) {
			this.base = base;
		}
		try (FileInputStream in = new FileInputStream(file)){
			init(in);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	/**
	 * 从url初始化
	 * @param url
	 */
	public M3U8InputStream(String url) {
		this.base = url.substring(0, url.lastIndexOf("/"));
		try(InputStream in = HTTPUtil.getInstance().getInput(url)) {
			init(in);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 初始化
	 * 作者 ：肖逵
	 * 时间 ：2020年9月22日 上午11:30:44
	 * @param in
	 */
	private void init(InputStream in) {
		InputStreamReader ir = new InputStreamReader(in);
		BufferedReader reader = new BufferedReader(ir);
		String line = null;
		try {
			while((line = reader.readLine()) != null) {
				if(!(line.startsWith("#EXT")) && !line.isEmpty()) {
					if(!line.startsWith("http")) {
						line = (base.endsWith("/") ? base : (base + "/")) + line;
					}
					sites.add(line.trim());
				} else if(line.startsWith("#EXT-X-KEY:METHOD=AES-128,URI=")) {
					line = line.replace("#EXT-X-KEY:METHOD=AES-128,URI=", "").replace("\"", "");
					key = HTTPUtil.getInstance().getHtml(line);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				ir.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		next();
	}
	
	private boolean next() {
		if(sites.isEmpty()) {
			System.out.println("没有数据");
			return false;
		}
		String next = sites.remove(0);
		System.out.println("访问 ：" + next);
		current = HTTPUtil.getInstance().getInput(next);
		return null != current;
	}
	

	@Override
	public int read() throws IOException {
		if(null == current) {
			System.out.println("没有current了 ");
			return -1;
		}
		int rst = current.read();
		if(rst >= 0) {
			return rst;
		}
		//此时，已经读完一段
		System.out.println("此时，已经读完一段");
		try {
			current.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(next()) {
			return current.read();
		}
		return -1;
	}

	@Override
	public void close() throws IOException {
		System.out.println("我被关闭了");
		sites.clear();
		if(null != current) {
			current.close();
		}
	}

	
	
}
