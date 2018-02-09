package com.xk.utils;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.xk.uiLib.ICallback;
import com.xk.utils.song.SongLocation;


public class HTTPUtil {
	private CookieStore cookieStore = new BasicCookieStore(); 
	public CloseableHttpClient httpClient = HttpClientUtils.createSSLClientDefault(cookieStore);
	public static String cid=null;
	private static HTTPUtil instance;
	
	
	public static HTTPUtil getInstance(){
		if(null == instance){
			instance=new HTTPUtil();
		}
		return instance;
	}
	
	private HTTPUtil(){
		
		
	}
	
	public String getCookie(String key) {
		if(key == null) {
			return null;
		}
		for(Cookie cookie : cookieStore.getCookies()) {
			if(key.equals(cookie.getName())) {
				return cookie.getValue();
			}
		}
		return null;
	}
	
	public void close(){
		try {
			httpClient.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String httpPostFile(String url, Map<String, String> params, Map<String, File> files) {
		return httpPostFile(url, params, files, null);
	}
	
	
	/**
	 * 用途：http图片上传
	 * @date 2017年1月5日
	 * @param url
	 * @param params
	 * @param files
	 * @return
	 */
	public String httpPostFile(String url, Map<String, String> params, Map<String, File> files, ICallback callBack) {
		try {
			MultipartEntityBuilder builder = MultipartEntityBuilder.create();
			if (null != files) {
				for (String key : files.keySet()) {
					File file = files.get(key);
					if(null == file) {
						continue;
					}
					FileHookBody body = new FileHookBody(file, ContentType.DEFAULT_BINARY, file.getName());
					body.setCallBack(callBack);
					builder.addPart(key, body);
				}
			}
			if (null != params) {
				for (String key : params.keySet()) {
					System.out.println(key);
					builder.addTextBody(key, params.get(key));
				}
			}
			HttpEntity entity = builder.build();
			HttpPost httppost = new HttpPost(url);
			httppost.setEntity(entity);
			CloseableHttpResponse resp = httpClient.execute(httppost);
			int status = resp.getStatusLine().getStatusCode();
			if (302 == status) {
				String redirect = resp.getHeaders("Location")[0].getValue();
				resp.close();
				if (null != redirect) {
					return redirect;
				}
			}
			HttpEntity resEntity = resp.getEntity();
			String respContent = EntityUtils.toString(resEntity, "UTF-8").trim();
			httppost.abort();
			return respContent;
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}
	
	
	public String getHtml(String url, Map<String, String> params) {
		if(null != params) {
			StringBuffer sb = new StringBuffer();
			sb.append("?");
			for(String key : params.keySet()) {
				sb.append(key).append("=").append(params.get(key)).append("&");
			}
			url += sb.toString();
		}
		return getHtml(url);
	}
	
	
	public String getHtml(String url, List<NameValuePair> params) {
		if(null != params) {
			StringBuffer sb = new StringBuffer();
			sb.append("?");
			for(NameValuePair param : params) {
				sb.append(param.getName()).append("=").append(param.getValue()).append("&");
			}
			url += sb.toString();
		}
		return getHtml(url);
	}
	
	public String getHtml(String url){
		StringBuffer result=new StringBuffer();
		try {
			HttpGet httppost = new HttpGet(url);  
			httppost.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.155 Safari/537.36");
			RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(50000).setConnectTimeout(50000).setConnectionRequestTimeout(50000).build();//设置请求和传输超时时间
			httppost.setConfig(requestConfig);
			CloseableHttpResponse response = httpClient.execute(httppost);  
			if(302==response.getStatusLine().getStatusCode()){
				Header[] headers=response.getHeaders("Location");
				response.close();
				if(null!=headers&&headers.length>0){
					Header header=headers[0];
					String redirect=header.getValue();
					HttpPost httppost1 = new HttpPost(redirect);  
					CloseableHttpResponse response1=httpClient.execute(httppost1);  
					HttpEntity entity = response1.getEntity();  
					InputStream instream=entity.getContent();
					BufferedReader br = new BufferedReader(new InputStreamReader(instream,StandardCharsets.UTF_8));  
		            String temp = "";  
		            while ((temp = br.readLine()) != null) {  
		                result.append(temp);  
		            }  
				}
				return null;
			}else if(200==response.getStatusLine().getStatusCode()){
				HttpEntity entity = response.getEntity();  
				InputStream instream=entity.getContent();
				BufferedReader br = new BufferedReader(new InputStreamReader(instream,StandardCharsets.UTF_8));  
	            String temp = "";  
	            while ((temp = br.readLine()) != null) {  
	                result.append(temp);  
	            }  
			}
			response.close();
		}  catch (Exception e) {
			System.out.println(e.getMessage());
			return null;
		}
		return result.toString();
	}
	
	public SongLocation getInputStream(String url) {
		return getInputStream(url, null);
	}
	
	public SongLocation getInputStream(String url, Map<String, String> params) {
		if(null != params) {
			url += "?";
			for(String key : params.keySet()) {
				String value = params.get(key);
				url += key + "=" + value + "&";
			}
		}
		HttpGet httppost = new HttpGet(url);  
		httppost.addHeader("Connection", "keep-alive");
		httppost.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.155 Safari/537.36");
		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(50000).setConnectTimeout(50000).setConnectionRequestTimeout(50000).build();//设置请求和传输超时时间
		httppost.setConfig(requestConfig);
		try {
			CloseableHttpResponse response = httpClient.execute(httppost);  
			if(302==response.getStatusLine().getStatusCode()){
				Header[] headers=response.getHeaders("Location");
				response.close();
				if(null!=headers&&headers.length>0){
					Header header=headers[0];
					String redirect=header.getValue();
					return getInputStream(redirect);
				}
			}else{
				HttpEntity entity = response.getEntity();
				SongLocation location = new SongLocation();
				location.input = entity.getContent();
				location.length = entity.getContentLength();
				return location;
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
		}
		return null;
	}
	
	
	
	public InputStream getInput(String url, Map<String, String> params){
		SongLocation loc = getInputStream(url, params);
		if(null != loc) {
			return loc.input;
		}
		return null;
		
	}
	public InputStream getInput(String url){
		return getInput(url, null);
		
	}
	
	
	public String getFileName(HttpResponse response) throws UnsupportedEncodingException {  
        Header contentHeader = response.getFirstHeader("Content-Disposition");  
        String filename = null;  
        if (contentHeader != null) {  
            HeaderElement[] values = contentHeader.getElements();  
            if (values.length == 1) {  
                NameValuePair param = values[0].getParameterByName("filename");  
                if (param != null) {  
                    try {  
                        filename = new String(param.getValue().getBytes("ISO-8859-1"), "GBK");  
                    } catch (Exception e) {  
                        e.printStackTrace();  
                    }  
                }  
            }  
        }  
        return URLDecoder.decode(filename, "GBK");  
    }  
	
	public String readJsonfromURL(String url,Map<String,String> params) throws ClientProtocolException, IOException{
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		if(null!=params){
			Set<String> keys=params.keySet();
			for(String key:keys){
				formparams.add(new BasicNameValuePair(key, params.get(key)));
			}
		}
		UrlEncodedFormEntity entity1 = new UrlEncodedFormEntity(formparams, "GB2312");  
        
        //新建Http  post请求  
        HttpPost httppost = new HttpPost(url);  
        httppost.setEntity(entity1);  
  
        //处理请求，得到响应  
        CloseableHttpResponse response = httpClient.execute(httppost);  
      
        //打印返回的结果  
        HttpEntity entity = response.getEntity();  
          
        StringBuilder result = new StringBuilder();  
        if (entity != null) {  
            InputStream instream = entity.getContent();  
            BufferedReader br = new BufferedReader(new InputStreamReader(instream,"GB2312"));  
            String temp = "";  
            while ((temp = br.readLine()) != null) {  
                result.append(temp);  
            }  
        }  
        httppost.releaseConnection();
        response.close();
		return result.toString();
	}
	
	public String postBody(String url , Map<String, String> params,String body) throws ClientProtocolException, IOException {
		if(null != params) {
			url += "?";
			for(String key : params.keySet()) {
				String value = params.get(key);
				url += key + "=" + value + "&";
			}
		}
		return postBody(url,body);
	}
	
	public String postBody(String url ,String body) throws ClientProtocolException, IOException {
		HttpPost httppost = new HttpPost(url);
		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(30000).setConnectTimeout(30000).build();//设置请求和传输超时时间
		StringEntity strEntity = new StringEntity(body, StandardCharsets.UTF_8);
		httppost.setEntity(strEntity);  
		httppost.setConfig(requestConfig);
		//处理请求，得到响应  
		CloseableHttpResponse response = httpClient.execute(httppost);  
		
		//打印返回的结果  
		HttpEntity entity = response.getEntity();  
		
		StringBuilder result = new StringBuilder();  
		if (entity != null) {  
			InputStream instream = entity.getContent();  
			BufferedReader br = new BufferedReader(new InputStreamReader(instream,"UTF-8"));  
			String temp = "";  
			while ((temp = br.readLine()) != null) {  
				result.append(temp);  
			}  
		}  
		httppost.releaseConnection();
		response.close();
		return result.toString();
	}
	
	public String readJsonfromURL2(String url, Map<String, String> params, Map<String, String> cookie) throws ClientProtocolException, IOException{
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		if(null!=params){
			Set<String> keys=params.keySet();
			for(String key:keys){
				formparams.add(new BasicNameValuePair(key, params.get(key)));
			}
		}
		UrlEncodedFormEntity entity1 = new UrlEncodedFormEntity(formparams, "UTF-8");  
		
		//新建Http  post请求  
		HttpPost httppost = new HttpPost(url);  
		httppost.setEntity(entity1);  
		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(30000).setConnectTimeout(30000).build();//设置请求和传输超时时间
		httppost.setConfig(requestConfig);
		if(null != cookie) {
		}
		
		//处理请求，得到响应  
		CloseableHttpResponse response = httpClient.execute(httppost);  
		
		//打印返回的结果  
		HttpEntity entity = response.getEntity();  
		
		StringBuilder result = new StringBuilder();  
		if (entity != null) {  
			InputStream instream = entity.getContent();  
			BufferedReader br = new BufferedReader(new InputStreamReader(instream,"UTF-8"));  
			String temp = "";  
			while ((temp = br.readLine()) != null) {  
				result.append(temp);  
			}  
		} 
		httppost.releaseConnection();
		response.close();
		return result.toString();
	}
	
	public String getJsonfromURL2(String url,Map<String,String> params) throws ClientProtocolException, IOException{
		StringBuffer sb = new StringBuffer(url);
		if(null != params) {
			sb.append("?");
			for(String key : params.keySet()) {
				sb.append(key).append("=").append(params.get(key)).append("&");
			}
		}
		return getHtml(sb.toString());
		
	}
	public String readJsonfromURL2(String url,Map<String,String> params) throws ClientProtocolException, IOException{
		return readJsonfromURL2(url, params, null);
	}
	
	
	
	public void saveToStream(String url,OutputStream out){
		try {
			HttpGet httppost = new HttpGet(url);  
			httppost.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.155 Safari/537.36");
			RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(2000).setConnectTimeout(2000).build();//设置请求和传输超时时间
			httppost.setConfig(requestConfig);
			CloseableHttpResponse response = httpClient.execute(httppost);  
			if(302==response.getStatusLine().getStatusCode()){
				Header[] headers=response.getHeaders("Location");
				response.close();
				if(null!=headers&&headers.length>0){
					Header header=headers[0];
					String redirect=header.getValue();
					HttpPost httppost1 = new HttpPost(redirect);  
					CloseableHttpResponse response1=httpClient.execute(httppost1);  
					HttpEntity entity = response1.getEntity();  
					InputStream instream=entity.getContent();
					byte[]buff=new byte[10240];
					int len=0;
		            while ((len = instream.read(buff, 0, buff.length))>=0) {  
		            	out.write(buff, 0, len);  
		            }  
		            out.flush();
		            out.close();
				}
			}else if(200==response.getStatusLine().getStatusCode()){
				HttpEntity entity = response.getEntity();  
				InputStream instream=entity.getContent();
				byte[]buff=new byte[10240];
				int len=0;
	            while ((len = instream.read(buff, 0, buff.length))>=0) {  
	            	out.write(buff, 0, len);  
	            }  
	            out.flush();
	            out.close();
			}
			response.close();
		}  catch (Exception e) {
			System.out.println(e.getMessage()); 
		}
	}
	
}
