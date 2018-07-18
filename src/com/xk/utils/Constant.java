package com.xk.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;

import com.xk.bean.ContactsStruct;
import com.xk.bean.User;
import com.xk.bean.WeChatSign;

public class Constant {
	
	
	
	public static final String SEND_MSG = "https://wx.qq.com/cgi-bin/mmwebwx-bin/webwxsendmsg";
	public static final String STATUS_NOTIFY = "https://wx.qq.com/cgi-bin/mmwebwx-bin/webwxstatusnotify";
	public static final String GET_STATUS = "https://wx.qq.com/cgi-bin/mmwebwx-bin/webwxsync";
	public static final String SYNC_CHECK = "https://webpush.wx.qq.com/cgi-bin/mmwebwx-bin/synccheck";
	public static final String GET_MEMBER_ICON = "https://wx.qq.com/cgi-bin/mmwebwx-bin/webwxgeticon";
	public static final String GET_CONTACT = "https://wx.qq.com/cgi-bin/mmwebwx-bin/webwxgetcontact";
	public static final String GET_GROUPS = "https://wx.qq.com/cgi-bin/mmwebwx-bin/webwxbatchgetcontact?type=ex&lang=zh_CN&r={TIME}&pass_ticket={TICKET}";
	public static final String GET_CONV_ID = "https://login.weixin.qq.com/jslogin?appid=wx782c26e4c19acffb&redirect_uri=https%3A%2F%2Fwx.qq.com%2Fcgi-bin%2Fmmwebwx-bin%2Fwebwxnewloginpage&fun=new&lang=zh_CN&_={TIME}";
	public static final String GET_QR_IMG = "https://login.weixin.qq.com/qrcode/{UUID}";
	public static final String GET_STATUE = "https://login.weixin.qq.com/cgi-bin/mmwebwx-bin/login?uuid={UUID}&tip=1&_={TIME}";
	public static final String GET_INIT = "https://wx.qq.com/cgi-bin/mmwebwx-bin/webwxinit?r={TIME}";
	public static final String BASE_URL = "https://wx.qq.com/";
	public static final String LOGOUT_URL = "https://wx.qq.com/cgi-bin/mmwebwx-bin/webwxlogout?redirect=1&type=1&skey={SKEY}";
	public static final String LOAD_IMG = "https://wx.qq.com/cgi-bin/mmwebwx-bin/webwxgetmsgimg";
	public static final String LOAD_VOICE = "https://wx.qq.com/cgi-bin/mmwebwx-bin/webwxgetvoice";
	public static final String UPLOAD_MEDIA = "https://file.wx.qq.com/cgi-bin/mmwebwx-bin/webwxuploadmedia";
	public static final String SEND_IMG = "https://wx.qq.com/cgi-bin/mmwebwx-bin/webwxsendmsgimg";
	public static final String SEND_GIF = "https://wx.qq.com/cgi-bin/mmwebwx-bin/webwxsendemoticon";
	public static final String SEND_FILE = "https://wx.qq.com/cgi-bin/mmwebwx-bin/webwxsendappmsg";
	public static final String OP_LOG = "https://wx.qq.com/cgi-bin/mmwebwx-bin/webwxoplog";
	public static final String REVOKE_MSG = "https://wx.qq.com/cgi-bin/mmwebwx-bin/webwxrevokemsg";
	
	
	public static final String[] FORMATS = {".bmp", ".bmp", ".gif", ".ico", ".jpg", ".png", ".tiff", ".bmp"};
	
	public static Map<String, String> imgTypes = new HashMap<String, String>();
	public static User user;
	public static Set<String> noReply = new HashSet<String>();
	public static Boolean globalSilence = true;
	public static WeChatSign sign;
	public static Map<String, ContactsStruct> contacts = new HashMap<String, ContactsStruct>();
	public static Integer file_index = 0;
	
	public static String DOWNLOAD_TEMP = "songtemp";
	public static final Map<String, String> SONG_SOURCE = new HashMap<String, String>();
	public static final List<String> SUPPORTED_SOURCE = new ArrayList<String>(){
		/**
		 * 
		 */
		private static final long serialVersionUID = -1263290324005652107L;

		{
			add("kugou");
			add("kuwo");
			add("ne");
			add("qier");
		}
	};
	
	public static final List<String> SUPPORTED_CMD = new ArrayList<String>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 2508424727530199543L;

		{
			add("歌曲");
			add("圣诞帽");
		}
	};
	
	public static final String[] SYNC_HOST = {
		"webpush.weixin.qq.com",
		"webpush2.weixin.qq.com",
		"webpush.wechat.com",
		"webpush1.wechat.com",
		"webpush2.wechat.com",
		"webpush1.wechatapp.com"
	};
	
	// 特殊用户 须过滤
	public static final List<String> FILTER_USERS = Arrays.asList("newsapp", "fmessage", "filehelper", "weibo", "qqmail", 
			"fmessage", "tmessage", "qmessage", "qqsync", "floatbottle", "lbsapp", "shakeapp", "medianote", "qqfriend", 
			"readerapp", "blogapp", "facebookapp", "masssendapp", "meishiapp", "feedsapp", "voip", "blogappweixin", 
			"weixin", "brandsessionholder", "weixinreminder", "wxid_novlwrv3lqwv11", "gh_22b87fa7cb3c", "officialaccounts",
			"notification_messages", "wxid_novlwrv3lqwv11", "gh_22b87fa7cb3c", "wxitil", "userexperience_alarm", 
			"notification_messages");
	
	static {
		imgTypes.put("png", "image/png");
		imgTypes.put("jpg", "image/jpeg");
		imgTypes.put("jpeg", "image/jpeg");
		imgTypes.put("bmp", "image/bmp");
		imgTypes.put("gif", "image/gif");
	}
}
