package com.xk.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class FileUtils {

	
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
	
	
	public static String readString(File file){
		int len=0;
        StringBuffer str=new StringBuffer("");
        try {
            FileInputStream is=new FileInputStream(file);
            InputStreamReader isr= new InputStreamReader(is,StandardCharsets.UTF_8);
            BufferedReader in= new BufferedReader(isr);
            String line=null;
            while( (line=in.readLine())!=null ){
                if(len != 0){
                    str.append("\r\n"+line);
                }else{
                    str.append(line);
                }
                len++;
            }
            in.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return str.toString();
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
}
