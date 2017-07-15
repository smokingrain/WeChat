package com.xk.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

public class FileUtils {

	public static String getFileExt(File file) {
		if(null == file || file.isDirectory()) {
			return "";
		}
		String name = file.getName();
		if(!name.contains(".")) {
			return "";
		}
		String ext = name.substring(name.lastIndexOf(".") + 1, name.length());
		return ext.toLowerCase();
	}
	
	
	public static void writeString(String content,File file){
		try {
			FileOutputStream fout=new FileOutputStream(file);
			fout.write(content.getBytes(StandardCharsets.UTF_8));
			fout.flush();
			fout.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static String readString(InputStream input) {
		int len = 0;
		StringBuffer str = new StringBuffer();
		BufferedReader in = null;
		try {
			InputStreamReader isr = new InputStreamReader(input, StandardCharsets.UTF_8);
			in = new BufferedReader(isr);
			String line = null;
			while ((line = in.readLine()) != null) {
				if (len != 0) {
					str.append("\r\n" + line);
				} else {
					str.append(line);
				}
				len++;
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			try {
				input.close();
				in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return str.toString();
	}
	
	
	public static String readString(File file){
        FileInputStream is = null;
		try {
			is = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			return null;
		}
        return readString(is);
	}
	
	
	public static String readString(String path){
        File file=new File(path);
        if(file.exists()&&file.isFile()){
        	return readString(file);
        }
        return null;

    }
	
	public static String getLimitString(String source ,int limit){
		if(null==source){
			return "";
		}
		if(source.length()>limit){
			return source.substring(0,limit)+"...";
		}
		return source;
	}
	
	public static String getFirstSpell(String source) {
		if(null == source || source.trim().isEmpty()) {
			return null;
		}
		HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
		format.setCaseType(HanyuPinyinCaseType.UPPERCASE);
		format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
		try {
			char first = source.toUpperCase().charAt(0);
			if(first >= 65 && first <= 90) {
				return String.valueOf(first);
			}
			String[] array = PinyinHelper.toHanyuPinyinStringArray(source.charAt(0), format);
			if(null != array && array.length > 0) {
				return array[0].substring(0, 1);
			}
		} catch (BadHanyuPinyinOutputFormatCombination e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 获取字符串编码
	 * @param str
	 * @return
	 */
	public static String getEncoding(String str) {  
        String encode = "GB2312";  
        try {  
            if (str.equals(new String(str.getBytes(encode), encode))) {  
            	return encode;  
            }  
        } catch (Exception exception) {  
        }  
        encode = "ISO-8859-1";  
        try {  
            if (str.equals(new String(str.getBytes(encode), encode))) {  
            	return encode;   
            }  
        } catch (Exception exception1) {  
        }  
        encode = "UTF-8";  
        try {  
            if (str.equals(new String(str.getBytes(encode), encode))) {  
            	return encode;  
            }  
        } catch (Exception exception2) {  
        }  
        encode = "GBK";  
        try {  
            if (str.equals(new String(str.getBytes(encode), encode))) {  
            	return encode;  
            }  
        } catch (Exception exception3) {  
        }  
        return "GBK";  
    }  
	
	public static void main(String[] args) {
		System.out.println(getFirstSpell("%sdfaas"));
	}
}
